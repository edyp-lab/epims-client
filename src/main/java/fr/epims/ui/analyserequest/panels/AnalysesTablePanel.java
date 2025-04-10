package fr.epims.ui.analyserequest.panels;

import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.epims.tasks.analyses.LoadAnalysisTask;
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.analyserequest.panels.model.AnalysesTableModel;
import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;

public class AnalysesTablePanel extends JPanel {

    private DecoratedTable m_analysesTable;
    private AnalysesTableModel m_analysesTableModel;


    private JButton m_editButton;

    private boolean m_readOnly = true;

    public AnalysesTablePanel(final AnalysesListPanel analysesListPanel, boolean importSelection) {

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

        m_analysesTable = new DecoratedTable();
        m_analysesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_analysesTableModel = new AnalysesTableModel(importSelection);
        m_analysesTable.setModel(m_analysesTableModel);

        m_analysesTable.getColumnModel().getColumn(AnalysesTableModel.COLTYPE_REF_STUDY).setMaxWidth(80);
        m_analysesTable.getColumnModel().getColumn(AnalysesTableModel.COLTYPE_REQUEST_DATE).setMaxWidth(80);
        m_analysesTable.getColumnModel().getColumn(AnalysesTableModel.COLTYPE_EDYP_RESPONSIBLE).setPreferredWidth(200);
        m_analysesTable.getColumnModel().getColumn(AnalysesTableModel.COLTYPE_EDYP_RESPONSIBLE).setMaxWidth(320);
        if (!importSelection) {
            m_analysesTable.getColumnModel().getColumn(AnalysesTableModel.COLTYPE_SAVE_DATE).setMaxWidth(80);
            m_analysesTable.getColumnModel().getColumn(AnalysesTableModel.COLTYPE_EXPORT_DATE).setMaxWidth(80);
        }

        m_analysesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = m_analysesTable.getSelectedRow();
                if (row == -1) {
                    m_editButton.setEnabled(false);
                    return;
                }

                try {
                    m_editButton.setEnabled(true);

                    if (DataManager.isAdminUser()) {
                        m_readOnly = false;
                        return;
                    }

                    row = m_analysesTable.convertRowIndexToModel(row);
                    m_readOnly = !m_analysesTableModel.canBeEdited(row, DataManager.getAnalysesLoggedUser());

                } finally {
                    if (e.getClickCount() == 2) {
                        // double click on a row, we open it
                        edit(analysesListPanel);
                    }
                }


            }
        });


        JScrollPane tableScrollPane = new JScrollPane(m_analysesTable);

        m_analysesTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        c.weightx = 1;
        if (! importSelection) {
            add(toolbar, c);
        }
        c.gridy++;
        c.weighty = 1;
        add(tableScrollPane, c);

    }

    public void filter(boolean filter, String userLogging) {
        m_editButton.setEnabled(false);
        m_analysesTableModel.filter(filter, userLogging);
    }

    public void setAnalyses(ArrayList<ProAnalysisJson> analyses, ArrayList<AnalysisMapJson> analysesMaps, boolean filterLogging, String userLogging, boolean filterSavedAnalysis) {
        m_analysesTableModel.setAnalyses(analyses, analysesMaps, filterLogging, userLogging, filterSavedAnalysis);
    }

    public void modificationDone(ProAnalysisJson proAnalysisJson, AnalysisMapJson analysisMapJson) {

        int row = m_analysesTable.getSelectedRow();

        m_analysesTableModel.modificationDone(proAnalysisJson, analysisMapJson);

        if (row != -1) {
            // reselect row in the table
            m_analysesTable.setRowSelectionInterval(row, row);
        }
    }

    public void exportDone(int analysisId, Date exportDate) {
        int row = m_analysesTable.getSelectedRow();

        m_analysesTableModel.exportDone(analysisId, exportDate);

        if (row != -1) {
            // reselect row in the table
            m_analysesTable.setRowSelectionInterval(row, row);
        }
    }

    public ProAnalysisJson getSelectedAnalyse() {
        int row = m_analysesTable.getSelectedRow();
        if (row == -1) {
            return null;
        }
        row = m_analysesTable.convertRowIndexToModel(row);
        ProAnalysisJson analyseRequest = m_analysesTableModel.getAnalyse(row);
        return analyseRequest;
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
        int row = m_analysesTable.getSelectedRow();
        if (row == -1) {
            // should not happen
            return;
        }
        row = m_analysesTable.convertRowIndexToModel(row);
        ProAnalysisJson analyseRequest = m_analysesTableModel.getAnalyse(row);
        String studyRef = m_analysesTableModel.getStudyRef(row);

        AnalysisMapJson[] analysis = new AnalysisMapJson[1];


        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    ModifyAnalyseRequestDialog analyseRequestDialog = new ModifyAnalyseRequestDialog(MainFrame.getMainWindow(), analysesListPanel, studyRef, analyseRequest, analysis[0], m_readOnly);
                    analyseRequestDialog.centerToWindow(MainFrame.getMainWindow());
                    analyseRequestDialog.setVisible(true);

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

            }
        };

        LoadAnalysisTask task = new LoadAnalysisTask(callback, analyseRequest.getAnalyseId(), analysis);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
}
