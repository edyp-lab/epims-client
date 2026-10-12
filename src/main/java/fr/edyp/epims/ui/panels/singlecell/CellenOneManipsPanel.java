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
import fr.edyp.epims.dataaccess.DataManager;
import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.json.CellenOneImportJson;
import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.PlateJson;
import fr.edyp.epims.json.SampleJson;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.tasks.AddSampleTask;
import fr.edyp.epims.tasks.singlecell.CellenOnePlatesTask;
import fr.edyp.epims.tasks.SamplesTask;
import fr.edyp.epims.tasks.singlecell.ImportCellenOneManipTask;
import fr.edyp.epims.tasks.singlecell.LoadCellenOneManipsTask;
import fr.edyp.epims.tasks.singlecell.LoadCellenOneStudiesTask;
import fr.edyp.epims.ui.common.DecoratedTable;
import fr.edyp.epims.ui.common.FlatButton;
import fr.edyp.epims.ui.common.HourGlassPanel;
import fr.edyp.epims.ui.common.IconManager;
import fr.edyp.epims.ui.common.InfoDialog;
import fr.edyp.epims.ui.dialogs.CellenOneImportDialog;
import fr.edyp.epims.ui.panels.model.CellenOneManipTableModel;
import fr.edyp.epims.ui.panels.model.CellenOneRunTableModel;
import fr.edyp.epims.ui.tree.AbstractNode;
import fr.edyp.epims.ui.tree.ProgramNode;
import fr.edyp.epims.ui.tree.ProjectNode;
import fr.edyp.epims.ui.tree.RootNode;
import fr.edyp.epims.ui.tree.StudyNode;
import fr.edyp.epims.ui.tree.TreeRenderer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;

/**
 * Panel displaying SingleCell CellenOne Manipulations and their associated Runs
 */
public class CellenOneManipsPanel extends HourGlassPanel {

    private static CellenOneManipsPanel m_singleton = null;

    private DecoratedTable m_manipTable;
    private CellenOneManipTableModel m_manipTableModel;

    private CellenOneRunTableModel m_runTableModel;

    private FlatButton m_importButton;

    private JTree m_studyTree;

