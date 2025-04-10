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

package fr.epims;

import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.epims.ftp.FTPTransferThread;
import fr.edyp.epims.json.ActorJson;
import fr.edyp.epims.json.ContactJson;
import fr.epims.mgf.MgfPanel;
import fr.epims.mgf.MgfTransferThread;
import fr.epims.tasks.ConnectTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.EpimsConnectionDialog;
import fr.epims.ui.panels.AcquisitionsPanel;
import fr.epims.ui.panels.ActivitiesPanel;
import fr.epims.ui.panels.admin.AdminPanel;
import fr.epims.ui.analyserequest.panels.AnalysesRequestsPanel;
import fr.epims.ui.panels.robot.RobotPanel;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * Main Frame of the application
 *
 * @author JM235353
 *
 */
public class MainFrame extends JFrame {

    private static MainFrame m_singleton;

    private JLabel m_loginLabel;
    private JLabel m_roleLabel;
    private FlatButton m_logoutButton;

    private JTabbedPane m_tabbedPane;

    public static MainFrame getMainWindow() {
        if (m_singleton == null) {
            m_singleton = new MainFrame();
        }

        return m_singleton;
    }

    private MainFrame() {
        String version = "";
        try {
            final Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("epims.properties"));
            version = properties.getProperty("epims.version", "");
        } catch (Exception ignored) {

        }
        LoggerFactory.getLogger("Epims.Client").debug(" Start ePims Client Frame -- Version "+version);
        setTitle("ePims "+version);
        setSize(new Dimension(1200,800));
        setResizable(true);

