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
import fr.edyp.epims.json.ContactJson;
import fr.epims.tasks.CreateContactTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.panels.admin.CreateOrModifyUserDialog;
import fr.epims.ui.panels.renderers.ContactDisplayCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 * Dialog to Create a new Actor+Contact (lab user) or a new Contact (analysis client)
 *
 * @author JM235353
 *
 */
public class AddUserDialog extends DefaultDialog {

    private JTextField m_nameFilter;
    private JList<ContactJson> m_membersList;

    private boolean m_addContact;
    private DefaultListModel<ContactJson> m_listModel;

    private ArrayList<ContactJson> m_contacts;

    public AddUserDialog(Window parent, boolean member, ArrayList<ContactJson> alreadyPresentContacts) {
        super(parent);

        setTitle(member ? "Add Member" : "Add Contact");
        m_addContact = !member;

        setInternalComponent(createUserChoicePanel(member, alreadyPresentContacts));

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);


        setStatusVisible(false);

        pack();
    }

    public JPanel createUserChoicePanel(boolean member, ArrayList<ContactJson> alreadyPresentContacts) {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_nameFilter = new JTextField(20);


        if (member) {
            m_contacts = DataManager.getAllContactsOfActorWithoutPresents(alreadyPresentContacts);
        } else {
            m_contacts = DataManager.getAllContactsWithoutPresents(alreadyPresentContacts);
        }

        m_listModel = new DefaultListModel<>();
        fillModel(m_contacts);



        FlatButton createContactButton = null;
        if (m_addContact) {
            createContactButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_USER), "Create", true);
        }

        m_membersList = new JList(m_listModel);
        m_membersList.setCellRenderer(new ContactDisplayCellRenderer());
        JScrollPane listScrollPane = new JScrollPane(m_membersList) {

            private final Dimension preferredSize = new Dimension(220, 300);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };

        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Filter:"), c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        p.add(m_nameFilter, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        p.add(listScrollPane, c);

        if (m_addContact) {
            c.gridy++;
            c.weightx = 1;
            c.weighty = 0;
            c.gridwidth = 1;
            p.add(Box.createGlue(), c);

            c.gridx++;
            p.add(Box.createGlue(), c);


            c.gridx++;
            c.weightx = 0;
            p.add(createContactButton, c);


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
                                    ArrayList<ContactJson> contacts = DataManager.getAllContactsWithoutPresents(alreadyPresentContacts);
                                    m_listModel.clear();
                                    for (ContactJson contact : contacts) {
                                        m_listModel.addElement(contact);
                                    }

                                    m_membersList.setSelectedValue(contactList[0], true);


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
        }

        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        };

        m_nameFilter.getDocument().addDocumentListener(docListener);

        return p;
    }

    private void filter() {
        String text = m_nameFilter.getText().trim();
        if (text.isEmpty()) {
            fillModel(m_contacts);
            return;
        }
        text = text.toLowerCase();

        ArrayList<ContactJson> elements = new ArrayList<>();
        for (ContactJson contact : m_contacts) {
            String name = contact.getLastName() + " " + contact.getFirstName();
            if (name.toLowerCase().indexOf(text) != -1) {
                elements.add(contact);
            }
        }

        fillModel(elements);
    }

    private void fillModel(ArrayList<ContactJson> elements) {
        m_listModel.removeAllElements();

        for (ContactJson contact : elements) {
            m_listModel.addElement(contact);
        }
    }


    public java.util.List<ContactJson> getSelectedContacts() {
        return m_membersList.getSelectedValuesList();
    }


}
