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

import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

/**
 *
 * Result of Search Acquisitions
 *
 * @author JM235353
 *
 */
public class AcquisitionsPanel extends JPanel implements DataManager.DataManagerListener {
    private static AcquisitionsPanel m_singleton = null;

    private AcquisitionsSearchListPanel m_acquisitionsSearchListPanel;
    private FilterAcquisitionsPanel m_filterAcquisitionsPanel;

    public static AcquisitionsPanel getAcquisitionsPanel() {
        if (m_singleton == null) {
            m_singleton = new AcquisitionsPanel();
        }
        return m_singleton;
    }

    private AcquisitionsPanel() {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_acquisitionsSearchListPanel = new AcquisitionsSearchListPanel();
        m_filterAcquisitionsPanel = new FilterAcquisitionsPanel(m_acquisitionsSearchListPanel);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(m_filterAcquisitionsPanel, c);


        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        add(m_acquisitionsSearchListPanel, c);


        DataManager.addListener(ActorJson.class, this);
        DataManager.addListener(ContactJson.class, this);
        DataManager.addListener(InstrumentJson.class, this);
        DataManager.addListener(StudyJson.class, this);

    }

    public void reinit() {
        m_acquisitionsSearchListPanel.reinitData();
        m_filterAcquisitionsPanel.reinit();
    }

    @Override
    public void updateAll(HashSet<Class> c) {
        // we do not redo the search, we just reinit the data
        m_acquisitionsSearchListPanel.reinitData();
        m_filterAcquisitionsPanel.fillModels();
    }

    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {
        // nothing to do

    }


}
