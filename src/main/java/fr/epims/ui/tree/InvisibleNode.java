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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * Used to hide nodes during filtering
 *
 * @author JM235353
 *
 */
public abstract class InvisibleNode extends DefaultMutableTreeNode {

    protected boolean m_visible = true;

    public InvisibleNode() {
        this(null);
    }

    public InvisibleNode(Object userObject) {
        this(userObject, true, true);
    }

    public InvisibleNode(Object userObject, boolean allowsChildren, boolean visible) {
        super(userObject, allowsChildren);
        this.m_visible = visible;
    }

    public TreeNode getChildAt(int index, boolean filterIsActive) {
        if (!filterIsActive) {
            return super.getChildAt(index);
        }
        if (children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }

        int realIndex = -1;
        int visibleIndex = -1;
        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            InvisibleNode node = (InvisibleNode) e.nextElement();
            if (node.isVisible()) {
                visibleIndex++;
            }
            realIndex++;
            if (visibleIndex == index) {
                return (TreeNode) children.elementAt(realIndex);
            }
        }

        throw new ArrayIndexOutOfBoundsException("index unmatched");
        //return (TreeNode)children.elementAt(index);
    }

    public int getChildCount(boolean filterIsActive) {
        if (!filterIsActive) {
            return super.getChildCount();
        }
        if (children == null) {
            return 0;
        }

        int count = 0;
        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            InvisibleNode node = (InvisibleNode) e.nextElement();
            if (node.isVisible()) {
                count++;
            }
        }

        return count;
    }

    public void setVisible(boolean visible) {
        this.m_visible = visible;
    }

    public boolean isVisible() {
        return m_visible;
    }


    public abstract void filter(String owner, boolean onGoing, String searchText, Date dateFrom, Date dateTo);

}
