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
import fr.epims.tasks.ProtocolApplicationTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.ServerFilesDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.panels.model.AcquisitionTableModel;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 * Acquisitions corresponding to a Study
 *
 * @author JM235353
 *
 */
public class AcquisitionsListPanel extends HourGlassPanel {

    private AcquisitionTableModel m_model;

    public static boolean m_keepAcquisitionsListDisplay = false;

    public AcquisitionsListPanel(StudyJson s) {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JToolBar toolbar = createTreeToolbar(s);

        boolean editable = s.isRunningStatus() && DataManager.checkOwner(s);

        DecoratedTable table = new DecoratedTable();
        m_model = new AcquisitionTableModel(editable);
        table.setModel(m_model);

        JScrollPane tableScrollPane = new JScrollPane(table);

        table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        c.weightx = 1;
        add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        add(tableScrollPane, c);

        loadData(s);
    }


    private void loadData(StudyJson s) {

        setLoading(getNewLoadingIndex());

        final ArrayList<ProtocolApplicationJson> protocolApplicationJsons = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_model.setValues(protocolApplicationJsons);
                } else {
                    m_model.setValues(new ArrayList<>());
                }

                setLoaded(m_id);
            }
        };

        ProtocolApplicationTask task = new ProtocolApplicationTask(callback, s, protocolApplicationJsons);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private JToolBar createTreeToolbar(StudyJson s) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);

        FlatButton ftpButton = new FlatButton(IconManager.getIcon(IconManager.IconType.FTP), false);
        ftpButton.setToolTipText("Access by FTP to Study Data");
        toolbar.add(ftpButton);

        FlatButton refreshButton = new FlatButton(IconManager.getIcon(IconManager.IconType.REFRESH), false);
        refreshButton.setToolTipText("Update Acquisitions List");
        toolbar.add(refreshButton);

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // WART so the correct Tab is displayed back after the update
                m_keepAcquisitionsListDisplay = true;

                // WART : force the update (because for the moment, creations of new acquisitions are not tracked)
                DataManager.getDatabaseVersion().setVersion(StudyJson.class.getSimpleName(), -1);

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    m_keepAcquisitionsListDisplay = false;
                    return;
                }

            }
        });

        ftpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!DataManager.checkOwner(s)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to access FTP.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }


                FtpConfigurationJson configuration = DataManager.getFtpConfigurationForStudy(s);

                String[] autoExpandNames = { "data", "samples", "RAW"};
                ServerFilesDialog dialog = ServerFilesDialog.getSingleton(MainFrame.getMainWindow(), configuration, autoExpandNames, null);
                dialog.centerToScreen();
                dialog.setVisible(true);
            }
        });

        return toolbar;

    }

}
