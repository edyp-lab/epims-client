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
import java.awt.*;

/**
 *
 * Title Panel is used by Program, Project and Study Panel to display the title.
 *
 * @author JM235353
 *
 */
public class TitlePanel extends JPanel {

    public TitlePanel(String title, ImageIcon icon, JButton[] buttons) {
        super(new GridBagLayout());

        setBackground(new Color(80,180, 255));
        setOpaque(true);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setIcon(icon);
        titleLabel.setOpaque(false);
        titleLabel.setForeground(Color.white);
        titleLabel.setIconTextGap(10);

        c.gridx = 0;
        c.gridy = 0;
        add(titleLabel, c);

        c.gridx++;
        c.weightx++;
        add(Box.createGlue(), c);


        if (buttons != null) {
            c.weightx = 0;
            c.gridx++;
            JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
            toolbar.setOpaque(false);
            toolbar.setFloatable(false);
            for (JButton b : buttons) {
                if (b == null) {
                    continue;
                }
                toolbar.add(b);
            }
            add(toolbar, c);
        }



    }
}
