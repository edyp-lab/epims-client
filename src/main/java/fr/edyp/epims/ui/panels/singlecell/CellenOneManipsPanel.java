/*
 * Copyright (C) 2024
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

package fr.edyp.epims.ui.panels.singlecell;

import fr.edyp.epims.MainFrame;
import fr.edyp.epims.dataaccess.AbstractDatabaseCallback;
import fr.edyp.epims.dataaccess.AccessDatabaseThread;
import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.tasks.singlecell.LoadCellenOneManipsTask;
import fr.edyp.epims.ui.common.DecoratedTable;
import fr.edyp.epims.ui.common.FlatButton;
import fr.edyp.epims.ui.common.HourGlassPanel;
import fr.edyp.epims.ui.common.IconManager;
import fr.edyp.epims.ui.common.InfoDialog;
import fr.edyp.epims.ui.panels.model.CellenOneManipTableModel;
import fr.edyp.epims.ui.panels.model.CellenOneRunTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * Panel displaying SingleCell CellenOne Manipulations and their associated Runs
 */
public class CellenOneManipsPanel extends HourGlassPanel {

    private static CellenOneManipsPanel m_singleton = null;

    private DecoratedTable m_manipTable;
    private CellenOneManipTableModel m_manipTableModel;

    private DecoratedTable m_runTable;
    private CellenOneRunTableModel m_runTableModel;

    private FlatButton m_refreshButton;
    private FlatButton m_importManipButton;

    private boolean m_dataLoaded = false;

    public static CellenOneManipsPanel getPanel() {
        if (m_singleton == null) {
            m_singleton = new CellenOneManipsPanel();
        }
        return m_singleton;
    }

    private CellenOneManipsPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);

        JPanel toolbar = createToolbar();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        add(toolbar, c);

        JSplitPane splitPane = createMainSplitPane();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(splitPane, c);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        m_importManipButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UPLOAD_TO_SERVER), false);
        m_importManipButton.setToolTipText("Import Manip");
        m_importManipButton.setEnabled(false);
        m_importManipButton.addActionListener( e -> importManip());
        toolbar.add(m_importManipButton);

        m_refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH), false);
        m_refreshButton.setToolTipText("Refresh CellenOne Manipulations");
        m_refreshButton.addActionListener(e -> loadData(true));
        toolbar.add(m_refreshButton);

        return toolbar;
    }

    private JSplitPane createMainSplitPane() {
        m_manipTableModel = new CellenOneManipTableModel();
        m_manipTable = new DecoratedTable();
        m_manipTable.setModel(m_manipTableModel);
        m_manipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_runTableModel = new CellenOneRunTableModel();
        m_runTable = new DecoratedTable();
        m_runTable.setModel(m_runTableModel);
        m_runTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_manipTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = m_manipTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int modelRow = m_manipTable.convertRowIndexToModel(selectedRow);
                        CellenOneManipJson selectedManip = m_manipTableModel.getManipAt(modelRow);
                        if (selectedManip != null) {
                            m_runTableModel.setRuns(selectedManip.getRuns());
                        } else {
                            m_runTableModel.clear();
                        }
                    } else {
                        m_runTableModel.clear();
                    }
                }
            }
        });

        JScrollPane manipScrollPane = new JScrollPane(m_manipTable);
        JPanel manipPanel = new JPanel(new BorderLayout());
        manipPanel.setBorder(new TitledBorder("CellenOne Manipulations"));
        manipPanel.add(manipScrollPane, BorderLayout.CENTER);

        JScrollPane runScrollPane = new JScrollPane(m_runTable);
        JPanel runPanel = new JPanel(new BorderLayout());
        runPanel.setBorder(new TitledBorder("Runs"));
        runPanel.add(runScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, manipPanel, runPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    public void importManip() {
        int row = m_manipTable.getSelectedRow();
        int rowInModel = m_manipTable.convertRowIndexToModel(row);
        CellenOneManipJson manip = m_manipTableModel.getManipAt(rowInModel);
        JOptionPane.showMessageDialog(this.getParent(), "Import CellenOne Manip "+manip.getName(), "Import Manip", JOptionPane.INFORMATION_MESSAGE);
    }

    public void loadData() {
        if (!m_dataLoaded) {
            loadData(false);
        }
    }

    public void loadData(boolean force) {
        final int loadingId = getNewLoadingIndex();
        setLoading(loadingId);

        final ArrayList<CellenOneManipJson> manips = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                setLoaded(loadingId);
                if (success) {
                    m_dataLoaded = true;
                    m_manipTableModel.setValues(manips);
                    if (!manips.isEmpty()) {
                        m_manipTable.setRowSelectionInterval(0, 0);
                    } else {
                        m_runTableModel.clear();
                    }
                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Load Failed", "Failed to load CellenOne Manipulations from server.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }
            }
        };

        LoadCellenOneManipsTask task = new LoadCellenOneManipsTask(callback, manips);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    public void reinit() {
        m_dataLoaded = false;
        m_manipTableModel.clear();
        m_runTableModel.clear();
    }
}
