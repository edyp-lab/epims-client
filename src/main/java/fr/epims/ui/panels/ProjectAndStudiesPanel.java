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


import fr.edyp.epims.json.ProjectJson;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Panel with Project info and a list of its Studies
 *
 * @author JM235353
 *
 */
public class ProjectAndStudiesPanel extends JPanel {

    public ProjectAndStudiesPanel(ProjectJson p) {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JPanel projectPanel = new ProjectPanel(p);

        JPanel studiesListPanel = new StudiesListPanel(p);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(projectPanel);
        splitPane.setBottomComponent(studiesListPanel);
        splitPane.setDividerLocation(0.5);

        c.weightx = 1;
        c.weighty = 1;
        add(splitPane, c);

    }
}
