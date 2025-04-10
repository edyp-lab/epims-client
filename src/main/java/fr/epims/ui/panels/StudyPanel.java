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
import fr.edyp.epims.json.ContactJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.tasks.ChangeStudyStatusTask;
import fr.epims.ui.common.*;
import fr.epims.ui.common.QuestionDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.dialogs.UserInfoDialog;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 * Panel with study info
 *
 * @author JM235353
 *
 */
public class StudyPanel extends JPanel {

    public StudyPanel(StudyJson s) {
        super(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Study ");
        setBorder(titledBorder);

        init(s);
    }

    private void init(final StudyJson s) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);




        FlatButton statusButton = null;

        if (s.isRunningStatus()) {
            statusButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UNLOCK), false);
            statusButton.setToolTipText("Close the Study");
            statusButton.addActionListener(createCloseActionListener(statusButton, s));
        } else if (s.isCloseStatus()) {
            statusButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARCHIVE_ALLOW), false);
            statusButton.setToolTipText("Allow the Archival Storage");
            statusButton.addActionListener(createArchivableActionListener(statusButton, s));
        } else if (s.isArchivableStatus()) {
            statusButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARCHIVE_WAITING), false);
            statusButton.setToolTipText("Admin is allowed to Archive");
            // user can not do anything in this state
        } else if (s.isArchiveeStatus()) {
            statusButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARCHIVE), false);
            statusButton.setToolTipText("Study has been Archived");
            // user can not do anything in this state : study is archived
        }

        JButton[] buttons = { statusButton };

        TitlePanel titlePanel = new TitlePanel(s.getTitle()+" ("+s.getNomenclatureTitle()+")", IconManager.getIcon(IconManager.IconType.STUDY), buttons);



        final ContactJson contact = DataManager.getContactFromActorKey(s.getActorKey());
        HypertextLabel responsibleLabel = new HypertextLabel(contact.getFirstName()+" "+contact.getLastName(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserInfoDialog dialog = new UserInfoDialog(MainFrame.getMainWindow(), contact);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
            }
        });

        JLabel dateLabel = new JLabel("Created on "+UtilDate.dateToStringForIHM(s.getCreationDate()), SwingConstants.RIGHT);

        JScrollPane scrollPane = new JScrollPane();
        JTextArea descriptionTextArea = new JTextArea(s.getDescription());
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setEditable(false);
        scrollPane.setViewportView(descriptionTextArea);

        JPanel contactsAndMembersPanel = createContactsAndMembersPanel(s);

        String analysisType = ((s.getIdentificationType() != null) && (!s.getIdentificationType().isEmpty())) ? s.getIdentificationType() : " /         ";
        JLabel analysisTypeLabel = new JLabel("Analysis Type: "+analysisType);

        String contractualContext = ((s.getContractualFrame() != null) && (!s.getContractualFrame().isEmpty())) ? s.getContractualFrame() : " /         ";
        JLabel contractualContextLabel = new JLabel("Contractual Context : "+contractualContext, SwingConstants.RIGHT);


        c.gridx = 0;
        c.gridy = 0;

        c.weightx = 1;
        c.gridwidth = 3;
        add(titlePanel, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        add(responsibleLabel, c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createHorizontalGlue(), c);

        c.gridx++;
        c.weightx = 0;
        add(dateLabel, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 1;
        add(scrollPane, c);

        c.gridy++;
        c.gridx = 0;
        c.weighty = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        add(analysisTypeLabel, c);

        c.gridx = 2;
        add(contractualContextLabel, c);

        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.gridheight = 4;
        add(contactsAndMembersPanel, c);




    }

    private ActionListener createCloseActionListener(final FlatButton button, final StudyJson s) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), StudyJson.class.getSimpleName());
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

                if (!DataManager.checkOwner(s)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to close it.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                QuestionDialog dialog = new QuestionDialog(MainFrame.getMainWindow(), "Close Study", "Do you want to Close the Study ?" );
                dialog.centerToScreen();
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    StudyJson[] arrStudy = new StudyJson[1];
                    arrStudy[0] = s;

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                s.setStatus(arrStudy[0].getStatus());
                                button.setIcon(IconManager.getIcon(IconManager.IconType.ARCHIVE_ALLOW));
                                button.setToolTipText("Allow the Archival Storage");
                                button.removeActionListener(button.getActionListeners()[0]);
                                button.addActionListener(createArchivableActionListener(button, s));
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };

                    ChangeStudyStatusTask task = new ChangeStudyStatusTask(callback, "close", arrStudy);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }
            }
        };
    }

    private ActionListener createArchivableActionListener(final FlatButton button, final StudyJson s) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), StudyJson.class.getSimpleName());
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

                if (!DataManager.checkOwner(s)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to perform this action.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                QuestionDialog dialog = new QuestionDialog(MainFrame.getMainWindow(), "Allow Archival Storage", "Do you want to Allow the Archival Storage of the Study ?" );
                dialog.centerToScreen();
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    StudyJson[] arrStudy = new StudyJson[1];
                    arrStudy[0] = s;

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                s.setStatus(arrStudy[0].getStatus());
                                button.setIcon(IconManager.getIcon(IconManager.IconType.ARCHIVE_WAITING));
                                button.setToolTipText("Admin is allowed to Archive");
                                button.removeActionListener(button.getActionListeners()[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };

                    ChangeStudyStatusTask task = new ChangeStudyStatusTask(callback, "archivable", arrStudy);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }
            }
        };
    }


    private JPanel createContactsAndMembersPanel(StudyJson s) {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);


        ArrayList<Integer> contactKeys = s.getContactsKey();
        ArrayList<ContactJson> contacts = DataManager.getContacts(contactKeys);
        JPanel contactsPanel = new UsersPanel(" Contacts ", false, s, contacts);

        ArrayList<String>  actorsKey = s.getActorsKey();
        ArrayList<ContactJson> members = DataManager.getContactsFromActorsKeys(actorsKey);
        JPanel membersPanel = new UsersPanel(" Members ", true, s, members);

        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        panel.add(membersPanel, c);

        c.gridx++;
        panel.add(Box.createHorizontalStrut(20));

        c.gridx++;
        panel.add(contactsPanel, c);

        return panel;


    }



}