    private boolean m_dataLoaded = false;
    private int m_importLoadingId = -1;

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,  createStudyPanel(), createMainSplitPane());
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.25);
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(10,10,5,5);
        add(splitPane, c);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        m_importButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UPLOAD_TO_SERVER), false);
        m_importButton.setToolTipText("Import CellenOne Manip");
        m_importButton.setEnabled(false);
        m_importButton.addActionListener(e -> importAction());
        toolbar.add(m_importButton);

      FlatButton m_refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH), false);
        m_refreshButton.setToolTipText("Refresh CellenOne Manips");
        m_refreshButton.addActionListener(e -> loadData(true));
        toolbar.add(m_refreshButton);

        return toolbar;
    }

    private JPanel createStudyPanel() {
        AbstractNode root = new RootNode();
        m_studyTree = new JTree(root);
        m_studyTree.setCellRenderer(new TreeRenderer());
        m_studyTree.addTreeSelectionListener(this::studySelected);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Select study to import CellenOne data"));
        panel.add(new JScrollPane(m_studyTree), BorderLayout.CENTER);
        return panel;
    }

    private void studySelected(TreeSelectionEvent event) {
        m_importButton.setEnabled(getSelectedStudy() != null && m_manipTable.getSelectedRow() != -1);
    }

    private StudyJson getSelectedStudy() {
        TreePath path = m_studyTree == null ? null : m_studyTree.getSelectionPath();
        if (path == null) return null;
        Object node = path.getLastPathComponent();
        return node instanceof StudyNode ? (StudyJson) ((StudyNode) node).getData() : null;
    }

    private void setStudies(List<StudyJson> studies) {
        AbstractNode root = new RootNode();
        Map<String, AbstractNode> programs = new LinkedHashMap<>();
        Map<String, AbstractNode> projects = new LinkedHashMap<>();
        studies.stream()
                .sorted(Comparator.comparing(StudyJson::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(study -> {
                    ProjectJson project = study.getProjectId() == -1 ? null : DataManager.getProject(study.getProjectId());
                    String programName = project == null ? "Orphan Projects" : getProgramName(project);
                    AbstractNode programNode = programs.computeIfAbsent(programName, name -> {
                        ProgramJson program = (project == null || project.getProgramId() == -1)  ? null : DataManager.getProgram(project.getProgramId());
                        AbstractNode node = (program == null) ? new StudyTreeGroupNode(name, IconManager.IconType.PROGRAM) : new ProgramNode(program);
                        root.add(node);
                        return node;
                    });
                    String projectName = project == null ? "Orphan Studies" : project.getTitle() + "(" + project.getNomenclatureTitle() + ")";
                    String projectKey = programName + "\u0000" + projectName;
                    AbstractNode projectNode = projects.computeIfAbsent(projectKey, name -> {
                        AbstractNode node = project == null ? new StudyTreeGroupNode(projectName, IconManager.IconType.PROJECT) : new ProjectNode(project);
                        programNode.add(node);
                        return node;
                    });
                    projectNode.add(new StudyNode(study));
                });
        m_studyTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
        if (root.getChildCount() > 0) m_studyTree.expandRow(0);
        m_importButton.setEnabled(false);
    }

    private String getProgramName(ProjectJson project) {
        if (project.getProgramId() != -1) {
            ProgramJson program = DataManager.getProgram(project.getProgramId());
            if (program != null) return program.getNomenclatureTitle();
        }
        return "Orphan Projects";
    }

    private JSplitPane createMainSplitPane() {
        m_manipTableModel = new CellenOneManipTableModel();
        m_manipTable = new DecoratedTable();
        m_manipTable.setModel(m_manipTableModel);
        m_manipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_runTableModel = new CellenOneRunTableModel();
        DecoratedTable m_runTable = new DecoratedTable();
        m_runTable.setModel(m_runTableModel);
        m_runTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_manipTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = m_manipTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = m_manipTable.convertRowIndexToModel(selectedRow);
                    CellenOneManipJson selectedManip = m_manipTableModel.getManipAt(modelRow);
                    m_importButton.setEnabled(selectedManip != null && getSelectedStudy() != null);
                    if (selectedManip != null) {
                        m_runTableModel.setRuns(selectedManip.getRuns());
                    } else {
                        m_runTableModel.clear();
                    }
                } else {
                    m_importButton.setEnabled(false);
                    m_runTableModel.clear();
                }
            }
        });

        JScrollPane manipScrollPane = new JScrollPane(m_manipTable);
        JPanel manipPanel = new JPanel(new BorderLayout());
        manipPanel.setBorder(new TitledBorder("'Orphan' CellenOne Manips"));
        manipPanel.add(manipScrollPane, BorderLayout.CENTER);

        JScrollPane runScrollPane = new JScrollPane(m_runTable);
        JPanel runPanel = new JPanel(new BorderLayout());
        runPanel.setBorder(new TitledBorder("Associated CellenOne Runs"));
        runPanel.add(runScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, manipPanel, runPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.5);

        return splitPane;
    }

    private void importAction() {
        if (isLoading()) {
            return;
        }
        int selectedRow = m_manipTable.getSelectedRow();
        StudyJson selectedStudy = getSelectedStudy();
        if (selectedRow == -1 || selectedStudy == null) {
            return;
        }
        int rowInModel = m_manipTable.convertRowIndexToModel(selectedRow);
        CellenOneManipJson manip = m_manipTableModel.getManipAt(rowInModel);
        if (manip == null) {
            return;
        }
        m_importLoadingId = getNewLoadingIndex();
        setLoading(m_importLoadingId);
        m_manipTable.setEnabled(false);
        m_studyTree.setEnabled(false);
        m_importButton.setEnabled(false);
        loadSamplesAndImport(manip, List.of(selectedStudy));
    }

    private void loadSamplesAndImport(CellenOneManipJson manip, List<StudyJson> studies) {
        Map<Integer, List<SampleJson>> samplesByStudy = new java.util.LinkedHashMap<>();
        loadSamplesForStudy(manip, studies, 0, samplesByStudy);
    }

    private void loadSamplesForStudy(CellenOneManipJson manip, List<StudyJson> studies, int index,
                                     Map<Integer, List<SampleJson>> samplesByStudy) {
        if (index == studies.size()) {
            loadPlatesAndShowImportDialog(manip, studies, samplesByStudy);
            return;
        }
        StudyJson study = studies.get(index);
        ArrayList<SampleJson> samples = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override public boolean mustBeCalledInAWT() { return true; }
            @Override public void run(boolean success, long taskId, boolean finished) {
                if (!success) {
                    finishImportLoading();
                    showError("Load Samples Failed", "The server load samples request failed.");
                    return;
                }
                samplesByStudy.put(study.getId(), samples);
                loadSamplesForStudy(manip, studies, index + 1, samplesByStudy);
            }
        };
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new SamplesTask(callback, study, samples));
    }

    private void loadPlatesAndShowImportDialog(CellenOneManipJson manip, List<StudyJson> studies,
                                               Map<Integer, List<SampleJson>> samplesByStudy) {
        List<PlateJson> plates = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override public boolean mustBeCalledInAWT() { return true; }
            @Override public void run(boolean success, long taskId, boolean finished) {
                if (!success) {
                    finishImportLoading();
                    showError("Load Plates Failed", "The server load plates request failed.");
                    return;
                }
                finishImportLoading();
                CellenOneImportDialog dialog = new CellenOneImportDialog(MainFrame.getMainWindow(), manip, studies.get(0), samplesByStudy, plates);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == CellenOneImportDialog.BUTTON_OK) {
                    startImportLoading();
                    createSamplesIfNeeded(studies.get(0), dialog.getRequest(), dialog.getSamplesToCreate());
                }
            }
        };
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new CellenOnePlatesTask(callback, studies.get(0), plates));
    }

    private void createSamplesIfNeeded(StudyJson study, CellenOneImportJson request,
                                       List<SampleJson> samplesToCreate) {
        if (!samplesToCreate.isEmpty()) {
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                @Override public boolean mustBeCalledInAWT() { return true; }

                @Override public void run(boolean success, long taskId, boolean finished) {
                    if (success) {
                        importManip(request, study);
                    } else {
                        finishImportLoading();
                        showError("Create Samples Failed", "The selected new Samples could not be created.");
                    }
                }
            };
            AccessDatabaseThread.getAccessDatabaseThread().addTask(new AddSampleTask(callback, study,
                    new ArrayList<>(samplesToCreate)));
            return;
        }
        importManip(request, study);
    }

    private void finishImportLoading() {
        if (m_importLoadingId != -1) {
            setLoaded(m_importLoadingId);
            m_importLoadingId = -1;
        }
        m_manipTable.setEnabled(true);
        m_studyTree.setEnabled(true);
        m_importButton.setEnabled(m_manipTable.getSelectedRow() != -1 && getSelectedStudy() != null);
    }

    private void startImportLoading() {
        m_importLoadingId = getNewLoadingIndex();
        setLoading(m_importLoadingId);
        m_manipTable.setEnabled(false);
        m_studyTree.setEnabled(false);
        m_importButton.setEnabled(false);
    }

    private void importManip(CellenOneImportJson request, StudyJson study) {
        m_importButton.setEnabled(false);
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                finishImportLoading();
                if (success) {
                    JOptionPane.showMessageDialog(CellenOneManipsPanel.this,
                            "CellenOne Manip imported in Study " + study.getTitle() + ".",
                            "Import Manip", JOptionPane.INFORMATION_MESSAGE);
                    loadData(true);
                } else {
                    String details = getTaskError() == null ? "The server import data request failed." : getTaskError().toString();
                    showError("Import Failed", details);
                }
            }
        };
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new ImportCellenOneManipTask(callback, request));
    }

    private void showError(String title, String details) {
        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, title, details);
        infoDialog.centerToWindow(MainFrame.getMainWindow());
        infoDialog.setVisible(true);
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
        loadStudies();
    }

    private void loadStudies() {
        ArrayList<StudyJson> studies = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override public boolean mustBeCalledInAWT() { return true; }

            @Override public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    setStudies(studies);
                } else {
                    showError("Load Studies Failed", "Failed to load eligible Studies from server.");
                }
            }
        };
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new LoadCellenOneStudiesTask(callback, studies));
    }

    public void reinit() {
        m_dataLoaded = false;
        m_manipTableModel.clear();
        m_runTableModel.clear();
        m_importButton.setEnabled(false);
    }

    private static class StudyTreeGroupNode extends AbstractNode {
        private final String m_name;
        private final IconManager.IconType m_iconType;

        private StudyTreeGroupNode(String name, IconManager.IconType iconType) {
            super(NodeTypes.TREE_PARENT, name);
            m_name = name;
            m_iconType = iconType;
        }

        @Override
        public void loadNode(boolean studyNomenclature) {
        }

        @Override
        public ImageIcon getIcon(boolean expanded) {
            return IconManager.getIcon(m_iconType);
        }

        @Override
        public void filter(String owner, boolean onGoing, String searchText, java.util.Date dateFrom,
                           java.util.Date dateTo) {
        }

        @Override
        public String toString() {
            return m_name;
        }
    }
}
