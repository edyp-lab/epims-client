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
import fr.edyp.epims.json.ControlAcquisitionArchivableJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.epims.tasks.archive.LoadControlToArchiveTask;
import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * Main Panel for Archiving listing archivable studies and control acquisitions
 *
 * @author JM235353
 *
 */
public class AdminArchiveStudiesPanel extends JPanel implements DataManager.DataManagerListener {

    private DefaultListModel<StudyJson> m_studiesListModel;
    private JList m_studiesList;

    private ArchiveControlAcquisitionsTableModel m_controlsAcquisitionsDataModel;
    private JTable m_controlsTable;

    private FlatButton m_studyArchiveButton;
    private FlatButton m_acquisitionArchiveButton;

    private boolean m_studiesDataLoaded;
    private boolean m_controlDataLoaded;

    private ArchiveInfoPanel m_archiveInfoPanel;

    public AdminArchiveStudiesPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Archiving ");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_studiesDataLoaded = false;
        m_controlDataLoaded = false;



        JPanel listsPanel = createListsPanel();

        m_archiveInfoPanel = new ArchiveInfoPanel();

        JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, listsPanel, m_archiveInfoPanel);
        splitPane.setDividerLocation(0.5);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(Box.createGlue(), c);

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        add(splitPane, c);



        ListSelectionListener studySelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean somethingSelected = (m_studiesList.getSelectedValue() != null);
                m_studyArchiveButton.setEnabled(somethingSelected);
            }
        };

        m_studiesList.addListSelectionListener(studySelectionListener);


        ListSelectionListener acquisitionSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean somethingSelected = (m_controlsTable.getSelectedRow() != -1);
                m_acquisitionArchiveButton.setEnabled(somethingSelected);
            }
        };

        ListSelectionModel tableSelectionModel = m_controlsTable.getSelectionModel();
        tableSelectionModel.addListSelectionListener(acquisitionSelectionListener);



        DataManager.addListener(StudyJson.class, this);

    }

    private JPanel createListsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_studiesListModel = new DefaultListModel<>();
        m_studiesDataLoaded = false;
        m_controlDataLoaded = false;


        m_studyArchiveButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARCHIVE_ADD), true);
        m_studyArchiveButton.setEnabled(false);

        m_acquisitionArchiveButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARCHIVE_ADD), true);
        m_acquisitionArchiveButton.setEnabled(false);


        m_studyArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<ArchivingInfoJson> archiveInfoList = new ArrayList<>();

                List<StudyJson>  studiesList = m_studiesList.getSelectedValuesList();
                for (StudyJson s : studiesList) {
                    ArchivingInfoJson archiveInfo = new ArchivingInfoJson(null, s);
                    archiveInfoList.add(archiveInfo);
                    m_studiesListModel.removeElement(s);
                }


                m_archiveInfoPanel.addDataToArchive(archiveInfoList);

                m_archiveInfoPanel.askPollingArchiving();

            }
        });

        m_acquisitionArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<ArchivingInfoJson> archiveInfoList = new ArrayList<>();


                int[] rows = m_controlsTable.getSelectedRows();
                HashSet<Integer> rowsToRemoveInModel = new HashSet<>();
                for (int row : rows) {
                    row = m_controlsTable.convertRowIndexToModel(row);
                    rowsToRemoveInModel.add(row);
                    ControlAcquisitionArchivableJson archivableAcquisition = m_controlsAcquisitionsDataModel.getAcquisitionArchivable(row);
                    ArchivingInfoJson archiveInfo = new ArchivingInfoJson(archivableAcquisition, null);
                    archiveInfoList.add(archiveInfo);
                }
                m_controlsAcquisitionsDataModel.removeRows(rowsToRemoveInModel);

                m_archiveInfoPanel.addDataToArchive(archiveInfoList);

                m_archiveInfoPanel.askPollingArchiving();

            }
        });


        m_studiesList = new JList(m_studiesListModel);
        m_studiesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane listScrollPane = new JScrollPane(m_studiesList) {

            private final Dimension preferredSize = new Dimension(120, 320);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };
        listScrollPane.setBorder(BorderFactory.createTitledBorder(" Archivable Studies "));

        m_controlsTable = new DecoratedTable();
        m_controlsTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_controlsAcquisitionsDataModel = new ArchiveControlAcquisitionsTableModel(m_controlsTable);
        m_controlsTable.setModel(m_controlsAcquisitionsDataModel);

        JScrollPane controlListScrollPane = new JScrollPane(m_controlsTable) {

            private final Dimension preferredSize = new Dimension(120, 320);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };
        controlListScrollPane.setBorder(BorderFactory.createTitledBorder(" Archivable Control Acquisitions "));

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        panel.add(listScrollPane, c);

        c.gridx+=3;
        panel.add(controlListScrollPane, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.gridwidth = 1;
        panel.add(Box.createGlue(), c);

        c.gridx++;
        c.weightx = 0;
        panel.add(m_studyArchiveButton, c);

        c.gridx++;
        c.weightx = 1;
        panel.add(Box.createGlue(), c);

        c.gridx++;
        c.weightx = 1;
        panel.add(Box.createGlue(), c);

        c.gridx++;
        c.weightx = 0;
        panel.add(m_acquisitionArchiveButton, c);

        c.gridx++;
        c.weightx = 1;
        panel.add(Box.createGlue(), c);




        return panel;

    }

    public void reinit() {
        m_studiesDataLoaded = false;
        m_controlDataLoaded = false;
    }


    public void loadData() {
        if (! m_studiesDataLoaded) {


            m_studiesListModel.clear();

            DataAvailableCallback callback = new DataAvailableCallback() {

                @Override
                public void dataAvailable() {

                    ArrayList<StudyJson> list = DataManager.getArchivableStudies();
                    for (StudyJson study : list) {
                        m_studiesListModel.addElement(study);
                    }

                    m_studiesDataLoaded = true;

                }
            };
            DataManager.dataAvailable(callback, false);
        }

        if (!m_controlDataLoaded) {

            m_controlsAcquisitionsDataModel.setData(new ArrayList<>(0));

            final ArrayList<ControlAcquisitionArchivableJson> list = new ArrayList<>();
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, boolean finished) {
                    if (success) {
                        m_controlsAcquisitionsDataModel.setData(list);

                        m_controlDataLoaded = true;
                    }
                }
            };

            LoadControlToArchiveTask task = new LoadControlToArchiveTask(callback, list);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }

    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {
        updateAll(null);
    }

    @Override
    public void updateAll(HashSet<Class> c) {

        List<StudyJson> studiesList = m_studiesList.getSelectedValuesList();
        HashSet<Integer> selectedIds = new HashSet<>();
        for (StudyJson selectedStudy : studiesList) {
            selectedIds.add(selectedStudy.getId());
        }

        m_studiesDataLoaded = false;
        m_studiesListModel.clear();

        ArrayList<StudyJson> list  = DataManager.getArchivableStudies();
        for (StudyJson study : list) {
            m_studiesListModel.addElement(study);
        }

        int index = 0;
        for (StudyJson study : list) {
            if (selectedIds.contains(study.getId())) {
                m_studiesList.addSelectionInterval(index, index);
            }
            index++;
        }


        m_studiesDataLoaded = true;

    }


    public void refreshArchiving() {
        m_archiveInfoPanel.startPollingArchiving();
    }

}
