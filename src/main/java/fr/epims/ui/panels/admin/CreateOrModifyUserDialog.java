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
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ActorJson;
import fr.edyp.epims.json.CompanyJson;
import fr.edyp.epims.json.ContactJson;
import fr.epims.tasks.CreateCompanyTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.renderers.CompanyComboBoxRenderer;
import fr.epims.ui.renderers.RoleComboBoxRenderer;
import fr.epims.util.UtilMail;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 *
 * Dialog to create or modify an actor/contact
 *
 * @author JM235353
 *
 */
public class CreateOrModifyUserDialog extends DefaultDialog {

    private boolean m_actorCreation;
    private ActorJson m_modifyActor;
    private ContactJson m_modifyContact;

    private JTextField m_lastNameTextField;
    private JTextField m_firstNameTextField;
    private JTextField m_telephoneTextField;
    private JTextField m_faxTextField;
    private JTextField m_emailTextField1;
    private JTextField m_emailTextField2;
    private JComboBox m_companyCombobox;

    private JTextField m_loginTextField;
    private JPasswordField m_passwordTextField1;
    private JPasswordField m_passwordTextField2;
    private JComboBox m_roleCombobox;

    private DefaultComboBoxModel<CompanyJson> m_companiesModel = new DefaultComboBoxModel<>( );

