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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * Parent Node of All node classes
 *
 * @author JM235353
 *
 */
public abstract class AbstractNode extends InvisibleNode  {

    public enum NodeTypes {
        TREE_PARENT,
        PROGRAM,
        PROJECT,
        STUDY
    }
    private static Action[] m_actionInstance = null;
    protected NodeTypes m_type;


    public AbstractNode(NodeTypes type, Object data) {
        super(data);
        m_type = type;
    }

    public abstract void loadNode(boolean studyNomenclature);

    public NodeTypes getType() {
        return m_type;
    }

    public Object getData() {
        return getUserObject();
    }


    public abstract ImageIcon getIcon(boolean expanded);

    public AbstractNode findNode(Object data, boolean filterActive) {
        if (getData().equals(data)) {
            return this;
        }

        int nb = getChildCount(filterActive);
        for (int i=0;i<nb;i++) {
            AbstractNode node = ((AbstractNode) getChildAt(i, filterActive)).findNode(data, filterActive);
            if (node != null) {
                return node;
            }
        }

        return null;
    }

}
