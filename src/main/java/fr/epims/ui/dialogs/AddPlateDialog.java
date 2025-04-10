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

import fr.edyp.epims.json.*;
import fr.epims.ui.common.DefaultDialog;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;

/**
 *
 * Dialog to Create a new Robot Plate
 *
 * @author JM235353
 *
 */
public class AddPlateDialog extends DefaultDialog {

    private HashSet<String> m_takenNamesSet;

    private JTextField m_plateNameTextField = null;
    private JFormattedTextField m_xSizeTextField = null;
    private JFormattedTextField m_ySizeTextField = null;


    public AddPlateDialog(Window parent, HashSet<String> takenNamesSet, String defaultName, int defaultX, int defaultY) {
        super(parent);

        m_takenNamesSet = takenNamesSet;

        setTitle("Add Plate");

        setInternalComponent(createInternalPanel(defaultName, defaultX, defaultY));

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

    }

    public JPanel createInternalPanel(String defaultName, int defaultX, int defaultY) {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        m_plateNameTextField = new JTextField(30);
        m_plateNameTextField.setText(defaultName);

        NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();

        m_xSizeTextField = new JFormattedTextField(integerFieldFormatter);
        m_xSizeTextField.setColumns(10);
        m_xSizeTextField.setPreferredSize(m_xSizeTextField.getPreferredSize());
        m_xSizeTextField.setText(String.valueOf(defaultX));

        m_ySizeTextField = new JFormattedTextField(integerFieldFormatter);
        m_ySizeTextField.setColumns(10);
        m_ySizeTextField.setPreferredSize(m_ySizeTextField.getPreferredSize());
        m_ySizeTextField.setText(String.valueOf(defaultY));

        // -------------- Place Widgets

        // --- Name
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Plate Name:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_plateNameTextField, c);

        // --- X Size
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Plate Width:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_xSizeTextField, c);

        // --- Y Size
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Plate Height:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_ySizeTextField, c);

        return p;
    }

    @Override
    public boolean okCalled() {

        // Name check
        String name = m_plateNameTextField.getText().trim();
        if (name.isEmpty()) {
            highlight(m_plateNameTextField);
            setStatus(true, "You must fill the Plate Name.");
            return false;
        }
        if (name.length()>50) {
            highlight(m_plateNameTextField);
            setStatus(true, "The Plate Name can not exceed 50 characters.");
            return false;
        }
        if (m_takenNamesSet.contains(name)) {
            highlight(m_plateNameTextField);
            setStatus(true, "A Plate named "+name+" already exists.");
            return false;
        }

        // xSize check
        String xSize = m_xSizeTextField.getText();
        Integer xSizeInteger;
        try {
            xSizeInteger = Integer.valueOf(xSize);
        } catch (NumberFormatException nfe) {
            highlight(m_xSizeTextField);
            setStatus(true, "The Plate Width must be an integer.");
            return false;
        }
        if (xSizeInteger<4 || xSizeInteger>256) {
            highlight(m_xSizeTextField);
            setStatus(true, "Plate Width must be between 4 and 256.");
            return false;
        }

        // ySize check
        String ySize = m_ySizeTextField.getText();
        Integer ySizeInteger;
        try {
            ySizeInteger = Integer.valueOf(ySize);
        } catch (NumberFormatException nfe) {
            highlight(m_ySizeTextField);
            setStatus(true, "The Plate Height must be an integer.");
            return false;
        }
        if (ySizeInteger<4 || ySizeInteger>256) {
            highlight(m_ySizeTextField);
            setStatus(true, "Plate Height must be between 4 and 256.");
            return false;
        }

        return true;
    }


    public VirtualPlateJson getVirtualPlateToCreate(String actorKey) {

        String xSize = m_xSizeTextField.getText();
        Integer xSizeInteger = 0;
        try {
            xSizeInteger = Integer.valueOf(xSize);
        } catch (NumberFormatException nfe) {

        }

        String ySize = m_ySizeTextField.getText();
        Integer ySizeInteger = 0;
        try {
            ySizeInteger = Integer.valueOf(ySize);
        } catch (NumberFormatException nfe) {

        }

        VirtualPlateJson virtualPlateJson = new VirtualPlateJson(m_plateNameTextField.getText().trim(), actorKey,
                new Date(), Boolean.FALSE, xSizeInteger, ySizeInteger, new HashSet());


        return virtualPlateJson;

    }


}
