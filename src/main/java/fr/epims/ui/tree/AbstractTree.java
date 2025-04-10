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

package fr.epims.ui.tree;

import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * Activities Tree
 *
 * @author JM235353
 *
 */
public class AbstractTree extends JTree implements TreeSelectionListener {

    protected InvisibleTreeModel m_model;
    protected RootNode m_rootNode;

    private TreeSelectionInterface m_treeSelectionInterface;

    public AbstractTree(RootNode top, TreeSelectionInterface treeSelectionInterface) {
        initTree(top);
        m_treeSelectionInterface = treeSelectionInterface;

        addTreeSelectionListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = getSelectionPath();
                    if (path == null) {
                        return;
                    }
                    AbstractNode node = (AbstractNode) path.getLastPathComponent();
                    display(path, true);
                }
            }

        });
    }

    public void changeRoot(RootNode top) {
        m_rootNode = top;

        m_model = new InvisibleTreeModel(top);
        setModel(m_model);
    }

    private void initTree(RootNode top) {

        m_rootNode = top;

        m_model = new InvisibleTreeModel(top);
        setModel(m_model);

        setRowHeight(18);

        setRootVisible(true);


        TreeRenderer renderer = new TreeRenderer();
        setCellRenderer(renderer);


        // add tooltips
        ToolTipManager.sharedInstance().registerComponent(this);

        TreeUtils.expandTree(this, true);
    }


    public TreePath getSelectedPath() {
        int[] selectedRows = getSelectionRows();
        if (selectedRows == null) {
            return null;
        }
        int nbSelectedRows = selectedRows.length;
        if (nbSelectedRows != 1) {
            return null;
        }

        int row = selectedRows[0];
        TreePath path = getPathForRow(row);

        return path;
    }


    public void display(TreePath path, boolean doubleClick) {

        if (path == null) {
            return;
        }

        scrollPathToVisible(path);
        setSelectionPath(path);

        AbstractNode node = (AbstractNode)  path.getLastPathComponent();

        if (doubleClick) {
            if (node.getType() == AbstractNode.NodeTypes.STUDY) {
                StudyJson s = (StudyJson) node.getData();

                m_treeSelectionInterface.setStudy(s, doubleClick);
            }
            return;
        }

        if (node.getType() == AbstractNode.NodeTypes.PROGRAM) {
            ProgramJson p = (ProgramJson) node.getData();

            m_treeSelectionInterface.setProgram(p);


        } else if (node.getType() == AbstractNode.NodeTypes.PROJECT) {

            ProjectJson p = (ProjectJson) node.getData();

            m_treeSelectionInterface.setProject(p);

        } else if (node.getType() == AbstractNode.NodeTypes.STUDY) {
            StudyJson s = (StudyJson) node.getData();

            m_treeSelectionInterface.setStudy(s, false);

        }
    }



    public boolean display(Object userObject, boolean doubleClick) {
        AbstractNode node = m_rootNode.findNode(userObject, true);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            display(path, doubleClick);
            return true;
        } else {
            node = m_rootNode.findNode(userObject, false);
            if (node != null) {
                clearSelection();
                if (node.getType() == AbstractNode.NodeTypes.PROGRAM) {
                    ProgramJson p = (ProgramJson) node.getData();

                    m_treeSelectionInterface.setProgram(p);


                } else if (node.getType() == AbstractNode.NodeTypes.PROJECT) {

                    ProjectJson p = (ProjectJson) node.getData();

                    m_treeSelectionInterface.setProject(p);

                } else if (node.getType() == AbstractNode.NodeTypes.STUDY) {
                    StudyJson s = (StudyJson) node.getData();

                    m_treeSelectionInterface.setStudy(s, doubleClick);

                }

            }
        }

        return false;
    }


    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = getSelectedPath();
        if (path == null) {
            return;
        }

        display(path, false);
    }


}
