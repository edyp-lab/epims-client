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

import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.epims.ftp.TreeUtils;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatPanel;
import fr.epims.ui.tree.AbstractTree;
import fr.epims.ui.tree.InvisibleTreeModel;
import fr.epims.ui.tree.RootNode;
import fr.epims.ui.tree.TreeSelectionInterface;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashSet;

/**
 *
 * Studies Tree of the SearchStudiesRefDialog
 *
 * @author JM235353
 *
 */
public class StudiesTreePanel extends JPanel implements DataManager.DataManagerListener, TreeSelectionInterface {

    private GridBagConstraints c;

    private FilterStudiesPanel m_filterStudiesPanel;

    private JTextField m_studyNomenclatureTF;

    private AbstractTree m_tree = null;


    private DefaultDialog m_parentDialog;

    public StudiesTreePanel(String nomenclature, DefaultDialog parentDialog) {
        super(new GridBagLayout());

        m_parentDialog = parentDialog;

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel programTreePanel = createProgramTreePanel(nomenclature);



        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(programTreePanel, c);


        final StudiesTreePanel _this = this;
        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();

                RootNode rootNode = ((RootNode) model.getRoot());
                rootNode.setData(DataManager.getPrograms());
                rootNode.loadNode(true);

                m_filterStudiesPanel.filter(false);

                DataManager.addListener(ProgramJson.class, _this);
                DataManager.addListener(ProjectJson.class, _this);
                DataManager.addListener(StudyJson.class, _this);

            }
        };
        DataManager.dataAvailable(callback, false);



    }

    public String getStudyRef() {
        return m_studyNomenclatureTF.getText();
    }

    @Override
    public void setProgram(ProgramJson p) {

    }

    @Override
    public void setProject(ProjectJson p) {

    }

    @Override
    public void setStudy(StudyJson s, boolean doubleClick) {
        m_studyNomenclatureTF.setText(s.getNomenclatureTitle());
        if (doubleClick) {
            m_parentDialog.okButtonActionPerformed();
        }
    }


    public AbstractTree getTree() {
        return m_tree;
    }

    public void filter(String searchText) {
        InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();
        RootNode rootNode = ((RootNode) model.getRoot());

        if (searchText != null) {
            searchText = searchText.toLowerCase();
        }
        rootNode.filter(null, false, searchText, null, null);

        model.activateFilter(true);
        model.reload();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    m_tree.expandRow(0);
                    for (int i = 0; i < m_tree.getRowCount(); i++) {
                        m_tree.expandRow(i);
                    }
                }
            });

    }


    private JPanel createProgramTreePanel(String nomenclature) {

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_filterStudiesPanel = new FilterStudiesPanel(this);

        m_studyNomenclatureTF = new JTextField();
        m_studyNomenclatureTF.setEditable(false);
        m_studyNomenclatureTF.setText(nomenclature);
        Component[] components = { new JLabel("Study Reference:"),
                m_studyNomenclatureTF};


        JPanel treePanel = createTreePanel();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        mainPanel.add(m_filterStudiesPanel, c);


        c.weighty = 1;
        c.gridy++;
        mainPanel.add(treePanel, c);

        c.weighty = 0;
        c.gridy++;
        mainPanel.add(new FlatPanel(components), c);




        return mainPanel;
    }

    private JPanel createTreePanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);



        JScrollPane treeScrollPane = new JScrollPane();
        m_tree = new AbstractTree(new RootNode(), this);
        treeScrollPane.setViewportView(m_tree);



        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        panel.add(treeScrollPane, c);


        return panel;

    }

    public void clearTree(final boolean keepExpansion) {

        HashSet<String> expandedPaths = null;
        if (keepExpansion) {
            expandedPaths = TreeUtils.getExpansionState(m_tree);
        }
        final TreePath currentPath = m_tree.getSelectionPath();



        m_tree.changeRoot(new RootNode());

        final HashSet<String> _expandedPaths = expandedPaths;
        final StudiesTreePanel _this = this;
        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();

                RootNode rootNode = ((RootNode) model.getRoot());
                rootNode.setData(DataManager.getPrograms());
                rootNode.loadNode(true);

                m_filterStudiesPanel.filter(false);

                DataManager.addListener(ProgramJson.class, _this);
                DataManager.addListener(ProjectJson.class, _this);
                DataManager.addListener(StudyJson.class, _this);

                TreeUtils.setExpansionState(_expandedPaths, m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), null);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        m_tree.display(currentPath, false);
                    }
                });

            }
        };
        DataManager.dataAvailable(callback, false);
    }

    @Override
    public void updateAll(HashSet<Class> c) {

        clearTree(true);


    }

    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {

        // for the moment : for ProgramJson.class, ProjectJson.class and Study.class : do the same thing
        switch (actionType) {
            case UPDATE:
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        m_tree.display(m_tree.getSelectionPath(), false); // selecting again > update the display
                    }
                });

                break;
            case CREATE:
                TreePath path = m_tree.getSelectionPath();
                reload();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (! m_tree.display(o, false)) { // select new created object
                            m_tree.display(path, false); // reselect old path
                        }
                    }
                });

                break;
        }


    }



    public void reload() {
        InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();

        RootNode rootNode = ((RootNode) model.getRoot());
        rootNode.setData(DataManager.getPrograms());
        rootNode.loadNode(true);


        m_filterStudiesPanel.filter(true);
    }

}