        ArrayList<Image> icons = new ArrayList<>();
        icons.add(IconManager.getIcon(IconManager.IconType.EPIMS_LOGO16).getImage());
        icons.add(IconManager.getIcon(IconManager.IconType.EPIMS_LOGO32).getImage());
        icons.add(IconManager.getIcon(IconManager.IconType.EPIMS_LOGO64).getImage());
        icons.add(IconManager.getIcon(IconManager.IconType.EPIMS_LOGO128).getImage());
        setIconImages(icons);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                askForExit();
                super.windowClosing(e);
            }
        });

        JPanel mainPanel = createMainPanel();
        getContentPane().add(mainPanel);

        setJMenuBar(createMenu());

        /*GlassValidationPane gp = new GlassValidationPane();

        setGlassPane(gp);
        gp.setVisible(true);*/
    }

    private JMenuBar createMenu() {


        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new GridBagLayout());


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 2, 0, 2);


        JMenu fileMenu = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (askForLogout() ) {
                    disconnect();
                }
            }
        });


        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                askForExit();
            }
        });

        fileMenu.add(logout);
        fileMenu.add(quit);


        c.gridx = 0;
        c.gridy = 0;
        menuBar.add(fileMenu, c);

        c.gridx++;
        c.weightx = 1;
        menuBar.add(Box.createHorizontalGlue(), c);

        c.gridx++;
        c.weightx = 0;
        menuBar.add(createLogoutPanelFlat(), c);



        return menuBar;

    }

    private JPanel createLogoutPanelFlat() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBackground(Color.white);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_loginLabel = new JLabel();
        m_loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
        m_roleLabel = new JLabel();
        m_roleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        m_logoutButton = new FlatButton(IconManager.getIcon(IconManager.IconType.LOGOUT), false);
        m_logoutButton.setToolTipText("Logout");
        m_logoutButton.setVisible(false);

        c.gridx = 0;
        c.gridy = 0;
        p.add(m_loginLabel, c);

        c.gridx++;
        p.add(m_roleLabel, c);

        c.gridx++;
        p.add(m_logoutButton, c);

        m_logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (askForLogout() ) {
                    disconnect();
                }
            }
        });

        return p;

    }

    private JPanel createMainPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JTabbedPane tabbedPane = createTabbedPane();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;

        c.gridy++;
        c.weighty = 1;
        panel.add(tabbedPane, c);

        tabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {

                if (tabbedPane.getSelectedComponent().equals(RobotPanel.getPanel())) {
                    RobotPanel.getPanel().loadData();
                } else {
                    // so we load fresh data each time, we open the robot panel
                    RobotPanel.getPanel().reinit();
                }
            }
        });



        return panel;

    }

    private JTabbedPane createTabbedPane() {
        m_tabbedPane = new JTabbedPane();

        JPanel activityPanel = ActivitiesPanel.getActivitiesPanel();
        m_tabbedPane.addTab("Activities", activityPanel);
        m_tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.ACTIVITIES));

        JPanel acquisitionPanel = AcquisitionsPanel.getAcquisitionsPanel();
        m_tabbedPane.addTab("Acquisitions", acquisitionPanel);
        m_tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.ACQUISITIONS));

        JPanel mgfPanel = MgfPanel.getPanel();
        m_tabbedPane.addTab("MGF Files", mgfPanel);
        m_tabbedPane.setIconAt(2, IconManager.getIcon(IconManager.IconType.MGFFILE));

        JPanel robotPanel = RobotPanel.getPanel();
        m_tabbedPane.addTab("Robot", robotPanel);
        m_tabbedPane.setIconAt(3, IconManager.getIcon(IconManager.IconType.ROBOT));

        JPanel analysesRequestsPanel = AnalysesRequestsPanel.getPanel();
        m_tabbedPane.addTab("Analyses Requests", analysesRequestsPanel);
        m_tabbedPane.setIconAt(4, IconManager.getIcon(IconManager.IconType.ANALYSE_REQUEST));

        JPanel adminPanel = AdminPanel.getAdminPanel();
        m_tabbedPane.addTab("Admin", adminPanel);
        m_tabbedPane.setIconAt(5, IconManager.getIcon(IconManager.IconType.ADMIN));


        enableTabbedPane(false, false, false, false, false, false);

        m_tabbedPane.addChangeListener(new ChangeListener() { //add the Listener

            public void stateChanged(ChangeEvent e) {

                // Analyses Requests
                if (m_tabbedPane.getSelectedIndex() == 4) {
                    AnalysesRequestsPanel.getPanel().analysesServerConnection();
                } else if (m_tabbedPane.getSelectedIndex() == 2) {
                    MgfPanel.getPanel().loadData(true, true);
                }

            }
        });


        return m_tabbedPane;
    }

    public void enableTabbedPane(boolean activities, boolean acquisitions, boolean mgf, boolean robot, boolean admin, boolean analyses) {

        m_tabbedPane.setEnabledAt(0, activities);
        m_tabbedPane.setEnabledAt(1, acquisitions);
        m_tabbedPane.setEnabledAt(2, mgf);
        m_tabbedPane.setEnabledAt(3, robot);
        m_tabbedPane.setEnabledAt(4, analyses );
        m_tabbedPane.setEnabledAt(5, admin && (DataManager.isAdmin() || DataManager.isAdminUser()) );
        AdminPanel.getAdminPanel().setArchiveEnabled(admin && DataManager.isAdmin());
        

        RobotPanel.getPanel().enableTabbedPane(robot && DataManager.isRobotUser());

    }

    public void selectActivitiesTabbedPane() {
        m_tabbedPane.setSelectedIndex(0);
    }


    public void connection() {
        EpimsConnectionDialog connectionDialog = new EpimsConnectionDialog(this);
        connectionDialog.centerToWindow(this);
        connectionDialog.setVisible(true);

        if (connectionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            connect(connectionDialog.getLogin(), connectionDialog.getPassword());

        } else {
            askForExit();

            connection();

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
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    MainFrame.getMainWindow().disconnect();
                }
            }
        };

        ConnectTask task = new ConnectTask(callback, login, password);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    public void connectionDone(String login) {

        m_loginLabel.setText(login);
        m_roleLabel.setText("");

        WaitDialog waitDialog = new WaitDialog( "Data Loading", "Data is Loading.... Please Wait");
        waitDialog.setVisible(true);


        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {
                DataManager.setLoggedUser(login);
                waitDialog.setVisible(false);
                ActorJson actorJson = DataManager.getActor(login);
                ContactJson contact = actorJson.getContact();
                m_loginLabel.setText(contact.getFirstName() + " " + contact.getLastName());
                m_roleLabel.setText(DataManager.getRoleTitle());

                enableTabbedPane(true, true, true, true, true, false);

                m_logoutButton.setVisible(true);
                ActivitiesPanel.getActivitiesPanel().connect();

                //JPM.PUTBACK
                //ManageStudiesDialog manageStudiesDialog = new ManageStudiesDialog(MainFrame.getMainWindow());
                //manageStudiesDialog.centerToWindow(MainFrame.getMainWindow());
                //manageStudiesDialog.setVisible(true);
            }
        };
        DataManager.dataAvailable(callback, false, true);

    }

    public void disconnect() {
        m_loginLabel.setText("");
        m_roleLabel.setText("");
        m_logoutButton.setVisible(false);
        DataManager.clearAllData();
        DataManager.reinitLoggedUser();
        DataManager.setAnalysesLoggedUser(null);
        ActivitiesPanel.getActivitiesPanel().disconnect();
        AcquisitionsPanel.getAcquisitionsPanel().reinit();
        RobotPanel.getPanel().reinit();
        AnalysesRequestsPanel.getPanel().reinit();
        AdminPanel.getAdminPanel().reinit();

        m_tabbedPane.setSelectedIndex(0);
        enableTabbedPane(false, false, false, false, false, false);


        connection();

    }


    private void askForExit() {

        boolean mgfAction = MgfTransferThread.isTransfering();
        boolean ftpAction = FTPTransferThread.isTransfering();
        boolean plateAction = RobotPanel.hasModification();

        QuestionDialog questionDialog;

        if (mgfAction) {
            questionDialog = new QuestionDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "MGF Uploading not Finished", "MGF Uploading is not finished. Do you really want to quit ?");
        } else if (ftpAction) {
            questionDialog = new QuestionDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "FTP Downloading not Finished", "FTP Downloading is not finished. Do you really want to quit ?");
        } else if (plateAction) {
            questionDialog = new QuestionDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Robot Plate not Saved", "There is a Robot Plate not saved. Do you really want to quit ?");
        }
        else {
            questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Quit ?", "Do you want to quit  ?");
        }
        questionDialog.centerToWindow(MainFrame.getMainWindow());
        questionDialog.setVisible(true);
        if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if(MgfTransferThread.isInitialized()) {
                MgfTransferThread.getTransferThread().saveMgfFileCache();
            }
            System.exit(0);
        }
    }

    private boolean askForLogout() {

        boolean ftpAction = FTPTransferThread.isTransfering();
        boolean plateAction = RobotPanel.hasModification();

        QuestionDialog questionDialog = null;
        if (ftpAction) {
            questionDialog = new QuestionDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "FTP Download not Finished", "FTP Download is not finished. Do you really want to logout ?");
        } else if (plateAction) {
            questionDialog =  new QuestionDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Robot Plate not Saved", "There is a Robot Plate not saved. Do you really want to logout ?");
        }
        if (questionDialog != null) {
            questionDialog.centerToWindow(MainFrame.getMainWindow());
            questionDialog.setVisible(true);
          return questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK;
        }
        return true;

    }

 public class WaitDialog extends InfoDialog{

     public WaitDialog(String title, String message) {
         super(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, title, message);
         init();
     }

     private void init(){
         setButtonVisible(InfoDialog.BUTTON_OK, false);
         setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
         centerToWindow(MainFrame.getMainWindow());
         setModalityType(Dialog.ModalityType.MODELESS);
         setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
     }

     @Override
     protected boolean cancelCalled() {
         return false;
     }
 }


}
