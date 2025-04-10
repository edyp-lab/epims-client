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

import fr.edyp.epims.json.StudyJson;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Panel with study info and list of its samples
 *
 * @author JM235353
 *
 */
public class StudyAndSamplesPanel extends JPanel {

    private SamplesListPanel m_samplePanel;
    private AcquisitionsListPanel m_acquisitionsPanel;
    private StudyCommentPanel m_studyCommentPanel;

    public StudyAndSamplesPanel(StudyJson s) {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JPanel studyPanel = new StudyPanel(s);

        JTabbedPane sampleAndAcquisitionsTabbedPane = createTabbedPane(s);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(studyPanel);
        splitPane.setBottomComponent(sampleAndAcquisitionsTabbedPane);
        splitPane.setDividerLocation(0.5);

        c.weightx = 1;
        c.weighty = 1;
        add(splitPane, c);

    }

    private JTabbedPane createTabbedPane(StudyJson s) {

        JTabbedPane tabbedPane = new JTabbedPane();

        m_samplePanel = new SamplesListPanel(s);
        tabbedPane.addTab("Samples", m_samplePanel);
        tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.SAMPLE));

        m_acquisitionsPanel = new AcquisitionsListPanel(s);
        tabbedPane.addTab("Acquisitions", m_acquisitionsPanel);
        tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.ACQUISITIONS));

        m_studyCommentPanel = new StudyCommentPanel(s);
        tabbedPane.addTab("Comments", m_studyCommentPanel);
        tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.FILE));

        if (AcquisitionsListPanel.m_keepAcquisitionsListDisplay) {
            // WART to keep the Acquisitions List displayed after a Refresh on this list
            AcquisitionsListPanel.m_keepAcquisitionsListDisplay = false;
            tabbedPane.setSelectedIndex(1);
        }

        return tabbedPane;
    }




    private void loadAcquisitionData() {

    }
}
