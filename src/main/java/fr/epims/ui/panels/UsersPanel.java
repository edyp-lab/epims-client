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

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.tasks.AddContactTask;
import fr.epims.tasks.AddMemberTask;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.AddUserDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.dialogs.UserInfoDialog;
import fr.epims.ui.panels.renderers.ContactClickableCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 *
 * Panel with list of users of a Program / Project / Study
 *
 * @author JM235353
 *
 */
public class UsersPanel extends JPanel {

    private ArrayList<ContactJson> m_contacts;

    private ProgramJson m_programJson = null;
    private ProjectJson m_projectJson = null;
    private StudyJson m_studyJson = null;

    private boolean m_member;

    public UsersPanel(String title, boolean member, ProgramJson programJson, ArrayList<ContactJson> contacts) {
        this(title, member, contacts);

        m_programJson = programJson;
    }

    public UsersPanel(String title, boolean member, ProjectJson projectJson, ArrayList<ContactJson> contacts) {
        this(title, member, contacts);

        m_projectJson = projectJson;
    }

    public UsersPanel(String title, boolean member, StudyJson studyJson, ArrayList<ContactJson> contacts) {
        this(title, member, contacts);

        m_studyJson = studyJson;
    }
    private UsersPanel(String title, boolean member, ArrayList<ContactJson> contacts) {
        super(new GridBagLayout());

        m_member = member;
        m_contacts = contacts;

        Border titledBorder = BorderFactory.createTitledBorder(title);
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        DefaultListModel<ContactJson> listModel = new DefaultListModel<>();
        for (ContactJson actor : contacts) {
            listModel.addElement(actor);
        }



        final JList membersList = new JList(listModel);
        membersList.setCellRenderer(new ContactClickableCellRenderer());
        JScrollPane listScrollPane = new JScrollPane(membersList) {

            private final Dimension preferredSize = new Dimension(120, 160);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };

        // Set the Hand cursor when the mouse is over an element of the JList
        membersList.addMouseMotionListener ( new MouseMotionListener() {
            public void mouseDragged (MouseEvent e) {
                updateCursor (e);
            }

            public void mouseMoved (MouseEvent e) {
                updateCursor (e);
            }

            private void updateCursor (MouseEvent e) {
                int index = membersList.locationToIndex(e.getPoint());
                if (index != -1) {

                    Rectangle bounds = membersList.getCellBounds(index, index);
                    if (!bounds.contains(e.getPoint())) {
                        // index can be !=-1 because locationToIndex can return the nearest index.
                        membersList.setCursor(null);
                    } else {
                        membersList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                } else {
                    membersList.setCursor(null);
                }
            }
        } );


        FlatButton addMemberButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_USER), true);


        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        add(listScrollPane, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;
        add(Box.createGlue(), c);

        c.gridx++;
        c.weightx = 0;
        add(addMemberButton, c);

        addMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (updateDataDialog.isDataUpdated()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Retry", "Data were not up-to-date. Please Retry.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    return;
                }

                String actorKey = null;
                ArrayList<String> members = null;
                boolean locked = false;
                String closeSentence = "";
                if (m_studyJson != null) {
                    actorKey = m_studyJson.getActorKey();
                    members = m_studyJson.getActorsKey();
                    locked = ! m_studyJson.isRunningStatus();
                    closeSentence = "Study is Closed.";
                } else if (m_projectJson != null) {
                    actorKey = m_projectJson.getActorKey();
                    members = m_projectJson.getActorsKey();
                    locked = (m_projectJson.getClosingDate() != null);
                    closeSentence = "Project is Closed.";
                } else if (m_programJson != null) {
                    actorKey = m_programJson.getResponsible();
                    members = m_programJson.getActorsKey();
                    locked = (m_programJson.getClosingDate() != null);
                    closeSentence = "Program is Closed.";
                }

                if (locked) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Closed", closeSentence+" You can not add members or contacts.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                if (!DataManager.checkOwner(actorKey, members)) {

                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member to perform this action.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }


                addMemberButton.setEnabled(false);

                AddUserDialog addUserDialog = new AddUserDialog(MainFrame.getMainWindow(), member, m_contacts);
                addUserDialog.centerToWindow(MainFrame.getMainWindow());
                addUserDialog.setVisible(true);

                if (addUserDialog.getButtonClicked() == AddUserDialog.BUTTON_OK) {

                    if (m_member) {
                        // ------------ Add Member

                        ProgramJson[] arrProgram = new ProgramJson[1];
                        ProjectJson[] arrProject = new ProjectJson[1];
                        StudyJson[] arrStudy = new StudyJson[1];

                        java.util.List<ContactJson> contacts = addUserDialog.getSelectedContacts();
                        if (!contacts.isEmpty()) {

                            AddMemberTask task = null;

                            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success, long taskId, boolean finished) {
                                    if (success) {
                                        if (m_programJson != null) {
                                            DataManager.updateProgram(arrProgram[0]);
                                        } else if (m_projectJson != null) {
                                            DataManager.updateProject(arrProject[0]);
                                        } else {
                                            DataManager.updateStudy(arrStudy[0]);
                                        }
                                    } else {
                                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                                        infoDialog.setVisible(true);
                                    }
                                    addMemberButton.setEnabled(true);
                                }
                            };


                            if (m_programJson != null) {

                                arrProgram[0] = m_programJson;
                                task = new AddMemberTask(callback, arrProgram, contacts);
                            } else if (m_projectJson != null) {
                                arrProject[0] = m_projectJson;
                                task = new AddMemberTask(callback, arrProject, contacts);
                            } else {
                                arrStudy[0] = m_studyJson;
                                task = new AddMemberTask(callback, arrStudy, contacts);
                            }


                            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                        }
                    } else {
                        // ------------ Add Contact
                        ProgramJson[] arrProgram = new ProgramJson[1];
                        ProjectJson[] arrProject = new ProjectJson[1];
                        StudyJson[] arrStudy = new StudyJson[1];

                        java.util.List<ContactJson> contacts = addUserDialog.getSelectedContacts();
                        if (!contacts.isEmpty()) {
                            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success, long taskId, boolean finished) {
                                    if (success) {

                                        if (m_programJson != null) {
                                            DataManager.updateProgram(arrProgram[0]);
                                        } else if (m_projectJson != null) {
                                            DataManager.updateProject(arrProject[0]);
                                        } else {
                                            DataManager.updateStudy(arrStudy[0]);
                                        }

                                    } else {
                                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                                        infoDialog.setVisible(true);
                                    }
                                    addMemberButton.setEnabled(true);
                                }
                            };

                            AddContactTask task = null;
                            if (m_programJson != null) {

                                arrProgram[0] = m_programJson;
                                task = new AddContactTask(callback, arrProgram, contacts);
                            } else if (m_projectJson != null) {
                                arrProject[0] = m_projectJson;
                                task = new AddContactTask(callback, arrProject, contacts);
                            } else {
                                arrStudy[0] = m_studyJson;
                                task = new AddContactTask(callback, arrStudy, contacts);
                            }


                            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                        }
                    }
                } else {
                    addMemberButton.setEnabled(true);
                }
            }
        });

        membersList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ContactJson contact = (ContactJson) membersList.getSelectedValue();
                if (contact == null) {
                    return;
                }
                UserInfoDialog dialog = new UserInfoDialog(MainFrame.getMainWindow(), contact);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);

                membersList.clearSelection();
            }
        });

    }


}
