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

package fr.epims.ui.panels.admin;

import fr.epims.MainFrame;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.InstrumentJson;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.dialogs.TextSelectionDialog;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 *
 * Dialog to create a new Instrument (Spectrometer)
 *
 * @author JM235353
 *
 */
public class CreateInstrumentDialog extends DefaultDialog {

    private InstrumentJson m_modifyInstrument;

    private JTextField m_nameTextField;
    private JTextField m_modelTextField;
    private JTextField m_manufacturerTextField;
    private JRadioButton m_spectrometerRadioButton;
    private JRadioButton m_robotRadioButton;
    private JCheckBox m_availableCheckbox;

    private JDialog m_dialog;

    public CreateInstrumentDialog(Window parent, InstrumentJson modifyInstrument) {
        super(parent);

        m_modifyInstrument = modifyInstrument;

        setTitle((modifyInstrument == null) ? "Create Instrument" : "Modify Instrument");

        setInternalComponent(createInternalPanel());

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        m_dialog = this;

    }

    public JPanel createInternalPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        m_nameTextField = new JTextField(50);
        m_modelTextField = new JTextField(50);
        m_manufacturerTextField = new JTextField(50);

        m_spectrometerRadioButton = new JRadioButton("Spectrometer");
        m_robotRadioButton = new JRadioButton("Robot");
        ButtonGroup group = new ButtonGroup();
        group.add(m_spectrometerRadioButton);
        group.add(m_robotRadioButton);

        m_availableCheckbox = new JCheckBox("Available");


        FlatButton selectModelButton = new FlatButton(IconManager.getIcon(IconManager.IconType.LIST), true);
        FlatButton selectManufacturerButton = new FlatButton(IconManager.getIcon(IconManager.IconType.LIST), true);

        if (m_modifyInstrument != null) {
            m_nameTextField.setText(m_modifyInstrument.getName());
            m_manufacturerTextField.setText(m_modifyInstrument.getManufacturer());
            m_modelTextField.setText(m_modifyInstrument.getModel());
            m_spectrometerRadioButton.setSelected(m_modifyInstrument.getIsSpectrometer());

            m_nameTextField.setEnabled(false);
            m_spectrometerRadioButton.setEnabled(false);
            m_robotRadioButton.setEnabled(false);

            if (m_modifyInstrument.getIsSpectrometer()) {
                m_spectrometerRadioButton.setSelected(true);
            } else {
                m_robotRadioButton.setSelected(true);
            }

            m_availableCheckbox.setSelected(m_modifyInstrument.isAvailable());
        } else {
            m_spectrometerRadioButton.setSelected(true);
            m_availableCheckbox.setSelected(true);
            m_availableCheckbox.setEnabled(false);
        }


        // -------------- Place Widgets

        // --- Name
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Name:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 2;
        p.add(m_nameTextField, c);
        c.gridwidth = 1;

        // --- Model
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Model:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 2;
        p.add(m_modelTextField, c);
        c.gridwidth = 1;

        c.gridx = 3;
        p.add(selectModelButton, c);

        // --- Manufacturer
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Manufacturer:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);


        c.gridx++;
        c.gridwidth = 2;
        p.add(m_manufacturerTextField, c);
        c.gridwidth = 1;

        c.gridx = 3;
        p.add(selectManufacturerButton, c);

        // --- Spectrometer or Robot
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Type:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_spectrometerRadioButton, c);

        c.gridx++;
        p.add(m_robotRadioButton, c);

        // Available
        c.gridy++;
        c.gridx = 1;
        c.gridwidth = 2;
        p.add(m_availableCheckbox, c);
        c.gridwidth = 1;


        selectModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<String> modelsSet = new HashSet<>();
                ArrayList<InstrumentJson> spectrometers = DataManager.getSpectrometers();
                for (InstrumentJson i : spectrometers) {
                    String model = i.getModel();
                    modelsSet.add(model);
                }

                ArrayList<String> modelList = new ArrayList<>(modelsSet);
                Collections.sort(modelList);

                TextSelectionDialog dialog = new TextSelectionDialog(m_dialog, "a Model", modelList);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    m_modelTextField.setText(dialog.getSelectedValue());
                }

            }
        });

        selectManufacturerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HashSet<String> modelsSet = new HashSet<>();
                ArrayList<InstrumentJson> spectrometers = DataManager.getSpectrometers();
                for (InstrumentJson i : spectrometers) {
                    String manufacturer = i.getManufacturer();
                    modelsSet.add(manufacturer);
                }

                ArrayList<String> manufacturerList = new ArrayList<>(modelsSet);
                Collections.sort(manufacturerList);

                TextSelectionDialog dialog = new TextSelectionDialog(m_dialog, "a Manufacturer", manufacturerList);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    m_manufacturerTextField.setText(dialog.getSelectedValue());
                }

            }
        });

        return p;
    }

    @Override
    public boolean okCalled() {

        String name = m_nameTextField.getText().trim();
        if (name.isEmpty()) {
            highlight(m_nameTextField);
            setStatus(true, "You must fill the Name.");
            return false;
        }
        if (name.length()>50) {
            highlight(m_nameTextField);
            setStatus(true, "The Name can not exceed 50 characters.");
            return false;
        }

        String model = m_modelTextField.getText().trim();
        if (model.isEmpty()) {
            highlight(m_modelTextField);
            setStatus(true, "You must fill the Model.");
            return false;
        }
        if (model.length()>50) {
            highlight(m_modelTextField);
            setStatus(true, "The Model can not exceed 50 characters.");
            return false;
        }

        String manufacturer = m_manufacturerTextField.getText().trim();
        if (manufacturer.isEmpty()) {
            highlight(m_manufacturerTextField);
            setStatus(true, "You must fill the Manufacturer.");
            return false;
        }
        if (manufacturer.length()>50) {
            highlight(m_manufacturerTextField);
            setStatus(true, "The Manufacturer can not exceed 50 characters.");
            return false;
        }

        if ((m_modifyInstrument == null) && DataManager.getInstrument(name) != null) {
            highlight(m_nameTextField);
            setStatus(true, "An instrument with this name already exists");
            return false;
        }

        return true;

    }

    public InstrumentJson getInstrumentToCreate() {

        String availability;
        if (m_modifyInstrument != null) {
            availability = m_availableCheckbox.isSelected() ? "available" : "unavailable";
        } else {
            availability = "available";
        }

        InstrumentJson instrumentJson = new InstrumentJson((m_modifyInstrument ==null) ? -1 : m_modifyInstrument.getId(),
                m_nameTextField.getText().trim(), m_manufacturerTextField.getText().trim(),
                m_modelTextField.getText().trim(), availability,
                m_spectrometerRadioButton.isSelected());

        return instrumentJson;

    }

}
