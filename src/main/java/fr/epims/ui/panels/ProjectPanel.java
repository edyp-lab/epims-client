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
import fr.edyp.epims.json.ProjectJson;
import fr.epims.tasks.CloseProjectTask;
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
import java.util.Date;

/**
 *
 * Panel with Project info
 *
 * @author JM235353
 *
 */
public class ProjectPanel extends JPanel {

    public ProjectPanel(ProjectJson p) {
        super(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Project ");
        setBorder(titledBorder);

        init(p);
    }

    private void init(final ProjectJson p) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        FlatButton stateButton = null;
        if (p.getClosingDate() == null) {
            stateButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UNLOCK), false);
            stateButton.setToolTipText("Close the Project");
            stateButton.addActionListener(createCloseActionListener(stateButton, p));
        } else {
            stateButton = new FlatButton(IconManager.getIcon(IconManager.IconType.LOCK), false);
            stateButton.setToolTipText("Project Closed");
            stateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Project Closed", "The project is closed. You can no longer modify it.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }
            });
        }

        JButton[] buttons = { stateButton };

        String title = p.getNomenclatureTitle().isEmpty() ? p.getTitle() : p.getTitle()+" ("+p.getNomenclatureTitle()+")";
        TitlePanel titlePanel = new TitlePanel(title, IconManager.getIcon(IconManager.IconType.PROJECT), buttons);


        final ContactJson contact = DataManager.getContactFromActorKey(p.getActorKey());
        JLabel responsibleLabel = (contact == null) ? new JLabel("") : new HypertextLabel(contact.getFirstName()+" "+contact.getLastName(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserInfoDialog dialog = new UserInfoDialog(MainFrame.getMainWindow(), contact);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
            }
        });
        JLabel dateLabel = new JLabel("Created on "+ UtilDate.dateToStringForIHM(p.getCreationDate()), SwingConstants.RIGHT);

        JScrollPane scrollPane = new JScrollPane();
        JTextArea descriptionTextArea = new JTextArea(p.getDescription());
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setEditable(false);
        scrollPane.setViewportView(descriptionTextArea);

        JPanel contactsAndMembersPanel = createContactsAndMembersPanel(p);

        String analysisType = ((p.getIdentificationType() != null) && (!p.getIdentificationType().isEmpty())) ? p.getIdentificationType() : " /         ";
        JLabel analysisTypeLabel = new JLabel("Analysis Type: "+analysisType);

        String contractualContext = ((p.getContractualFrame() != null) && (!p.getContractualFrame().isEmpty())) ? p.getContractualFrame(): " /         ";
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

    private ActionListener createCloseActionListener(final FlatButton button, final ProjectJson p) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (p.getId() == -1) {
                    // "Orphan Studies"
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Close not allowed", "\"Orphan Studies\" Project can not be closed.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), ProjectJson.class.getSimpleName());
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

                if (!DataManager.checkOwner(p)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Project to close it.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                QuestionDialog dialog = new QuestionDialog(MainFrame.getMainWindow(), "Close Project", "Do you want to Close the Project ?" );
                dialog.centerToScreen();
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    ProjectJson[] arrJsonParam = new ProjectJson[1];
                    arrJsonParam[0] = p;

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                p.setClosingDate(arrJsonParam[0].getClosingDate());
                                button.setIcon(IconManager.getIcon(IconManager.IconType.LOCK));
                                button.setToolTipText("Project Closed");
                                // remove first and only action listener
                                for( ActionListener al : button.getActionListeners() ) {
                                    button.removeActionListener( al );
                                    break;
                                }
                                button.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Project Closed", "The project is closed. You can no longer modify it.");
                                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                                        infoDialog.setVisible(true);
                                        return;
                                    }
                                });

                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };

                    CloseProjectTask task = new CloseProjectTask(callback, new Date(), arrJsonParam);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }
            }
        };
    }


    private JPanel createContactsAndMembersPanel(ProjectJson p) {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);




        ArrayList<Integer> contactKeys = p.getContactsKey();
        ArrayList<ContactJson> contacts = DataManager.getContacts(contactKeys);
        JPanel contactsPanel = new UsersPanel(" Contacts ", false, p, contacts);

        ArrayList<String>  actorsKey = p.getActorsKey();
        ArrayList<ContactJson> members = DataManager.getContactsFromActorsKeys(actorsKey);
        JPanel membersPanel = new UsersPanel(" Members ", true, p, members);

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
