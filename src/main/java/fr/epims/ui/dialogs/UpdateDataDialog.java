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
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.DatabaseVersionJson;
import fr.epims.tasks.GetDatabaseVersionTask;
import fr.epims.ui.common.InfoDialog;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *
 * Open this dialog to check if data needs to be updated.
 * It is opened before another dialog which needs that data is up-to-date
 *
 * @author JM235353
 *
 */
public class UpdateDataDialog extends JDialog {

    private JLabel m_infoLabel = null;

    private String m_specificClassName;

    private boolean m_dataWasUpdated = false;
    private boolean m_serverDown = false;

    private DatabaseVersionJson m_serverDatabaseVersion = null;

    public UpdateDataDialog(Window parent, String specificClassName) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        m_specificClassName = specificClassName;

        setUndecorated(true);

        setLayout(new GridBagLayout());

        JPanel panel = createInternalPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;

        add(panel, c);


        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

                final DatabaseVersionJson[] databaseVersion = new DatabaseVersionJson[1];
                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, boolean finished) {
                        if (success) {

                            long updatingStart = System.currentTimeMillis();

                            m_serverDatabaseVersion = databaseVersion[0];
                            boolean hasDataToUpdate = DataManager.updateData(m_specificClassName, m_serverDatabaseVersion);

                            if (! hasDataToUpdate) {
                                setVisible(false);
                                return;
                            }

                            m_dataWasUpdated = true;


                            m_infoLabel.setText("Updating Data...");


                            DataManager.dataAvailable(new DataAvailableCallback() {
                                @Override
                                public void dataAvailable() {

                                    long delay = 2000 - (System.currentTimeMillis()-updatingStart);
                                    if (delay<0) {
                                        delay = 0;
                                    }
                                    final long _delay = delay;
                                    Thread t = new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(_delay);
                                            } catch (InterruptedException e) {

                                            }
                                            SwingUtilities.invokeLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setVisible(false);
                                                }
                                            });
                                        }
                                    });
                                    t.start();

                                }
                            }, false);


                        } else {
                            m_serverDown = true;
                            setVisible(false);
                            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down");
                            infoDialog.centerToWindow(MainFrame.getMainWindow());
                            infoDialog.setVisible(true);
                        }
                    }
                };

                GetDatabaseVersionTask task = new GetDatabaseVersionTask(callback, DataManager.getServerURL(), databaseVersion);
                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    private JPanel createInternalPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_infoLabel = new JLabel("Checking Data Update");

        // -------------- Place Widgets

        // --- Title
        c.gridx = 0;
        c.gridy = 0;
        p.add(m_infoLabel, c);


        return p;
    }

    public void centerToWindow(Window w) {

        // pack must have been done beforehand
        pack();

        int width = getWidth();
        int height = getHeight();

        int frameX = w.getX();
        int frameY = w.getY();
        int frameWidth = w.getWidth();
        int frameHeight = w.getHeight();

        int x = frameX + (frameWidth - width) / 2;
        int y = frameY + (frameHeight - height) / 2;

        setLocation(x, y);

    }

    public boolean isDataUpdated() {
        return m_dataWasUpdated;
    }

    public boolean isServerDown() {
        return m_serverDown;
    }

    public DatabaseVersionJson getServerDatabaseVersion() {
        return m_serverDatabaseVersion;
    }

}
