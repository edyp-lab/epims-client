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

import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.SampleSpeciesJson;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;

public class AddSampleSpeciesDialog extends DefaultDialog {

    private JTextField m_nameTextField;


    public AddSampleSpeciesDialog(Window parent) {
        super(parent);


        setTitle("Create Sample Species");

        setInternalComponent(createInternalPanel());

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

    }


    private JPanel createInternalPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        m_nameTextField = new JTextField(50);


        // -------------- Place Widgets

        // --- Last Name
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Name:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_nameTextField, c);



        return p;
    }

    @Override
    public boolean okCalled() {

        String name = m_nameTextField.getText().trim();
        if (name.isEmpty()) {
            highlight(m_nameTextField);
            setStatus(true, "You must fill the Sample Species Name.");
            return false;
        }
        if (name.length()>50) {
            highlight(m_nameTextField);
            setStatus(true, "The Sample Species Name can not exceed 50 characters.");
            return false;
        }

        boolean sampleSpeciesEist = false;
        ArrayList<SampleSpeciesJson> sampleSpeciesList = DataManager.getSampleSpecies();
        for (SampleSpeciesJson sampleSpecies : sampleSpeciesList) {
            if (name.equalsIgnoreCase(sampleSpecies.getName())) {
                sampleSpeciesEist = true;
                break;
            }
        }
        if (sampleSpeciesEist) {
            highlight(m_nameTextField);
            setStatus(true, name+" Sample Species already exists.");
            return false;
        }


        return true;

    }

    public SampleSpeciesJson getSampleSpeciesToCreate() {
        SampleSpeciesJson sampleSpeciesJson = new SampleSpeciesJson(-1, m_nameTextField.getText().trim());

        return sampleSpeciesJson;

    }

}
