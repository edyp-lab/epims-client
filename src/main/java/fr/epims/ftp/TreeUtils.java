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

package fr.epims.ftp;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 *
 * Util class on Tree to expand a tree, save the expand state of a tree and restore it if needed
 *
 * @author AK249877
 *
 */
public class TreeUtils {


    public static void saveExpansionState(JTree tree, String rootSuffix) {

        StringBuilder builder = new StringBuilder();

        HashSet<String> set = TreeUtils.getExpansionState(tree);

        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            builder.append((String) iter.next());
            builder.append(";");
        }

    }


    public static HashSet<String> getExpansionState(JTree tree) {
        HashSet<String> expandedPaths = new HashSet<String>();
        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath tp = tree.getPathForRow(i);
            if (tree.isExpanded(i)) {
                expandedPaths.add(tp.toString());
            }
        }
        return expandedPaths;
    }

    public static void setExpansionState(HashSet<String> previouslyExpandedPaths, JTree tree, DefaultMutableTreeNode root, String rootSuffix) {
        if (previouslyExpandedPaths == null || previouslyExpandedPaths.isEmpty()) {
            return;
        }

            tree.getModel().addTreeModelListener(new TreeModelListener() {

                @Override
                public void treeNodesChanged(TreeModelEvent tme) {
                }

                @Override
                public void treeNodesInserted(TreeModelEvent tme) {
                }

                @Override
                public void treeNodesRemoved(TreeModelEvent tme) {
                    ;
                }

                @Override
                public void treeStructureChanged(TreeModelEvent tme) {
                    tree.getModel().removeTreeModelListener(this);
                    TreePath triggerPath = new TreePath(tme.getPath());
                    if (!tree.isExpanded(triggerPath)) {
                        tree.expandPath(triggerPath);
                    }
                    setExpansionState(previouslyExpandedPaths, tree, root, rootSuffix);
                }

            });


        Enumeration totalNodes = root.preorderEnumeration();

        while (totalNodes.hasMoreElements() && !previouslyExpandedPaths.isEmpty()) {

            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) totalNodes.nextElement();
            TreePath tp = new TreePath(currentNode.getPath());

            if (previouslyExpandedPaths.contains(tp.toString())) {
                previouslyExpandedPaths.remove(tp.toString());
                tree.expandPath(tp);
            }
        }

    }

    public static void expandTree(JTree tree, boolean expand, boolean notFirst) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand, notFirst);
    }

    public static void expandAll(JTree tree, TreePath path, boolean expand, boolean notFirst) {
        TreeNode node = (TreeNode) path.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                TreeNode n = (TreeNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);

                expandAll(tree, p, expand, false);
            }
        }

        if (expand) {
            tree.expandPath(path);
        } else {
            if (!notFirst) {
                tree.collapsePath(path);
            } else {
                tree.expandPath(path);
            }
        }
    }
}
