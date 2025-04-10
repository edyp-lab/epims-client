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

package fr.epims.ui.dialogs;


import fr.edyp.epims.json.RobotPlanningJson;
import fr.edyp.epims.json.SampleJson;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.panels.robot.RobotRequestPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * Dialog to ask to add a Sample to a Robot Plate
 *
 * @author JM235353
 *
 */
public class RobotRequestDialog extends DefaultDialog {

    private RobotRequestPanel m_robotRequestPanel;

    public RobotRequestDialog(Window parent, ArrayList<SampleJson> samples, HashSet<SampleJson> selectedSamples) {
        super(parent);

        setTitle("Create Robot Request");

        m_robotRequestPanel = new RobotRequestPanel(this, samples, selectedSamples);

        setInternalComponent(m_robotRequestPanel);

        setButtonVisible(BUTTON_HELP, false);

        setResizable(true);
        setPreferredSize(new Dimension(900,600));

    }

    public ArrayList<RobotPlanningJson> getRobotPlannings() {
        return m_robotRequestPanel.getRobotPlannings();
    }

    @Override
    public boolean okCalled() {

        return m_robotRequestPanel.checkFields(this);
    }


}