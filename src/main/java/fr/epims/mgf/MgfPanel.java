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

package fr.epims.mgf;

import fr.edyp.epims.json.MgfFileInfoJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.*;

import fr.epims.tasks.mgf.LoadMgfFilesTask;
import fr.epims.tasks.mgf.StudyForMgfTask;
import fr.epims.ui.common.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Panel which displays the mgf files on disk
 * Possibility to select the mgf path root
 * Possibility to filter the mgf files according to their status (transferred, deleted, being transferred...)
 * Actions on mgf files through a toolbar
 *
 * @author JM235353
 *
 */
public class MgfPanel extends HourGlassPanel {


    private static MgfPanel m_singleton = null;

    public static MgfPanel getPanel() {
        if (m_singleton == null) {
            m_singleton = new MgfPanel();
        }
        return m_singleton;
    }


    private JTextField m_mgfRootTextField;
    private FlatButton m_selectRootButton;

    private FlatButton m_uploadToServerButton;
    private FlatButton m_deleteButton;

    private DecoratedTable m_mgfTable;
    private MgfTableModel m_model;

    private JTextField m_directoryTF;

    private JRadioButton m_allStatusRB;
    private JRadioButton m_specificStatusRB;
    private JCheckBox m_unsavedMgfCB;
    private JCheckBox m_transferredMgfCB;
    private JCheckBox m_errorMgfCB;
    private JCheckBox m_deletedMgfCB;

    private JLabel m_infoLabel;

    private MgfPanel() {
        setBorder(new TitledBorder(""));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_mgfTable = new DecoratedTable();
        m_model = new MgfTableModel();
        m_mgfTable.setModel(m_model);

        m_mgfRootTextField = new JTextField(60);
        m_mgfRootTextField.setEditable(false);
        String path = MgfFileManager.getSingleton().getRootDirectoryPath();
        boolean canModifyPath = path.isEmpty();
        m_mgfRootTextField.setText(path);
        m_mgfRootTextField.setEnabled(canModifyPath);
        m_mgfRootTextField.setMinimumSize(new Dimension(200, 5));
        m_mgfRootTextField.setMaximumSize(new Dimension(460, 5));
        m_selectRootButton = new FlatButton(IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED), true);
        m_selectRootButton.setVisible(canModifyPath);

        JPanel filterPanel = createFilterPanel();

        JPanel tablePanel = createTablePanel();


        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Mgf Root Directory:"), c);

        c.gridx++;
        c.weightx = 0.25;
        add(m_mgfRootTextField, c);

        c.gridx++;
        c.weightx = 0;
        add(m_selectRootButton, c);

        c.gridx++;
        c.weightx = 0.75;
        add(Box.createHorizontalGlue(), c);

        c.gridx = 0;
        c.gridwidth = 4;
        c.gridy++;
        add(filterPanel, c);

        c.gridy++;
        c.weighty = 1;
        add(tablePanel, c);

        m_selectRootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String path = m_mgfRootTextField.getText().trim();

                JFileChooser fchooser = new JFileChooser(path);
                fchooser.setDialogTitle("Select Mgf Root Directory Path");
                fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fchooser.setAcceptAllFileFilterUsed(false);

