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
 *
 * Framed Text Area put in a Scroll Pane with a label at the left
 *
 * @author JM235353
 *
 */
public class TextAreaPanel extends JPanel {
    public TextAreaPanel(String label, JTextArea textArea) {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel(label, SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createHorizontalGlue(), c);

        JScrollPane scrollPane = new JScrollPane();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        scrollPane.setViewportView(textArea);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;

        c.weightx = 1;
        c.weighty = 1;
        add(scrollPane, c);
    }
}
