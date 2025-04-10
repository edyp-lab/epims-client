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

import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.CompanyJson;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 *
 * Dialog to create or modify a company (affiliation)
 *
 * @author JM235353
 *
 */
public class CreateOrModifyCompanyDialog extends DefaultDialog {

    private CompanyJson m_modifyCompany;

    private JTextField m_nameTextField;
    private JTextField m_managerTextField;
    private JTextField m_addressTextField;
    private JTextField m_postalCodeField;


    public CreateOrModifyCompanyDialog(Window parent, CompanyJson modifyCompany) {
        super(parent);

        m_modifyCompany = modifyCompany;

        setTitle((modifyCompany == null) ? "Create Affiliation" : "Modify Affiliation");

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
        m_managerTextField = new JTextField(50);
        m_addressTextField = new JTextField(50);
        m_postalCodeField = new JTextField(50);



        if (m_modifyCompany != null) {
            m_nameTextField.setText(m_modifyCompany.getName());
            m_managerTextField.setText(m_modifyCompany.getManager());
            m_addressTextField.setText(m_modifyCompany.getAddress());
            m_postalCodeField.setText(m_modifyCompany.getPostalCode());

            m_nameTextField.setEnabled(false);
        }


        // -------------- Place Widgets

        // --- Last Name
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Name:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_nameTextField, c);

        // --- First Name
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Manager:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_managerTextField, c);

        // --- Telephone
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Adress:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_addressTextField, c);

        // --- Fax
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Postal Code:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_postalCodeField, c);


        return p;
    }

    @Override
    public boolean okCalled() {

        String name = m_nameTextField.getText().trim();
        if (name.isEmpty()) {
            highlight(m_nameTextField);
            setStatus(true, "You must fill the affiliation Name.");
            return false;
        }
        if (name.length()>50) {
            highlight(m_nameTextField);
            setStatus(true, "The Name can not exceed 50 characters.");
            return false;
        }

        if ((m_modifyCompany == null) && DataManager.getCompany(name) != null) {
            highlight(m_nameTextField);
            setStatus(true, "An affiliation with this name already exists");
            return false;
        }

        return true;

    }

    public CompanyJson getCompanyToCreate() {
        CompanyJson companyJson = new CompanyJson(m_nameTextField.getText().trim(), m_managerTextField.getText().trim(),
                m_addressTextField.getText().trim(), m_postalCodeField.getText().trim());

        return companyJson;

    }

}