                int result = fchooser.showOpenDialog(m_selectRootButton);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File directory = fchooser.getSelectedFile();
                    if (!directory.exists()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Error", "Directory does not exist");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }

                    String newPath = directory.getAbsolutePath();
                    if (! newPath.equals(path)) {
                        m_mgfRootTextField.setText(newPath);
                        MgfFileManager.getSingleton().setRoot(directory);
                        m_model.reset();
                        loadData(true, true);
                    }
                }
            }
        });

        ListSelectionModel selectionModel = m_mgfTable.getSelectionModel();

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int[] rows = m_mgfTable.getSelectedRows();

                long bytes = 0;

                boolean uploadAllowed = rows.length>0;
                boolean deleteAllowed = rows.length>0;
                String info = "";

                if (rows.length>0) {
                    for (int row : rows) {
                        row = m_mgfTable.convertRowIndexToModel(row);
                        MgfFileInfo mgfFileInfo = m_model.getMgfFileInfo(row);
                        MgfFileInfo.StatusEnum status = mgfFileInfo.getStatus();
                        if (status != MgfFileInfo.StatusEnum.NO_INFO) {
                            uploadAllowed = false;
                        }
                        if (status != MgfFileInfo.StatusEnum.FTP_DONE) {
                            deleteAllowed = false;
                        }

                        bytes += mgfFileInfo.getFile().length();

                    }

                    double mo = ((double) bytes) / 1000000;
                    String totalSizeMo = String.format("%.2f Mo", mo);

                    if (rows.length == 1) {
                        info = "1 file, size: " + totalSizeMo;
                    } else {
                        info = rows.length+" files, size: " + totalSizeMo;
                    }
                }

                m_infoLabel.setText(info);

                m_deleteButton.setEnabled(deleteAllowed);
                m_uploadToServerButton.setEnabled(uploadAllowed);
            }
        });

    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JToolBar toolbar = createToolbar();
        JScrollPane tableScrollPane = new JScrollPane(m_mgfTable);
        m_mgfTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        panel.add(toolbar, c);

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        panel.add(tableScrollPane, c);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Filter ");
        filterPanel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JLabel directoryLabel = new JLabel("Directory :", SwingConstants.RIGHT);
        m_directoryTF = new JTextField(60);

        JLabel statusLabel = new JLabel("Status :", SwingConstants.RIGHT);

        m_allStatusRB = new JRadioButton("All Mgf");
        m_specificStatusRB = new JRadioButton("Specific Mgf");
        ButtonGroup group = new ButtonGroup();
        group.add(m_allStatusRB);
        group.add(m_specificStatusRB);

        ActionListener rbActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = m_specificStatusRB.isSelected();
                m_unsavedMgfCB.setEnabled(enabled);
                m_transferredMgfCB.setEnabled(enabled);
                m_errorMgfCB.setEnabled(enabled);
                m_deletedMgfCB.setEnabled(enabled);

                filter();
            }
        };

        m_allStatusRB.addActionListener(rbActionListener);
        m_specificStatusRB.addActionListener(rbActionListener);

        JPanel checkBoxPanel = createCheckBoxPanel();

        m_allStatusRB.setSelected(true);


        c.gridx = 0;
        c.gridy = 0;
        filterPanel.add(directoryLabel, c);

        c.gridx++;
        filterPanel.add(m_directoryTF, c);

        c.gridx++;
        c.weightx = 1;
        filterPanel.add(Box.createGlue(), c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        filterPanel.add(statusLabel, c);

        c.gridx++;
        filterPanel.add(m_allStatusRB, c);

        c.gridy++;
        filterPanel.add(m_specificStatusRB, c);

        c.gridy++;
        filterPanel.add(checkBoxPanel, c);

        c.gridx++;
        c.weightx = 1;
        filterPanel.add(Box.createGlue(), c);


        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        };

        m_directoryTF.getDocument().addDocumentListener(docListener);

        return filterPanel;
    }

    private JPanel createCheckBoxPanel() {
        JPanel checkboxPanel = new JPanel(new GridBagLayout());


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_unsavedMgfCB = new JCheckBox("Unsaved Mgf");
        m_transferredMgfCB = new JCheckBox("Transferred Mgf");
        m_errorMgfCB = new JCheckBox("Mgf in Error");
        m_deletedMgfCB = new JCheckBox("Locally Deleted Mgf");

        m_unsavedMgfCB.setEnabled(false);
        m_transferredMgfCB.setEnabled(false);
        m_errorMgfCB.setEnabled(false);
        m_deletedMgfCB.setEnabled(false);
        m_unsavedMgfCB.setSelected(true);
        m_transferredMgfCB.setSelected(true);
        m_errorMgfCB.setSelected(true);
        m_deletedMgfCB.setSelected(true);

        c.gridx = 0;
        c.gridy = 0;
        checkboxPanel.add(Box.createHorizontalStrut(30), c);

        c.gridx++;
        checkboxPanel.add(m_unsavedMgfCB, c);

        c.gridx++;
        checkboxPanel.add(m_transferredMgfCB, c);

        c.gridx++;
        c.weightx = 1;
        checkboxPanel.add(Box.createGlue(), c);
        c.weightx = 0;

        c.gridy++;
        c.gridx = 0;
        checkboxPanel.add(Box.createHorizontalStrut(30), c);

        c.gridx++;
        checkboxPanel.add(m_errorMgfCB, c);

        c.gridx++;
        checkboxPanel.add(m_deletedMgfCB, c);

        c.gridx++;
        c.weightx = 1;
        checkboxPanel.add(Box.createGlue(), c);
        c.weightx = 0;

        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = m_specificStatusRB.isSelected();
                m_unsavedMgfCB.setEnabled(enabled);
                m_transferredMgfCB.setEnabled(enabled);
                m_errorMgfCB.setEnabled(enabled);
                m_deletedMgfCB.setEnabled(enabled);

                filter();
            }
        };

        m_unsavedMgfCB.addActionListener(actionListener);
        m_transferredMgfCB.addActionListener(actionListener);
        m_errorMgfCB.addActionListener(actionListener);
        m_deletedMgfCB.addActionListener(actionListener);

        return checkboxPanel;

    }

    private void filter() {
        String directoryName = m_directoryTF.getText().trim();

        boolean all = m_allStatusRB.isSelected();
        boolean unsavedMgf = true;
        boolean transferredMgf = true;
        boolean errorMgf = true;
        boolean deletedMgf = true;
        if (!all) {
            unsavedMgf = m_unsavedMgfCB.isSelected();
            transferredMgf = m_transferredMgfCB.isSelected();
            errorMgf = m_errorMgfCB.isSelected();
            deletedMgf = m_deletedMgfCB.isSelected();
        }

        m_model.filter(directoryName, all, unsavedMgf, transferredMgf, errorMgf, deletedMgf);
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);

        m_uploadToServerButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UPLOAD_TO_SERVER), false);
        m_uploadToServerButton.setToolTipText("Upload Mgf files to Server");
        m_uploadToServerButton.setEnabled(false);
        toolbar.add(m_uploadToServerButton);

        m_deleteButton = new FlatButton(IconManager.getIcon(IconManager.IconType.DELETE_MGFFILE), false);
        m_deleteButton.setToolTipText("Delete Mgf files");
        m_deleteButton.setEnabled(false);
        toolbar.add(m_deleteButton);

        FlatButton refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH), false);
        refreshButton.setToolTipText("Refresh Mgf List");
        toolbar.add(refreshButton);

        toolbar.add(Box.createHorizontalStrut(30));

        m_infoLabel = new JLabel("");
        toolbar.add(m_infoLabel);


        m_uploadToServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = m_mgfTable.getSelectedRows();

                m_mgfTable.clearSelection();

                for (int row : rows) {
                    int rowInModel = m_mgfTable.convertRowIndexToModel(row);
                    final MgfFileInfo mgfFileInfo = m_model.getMgfFileInfo(rowInModel);

                    if (mgfFileInfo.getStudyId() == -1) {

                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, boolean finished) {
                                if (success) {
                                    if (mgfFileInfo.getStudyId() == -1) {
                                        mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.FAILED);
                                        mgfFileInfo.setErrorMessage("Can not find Study");
                                        m_model.dataChanged(rowInModel);
                                    } else {
                                        mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.STUDY_DONE);
                                        m_model.dataChanged(rowInModel);

                                        MgfTransferThread.getTransferThread().setModel(m_model);

                                        MgfTransferThread.getTransferThread().addTask(mgfFileInfo);
                                    }

                                } else {
                                    //InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                    //infoDialog.centerToWindow(MainFrame.getMainWindow());
                                    //infoDialog.setVisible(true);
                                }
                            }
                        };

                        TaskInfoCallbackInterface taskCallback = new TaskInfoCallbackInterface() {

                            @Override
                            public void stateChaned(int state) {
                                if (state == TaskInfo.PUBLIC_STATE_RUNNING) {
                                    mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.STUDY_RUNNING);
                                    m_model.dataChanged(rowInModel);
                                }
                            }
                        };

                        mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.STUDY_WAITING);
                        m_model.dataChanged(rowInModel);

                        StudyForMgfTask task = new StudyForMgfTask(callback, taskCallback, mgfFileInfo);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                    } else {

                        mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.STUDY_WAITING);
                        m_model.dataChanged(rowInModel);

                        MgfTransferThread.getTransferThread().setModel(m_model);
                        MgfTransferThread.getTransferThread().addTask(mgfFileInfo);
                    }
                }

            }
        });

        m_deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = m_mgfTable.getSelectedRows();

                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.QUESTION, "Delete files", "Do you really want to delete "+rows.length+" file(s) ?");
                infoDialog.centerToWindow(MainFrame.getMainWindow());
                infoDialog.setVisible(true);
                if (infoDialog.getButtonClicked() != InfoDialog.BUTTON_OK) {
                    return;
                }

                for (int row : rows) {
                    row = m_mgfTable.convertRowIndexToModel(row);
                    MgfFileInfo mgfFileInfo = m_model.getMgfFileInfo(row);
                    try {
                        mgfFileInfo.getFile().delete();
                        mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.DELETED);
                        m_model.dataChanged(row);
                    } catch (Exception ex) {
                        mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.FAILED);
                        mgfFileInfo.setErrorMessage("Can not Delete File");
                        m_model.dataChanged(row);
                    }
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_model.reset();
                String path = m_mgfRootTextField.getText().trim();
                MgfFileManager.getSingleton().setRoot(new File(path));
                loadData(true, true);

            }
        });

        return toolbar;

    }

    public void loadData(boolean serverData, boolean localFiles) {

        if (m_model.getRowCount()>0) {
            // data already loaded
            return;
        }

        setLoading(getNewLoadingIndex(), false, false);

        int nbStep = 0;
        if (serverData) {
            nbStep++;
        }
        if (localFiles) {
            nbStep++;
        }

        MgfCallback callback = new MgfCallback(nbStep);

        if (serverData) {
            LoadMgfFilesTask task = new LoadMgfFilesTask(callback, callback.getMgfArrayList());
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }

        if (localFiles) {
            MgfFileManager.getSingleton().getMgfMap(callback);
        }
    }

    private void lookForFirstUnknowStudy() {
        boolean lookupDone = false;
        for (MgfFileInfo mgfFileInfo : m_model.getMgfInfoList()) {

            if (! mgfFileInfo.isStudySearched()) {

                lookupDone = true;
                StudyForMgfTask task = new StudyForMgfTask(new StudyCallback(mgfFileInfo), null, mgfFileInfo);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                break;
            }
        }
        if (!lookupDone) {
            MgfFileManager.getSingleton().writeMgfDB( m_model.getMgfInfoList());
        }
    }

    private class StudyCallback extends AbstractDatabaseCallback {

        private MgfFileInfo m_mgfFileInfo;

        public StudyCallback(MgfFileInfo mgfFileInfo) {
            m_mgfFileInfo = mgfFileInfo;
        }
        @Override
        public boolean mustBeCalledInAWT() {
            return true;
        }

        @Override
        public void run(boolean success, long taskId, boolean finished) {
            m_mgfFileInfo.setStudySearched();
            if (success) {
                m_model.dataChanged(m_mgfFileInfo);

                lookForFirstUnknowStudy();

            }
        }
    };

    private class MgfCallback extends AbstractDatabaseCallback implements MgfFileManager.MgfFilesListener {

        private int m_step = 0;
        private int m_nbSteps;

        private HashMap<String, ArrayList<File>> m_mgfMap = new HashMap<>();

        private ArrayList<MgfFileInfoJson> m_mgfArrayList = new ArrayList<>();

        public MgfCallback(int nbSteps) {
            m_nbSteps = nbSteps;
        }

        @Override
        public boolean mustBeCalledInAWT() {
            return true;
        }

        @Override
        public void run(boolean success, long taskId, boolean finished) {
            if (success) {
                done();

            } else {
                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                infoDialog.centerToWindow(MainFrame.getMainWindow());
                infoDialog.setVisible(true);
            }
            setLoaded(m_id);
        }

        public ArrayList<MgfFileInfoJson> getMgfArrayList() {
            return m_mgfArrayList;
        }

        private void done() {
            m_step++;
            if (m_step == m_nbSteps) {
                // informations from database and files are loaded

                HashMap<String, HashSet<String>> mgfOnServerMap = new HashMap<>();
                for (MgfFileInfoJson mgfFileInfoJson : m_mgfArrayList) {
                    String name = mgfFileInfoJson.getName();
                    String path = mgfFileInfoJson.getDirectoryPath();
                    int indexOFLastSlash = path.lastIndexOf('/');

                    String directoryName;
                    if (indexOFLastSlash != -1) {
                        directoryName = path.substring(indexOFLastSlash+1);
                    } else {
                        directoryName = path;
                    }

                    HashSet<String> directoriesSet = mgfOnServerMap.get(directoryName);
                    if (directoriesSet == null) {
                        directoriesSet = new HashSet<>();
                        mgfOnServerMap.put(directoryName, directoriesSet);
                    }
                    directoriesSet.add(name);

                }


                ArrayList<MgfFileInfo> mgfFileInfoArrayList = new ArrayList<>();
                for (String directoryName : m_mgfMap.keySet()) {
                    ArrayList<File> mgfFiles = m_mgfMap.get(directoryName);
                    for (File mgfFile : mgfFiles) {
                        MgfFileInfo mgfFileInfo = new MgfFileInfo(directoryName, mgfFile, -1);
                        mgfFileInfoArrayList.add(mgfFileInfo);
                        HashSet<String> filesOnFTP = mgfOnServerMap.get(directoryName);
                        if (filesOnFTP != null) {
                            if (filesOnFTP.contains(mgfFile.getName())) {
                                mgfFileInfo.setStatus(MgfFileInfo.StatusEnum.FTP_DONE);
                            }
                        }
                    }
                }
                m_model.setValues(mgfFileInfoArrayList);

                // load data from Local MGFInfo file
                MgfFileManager.getSingleton().getExtraInfo(m_model.getMgfInfoMap());

                // look for studies info from server
               lookForFirstUnknowStudy();
            }
        }

        @Override
        public void mgfFilesMapLoaded(HashMap<String, ArrayList<File>> map) {
            m_mgfMap = map;
            done();
        }
    }

    public static class MgfFileInfoRenderer extends DefaultTableCellRenderer {

        private boolean m_state;

        public MgfFileInfoRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, false, row, column);

            MgfFileInfo mgfFileInfo = (MgfFileInfo) value;

            label.setIcon(mgfFileInfo.getStateIcon());

            return this;
        }
    }

}
