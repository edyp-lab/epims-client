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

package fr.epims.ui.analyserequest.panels;



import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;

import fr.epims.tasks.analyses.*;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.AnalysesConnectionDialog;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 * Panel displayed in the Analysis Request Tab of the ePims application
 *
 * @author JM235353
 *
 */
public class AnalysesRequestsPanel extends JPanel {

    private static AnalysesRequestsPanel m_singleton = null;

    private JTabbedPane m_tabbedPane;

    private AnalysesListPanel m_analysesListPanel;

    private ArrayList<ProAnalysisJson> m_analyses = null;
    private ArrayList<AnalysisMapJson> m_analysesMaps = null;

    public static AnalysesRequestsPanel getPanel() {
        if (m_singleton == null) {
            m_singleton = new AnalysesRequestsPanel();
        }
        return m_singleton;
    }

    private AnalysesRequestsPanel() {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JTabbedPane tabbedPane = createTabbedPane();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(tabbedPane, c);

    }

    private JTabbedPane createTabbedPane() {
        m_tabbedPane = new JTabbedPane();

        m_analysesListPanel = new AnalysesListPanel(false);
        m_tabbedPane.addTab("Analyses", m_analysesListPanel);
        m_tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.ANALYSES));


        return m_tabbedPane;
    }

    public void analysesServerConnection() {
        if (DataManager.getAnalysesLoggedUser() != null) {
            return; // already logged to the Analyses Requests Server.
        }

        tryConnectDialog();
    }

    private void tryConnectDialog() {
        AnalysesConnectionDialog connectionDialog = new AnalysesConnectionDialog(MainFrame.getMainWindow());
        connectionDialog.centerToWindow(MainFrame.getMainWindow());
        connectionDialog.setVisible(true);

        if (connectionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            connect(connectionDialog.getLogin(), connectionDialog.getPassword());

        } else {
            MainFrame.getMainWindow().selectActivitiesTabbedPane();
        }
    }

    private void connect(final String login, String password) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {

                    connectionDone(login);
                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Connection Failed", "Connection has failed : your Password is incorrect or the Server is down.");
                    infoDialog.setButtonName(InfoDialog.BUTTON_OK, "Retry");
                    infoDialog.setButtonName(InfoDialog.BUTTON_CANCEL, "Exit Analyses Requests");
                    infoDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, true);
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    if (infoDialog.getButtonClicked() == InfoDialog.BUTTON_OK) {
                        tryConnectDialog();
                    } else {
                        MainFrame.getMainWindow().selectActivitiesTabbedPane();
                    }

                }
            }
        };

        AnalysesServerConnectTask task = new AnalysesServerConnectTask(callback, login, password);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private void loadAnalyses() {


        m_analyses = new ArrayList<>();
        m_analysesMaps = new ArrayList<>();
        final String[] fullUserName = new String[1];
        final String[] version = { "" };
        final String[] versionClass = { "" };
        taskCount = 0;
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    taskCount++;
                    if (taskCount >=3) {
                        DataManager.setAnalysesFullNameUser(fullUserName[0]);
                        m_analysesListPanel.userUpdated();
                        m_analysesListPanel.setAnalyses(m_analyses, m_analysesMaps, false);
                    }
                }
            }
        };

        AnalysesTask task = new AnalysesTask(callback, m_analyses);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        LoadAllAnalysisTask task2 = new LoadAllAnalysisTask(callback, m_analysesMaps);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task2);

        UserTask task3 = new UserTask(callback, DataManager.getAnalysesLoggedUser(), fullUserName);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task3);

    }
    private int taskCount = 0;

    private void connectionDone(String login) {

        DataManager.setAnalysesLoggedUser(login);

        m_analysesListPanel.userUpdated();

        loadAnalyses();


    }

    public void reinit() {
        m_analysesListPanel.reinit();
    }

    public void modificationDone(ProAnalysisJson proAnalysisJson, AnalysisMapJson analysisMapJson) {
        int i = 0;
        boolean found = false;
        for (AnalysisMapJson analyse : m_analysesMaps) {
            if (analyse.getId() == analysisMapJson.getId()) {
                m_analysesMaps.set(i, analysisMapJson);
                found = true;
                break;
            }
            i++;
        }
        if (!found) {
            m_analysesMaps.add(analysisMapJson);
        }
    }

    public ArrayList<ProAnalysisJson> getAnalysisJson() {
        return m_analyses;
    }
    public ArrayList<AnalysisMapJson> getAnalysisMapJson() {
        return m_analysesMaps;
    }
}
