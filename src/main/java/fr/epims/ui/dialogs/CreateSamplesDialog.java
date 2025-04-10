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


import fr.edyp.epims.json.SampleJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.panels.CreateSamplePanel;


import java.awt.*;
import java.util.ArrayList;

/**
 *
 * Dialog to Create Samples for a Study
 *
 * @author JM235353
 *
 */
public class CreateSamplesDialog extends DefaultDialog  {

    private CreateSamplePanel m_samplesPanel;

    public CreateSamplesDialog(Window parent, StudyJson s, ArrayList<SampleJson> samples) {
        super(parent);

        setTitle("Add Samples");

        m_samplesPanel = new CreateSamplePanel(this, s, samples);
        setInternalComponent(m_samplesPanel);

        setButtonVisible(BUTTON_HELP, false);

        setResizable(true);
        setPreferredSize(new Dimension(900,600));

    }

    public ArrayList<SampleJson> getSamplesToCreate() {
        return m_samplesPanel.getSamplesToCreate();
    }


    @Override
    public boolean okCalled() {

        return m_samplesPanel.checkFields(this);

    }
}
