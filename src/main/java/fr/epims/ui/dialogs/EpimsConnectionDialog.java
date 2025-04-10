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
import fr.edyp.epims.json.DatabaseVersionJson;
import fr.epims.preferences.EpimsPreferences;
import fr.epims.preferences.PreferencesKeys;
import fr.epims.tasks.GetDatabaseVersionTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.InfoDialog;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.prefs.Preferences;
import javax.swing.*;

/**
 *
 * Dialog to Connect to the Server
 *
 * @author JM235353
 *
 */
public class EpimsConnectionDialog extends DefaultDialog {


    private JTextField m_serverTextField;
    private JTextField m_userTextField;
    private JPasswordField m_passwordField;
    private JCheckBox m_rememberPasswordCheckBox;

    private Object m_serverMutex = new Object();
    private Boolean m_lock = true;


    public EpimsConnectionDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Connection");


        JPanel internalPanel = createInternalPanel();

        setInternalComponent(internalPanel);

        setButtonVisible(DefaultDialog.BUTTON_CANCEL, false);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

    }


    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JPanel loginPanel = createLoginPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(loginPanel, c);

        return internalPanel;
    }



    private JPanel createLoginPanel() {

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder(" User Parameters "));



        JLabel serverLabel = new JLabel("Server :");
        m_serverTextField = new JTextField(30);
        JLabel userLabel = new JLabel("User :");
        m_userTextField = new JTextField(30);
        JLabel passwordLabel = new JLabel("Password :");
        m_passwordField = new JPasswordField();
        m_rememberPasswordCheckBox = new JCheckBox("Remember Password");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        loginPanel.add(serverLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        loginPanel.add(m_serverTextField, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        loginPanel.add(userLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        loginPanel.add(m_userTextField, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        loginPanel.add(passwordLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        loginPanel.add(m_passwordField, c);

        c.gridy++;
        loginPanel.add(m_rememberPasswordCheckBox, c);

        fillFields();

        return loginPanel;
    }

    private void fillFields() {
        Preferences preferences = EpimsPreferences.root();

        String server = preferences.get(PreferencesKeys.EPIMS_CONNECT_SERVER, "http://localhost:8080");
        m_serverTextField.setText(server);

        String log = preferences.get(PreferencesKeys.EPIMS_CONNECT_LOG, "");
        m_userTextField.setText(log);

        Boolean savePassword = preferences.getBoolean(PreferencesKeys.EPIMS_CONNECT_SAVE_PASSWORD, Boolean.FALSE);
        m_rememberPasswordCheckBox.setSelected(savePassword);

        if (savePassword) {
            String password = preferences.get(PreferencesKeys.EPIMS_CONNECT_PASSWORD, "");
            m_passwordField.setText(password);
        }


    }

    public String getLogin() {
        return m_userTextField.getText().trim();
    }
    public String getPassword() {
        return new String(m_passwordField.getPassword()).trim();
    }



    protected boolean checkParameters() {

        final String server = m_serverTextField.getText().trim();
        if (server.isEmpty()) {
            setStatus(true, "You must fill the Server");
            highlight(m_serverTextField);
            return false;
        }

        String user = m_userTextField.getText().trim();
        if (user.isEmpty()) {
            setStatus(true, "You must fill the User");
            highlight(m_userTextField);
            return false;
        }

        if (m_passwordField.getPassword().length==0) {
            setStatus(true, "You must fill the Password");
            highlight(m_passwordField);
            return false;
        }

        // handshake with the server


        final DatabaseVersionJson[] databaseVersion = new DatabaseVersionJson[1];
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    DataManager.preloadData(server, databaseVersion[0]);
                }
                synchronized (m_serverMutex) {
                    m_lock = false;
                    m_serverMutex.notifyAll();
                }
            }
        };

        GetDatabaseVersionTask task = new GetDatabaseVersionTask(callback, server, databaseVersion);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


        while (m_lock) {

            try {
                synchronized (m_serverMutex) {
                    m_serverMutex.wait();
                }

            } catch (InterruptedException ex) {
                return false;
            }
        }

        if (DataManager.getDatabaseVersion() == null) {
            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the Server Address is incorrect.");
            infoDialog.centerToWindow(MainFrame.getMainWindow());
            infoDialog.setVisible(true);
            m_lock = true;
            return false;
        }

        if (DataManager.getDatabaseVersion().getServerVersion() != DataManager.CLIENT_VERSION) {
            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Version Error", "You are trying to connect to the ePims Server with an out of date ePims Client.");
            infoDialog.centerToWindow(MainFrame.getMainWindow());
            infoDialog.setVisible(true);
            m_lock = true;
            return false;
        }

        return true;

    }



    @Override
    protected boolean okCalled() {
        if (!checkParameters()) {
            return false;
        }

        Preferences preferences = EpimsPreferences.root();
        preferences.put(PreferencesKeys.EPIMS_CONNECT_LOG, m_userTextField.getText().trim());

        preferences.put(PreferencesKeys.EPIMS_CONNECT_SERVER, m_serverTextField.getText().trim());

        boolean savePassword = m_rememberPasswordCheckBox.isSelected();
        preferences.putBoolean(PreferencesKeys.EPIMS_CONNECT_SAVE_PASSWORD, savePassword);

        if (savePassword) {
            preferences.put(PreferencesKeys.EPIMS_CONNECT_PASSWORD, new String(m_passwordField.getPassword()));
        } else {
            preferences.put(PreferencesKeys.EPIMS_CONNECT_PASSWORD, "");
        }

        return true;
    }


}