    public CreateOrModifyUserDialog(Window parent, boolean actorCreation, ActorJson modifyActor, ContactJson modifyContact) {
        super(parent);

        m_actorCreation = actorCreation;
        m_modifyActor = modifyActor;
        m_modifyContact = modifyContact;

        if (actorCreation) {
            setTitle((modifyActor == null) ? "Create User" : "Modify User");
        } else {
            setTitle((modifyContact == null) ? "Create Contact" : "Modify Contact");
        }


        setInternalComponent(createInternalPanel());

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

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
        m_lastNameTextField = new JTextField(50);
        m_firstNameTextField = new JTextField(50);
        m_telephoneTextField = new JTextField(50);
        m_faxTextField = new JTextField(50);
        m_emailTextField1 = new JTextField(50);
        m_emailTextField2 = new JTextField(50);

        fillCompanies(null);
        m_companyCombobox = new JComboBox<>(m_companiesModel);
        m_companyCombobox.setRenderer(new CompanyComboBoxRenderer());

        FlatButton createCompanyButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_HOME), "Create", true);


        m_loginTextField = new JTextField(50);
        m_passwordTextField1 = new JPasswordField(50);
        m_passwordTextField2 = new JPasswordField(50);

        Vector<String> roles = new Vector<>();
        roles.add(null);
        for (String role : DataManager.getRoles()) {
            roles.add(role);
        }
        DefaultComboBoxModel<String> rolesModel = new DefaultComboBoxModel<>( roles );
        m_roleCombobox = new JComboBox<>(rolesModel);
        m_roleCombobox.setRenderer(new RoleComboBoxRenderer());


        ContactJson modifyContact = m_modifyContact;
        if (m_modifyActor != null) {
            modifyContact = m_modifyActor.getContact();
        } else {
            modifyContact = m_modifyContact;;
        }
        if (modifyContact != null) {

            m_lastNameTextField.setText(modifyContact.getLastName());
            m_firstNameTextField.setText(modifyContact.getFirstName());
            m_telephoneTextField.setText(modifyContact.getTelephoneNumber());
            m_faxTextField.setText(modifyContact.getFaxNumber());
            m_emailTextField1.setText(modifyContact.getEmail());
            m_emailTextField2.setText(modifyContact.getEmail());


            CompanyJson companyJson = DataManager.getCompany(modifyContact);
            m_companyCombobox.setSelectedItem(companyJson);

        }
        if (m_modifyActor != null) {
            m_loginTextField.setText(m_modifyActor.getLogin());
            m_loginTextField.setEnabled(false);

            m_roleCombobox.setSelectedItem(m_modifyActor.getRole());
        }


        // -------------- Place Widgets

        // --- Last Name
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Last Name:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_lastNameTextField, c);

        // --- First Name
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("First Name:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_firstNameTextField, c);

        // --- Telephone
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Telephone:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_telephoneTextField, c);

        // --- Fax
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Fax:", SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_faxTextField, c);

        // --- Email 1
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Email:"/*, IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY)*/, SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_emailTextField1, c);

        // --- Email 2
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Repeat Email:"/*, IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY)*/, SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_emailTextField2, c);

        // --- Company
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Affiliation:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_companyCombobox, c);

        c.gridx++;
        p.add(createCompanyButton, c);


        if (m_actorCreation) {


            // --- Login
            c.gridx = 0;
            c.gridy++;
            p.add(new JLabel("Login:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

            c.gridx++;
            p.add(m_loginTextField, c);

            // --- Password 1
            c.gridy++;
            c.gridx = 0;
            if (m_modifyActor == null) {
                p.add(new JLabel("Password:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);
            } else {
                p.add(new JLabel("Password:"), c);
            }

            c.gridx++;
            p.add(m_passwordTextField1, c);

            // --- Password 2
            c.gridy++;
            c.gridx = 0;
            if (m_modifyActor == null) {
                p.add(new JLabel("Repeat Password:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);
            } else {
                p.add(new JLabel("Repeat Password:"), c);
            }

            c.gridx++;
            p.add(m_passwordTextField2, c);

            // --- Role
            c.gridy++;
            c.gridx = 0;
            p.add(new JLabel("Role:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

            c.gridx++;
            p.add(m_roleCombobox, c);
        }


        createCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), CompanyJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                CreateOrModifyCompanyDialog dialog = new CreateOrModifyCompanyDialog(MainFrame.getMainWindow(), null);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    CompanyJson[] companyList = new CompanyJson[1];
                    companyList[0] = dialog.getCompanyToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.companyAdded(companyList[0]);
                                fillCompanies(companyList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the company already exists");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    CreateCompanyTask task = new CreateCompanyTask(callback, companyList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                }
            }
        });

        return p;
    }

    private void fillCompanies(CompanyJson companyJsonToSelect) {
        m_companiesModel.removeAllElements();
        m_companiesModel.addElement(null);
        for (CompanyJson c : DataManager.getCompanies()) {
            m_companiesModel.addElement(c);
        }

        if (companyJsonToSelect != null) {
            m_companyCombobox.setSelectedItem(companyJsonToSelect);
        }

    }

    @Override
    public boolean okCalled() {

        String lastName = m_lastNameTextField.getText().trim();
        if (lastName.isEmpty()) {
            highlight(m_lastNameTextField);
            setStatus(true, "You must fill the Last Name.");
            return false;
        }
        if (lastName.length()>50) {
            highlight(m_lastNameTextField);
            setStatus(true, "The Last Name can not exceed 50 characters.");
            return false;
        }

        String firstName = m_firstNameTextField.getText().trim();
        if (firstName.isEmpty()) {
            highlight(m_firstNameTextField);
            setStatus(true, "You must fill the First Name.");
            return false;
        }
        if (firstName.length()>50) {
            highlight(m_firstNameTextField);
            setStatus(true, "The First Name can not exceed 50 characters.");
            return false;
        }

        String email1 = m_emailTextField1.getText().trim();
        /*if (email1.isEmpty()) {
            highlight(m_emailTextField1);
            setStatus(true, "You must fill the Email.");
            return false;
        }*/
        if (email1.length()>128) {
            highlight(m_emailTextField1);
            setStatus(true, "The Email can not exceed 128 characters.");
            return false;
        }
        if ((email1.length()>0) && !UtilMail.checkEmail(email1)) {
            highlight(m_emailTextField1);
            setStatus(true, "The Email is not valid.");
            return false;
        }

        String email2 = m_emailTextField2.getText().trim();
        /*if (email2.isEmpty()) {
            highlight(m_emailTextField2);
            setStatus(true, "You must fill the Email.");
            return false;
        }*/
        if (email2.length()>128) {
            highlight(m_emailTextField2);
            setStatus(true, "The Email can not exceed 128 characters.");
            return false;
        }

        if (email1.compareTo(email2) != 0) {
            highlight(m_emailTextField2);
            setStatus(true, "Both emails must be the same.");
            return false;
        }

        Object company = m_companyCombobox.getSelectedItem();
        if (company == null) {
            highlight(m_companyCombobox);
            setStatus(true, "You must select the Affiliation.");
            return false;
        }

        if (!m_actorCreation) {
            return true;
        }

        String login = m_loginTextField.getText().trim();
        if (login.isEmpty()) {
            highlight(m_loginTextField);
            setStatus(true, "You must fill the Login.");
            return false;
        }
        if (login.length()>50) {
            highlight(m_loginTextField);
            setStatus(true, "The Login can not exceed 50 characters.");
            return false;
        }

        if ((m_modifyActor == null) && DataManager.getActor(login) != null) {
            highlight(m_loginTextField);
            setStatus(true, "A user with this Login already exists");
            return false;
        }


        String password1 = new String(m_passwordTextField1.getPassword());

        if (m_modifyActor == null) {
            if (password1.isEmpty()) {
                highlight(m_passwordTextField1);
                setStatus(true, "You must fill the password.");
                return false;
            }
        }
        if (password1.length()>32) {
            highlight(m_passwordTextField1);
            setStatus(true, "The Password can not exceed 32 characters.");
            return false;
        }

        String password2 = new String(m_passwordTextField2.getPassword());
        if (m_modifyActor == null) {
            if (password2.isEmpty()) {
                highlight(m_passwordTextField2);
                setStatus(true, "You must fill the password.");
                return false;
            }
        }
        if (password2.length()>32) {
            highlight(m_passwordTextField2);
            setStatus(true, "The Password can not exceed 32 characters.");
            return false;
        }

        if (password1.compareTo(password2) != 0) {
            highlight(m_passwordTextField1);
            setStatus(true, "Both passwords must be the same.");
            return false;
        }

        Object role = m_roleCombobox.getSelectedItem();
        if (role == null) {
            highlight(m_roleCombobox);
            setStatus(true, "You must select the Role.");
            return false;
        }

        return true;

    }

    public ContactJson getContactToCreate() {
        ContactJson contactJson = new ContactJson((m_modifyContact != null) ? m_modifyContact.getId() : -1, ((CompanyJson)m_companyCombobox.getSelectedItem()).getName(),
                m_lastNameTextField.getText().trim(), m_firstNameTextField.getText().trim(),
                m_telephoneTextField.getText().trim(), m_faxTextField.getText().trim(), m_emailTextField1.getText().trim());

        return contactJson;

    }

    public ActorJson getActorToCreate() {
        ContactJson contactJson = getContactToCreate();
        ActorJson actorJson = new ActorJson(m_loginTextField.getText().trim(), contactJson,
                (String) m_roleCombobox.getSelectedItem(), new String(m_passwordTextField1.getPassword()).trim());
        return actorJson;
    }

}
