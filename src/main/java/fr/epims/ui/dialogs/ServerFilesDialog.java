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

import fr.epims.ftp.*;
import fr.edyp.epims.json.FtpConfigurationJson;
import fr.epims.ui.common.DefaultDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 * FTP dialog to download/upload files from/to the FTP Server.
 *
 * The FTP dialog displays automatically files of a specific
 * Study
 *
 * @author JM235353
 *
 */
public class ServerFilesDialog extends DefaultDialog implements TransferInterface {

    private static ServerFilesDialog m_singleton = null;

    private TreeFileChooserPanel m_treeFileChooserPanel;
    private TransferInfoPanel m_transferInfoPanel;

    private ServerFilesDialog(Window parent, FtpConfigurationJson configuration, String[] autoExpandNames, String autoselectValue) {
        super(parent);

        setTitle("FTP");
        setResizable(true);

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);
        setButtonVisible(DefaultDialog.BUTTON_OK, false);
        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setInternalComponent(createInternalPanel(configuration, autoExpandNames));

        pack();

    }


    public static ServerFilesDialog getSingleton(Window parent, FtpConfigurationJson configuration, String[] autoExpandNames, String autoselectValue) {
        if (m_singleton == null) {
            m_singleton = new ServerFilesDialog(parent, configuration, autoExpandNames, autoselectValue);
        } else {
            m_singleton.setConfiguration(configuration, autoselectValue, autoExpandNames);
        }
        return m_singleton;
    }

    private void setConfiguration(FtpConfigurationJson configuration, String autoselectData, String[] autoExpandNames) {
        m_treeFileChooserPanel.setConfiguration(configuration, autoselectData, autoExpandNames);
    }

    private JPanel createInternalPanel(FtpConfigurationJson configuration, String[] autoExpandNames) {

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);

        m_transferInfoPanel = new TransferInfoPanel();
        m_treeFileChooserPanel = new TreeFileChooserPanel(new FTPServerFileSystemView(configuration), this, autoExpandNames);


        FTPTransferThread.getTransferThread().setTransferInterface(this);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0.66;
        p.add(m_treeFileChooserPanel, c);

        c.gridy++;
        c.weighty = 0.33;
        p.add(m_transferInfoPanel, c);

        return p;
    }

    protected boolean okCalled() {

        return true;
    }


    @Override
    public void addFilesToTransfer(ArrayList<TransferInfo> transferInfoList) {
        m_transferInfoPanel.addFilesToTransfer(transferInfoList);
    }

    @Override
    public void actionDone(boolean serverSideModified, boolean actionListModified) {
        if (serverSideModified) {
            m_treeFileChooserPanel.updateTree();
        }
        if (actionListModified) {
            m_transferInfoPanel.updateList();
        }
    }

    @Override
    public void setRootDirectoryName(String rootDirectoryName) {
        m_transferInfoPanel.setRootDirectoryName(rootDirectoryName);
    }
}



