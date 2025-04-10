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

package fr.epims.ftp;


import fr.edyp.epims.json.FtpConfigurationJson;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Secured FTP Client
 *
 * @author JM235353
 *
 */
public class FtpClient {

    private FtpConfigurationJson m_config;

    private SSHClient m_sshClient = null;
    private SFTPClient m_sftClient = null;

    private FTPServerFileSystemView m_systemView;

    private boolean connected = false;

    public FtpClient(FTPServerFileSystemView systemView, FtpConfigurationJson config) {
        m_config = config;
        m_systemView = systemView;
        connect();
    }


    private void connect() {
        connected = true;
        if (m_sftClient != null) {
            return;
        }
        try {
            m_sshClient = new SSHClient();
            m_sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            m_sshClient.connect(m_config.getHost());
            m_sshClient.authPassword(m_config.getLogin(), m_config.getPassword());
            m_sftClient = m_sshClient.newSFTPClient();

        } catch (IOException e) {
            //JPM.TODO : manage error and display a message for user
            LoggerFactory.getLogger("Epims.Client").error("Unexpected exception in FTP connect !", e);
            e.printStackTrace();
            disconnect();
        }
    }

    public void disconnect() {
        connected = false;

        if (m_sftClient != null) {
            try {
                m_sftClient.close();
            } catch (IOException e) {
                //logger.error("Unable to close SFTP Client" ,e);
            }
            m_sftClient = null;

        }
        if (m_sshClient != null){
            try {
                m_sshClient.close();
            } catch (IOException e2) {
            } catch (Throwable t) {
            }
            m_sshClient = null;
        }
    }

    public void download(File serverSource, File localDestination) throws IOException {
        try {
            _download(serverSource, localDestination);
        } catch (Exception e) {
            if (connected) {
                disconnect();
                connect();
                _download(serverSource, localDestination);
            }
        }
    }

    private void _download(File serverSource, File localDestination) throws IOException {
        m_sftClient.getFileTransfer().download(serverSource.getAbsolutePath(), localDestination.getAbsolutePath());
    }

    public void upload(File localSource, File serverDestination) throws IOException {
        try {
            _upload(localSource, serverDestination);
        } catch (Exception e) {
            if (connected) {
                disconnect();
                connect();
                _upload(localSource, serverDestination);
            }
        }
    }

    private void _upload(File localSource, File serverDestination) throws IOException {
        m_sftClient.getFileTransfer().upload(localSource.getAbsolutePath(), serverDestination.getAbsolutePath());
    }

    public ArrayList<File> getFiles(File dir) throws IOException {
        try {
            return _getFiles(dir);
        } catch (Exception e) {
            // try to reconnect
            disconnect();
            connect();
            return _getFiles(dir);
        }
    }

    public File createDirectory(File parentDirectory, String directoryName) throws IOException {

        String path = parentDirectory.getAbsolutePath()+"/"+directoryName;
        m_sftClient.mkdir(path);


        ServerFile f = new ServerFile(m_systemView, path, directoryName, true, m_sftClient.atime(path), m_sftClient.size(path));

        return f;
    }

    private ArrayList<File> _getFiles(File dir) throws IOException {
        ArrayList<File> m_files = new ArrayList<>();

        List<RemoteResourceInfo> infos = m_sftClient.ls(dir.getAbsolutePath()); // "/data/epims/repository/restore"
        for (RemoteResourceInfo info : infos) {
            boolean isFile = info.isRegularFile();
            boolean isDirectory = info.isDirectory();
            if (isFile || isDirectory) {
                String filepath = info.getPath();
                String name = info.getName();
                ServerFile f = new ServerFile(m_systemView, filepath, name, isDirectory, info.getAttributes().getAtime(), info.getAttributes().getSize());
                m_files.add(f);
            }
        }

        return m_files;
    }


}
