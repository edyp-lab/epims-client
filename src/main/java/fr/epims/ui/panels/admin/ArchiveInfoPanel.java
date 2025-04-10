/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */


package fr.epims.ui.panels.admin;



import fr.edyp.epims.json.ArchivingInfoJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.tasks.archive.ArchivePollingTask;
import fr.epims.tasks.archive.ArchiveTask;
import fr.epims.tasks.archive.FetchArchivingServerPathTask;
import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Timer;


/**
 *
 * Panel which displays the list of studies and control acquisitions being archived
 * in different state (finish, error, running, waiting)
 *
 * @author JM235353
 *
 */
public class ArchiveInfoPanel extends JPanel {

    private JLabel m_archivePathLabel = new JLabel();
    private DecoratedTable m_archiveInfoTable;
    private ArchiveInfoTableModel m_model;
    private FlatButton m_deleteButton;
    private FlatButton m_refreshButton;

    public ArchiveInfoPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Archiving Processing ");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_archivePathLabel = new JLabel();

        m_archiveInfoTable = new DecoratedTable();
        m_model = new ArchiveInfoTableModel();
        m_archiveInfoTable.setModel(m_model);

        JScrollPane tableScrollPane = new JScrollPane(m_archiveInfoTable);
        m_archiveInfoTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        m_archiveInfoTable.getColumnModel().getColumn(ArchiveInfoTableModel.COLTYPE_STEP).setPreferredWidth(20);
        m_archiveInfoTable.getColumnModel().getColumn(ArchiveInfoTableModel.COLTYPE_STEP).setMaxWidth(20);
        m_archiveInfoTable.getColumnModel().getColumn(ArchiveInfoTableModel.COLTYPE_ARCHIVE_NAME).setMinWidth(200);
        m_archiveInfoTable.getColumnModel().getColumn(ArchiveInfoTableModel.COLTYPE_ARCHIVE_NAME).setMaxWidth(300);
        m_archiveInfoTable.getColumnModel().getColumn(ArchiveInfoTableModel.COLTYPE_ERROR_MESSAGE).setMinWidth(300);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;

        add(m_archivePathLabel, c);

        c.gridy++;
        c.weighty = 1;
        add(tableScrollPane, c);

        c.gridx++;
        c.weightx = 0;
        add(createToolbar(), c);

        ListSelectionModel selectionModel = m_archiveInfoTable.getSelectionModel();

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int nbRowSelected = m_archiveInfoTable.getSelectedRows().length;
                m_deleteButton.setEnabled(nbRowSelected >0);
            }
        });

    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(SwingConstants.VERTICAL);

        m_deleteButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ERASER),false);
        m_deleteButton.setEnabled(false);
        toolbar.add(m_deleteButton);

        m_refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH),false);
        toolbar.add(m_refreshButton);

        m_deleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<Integer> rowToRemoveSet = new HashSet();
                for (int row : m_archiveInfoTable.getSelectedRows()) {
                    row = m_archiveInfoTable.convertRowIndexToModel(row);
                    rowToRemoveSet.add(row);
                }
                m_model.removeFromArchiveList (rowToRemoveSet);

                // ask to the server to archive the remaining
                askArchivingToServer();
            }
        });

        m_refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                askPollingArchiving();
            }
        });

        return toolbar;

    }

    public Dimension getPreferredSize() {
        return new Dimension(600,200);
    }

    public void addDataToArchive(ArrayList<ArchivingInfoJson> archiveInfoList) {
        m_model.addArchiveInfoList(archiveInfoList);

        // ask to the server to archive
        askArchivingToServer();
    }

    private void askArchivingToServer() {
        // ask to server to archive data
        LinkedList<ArchivingInfoJson> dataToArchive = m_model.dataToArchive();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    // nothing to do

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Archiving Request has failed. Perhaps the Server is down.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

            }
        };


        ArchiveTask task = new ArchiveTask(callback, dataToArchive);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }


    public void startPollingArchiving() {

        if (m_pollingTimerStarted) {
            return;
        }

        // retrieve archiving path
        fetchArchivingPath();

        m_pollingTimerStarted = true;
        m_pollingNow = true;

        Timer timer = new Timer();

        timer.schedule( new TimerTask() {
            public void run() {
                if (m_pollingNow) {
                    m_pollingNow = false;
                    m_pollingTimerCount = 0;
                    pollArchivingImplementation();
                } else {
                    m_pollingTimerCount++;
                    if (m_pollingTimerCount % 10 == 0) {
                        m_pollingTimerCount = 0;
                        pollArchivingImplementation();
                    }

                }
            }

        }, 0, 1000);

    }
    private boolean m_pollingTimerStarted = false;

    public void askPollingArchiving() {
        m_pollingNow = true;
    }
    private boolean m_pollingNow = false;
    private int m_pollingTimerCount = 0;


    private void pollArchivingImplementation() {
        final LinkedList<ArchivingInfoJson> dataOnServer = new LinkedList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {

                if (success) {

                    int[] rows = m_archiveInfoTable.getSelectedRows();
                    for (int i=0;i<rows.length;i++) {
                        rows[i] = m_archiveInfoTable.convertRowIndexToModel(rows[i]);
                    }
                    HashSet<String> selectedKeys = m_model.getArchiveInfoKey(rows);

                    m_model.replaceArchiveInfoList(dataOnServer);

                    m_archiveInfoTable.clearSelection();
                    ArrayList<Integer> rowsSelectedInModel = m_model.getSelectedModelRows(selectedKeys);
                    for (Integer rowInModel : rowsSelectedInModel) {
                        int row = m_archiveInfoTable.convertRowIndexToView(rowInModel);
                        m_archiveInfoTable.addRowSelectionInterval(row, row);
                    }

                } else {
                    if (m_firstArchivingError) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Can not get Archiving info from Server. Perhaps the Server is down. This error will not be displayed again.");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        m_firstArchivingError = false;
                    }
                }

            }
        };


        ArchivePollingTask task = new ArchivePollingTask(callback, dataOnServer);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }
    private boolean m_firstArchivingError = true;

    public void fetchArchivingPath() {

        final StringBuilder m_pathSB = new StringBuilder();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {

                if (success) {
                    m_archivePathLabel.setText("Server Archiving Path: "+m_pathSB.toString());

                } else {
                    m_archivePathLabel.setText("Failed to check Server Archiving Path");
                }

            }
        };


        FetchArchivingServerPathTask task = new FetchArchivingServerPathTask(callback, m_pathSB);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }



    public static class ArchiveInfoRenderer extends DefaultTableCellRenderer {

        private final static ImageIcon[] PUBLIC_STATE_ICONS = { IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16), IconManager.getIcon(IconManager.IconType.ARROW_RIGHT_SMALL), IconManager.getIcon(IconManager.IconType.TICK_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_SMALL16)};



        public ArchiveInfoRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, false, row, column);

            ArchivingInfoJson archiveInfo = (ArchivingInfoJson) value;

            label.setIcon(getIcon(archiveInfo));

            return this;
        }

        private Icon getIcon( ArchivingInfoJson archiveInfo) {
            return  PUBLIC_STATE_ICONS[archiveInfo.getState().getIndex()];
        }
    }
}
