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

package fr.epims.ui.common;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Horizontal panel containg a list of components, the last component takes the potential remaining place
 */
public class FlatPanel extends JPanel {

    public FlatPanel(Component[] components) {
        this(components, null);
    }
    public FlatPanel(Component[] components, String borderTitle) {

        setLayout(new GridBagLayout());

        if (borderTitle != null) {
            Border titledBorder = BorderFactory.createTitledBorder(borderTitle);
            setBorder(titledBorder);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 5);

        c.gridx = 0;
        c.gridy = 0;

        for (int x=0;x<components.length;x++) {
            if (x == components.length-1) {
                c.weightx = 1;
            }
            add(components[x], c);
            c.gridx++;
        }


    }
}
