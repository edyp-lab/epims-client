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

import fr.edyp.epims.json.*;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.epims.tasks.SearchAcquisitionsTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.ServerFilesDialog;
import fr.epims.ui.panels.model.AcquisitionSearchTableModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * Panel with Table of Searched Acquisitions
 *
 * @author JM235353
 *
 */
public class AcquisitionsSearchListPanel extends HourGlassPanel  {

    private JLabel m_resultLabel;
    private AcquisitionSearchTableModel m_model;

    private DecoratedTable m_table;

    public AcquisitionsSearchListPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_resultLabel = new JLabel();

        m_table = new DecoratedTable();
        m_model = new AcquisitionSearchTableModel(m_table, new NameCallback(), new StudyCallback());
        m_table.setModel(m_model);

        ViewTableCellRenderer rendererStudy = (ViewTableCellRenderer) m_model.getRenderer(0, AcquisitionSearchTableModel.COLTYPE_STUDY);
        m_table.addMouseListener(rendererStudy);
        m_table.addMouseMotionListener(rendererStudy);

        ViewTableCellRenderer rendererName = (ViewTableCellRenderer) m_model.getRenderer(0, AcquisitionSearchTableModel.COLTYPE_NAME);
        m_table.addMouseListener(rendererName);
        m_table.addMouseMotionListener(rendererName);

        JScrollPane tableScrollPane = new JScrollPane(m_table);

        m_table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        c.weightx = 1;
        add(m_resultLabel, c);

        c.gridy++;
        c.weighty = 1;
        add(tableScrollPane, c);
    }

    public AcquisitionSearchTableModel getModel() {
        return m_model;
    }

    public void reinitData() {
        m_resultLabel.setText("");
        m_model.setValues(new ArrayList<>());
    }

    public void loadData(final FilterAcquisitionsPanel parentPanel, String searchText, String acquisitionType, int instrumentId, String sampleOwnerActorKey, String startDate, String endDate) {

        setLoading(getNewLoadingIndex());

        final ArrayList<ProtocolApplicationJson> protocolApplicationJsons = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {


                if (success) {
                    m_model.setValues(protocolApplicationJsons);
                    String acquisitonFoundtext = (protocolApplicationJsons.size() == 0) ? " acquisition found." : protocolApplicationJsons.size()+" acquisitions found.";
                    m_resultLabel.setText(acquisitonFoundtext);
                } else {
                    m_model.setValues(new ArrayList<>());
                    m_resultLabel.setText("");
                }


                parentPanel.searchDone();

                setLoaded(m_id);
            }
        };

        SearchAcquisitionsTask task = new SearchAcquisitionsTask(callback, searchText, acquisitionType, instrumentId, sampleOwnerActorKey, protocolApplicationJsons, startDate, endDate);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }



    public class NameCallback implements RendererMouseCallback {
        @Override
        public void mouseAction(MouseEvent e) {

            int col = m_table.columnAtPoint(e.getPoint());
            int row = m_table.rowAtPoint(e.getPoint());
            if ((row != -1) && (col != -1)) {

                int colModelIndex = m_table.convertColumnIndexToModel(col);
                if (colModelIndex == AcquisitionSearchTableModel.COLTYPE_NAME) {

                    int rowModelIndex = m_table.convertRowIndexToModel(row);
                    StudyJson s = ((AcquisitionSearchTableModel)m_table.getModel()).getStudy(rowModelIndex);

                    if (s != null) {

                        // Acquisition is part of a study

                        if (!DataManager.checkOwner(s)) {
                            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to access FTP.");
                            infoDialog.centerToWindow(MainFrame.getMainWindow());
                            infoDialog.setVisible(true);
                            return;
                        }

                        String studyNomenclature = s.getNomenclatureTitle();
                        String projectNomenclature = "_UNCLASS_";
                        String programNomenclature = "_UNCLASS_";

                        if (s.getProjectId() != -1) {
                            ProjectJson project = DataManager.getProject(s.getProjectId());
                            projectNomenclature = project.getNomenclatureTitle();

                            if (project.getProgramId() != -1) {
                                ProgramJson program = DataManager.getProgram(project.getProgramId());
                                programNomenclature = program.getNomenclatureTitle();
                            }
                        }

                        String[] subDirs = {programNomenclature, projectNomenclature, studyNomenclature};

                        FtpConfigurationJson configuration = DataManager.getFtpConfiguration();
                        configuration.setSubDirs(subDirs);

                        String[] autoExpandNames = {"data", "samples", "RAW"};
                        ServerFilesDialog dialog = ServerFilesDialog.getSingleton(MainFrame.getMainWindow(), configuration, autoExpandNames, (String) m_table.getValueAt(row, col));
                        dialog.centerToScreen();
                        dialog.setVisible(true);
                    } else {
                        // Acquisition is not part of a study : Blanc, ControleIntrument, ControleLC

                        ProtocolApplicationJson protocolApplication = ((AcquisitionSearchTableModel)m_table.getModel()).getProtocolApplicationJson(rowModelIndex);
                        AcquisitionJson acquisitionJson = protocolApplication.getAcquisitionJson();
                        Date d = protocolApplication.getDate();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(d);
                        String year = String.valueOf(calendar.get(Calendar.YEAR));
                        String month = String.valueOf((calendar.get(Calendar.MONTH)+1));

                        String[] subDirs = {"share" };

                        FtpConfigurationJson configuration = DataManager.getFtpConfiguration();
                        configuration.setSubDirs(subDirs);

                        String[] autoExpandNames = {acquisitionJson.getNature(), DataManager.getInstrument(acquisitionJson.getInstrumentId()).getName(), year, month};
                        ServerFilesDialog dialog = ServerFilesDialog.getSingleton(MainFrame.getMainWindow(), configuration, autoExpandNames, (String) m_table.getValueAt(row, col));
                        dialog.centerToScreen();
                        dialog.setVisible(true);

                    }


                }
            }

        }
    }

    public class StudyCallback implements RendererMouseCallback {
        @Override
        public void mouseAction(MouseEvent e) {

            int col = m_table.columnAtPoint(e.getPoint());
            int row = m_table.rowAtPoint(e.getPoint());
            if ((row != -1) && (col != -1)) {

                int colModelIndex = m_table.convertColumnIndexToModel(col);
                if (colModelIndex == AcquisitionSearchTableModel.COLTYPE_STUDY) {

                    int rowModelIndex = m_table.convertRowIndexToModel(row);
                    StudyJson study = ((AcquisitionSearchTableModel)m_table.getModel()).getStudy(rowModelIndex);

                    MainFrame.getMainWindow().selectActivitiesTabbedPane();
                    ActivitiesPanel activitiesPanel = ActivitiesPanel.getActivitiesPanel();
                    activitiesPanel.getTree().display(study, e.getClickCount() == 2);


                }
            }

        }
    }

}
