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
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 *
 * Renderer for the Activities Tree (specific icons)
 *
 * @author JM235353
 *
 */
public  class TreeRenderer extends DefaultTreeCellRenderer {

    public TreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        /*if (((AbstractNode) value).isDisabled()) {
            l.setForeground(Color.LIGHT_GRAY);
        } else {
            if (sel) {
                l.setForeground(Color.WHITE);
            } else {
                l.setForeground(Color.BLACK);
            }
        }*/
        ImageIcon icon = ((AbstractNode) value).getIcon(expanded);
        if (icon != null) {
            setIcon(icon);
        }

        return this;
    }
}