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

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ActorJson;
import fr.edyp.epims.json.ContactJson;
import fr.edyp.epims.json.ProgramJson;
import fr.epims.tasks.CreateContactTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.panels.admin.CreateOrModifyUserDialog;
import fr.epims.ui.panels.renderers.ContactDisplayCellRenderer;
import fr.epims.ui.renderers.ActorComboBoxRenderer;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/**
 *
 * Dialog to Create a new Program
 *
 * @author JM235353
 *
 */
public class AddProgramDialog  extends DefaultDialog {

    private JTextField m_title = null;
    private JTextField m_longTitle = null;
    private JTextField m_nomenclature = null;
    private JComboBox<ActorJson> m_responsibleCombobox = null;
    private JFormattedTextField m_dateTextField;
    private JTextField m_contractualFrame = null;
    private JList<ContactJson> m_membersList = null;
    private JList<ContactJson> m_contactsList = null;
    private JTextArea m_descriptionTextArea = null;
    private JCheckBox m_confidentialityCheckBox = null;

    private DefaultListModel<ContactJson> m_contactModel = new DefaultListModel<>();


    public AddProgramDialog(Window parent) {
        super(parent);

        setTitle("Add Program");

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
        m_title = new JTextField(50);
        m_longTitle = new JTextField(50);
        m_nomenclature = new JTextField(50);

        ArrayList<ActorJson> actors = DataManager.getActors();
        Vector actorsVector = new Vector();
        for (ActorJson actor : actors) {
            actorsVector.add(actor);
        }
        DefaultComboBoxModel<ActorJson> responsibleModel = new DefaultComboBoxModel<>( actorsVector );
        m_responsibleCombobox = new JComboBox<ActorJson>(responsibleModel);
        m_responsibleCombobox.setRenderer(new ActorComboBoxRenderer());
        m_responsibleCombobox.setSelectedItem(DataManager.getActor(DataManager.getLoggedUser()));

        DateFormat format = UtilDate.getDateFormat();

        Date today = new Date();

        m_dateTextField = new JFormattedTextField(format);
        m_dateTextField.setText(format.format(today));
        m_dateTextField.setColumns(10);
        m_dateTextField.setPreferredSize(m_dateTextField.getPreferredSize());
        FlatButton dateStartButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CALENDAR), false);

        m_contractualFrame = new JTextField(50);

        ArrayList<String> actorsKeys = DataManager.getActorsKeys();
        ArrayList<ContactJson> members = DataManager.getContactsFromActorsKeys(actorsKeys);
        DefaultListModel<ContactJson> memberModel = new DefaultListModel<>();
        for (ContactJson member : members) {
            memberModel.addElement(member);
        }
        m_membersList = new JList<>(memberModel);
        m_membersList.setCellRenderer(new ContactDisplayCellRenderer());
        JScrollPane membersScrollPane = new JScrollPane(m_membersList) {

            private final Dimension preferredSize = new Dimension(120, 140);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };


        fillContacts(null);
        m_contactsList = new JList<>(m_contactModel);
        m_contactsList.setCellRenderer(new ContactDisplayCellRenderer());
        JScrollPane contactsScrollPane = new JScrollPane(m_contactsList) {

            private final Dimension preferredSize = new Dimension(120, 140);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };
        FlatButton createContactButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_USER), "Create", true);


        JScrollPane descriptionScrollPane = new JScrollPane();
        m_descriptionTextArea = new JTextArea(5, 40);
        m_descriptionTextArea.setWrapStyleWord(true);
        m_descriptionTextArea.setLineWrap(true);
        m_descriptionTextArea.setEditable(true);
        descriptionScrollPane.setViewportView(m_descriptionTextArea);

        m_confidentialityCheckBox = new JCheckBox("Confidential");


        // -------------- Place Widgets

        // --- Title
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Title (50 max length):", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(m_title, c);
        c.gridwidth = 1;

        // --- Free Title
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Free Title:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(m_longTitle, c);
        c.gridwidth = 1;

        // --- Nomenclature
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Nomenclature (10 max length):", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(m_nomenclature, c);
        c.gridwidth = 1;

        // --- Responsible
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Owner:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(m_responsibleCombobox, c);
        c.gridwidth = 1;

        // --- Creation Date
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Creation Date:", IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY), SwingConstants.RIGHT), c);

        c.gridx++;
        p.add(m_dateTextField, c);

        c.gridx++;
        p.add(dateStartButton, c);

        c.gridx++;
        p.add(Box.createGlue(), c);

        c.gridx++;
        p.add(Box.createGlue(), c);

        // --- Contractual Frame
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Contractual Frame:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(m_contractualFrame, c);
        c.gridwidth = 1;

        // --- Members
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Members:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 3;
        p.add(membersScrollPane, c);
        c.gridwidth = 1;

        // --- Contacts
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Contacts:", SwingConstants.RIGHT), c);


        c.gridx++;
        c.gridwidth = 3;
        c.gridheight = 2;
        c.weightx = 1;
        p.add(contactsScrollPane, c);
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0;

        c.gridx+=3;
        c.weighty = 1;
        p.add(Box.createGlue(), c);
        c.weighty = 0;

        c.gridy++;
        p.add(createContactButton, c);

        // --- Description
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel("Description:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(descriptionScrollPane, c);
        c.gridwidth = 1;

        // --- Confidential
        c.gridy++;
        c.gridx = 0;
        p.add(Box.createGlue(), c);

        c.gridx++;
        c.gridwidth = 4;
        p.add(m_confidentialityCheckBox, c);
        c.gridwidth = 1;

        dateStartButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = UtilDate.convertToDateWithoutHour(m_dateTextField.getText());
                DatePickerDialog dialog = new DatePickerDialog(MainFrame.getMainWindow(), "Pick Start Date", d);

                dialog.setLocation(dateStartButton.getLocationOnScreen().x+dateStartButton.getWidth()/2, dateStartButton.getLocationOnScreen().y+dateStartButton.getHeight()/2);
                dialog.setVisible(true);

                Date selectedDate = dialog.getSelectedDate();
                if (selectedDate != null) {
                    DateFormat format = UtilDate.getDateFormat();
                    m_dateTextField.setText(format.format(selectedDate));
                }
            }
        });

        createContactButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateOrModifyUserDialog dialog = new CreateOrModifyUserDialog(MainFrame.getMainWindow(), false, null, null);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    ContactJson[] contactList = new ContactJson[1];
                    contactList[0] = dialog.getContactToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.contactAdded(contactList[0]);
                                fillContacts(contactList[0]);

                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the contact already exists");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    CreateContactTask task = new CreateContactTask(callback, contactList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        return p;
    }

    private void fillContacts(ContactJson contactJsonToSelect) {
        m_contactModel.clear();
        for (ContactJson contact : DataManager.getContacts()) {
            m_contactModel.addElement(contact);
        }

        if (contactJsonToSelect != null) {
            m_contactsList.setSelectedValue(contactJsonToSelect, true);
        }
    }


    @Override
    public boolean okCalled() {

        // Title check
        String title = m_title.getText().trim();
        if (title.isEmpty()) {
            highlight(m_title);
            setStatus(true, "You must fill the Title.");
            return false;
        }
        if (title.length()>50) {
            highlight(m_title);
            setStatus(true, "The Title can not exceed 50 characters.");
            return false;
        }

        // Nomenclature check
        String nomenclature = m_nomenclature.getText().trim();
        if (nomenclature.isEmpty()) {
            highlight(m_nomenclature);
            setStatus(true, "You must fill the Nomenclature.");
            return false;
        }
        if (nomenclature.length()>10) {
            highlight(m_nomenclature);
            setStatus(true, "The Nomenclature can not exceed 10 characters.");
            return false;
        }

        // Date creation
        String date = m_dateTextField.getText();
        Date d = UtilDate.convertToDateWithoutHour(date);
        if (d == null) {
            highlight(m_dateTextField);
            setStatus(true, "You must fill the Date, format is YYYY-MM-DD.");
            return false;
        }



        return true;
    }

    public ProgramJson getProgramToCreate() {

        ProgramJson programJson = new ProgramJson(-1, m_title.getText().trim(), m_nomenclature.getText().trim(),
                m_longTitle.getText().trim(),  m_descriptionTextArea.getText().trim(),
                m_contractualFrame.getText().trim(), ((ActorJson) m_responsibleCombobox.getSelectedItem()).getLogin(),
                null, UtilDate.convertToDateWithoutHour(m_dateTextField.getText().trim()),
                m_confidentialityCheckBox.isSelected());


        // Add Members
        ArrayList<String> actorsKeys = new ArrayList<>();
        for (ContactJson member : m_membersList.getSelectedValuesList()) {
            actorsKeys.add(DataManager.getActorFromContactId(member.getId()).getLogin());
        }
        programJson.setActorsKey(actorsKeys);

        // Add Contacts
        ArrayList<Integer> contactsKey = new ArrayList<>();
        for (ContactJson contact : m_contactsList.getSelectedValuesList()) {
            contactsKey.add(contact.getId());
        }
        programJson.setContactsKey(contactsKey);


        return programJson;

    }


}
