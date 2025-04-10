package fr.epims.ui.dialogs;

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.epims.preferences.EpimsPreferences;
import fr.epims.preferences.PreferencesKeys;
import fr.epims.tasks.analyses.GetAnalysesDatabaseVersionTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.InfoDialog;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class AnalysesConnectionDialog extends DefaultDialog {


    private JTextField m_serverTextField;
    private JTextField m_userTextField;
    private JPasswordField m_passwordField;
    private JCheckBox m_rememberPasswordCheckBox;

    private Object m_serverMutex = new Object();
    private Boolean m_lock = true;


    public AnalysesConnectionDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Analyses Connection");


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



        JLabel serverLabel = new JLabel("Analyses Server :");
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

        String server = preferences.get(PreferencesKeys.ANALYSES_CONNECT_SERVER, "http://localhost:8082");

        m_serverTextField.setText(server);

        String log = preferences.get(PreferencesKeys.ANALYSES_CONNECT_LOG, "");
        m_userTextField.setText(log);

        Boolean savePassword = preferences.getBoolean(PreferencesKeys.ANALYSES_CONNECT_SAVE_PASSWORD, Boolean.FALSE);
        m_rememberPasswordCheckBox.setSelected(savePassword);

        if (savePassword) {
            String password = preferences.get(PreferencesKeys.ANALYSES_CONNECT_PASSWORD, "");
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

        final Integer[] serverVersion = new Integer[1];
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    DataManager.setAnalysesServerURL(server);
                }
                synchronized (m_serverMutex) {
                    m_lock = false;
                    m_serverMutex.notifyAll();
                }
            }
        };

        GetAnalysesDatabaseVersionTask task = new GetAnalysesDatabaseVersionTask(callback, server, serverVersion);
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

        if (serverVersion == null) {
            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Analyses Request Server Error", "Analyses Requests Server is down or the Server Address is incorrect.");
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
        preferences.put(PreferencesKeys.ANALYSES_CONNECT_LOG, m_userTextField.getText().trim());

        preferences.put(PreferencesKeys.ANALYSES_CONNECT_SERVER, m_serverTextField.getText().trim());

        boolean savePassword = m_rememberPasswordCheckBox.isSelected();
        preferences.putBoolean(PreferencesKeys.ANALYSES_CONNECT_SAVE_PASSWORD, savePassword);

        if (savePassword) {
            preferences.put(PreferencesKeys.ANALYSES_CONNECT_PASSWORD, new String(m_passwordField.getPassword()));
        } else {
            preferences.put(PreferencesKeys.ANALYSES_CONNECT_PASSWORD, "");
        }

        return true;
    }


}
