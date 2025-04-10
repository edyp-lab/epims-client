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

import fr.edyp.epims.json.ProgramJson;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Panel with Program info and a list of its Projects
 *
 * @author JM235353
 *
 */
public class ProgramAndProjectsPanel extends JPanel {

    public ProgramAndProjectsPanel(ProgramJson p) {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JPanel programPanel = new ProgramPanel(p);

        JPanel projectsListPanel = new ProjectsListPanel(p);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(programPanel);
        splitPane.setBottomComponent(projectsListPanel);
        splitPane.setDividerLocation(0.5);

        c.weightx = 1;
        c.weighty = 1;
        add(splitPane, c);

    }
}
