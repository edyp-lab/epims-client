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
import fr.epims.ui.common.IconManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * FileSystem view for the Server side. It uses an FTP Client to list files
 *
 * @author JM235353
 */
public class FTPServerFileSystemView extends FileSystemView {

    //private static FTPServerFileSystemView m_singleton;
    private File[] m_roots = null;
    //private static HashMap<String, ArrayList<String>> m_rootsInfo = null;

    private FtpConfigurationJson m_config = null;
    private FtpClient m_ftpClient = null;

    public FTPServerFileSystemView(FtpConfigurationJson config) {
        m_config = config;
    }

    public void setConfiguration(FtpConfigurationJson configuration) {
        m_config = configuration;
        m_roots = null;
    }


    @Override
    public File[] getRoots() {
        if (m_roots == null || m_roots.length==0) {

            m_roots = new File[1];
            File serverFile = new ServerFile(this, m_config.getStartPath(), m_config.getStartPathDirectoryName(), true, 0, 0);

            String[] subDirs = m_config.getSubDirs();
            if (subDirs != null) {

                String firstSubDir = subDirs[0];

                // look for parent directory named 'a', 'b', 'c'...
                // and the firstSubDir in it.
                File[] files = serverFile.listFiles();
                boolean firstDirFound = false;
                if (files != null) {
                    searchFirstDir:
                    for (File f : files) {
                        if (f.isDirectory()) {
                            String name = f.getName();
                            if (name.length() == 1) {
                                char c = name.charAt(0);
                                if ((c>='a') && (c<='z')) {
                                    File[] azFiles = f.listFiles();
                                    for (File azf : azFiles) {
                                        if (azf.getName().equals(firstSubDir)) {
                                            serverFile = azf;
                                            firstDirFound = true;
                                            break searchFirstDir;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (firstDirFound) {
                    for (int i=1;i<subDirs.length;i++) {
                        files = serverFile.listFiles();
                        for (File f : files) {
                            if (f.isDirectory()) {
                                String name = f.getName();
                                if (name.equals(subDirs[i])) {
                                    serverFile = f;
                                    continue;
                                }
                            }
                        }
                    }
                }


            }

            m_roots[0] = serverFile;

        }


        return m_roots;
    }

    public void download(File serverSource, File localDestination) throws IOException {
        if (m_ftpClient == null) {
            m_ftpClient = new FtpClient(this, m_config);
        }
        m_ftpClient.download(serverSource, localDestination);
    }

    public void upload(File localSource, File serverDestination) throws IOException {
        if (m_ftpClient == null) {
            m_ftpClient = new FtpClient(this, m_config);
        }
        m_ftpClient.upload(localSource, serverDestination);
    }

    public void abortFTPClient() {
        if (m_ftpClient == null) {
            return;
        }
        m_ftpClient.disconnect();
        m_ftpClient = null;
    }

    @Override
    public Icon getSystemIcon(File f) {
        if (f == null) {
            return null;
        }

        return f.isDirectory() ? IconManager.getIcon(IconManager.IconType.FOLDER) : IconManager.getIcon(IconManager.IconType.FILE);

    }

    @Override
    public File getParentDirectory(File dir) {
        if (dir == null) {
            return null;
        }

        return dir.getParentFile();

    }

    @Override
    public String getSystemDisplayName(File f) {
        return f.getName();
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        throw new IOException("It is not allowed to create a directory.");
    }

    public File createNewFolder(File parentDirectory, String directoryName) {

        try {
            if (m_ftpClient == null) {
                m_ftpClient = new FtpClient(this, m_config);
            }
            File directory = m_ftpClient.createDirectory(parentDirectory, directoryName);

            return directory;
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {

        try {
            if (m_ftpClient == null) {
                m_ftpClient = new FtpClient(this, m_config);
            }
            ArrayList<File> list = m_ftpClient.getFiles(dir);

            File[] files = new File[list.size()];
            files = list.toArray(files);

            return files;
        } catch (IOException e) {
            return null;
        }

        /*
        final Object mutexFileLoaded = new Object();

        ArrayList<ServerFile> files = new ArrayList<>();

        try {
            synchronized (mutexFileLoaded) {

//                boolean[] fileLoaded = new boolean[1];
//                fileLoaded[0] = false;

                AbstractJMSCallback callback = new AbstractJMSCallback() {

                    @Override
                    public boolean mustBeCalledInAWT() {
                        return false;
                    }

                    @Override
                    public void run(boolean success) {

                        synchronized (mutexFileLoaded) {
                            mutexFileLoaded.notifyAll();
                        }
                    }
                };

                fr.proline.studio.dpm.task.jms.FileSystemBrowseTask task = new fr.proline.studio.dpm.task.jms.FileSystemBrowseTask(callback, dir.getPath(), files);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

                mutexFileLoaded.wait();
            }

        } catch (InterruptedException ie) {
            // should not happen
        }

        File[] fileArray = new File[files.size()];
        for (int i=0;i<files.size();i++) {
            fileArray[i] = files.get(i);
        }



        return fileArray;

         */


    }
}
