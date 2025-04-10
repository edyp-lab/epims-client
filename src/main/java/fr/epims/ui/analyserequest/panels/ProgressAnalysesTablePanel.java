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
package fr.epims.ui.analyserequest.panels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.edyp.epims.json.AnalyseProgressJson;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.tasks.analyses.AnalyseStudyInfoTask;
import fr.epims.tasks.analyses.LoadAnalysisTask;
import fr.epims.ui.analyserequest.dialogs.ModifyProgressAnalyseDialog;
import fr.epims.ui.analyserequest.dialogs.SearchStudiesRefDialog;
import fr.epims.ui.analyserequest.panels.model.ProgressAnalysesTableModel;
import fr.epims.ui.common.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Overview of the progres of each analysis
 */
public class ProgressAnalysesTablePanel extends JPanel {

    private DecoratedTable m_progressAnalysesTable;
    private ProgressAnalysesTableModel m_progressAnalysesTableModel;

    private AnalysesListPanel m_analysesListPanel;

    private JButton m_editButton;


    public ProgressAnalysesTablePanel(final AnalysesListPanel analysesListPanel) {

        m_analysesListPanel = analysesListPanel;

        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JToolBar toolbar = createToolbar(analysesListPanel);

        m_progressAnalysesTable = new DecoratedTable();
        m_progressAnalysesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_progressAnalysesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

        ActionMap map = m_progressAnalysesTable.getActionMap();
        final Action copyAction = m_progressAnalysesTable.getActionMap().get("copy");
        Action excelCopy = new Action() {

            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder excelStr = new StringBuilder();
                int[] rows = m_progressAnalysesTable.getSelectedRows();
                for (int row : rows) {
                    row = m_progressAnalysesTable.convertRowIndexToModel(row);
                    m_progressAnalysesTableModel.getExcelString(row, excelStr);
                }

                StringSelection sel = new StringSelection(excelStr.toString());
                CLIPBOARD.setContents(sel, sel);




            }

            @Override
            public Object getValue(String key) {
                return copyAction.getValue(key);
            }

            @Override
            public void putValue(String key, Object value) {
                copyAction.putValue(key, value);
            }

            @Override
            public void setEnabled(boolean b) {
                copyAction.setEnabled(b);
            }

            @Override
            public boolean isEnabled() {
                return copyAction.isEnabled();
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                copyAction.addPropertyChangeListener(listener);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                copyAction.removePropertyChangeListener(listener);
            }
        };
        map.put("copy", excelCopy);

        m_progressAnalysesTable.setActionMap(map);

       // m_progressAnalysesTable.addKeyListener(new ExcelClipboardKeyAdapter(m_progressAnalysesTable));
        m_progressAnalysesTableModel = new ProgressAnalysesTableModel(m_progressAnalysesTable);
        m_progressAnalysesTable.setModel(m_progressAnalysesTableModel);

        for (int i=0;i<m_progressAnalysesTableModel.getColumnCount();i++) {
            m_progressAnalysesTable.getColumnModel().getColumn(i).setPreferredWidth(m_progressAnalysesTableModel.getColumnPreferredSize(i));
        }


