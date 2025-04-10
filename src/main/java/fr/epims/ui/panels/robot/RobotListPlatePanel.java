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

package fr.epims.ui.panels.robot;

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.tasks.ClosePlateTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.panels.model.SampleForPlateModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 *
 * Panel with the list of current Robot Plates
 *
 * @author JM235353
 *
 */
public class RobotListPlatePanel extends JPanel {

    private GridBagConstraints m_c;

    public RobotListPlatePanel() {
        super(new GridBagLayout());

        m_c = new GridBagConstraints();
        m_c.anchor = GridBagConstraints.CENTER;
        m_c.fill = GridBagConstraints.BOTH;
        m_c.insets = new java.awt.Insets(5, 5, 5, 5);
    }

    public void setPlates(ArrayList<ColoredRobotPlanning> freeList, ArrayList<VirtualPlateJson> plateSortedList) {

        m_c.gridx = 0;
        m_c.gridy = 0;

        m_c.weightx = 1;
        m_c.weighty = 1;


        JPanel tablePanel = new JPanel(new GridBagLayout());
        Border titledBorder = BorderFactory.createTitledBorder("Unassigned Samples");
        tablePanel.setBorder(titledBorder);
        GridBagConstraints tablePanel_c = new GridBagConstraints();
        tablePanel_c.anchor = GridBagConstraints.CENTER;
        tablePanel_c.fill = GridBagConstraints.BOTH;
        tablePanel_c.insets = new java.awt.Insets(5, 5, 5, 5);
        tablePanel_c.gridx = 0;
        tablePanel_c.gridy = 0;
        tablePanel_c.weightx = 1;
        tablePanel_c.weighty = 1;

        DecoratedTable plateTable = new DecoratedTable();

        SampleForPlateModel plateSamplesmodel = new SampleForPlateModel(false, false,false);
        plateSamplesmodel.setValues(freeList);
        plateTable.setModel(plateSamplesmodel);

        JScrollPane tableScrollPane = new JScrollPane(plateTable);
        tableScrollPane.setPreferredSize(new Dimension(600, height(freeList.size())));
        tablePanel.add(tableScrollPane, tablePanel_c);

        add(tablePanel, m_c);

        m_c.gridy++;


        for (VirtualPlateJson plate : plateSortedList) {

            HashMap<String, RobotPlanningJson> robotPlanningMap = new HashMap<>();
            for (VirtualWellJson well : plate.getVirtualWells()) {
                RobotPlanningJson robotPlanning = well.getRobotPlanning();
                String key = robotPlanning.getSample().getName();
                if (! robotPlanningMap.containsKey(key)) {
                    robotPlanningMap.put(key, robotPlanning);
                }
            }

            ArrayList<RobotPlanningJson> robotPlanningList = new ArrayList(robotPlanningMap.values());
            Collections.sort(robotPlanningList);

            tablePanel = new JPanel(new GridBagLayout());
            titledBorder = BorderFactory.createTitledBorder(" "+plate.getName()+" ");
            tablePanel.setBorder(titledBorder);
            tablePanel_c = new GridBagConstraints();
            tablePanel_c.anchor = GridBagConstraints.CENTER;
            tablePanel_c.fill = GridBagConstraints.BOTH;
            tablePanel_c.insets = new java.awt.Insets(5, 5, 5, 5);
            tablePanel_c.gridx = 0;
            tablePanel_c.gridy = 0;


            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setOrientation(SwingConstants.VERTICAL);

            FlatButton lockButton = new FlatButton(IconManager.getIcon(IconManager.IconType.UNLOCK),false);
            lockButton.setToolTipText("Close "+plate.getName()+" Plate");
            toolbar.add(lockButton);

            lockButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), RobotDataJson.class.getSimpleName());
                    updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                    updateDataDialog.setVisible(true);

                    if (updateDataDialog.isServerDown()) {
                        return;
                    }

                    if (updateDataDialog.isDataUpdated()) {

                        DatabaseVersionJson serverVersion = updateDataDialog.getServerDatabaseVersion();
                        String login = serverVersion.getLogin(RobotDataJson.class);
                        String user;
                        if ((login == null) || (login.length() == 0)) {
                            user = "Someone";
                        } else {
                            user = DataManager.getLastThenFirstNameFromActorKey(login);
                        }

                        QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Robot Plates Update", user+" has modified Robot Plates. You can not proceed, do you want to reload ?");
                        questionDialog.centerToWindow(MainFrame.getMainWindow());
                        questionDialog.setVisible(true);
                        if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                            // reload plates
                            RobotPanel.getPanel().reinit();
                            RobotPanel.getPanel().loadData();
                        }
                        return;
                    }


                    if (!DataManager.isRobotUser()) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be a Robot User to close a Plate.");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }

                    QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Close Plate", "Do you want to close "+plate.getName()+" Plate ?");
                    questionDialog.centerToWindow(MainFrame.getMainWindow());
                    questionDialog.setVisible(true);
                    if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, boolean finished) {
                                if (success) {
                                    RobotPanel.getPanel().reinit();
                                    RobotPanel.getPanel().loadData();

                                } else {
                                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                                    infoDialog.setVisible(true);
                                }
                            }
                        };

                        ClosePlateJson closePlateJson = new ClosePlateJson(plate.getName(), DataManager.getLoggedUser());

                        ClosePlateTask task = new ClosePlateTask(callback, closePlateJson);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                    }


                }
            });


            plateTable = new DecoratedTable();

            plateSamplesmodel = new SampleForPlateModel(false, false,false);
            plateSamplesmodel.setData(robotPlanningList);
            plateTable.setModel(plateSamplesmodel);

            tableScrollPane = new JScrollPane(plateTable);
            tableScrollPane.setPreferredSize(new Dimension(600, height(robotPlanningList.size())));

            tablePanel_c.weightx = 0;
            tablePanel_c.weighty = 1;
            tablePanel.add(toolbar, tablePanel_c);

            tablePanel_c.gridx++;
            tablePanel_c.weightx = 1;
            tablePanel.add(tableScrollPane, tablePanel_c);

            add(tablePanel, m_c);


            m_c.gridy++;
        }
    }

    private int height(int size) {
        int height = (size)*20+40;
        if (height<40) {
            height = 40;
        } else if (height>300) {
            height = 300;
        }
        return height;
    }

    public void reinit() {
        removeAll();
    }
}
