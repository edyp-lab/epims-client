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

package fr.epims.ui.panels;

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.tasks.*;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.*;
import fr.epims.ui.panels.model.SampleTableModel;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * Panel with list of Samples of a Study
 *
 * @author JM235353
 *
 */
public class SamplesListPanel extends HourGlassPanel implements RendererMouseCallback {

    private DecoratedTable m_table;
    private SampleTableModel m_model;

    private JLabel m_lastDateLabel = new JLabel();

    public SamplesListPanel(StudyJson s) {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JToolBar toolbar = createToolbar(s);


        boolean editable = s.isRunningStatus() && DataManager.checkOwner(s);
        m_table = new DecoratedTable();
        m_model = new SampleTableModel(this, editable);
        m_table.setModel(m_model);
        ViewTableCellRenderer renderer = (ViewTableCellRenderer) m_model.getRenderer(0, SampleTableModel.COLTYPE_LAST_STEP);
        m_table.addMouseListener(renderer);
        m_table.addMouseMotionListener(renderer);


        JScrollPane tableScrollPane = new JScrollPane(m_table);

        m_table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(toolbar, c);

        c.gridx++;
        c.weightx = 0;
        add(m_lastDateLabel, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        add(tableScrollPane, c);

        loadData(s);

    }

    private  void loadData(StudyJson s) {

        setLoading(getNewLoadingIndex());

        final ArrayList<SampleJson> samples = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_model.setSamples(samples);
                } else {
                    m_model.setSamples(new ArrayList<>());
                }

                Date date = m_model.getLastAcquisition();
                String dateString = UtilDate.dateToString(date);
                if (dateString.isEmpty()) {
                    dateString = "/ ";
                }
                m_lastDateLabel.setText("Last Acquisition : "+ dateString);

                setLoaded(m_id);
            }
        };

        SamplesTask task = new SamplesTask(callback, s, samples);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private JToolBar createToolbar(final StudyJson s) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);

        JButton addSampleButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_SAMPLE), false);
        addSampleButton.setToolTipText("Add or Fragment Samples");
        toolbar.add(addSampleButton);

        JButton addToRobotButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_TO_ROBOT), false);
        addToRobotButton.setToolTipText("Create a Robot Request");
        toolbar.add(addToRobotButton);

        addSampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), StudyJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (updateDataDialog.isDataUpdated()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Retry", "Data were not up-to-date. Please Retry.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    return;
                }

                if (! s.isRunningStatus()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Study Closed", "Study is closed. You can not add Samples.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                if (!DataManager.checkOwner(s)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to add Samples.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                AddOrFragmentSampleDialog selectSampleTypeDialog = new AddOrFragmentSampleDialog(MainFrame.getMainWindow());
                selectSampleTypeDialog.centerToWindow(MainFrame.getMainWindow());
                selectSampleTypeDialog.setVisible(true);
                if (selectSampleTypeDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    boolean fragmentation = ! selectSampleTypeDialog.isAddSampleSelected();
                    if (fragmentation) {
                        CreateSampleFragmentsDialog dialog = new CreateSampleFragmentsDialog(MainFrame.getMainWindow(), s, m_model.getSamples());
                        dialog.centerToWindow(MainFrame.getMainWindow());
                        dialog.setVisible(true);

                        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                            ArrayList<SampleJson> sampleJsons = new ArrayList<>();
                            FragmentsGroupToCreateJson fragmentsGroupToCreateJson = dialog.getFragmentsToCreate();

                            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success, long taskId, boolean finished) {
                                    if (success) {
                                        DataManager.updateStudy(s);
                                    } else {
                                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                                        infoDialog.setVisible(true);
                                    }
                                }
                            };

                            AddFragmentTask task = new AddFragmentTask(callback, fragmentsGroupToCreateJson, sampleJsons);
                            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


                        }

                    } else {
                        CreateSamplesDialog dialog = new CreateSamplesDialog(MainFrame.getMainWindow(), s, m_model.getSamples());
                        dialog.centerToWindow(MainFrame.getMainWindow());
                        dialog.setVisible(true);
                        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                            ArrayList<SampleJson> sampleJsons = dialog.getSamplesToCreate();

                            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success, long taskId, boolean finished) {
                                    if (success) {
                                        DataManager.updateStudy(s);
                                    } else {
                                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                                        infoDialog.setVisible(true);
                                    }
                                }
                            };

                            AddSampleTask task = new AddSampleTask(callback, s, sampleJsons);
                            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


                        }
                    }

                }



            }
        });

        addToRobotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                if (!DataManager.checkOwner(s)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to create Robot Requests.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                if (! s.isRunningStatus()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Study Closed", "Study is closed. You can not create Robot Requests.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                HashSet<SampleJson> selectedSamples = new HashSet();
                int[] selectedRows = m_table.getSelectedRows();
                for (int i = 0;i<selectedRows.length;i++) {
                    int row = selectedRows[i];
                    int rowInModel = m_table.convertRowIndexToModel(row);
                    selectedSamples.add(m_model.getSample(rowInModel));
                }


                RobotRequestDialog dialog = new RobotRequestDialog(MainFrame.getMainWindow(),  m_model.getSamples(), selectedSamples);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    ArrayList<RobotPlanningJson> robotPlanningJsonArrayList = dialog.getRobotPlannings();

                    String[] m_result = new String[1];

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {

                                if (m_result[0] == null) {


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
                                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                                infoDialog.setVisible(true);
                                            }
                                        }
                                    };

                                    AddRobotPlanningTask task = new AddRobotPlanningTask(callback, robotPlanningJsonArrayList);
                                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                                } else {
                                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Robot Plate Error", m_result[0]+" Sample is already planned for Robot Plate. Action canceled.");
                                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                                    infoDialog.setVisible(true);
                                }
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);

                            }
                        }
                    };

                    CheckAddRobotPlanningTask task = new CheckAddRobotPlanningTask(callback, m_result, robotPlanningJsonArrayList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


                }
            }
        });


        return toolbar;

    }

    @Override
    public void mouseAction(MouseEvent e) {

        int col = m_table.columnAtPoint(e.getPoint());
        int row = m_table.rowAtPoint(e.getPoint());
        if ((row != -1) && (col != -1)) {

            int colModelIndex = m_table.convertColumnIndexToModel(col);
            if (colModelIndex == SampleTableModel.COLTYPE_LAST_STEP) {

                int rowModelIndex = m_table.convertRowIndexToModel(row);
                ArrayList<String> history = ((SampleTableModel)m_table.getModel()).getStepsHistory(rowModelIndex);

                if (history.isEmpty()) {
                    return;
                }

                String sampleName = ((SampleTableModel)m_table.getModel()).getSampleName(rowModelIndex);

                HistoryDialog dialog = new HistoryDialog(MainFrame.getMainWindow(), sampleName, history);
                dialog.centerToScreen();
                dialog.setVisible(true);

            }
        }

    }
}