        m_progressAnalysesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int nbSelectedRows = m_progressAnalysesTable.getSelectedRowCount();
                m_editButton.setEnabled(nbSelectedRows==1);
                if ((nbSelectedRows==1) && (e.getClickCount() == 2)) {
                    // double click on a row, we open it
                    edit(analysesListPanel);
                }
            }
        });


        JScrollPane tableScrollPane = new JScrollPane(m_progressAnalysesTable);

        m_progressAnalysesTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        c.weightx = 1;
        add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        add(tableScrollPane, c);

    }

    public void filter(boolean filter, String userLogging) {
        m_progressAnalysesTableModel.filter(filter, userLogging);
    }

    public void setAnalyses(ArrayList<ProAnalysisJson> analyses, ArrayList<AnalysisMapJson> analysesMaps, boolean filterLogging, String userLogging, boolean filterSavedAnalysis) {
        m_progressAnalysesTableModel.setAnalyses(analyses, analysesMaps, filterLogging, userLogging, filterSavedAnalysis);
    }

    public void modificationDone(ProAnalysisJson proAnalysisJson, AnalysisMapJson analysisMapJson) {

        int row = m_progressAnalysesTable.getSelectedRow();

        m_progressAnalysesTableModel.modificationDone(proAnalysisJson, analysisMapJson);

        if (row != -1) {
            // reselect row in the table
            m_progressAnalysesTable.setRowSelectionInterval(row, row);

        }
    }

    private JToolBar createToolbar(final AnalysesListPanel analysesListPanel) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        m_editButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ANALYSE_EDIT), false);
        m_editButton.setEnabled(false);
        toolbar.add(m_editButton);



        m_editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit(analysesListPanel);
            }
        });

        return toolbar;

    }

    private void edit(final AnalysesListPanel analysesListPanel) {

        int row = m_progressAnalysesTable.getSelectedRow();
        if (row == -1) {
            // should not happen
            return;
        }
        row = m_progressAnalysesTable.convertRowIndexToModel(row);

        String studyRef = m_progressAnalysesTableModel.getStudyRef(row);

        if (studyRef == null) {
            SearchStudiesRefDialog dialog = new SearchStudiesRefDialog(MainFrame.getMainWindow(), "", analysesListPanel.getTakenStudyRefSet());
            dialog.centerToWindow(MainFrame.getMainWindow());
            dialog.setVisible(true);

            if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                studyRef = dialog.getStudyRef();

                if ((studyRef == null) || (studyRef.isEmpty())) {
                    // no study ref specified
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "No Study Reference Selected", "You have not selected a Study Reference. Action has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    return;
                }

            } else {
                return;
            }
        }
        final String _studyRef = studyRef;

        ProAnalysisJson analyseRequest = m_progressAnalysesTableModel.getAnalyse(row);



        AnalyseProgressJson[] analyseProgressJson = new AnalyseProgressJson[1];

        AnalysisMapJson[] analysis = new AnalysisMapJson[1];

        final int _row = row;
        AbstractDatabaseCallback loadAnalysisCallback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    if (analysis[0] == null) {

                        HashMap<String, String> map = new HashMap();

                        analysis[0] = new AnalysisMapJson();
                        analysis[0].setStudyRef(_studyRef);
                        analysis[0].setProAnalyseId(analyseRequest.getAnalyseId());
                        analysis[0].setPriceListId(-1);


                        HashMap<String, String> tagMap = new HashMap<>();
                        AnalysisRequestStep1Part1Panel.prefillData(analyseRequest, tagMap);
                        AnalysisRequestStep1Part2Panel.prefillData(analyseRequest, tagMap);
                        AnalysisRequestStep2Part1Panel.prefillData(analyseRequest, tagMap);
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonMap = null;
                        try {
                            jsonMap = mapper.writeValueAsString(tagMap);
                        } catch (JsonProcessingException e) {
                            // should not happen
                        }
                        analysis[0].setData(jsonMap);

                    }
                    ModifyProgressAnalyseDialog analyseRequestDialog = new ModifyProgressAnalyseDialog(MainFrame.getMainWindow(), analyseProgressJson[0], analysis[0]);
                    analyseRequestDialog.centerToWindow(MainFrame.getMainWindow());
                    analyseRequestDialog.setVisible(true);

                    if (analyseRequestDialog.saveDone()) {

                        m_analysesListPanel.modificationDone(analyseRequest, analyseRequestDialog.getAnalysisMapJson());


                    }

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

            }
        };

        AbstractDatabaseCallback analyseStudyInfoCallback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    LoadAnalysisTask task = new LoadAnalysisTask(loadAnalysisCallback, analyseRequest.getAnalyseId(), analysis);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

            }
        };

        AnalyseStudyInfoTask task = new AnalyseStudyInfoTask(analyseStudyInfoCallback, studyRef, analyseProgressJson);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }




}
