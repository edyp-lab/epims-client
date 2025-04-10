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

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * Thread used to download/upload files, one by one
 *
 * @author JM235353
 *
 */
public class FTPTransferThread extends Thread {

    private static FTPTransferThread m_instance;

    private TransferInterface m_transferInterface;
    private TransferTableModel m_model;

    private boolean m_transferFinished = true;

    private ServerFile m_serverFileBeingTreated = null;


    private FTPTransferThread() {
        super("FTPTransferThread"); // useful for debugging

    }

    public static FTPTransferThread getTransferThread() {
        if (m_instance == null) {
            m_instance = new FTPTransferThread();
            m_instance.start();
        }
        return m_instance;
    }

    public void setTransferInterface(TransferInterface transferInterface) {
        m_transferInterface = transferInterface;
    }

    public void setModel(TransferTableModel model) {
        m_model = model;
        synchronized (this) {
            notifyAll();
        }
    }

    public void awake() {
        synchronized (this) {
            notifyAll();
        }
    }

    public void stopDownload() {
        if (m_serverFileBeingTreated != null) {
            m_serverFileBeingTreated.abort();
        }

    }


    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                TransferInfo action = null;
                synchronized (this) {

                    while (true) {

                        // look for a task to be done
                        if (m_model != null) {
                            action = m_model.getFirstWaiting();
                            if (action != null) {
                                m_transferFinished = false;
                                break;
                            } else {
                                m_transferFinished = true;
                            }
                        }

                        wait();
                    }
                    notifyAll();
                }

                action.setRunning();
                dataUpdated(false, true);


                try {

                    if (action.isDownload()) {
                        // --- DOWNLOAD

                        ServerFile serverFile = action.getServerFile();
                        if (serverFile.isFile()) {
                            m_serverFileBeingTreated = serverFile;
                            try {
                                serverFile.download(action.getLocalFile());
                            } finally {
                                m_serverFileBeingTreated = null;
                            }
                            action.setDone();
                        } else {
                            action.getLocalFile().mkdir();

                            ArrayList<TransferInfo> list = new ArrayList<>();
                            File[] childrenFiles = serverFile.listFiles();
                            if (childrenFiles != null) {
                                for (File childFile : childrenFiles) {
                                    TransferInfo transferInfoChild;
                                    if (childFile.isDirectory()) {
                                        transferInfoChild = new TransferInfo(new File(action.getLocalFile(), childFile.getName()), (ServerFile) childFile, true);
                                    } else {
                                        transferInfoChild = new TransferInfo(action.getLocalFile(), (ServerFile) childFile, true);
                                    }
                                    list.add(transferInfoChild);
                                }
                                m_model.addDownloadInfoList(list);
                            }
                            action.setDone();

                        }
                    } else {
                        // --- UPLOAD
                        ServerFile serverFile = action.getServerFile();
                        m_serverFileBeingTreated = serverFile;
                        try {

                            serverFile.uploadto(action.getLocalFile());
                        } finally {
                            m_serverFileBeingTreated = null;
                        }
                        action.setDone();
                    }

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    action.setFailed();
                }

                dataUpdated(!action.isDownload(), true);

            }


        } catch (Throwable t) {
            m_instance = null; // reset thread
        }

    }

    private void dataUpdated(final boolean serverSideModified, final boolean actionListModified) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_transferInterface.actionDone(serverSideModified, actionListModified);
            }
        });
    }

    public static boolean isTransfering() {
        if (m_instance == null) {
            return false;
        }
        return ! m_instance.m_transferFinished;
    }


}