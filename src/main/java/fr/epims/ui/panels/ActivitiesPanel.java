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
import fr.epims.dataaccess.*;
import fr.epims.ftp.TreeUtils;
import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.tasks.AddProgramTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.AddProgramDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.tree.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * Panel with all activities in a tree with a Filter Panel
 *
 * @author JM235353
 *
 */
public class ActivitiesPanel extends JPanel implements DataManager.DataManagerListener, TreeSelectionInterface {

    private static ActivitiesPanel m_singleton = null;

    private Component m_currentRightPanel = null;
    private GridBagConstraints c;

    private FilterActivitiesPanel m_filterActivitiesPanel;
    //private AbstractTree m_favouritesTree = null;
    private AbstractTree m_tree = null;

    public static ActivitiesPanel getActivitiesPanel() {
        if (m_singleton == null) {
            m_singleton = new ActivitiesPanel();
        }
        return m_singleton;
    }

    private ActivitiesPanel() {
        super(new GridBagLayout());

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel activitiesPanel = createProgramTreePanel();
        m_currentRightPanel = createWhitePanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(activitiesPanel);
        splitPane.setRightComponent(m_currentRightPanel);
        //splitPane.setDividerLocation(0.5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(splitPane, c);

        /*
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        add(activitiesPanel, c);

        // prepare to put a right panel
        c.gridx++;
        c.weightx = 1;

        add(m_currentRightPanel, c);*/

        final ActivitiesPanel _this = this;
        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();

                RootNode rootNode = ((RootNode) model.getRoot());
                rootNode.setData(DataManager.getPrograms());
                rootNode.loadNode(false);

                m_filterActivitiesPanel.filter(false);

                DataManager.addListener(ProgramJson.class, _this);
                DataManager.addListener(ProjectJson.class, _this);
                DataManager.addListener(StudyJson.class, _this);

            }
        };
        DataManager.dataAvailable(callback, false);



    }

    public void clearTree(final boolean keepExpansion) {

        HashSet<String> expandedPaths = null;
        if (keepExpansion) {
            expandedPaths = TreeUtils.getExpansionState(m_tree);
        }
        final TreePath currentPath = m_tree.getSelectionPath();

        clearRightPanel();


        m_tree.changeRoot(new RootNode());

        final HashSet<String> _expandedPaths = expandedPaths;
        final ActivitiesPanel _this = this;
        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();

                RootNode rootNode = ((RootNode) model.getRoot());
                rootNode.setData(DataManager.getPrograms());
                rootNode.loadNode(false);

                m_filterActivitiesPanel.filter(false);

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

    public AbstractTree getTree() {
        return m_tree;
    }

    public void filter(String user, boolean onGoing, String searchText, Date startDate, Date endDate, boolean expand) {
        InvisibleTreeModel model = (InvisibleTreeModel) m_tree.getModel();
        RootNode rootNode = ((RootNode) model.getRoot());

        if (searchText != null) {
            searchText = searchText.toLowerCase();
        }
        rootNode.filter(user, onGoing, searchText, startDate, endDate);

        model.activateFilter(true);
        model.reload();

        if (expand) {
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
    }

    private JPanel createWhitePanel() {


        JPanel p = new JPanel();
        p.setBackground(Color.white);
        p.setOpaque(true);
        //p.setPreferredSize(new Dimension(50,50));

        return p;
    }

    private void replaceRightPanel(JPanel newPanel) {
        JSplitPane parent = (JSplitPane) m_currentRightPanel.getParent();
        int dividerLocation = parent.getDividerLocation();
        parent.remove(m_currentRightPanel);
        m_currentRightPanel = newPanel;
        parent.add(m_currentRightPanel);
        parent.setDividerLocation(dividerLocation);
        parent.revalidate();
        parent.repaint();
    }

    @Override
    public void setProgram(ProgramJson p) {
        replaceRightPanel(new ProgramAndProjectsPanel(p));
    }

    @Override
    public void setProject(ProjectJson p) {
        replaceRightPanel(new ProjectAndStudiesPanel(p));
    }

    @Override
    public void setStudy(StudyJson s, boolean doubleClick) {
        replaceRightPanel(new StudyAndSamplesPanel(s));
    }
    public void clearRightPanel() {
        replaceRightPanel(createWhitePanel());
    }


    private JPanel createProgramTreePanel() {

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_filterActivitiesPanel = new FilterActivitiesPanel(this);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel treePanel = createTreePanel();
        tabbedPane.addTab("Activities", treePanel);
        tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.ACTIVITIES_FILTERED));

        /*JPanel favouriteTreePanel = createFavouritesTreePanel();
        tabbedPane.addTab("Favourite Activities", favouriteTreePanel);
        tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.FAVOURITES));
*/
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        mainPanel.add(m_filterActivitiesPanel, c);


        c.weighty = 1;
        c.gridy++;
        mainPanel.add(tabbedPane, c);


        return mainPanel;
    }

    private JPanel createTreePanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 5, 5);

        JToolBar toolbar = createTreeToolbar();

        JScrollPane treeScrollPane = new JScrollPane();
        m_tree = new AbstractTree(new RootNode(), this);
        treeScrollPane.setViewportView(m_tree);
        m_tree.setVisible(false);



        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        panel.add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        panel.add(treeScrollPane, c);

        return panel;

    }

    /*
    private JPanel createFavouritesTreePanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 5, 5, 5);

        JToolBar toolbar = createTreeToolbar();

        JScrollPane treeScrollPane = new JScrollPane();
        m_favouritesTree = new AbstractTree(new RootNode());
        treeScrollPane.setViewportView(m_favouritesTree);
        m_favouritesTree.setVisible(false);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        panel.add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        panel.add(treeScrollPane, c);

        return panel;

    }*/

    public void connect() {
        //m_favouritesTree.setVisible(true);
        m_tree.setVisible(true);
    }

    public void disconnect() {
        //m_favouritesTree.setVisible(false);
        m_tree.setVisible(false);

        clearRightPanel();

        m_tree.clearSelection();
        clearTree(false);

        m_filterActivitiesPanel.reinit();
    }




    private JToolBar createTreeToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);

        FlatButton addProgramButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_PROGRAM), false);
        toolbar.add(addProgramButton);

        toolbar.addSeparator();

        FlatButton expandButton = new FlatButton(IconManager.getIcon(IconManager.IconType.EXPAND_TREE), false);
        toolbar.add(expandButton);

        FlatButton collapseButton = new FlatButton(IconManager.getIcon(IconManager.IconType.COLLAPSE_TREE), false);
        toolbar.add(collapseButton);

        toolbar.addSeparator();

        FlatButton refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH), false);
        toolbar.add(refreshButton);

        addProgramButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), ProgramJson.class.getSimpleName());
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

                if (!DataManager.checkOwner((ProgramJson)null)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be an Admin or Admin User to create a Program.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                AddProgramDialog dialog = new AddProgramDialog(MainFrame.getMainWindow());
                dialog.centerToScreen();
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    ProgramJson p = dialog.getProgramToCreate();
                    ProgramJson[] arrProgram = new ProgramJson[1];
                    arrProgram[0] = p;

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.createProgram(arrProgram[0]);

                                // refresh tree
                                //reload();
                            } else {
                               InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                               infoDialog.centerToWindow(MainFrame.getMainWindow());
                               infoDialog.setVisible(true);
                            }
                        }
                    };

                    AddProgramTask task = new AddProgramTask(callback, arrProgram);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }

            }
        });


        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreeUtils.expandTree(m_tree, true, false);
            }
        });

        collapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreeUtils.expandTree(m_tree, false, true);
            }
        });



        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (! updateDataDialog.isDataUpdated()) {
                    // no data update was needed  : display a message for the user
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Up-to-date", "Data were already up-to-date.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }
            }
        });

        return toolbar;

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
        rootNode.loadNode(false);


        m_filterActivitiesPanel.filter(true);
    }

}
