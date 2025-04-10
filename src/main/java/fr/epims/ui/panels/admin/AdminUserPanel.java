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
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.tasks.CreateActorTask;
import fr.epims.tasks.ModifyActorTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

/**
 *
 * Panel : List of Actors (lab Users), only available for Admin
 *
 * @author JM235353
 *
 */
public class AdminUserPanel extends JPanel implements DataManager.DataManagerListener {

    private DefaultListModel<ActorJson> m_actorsListModel;
    private JList m_membersList;

    public AdminUserPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Users ");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_actorsListModel = new DefaultListModel<>();
        m_dataLoaded = false;
        loadData(null);

        m_membersList = new JList(m_actorsListModel);
        m_membersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_membersList.setCellRenderer(new ActorRenderer());
        JScrollPane listScrollPane = new JScrollPane(m_membersList){

            private final Dimension preferredSize = new Dimension(120, 320);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };

        FlatButton addMemberButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_USER), true);
        FlatButton modifyMemberButton = new FlatButton(IconManager.getIcon(IconManager.IconType.EDIT_USER), true);
        modifyMemberButton.setEnabled(false);

        c.gridx = 0;
        c.gridy = 0;
        add(addMemberButton, c);

        c.gridx++;
        add(modifyMemberButton, c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);

        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        add(listScrollPane, c);

        c.gridx = 3;
        c.gridwidth = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(Box.createGlue(), c);

        m_membersList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                modifyMemberButton.setEnabled(m_membersList.getSelectedValue() != null);
            }
        });

        addMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), ActorJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                CreateOrModifyUserDialog dialog = new CreateOrModifyUserDialog(MainFrame.getMainWindow(), true, null, null);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    ActorJson[] actorList = new ActorJson[1];
                    actorList[0] = dialog.getActorToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.actorAdded(actorList[0]);
                                m_dataLoaded = false;
                                loadData(actorList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the user already exists");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    CreateActorTask task = new CreateActorTask(callback, actorList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        modifyMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), ActorJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                ActorJson actorJson = (ActorJson) m_membersList.getSelectedValue();

                CreateOrModifyUserDialog dialog = new CreateOrModifyUserDialog(MainFrame.getMainWindow(), true, actorJson, null);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    ActorJson[] actorList = new ActorJson[1];
                    actorList[0] = dialog.getActorToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.actorModified(actorList[0]);
                                m_dataLoaded = false;
                                loadData(actorList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the user already exists");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    ModifyActorTask task = new ModifyActorTask(callback, actorList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        DataManager.addListener(ActorJson.class, this);

    }


    public void reinit() {
        m_dataLoaded = false;
        loadData(null);
    }

    public void loadData(ActorJson actorJsonToSelect) {
        if (m_dataLoaded) {
            return;
        }

        m_actorsListModel.clear();

        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                for (ActorJson actor : DataManager.getActors()) {
                    m_actorsListModel.addElement(actor);
                }

                if (actorJsonToSelect != null) {
                    m_membersList.setSelectedValue(actorJsonToSelect, true);
                }

            }
        };
        DataManager.dataAvailable(callback, false);
    }
    private boolean m_dataLoaded = false;


    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {
        // nothing to do
    }

    @Override
    public void updateAll(HashSet<Class> c) {

        ActorJson prevSelectedActor = (ActorJson) m_membersList.getSelectedValue();
        ActorJson nextSelectedActor = null;

        m_dataLoaded = false;
        m_actorsListModel.clear();
        for (ActorJson actor : DataManager.getActors()) {
            m_actorsListModel.addElement(actor);
            if ((prevSelectedActor != null) && (prevSelectedActor.getLogin().equals(actor.getLogin()))) {
                nextSelectedActor = actor;
            }
        }

        if (nextSelectedActor != null) {
            m_membersList.setSelectedValue(nextSelectedActor, true);
        }

        m_dataLoaded = true;
    }



    public class ActorRenderer extends DefaultListCellRenderer  {


        public ActorRenderer() {
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            ActorJson actor = (ActorJson) value;
            ContactJson contact = actor.getContact();
            setText(contact.getLastName()+" "+contact.getFirstName());

            return this;
        }
    }
}
