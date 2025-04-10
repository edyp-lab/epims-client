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

package fr.epims.mgf;

import fr.edyp.epims.json.FtpConfigurationJson;
import fr.edyp.epims.json.MgfFileInfoJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.dataaccess.DataManager;
import fr.epims.ftp.*;
import fr.epims.tasks.mgf.RegisterMgfTask;
import fr.epims.util.Util;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * Thread used to transfer mgf files to server by FTP
 *
 * @author JM235353
 *
 */
public class MgfTransferThread extends Thread {

    private static MgfTransferThread m_instance;

    private MgfTableModel m_model;


    private ServerFile m_serverFileBeingTreated = null;

    private LinkedList<MgfFileInfo> m_actions;
    private HashSet<MgfFileInfo> m_actionSet;
    private ArrayList<MgfFileInfo> m_abortedActionIdList;

    private boolean m_transferFinished = true;

    private MgfTransferThread() {
        super("MgfTransferThread"); // useful for debugging

        m_actions = new LinkedList<>();
        m_abortedActionIdList = new ArrayList<>();
        m_actionSet = new HashSet<>();

    }

    public static MgfTransferThread getTransferThread() {
        if (m_instance == null) {
            m_instance = new MgfTransferThread();
            m_instance.start();
        }
        return m_instance;
    }

    public static boolean isInitialized(){
        return m_instance != null;
    }

    public void setModel(MgfTableModel model) {
        m_model = model;
    }

    public final void addTask(MgfFileInfo mgfFileInfo) {

        // action is queued
        synchronized (this) {
            m_actions.add(mgfFileInfo);
            m_actionSet.add(mgfFileInfo);
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
                MgfFileInfo action;
                synchronized (this) {

                    while (true) {

                        // Management of aborted task
                        if (!m_abortedActionIdList.isEmpty()) {
                            int nbAbortedTask = m_abortedActionIdList.size();
                            for (int i = 0; i < nbAbortedTask; i++) {
                                MgfFileInfo abortedTask = m_abortedActionIdList.get(i);
                                boolean present = m_actionSet.remove(abortedTask);
                                if (!present) {
                                    continue;
                                }
                                if (m_actions.contains(abortedTask)) {
                                    m_actions.remove(abortedTask);
                                    abortedTask.setStatus(MgfFileInfo.StatusEnum.FAILED);
                                    abortedTask.setErrorMessage("Aborted");

                                }
                            }
                            m_abortedActionIdList.clear();
                        }


                        // look for a task to be done
                        if (!m_actions.isEmpty()) {
                            m_transferFinished = false;
                            action = m_actions.poll();
                            break;
                        }
                        m_transferFinished = true;
                        saveMgfFileCache();
                        wait();
                    }
                    notifyAll();
                }

                try {
                    action.setStatus(MgfFileInfo.StatusEnum.FTP_RUNNING);
                    m_model.dataChanged(action);

                    StudyJson studyJson = DataManager.getStudy(action.getStudyId());

                    FtpConfigurationJson ftpConfigurationJson = DataManager.getFtpConfigurationForStudy(studyJson);
                    FTPServerFileSystemView serverFileSystemView = new FTPServerFileSystemView(ftpConfigurationJson);
                    final File[] roots = serverFileSystemView.getRoots();

                    File serverRoot = roots[0];

                    // --- Look for or create samples/data/MGF directory in the study
                    ServerFile samplesDirectory = findOrCreateDirectory((ServerFile) serverRoot,"samples");
                    if (samplesDirectory == null) {
                        action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                        action.setErrorMessage("Can not find or create samples directory");
                        m_model.dataChanged(action);
                        continue;
                    }
                    ServerFile dataDirectory = findOrCreateDirectory(samplesDirectory,"data");
                    if (dataDirectory == null) {
                        action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                        action.setErrorMessage("Can not find or create data directory");
                        m_model.dataChanged(action);
                        continue;
                    }
                    ServerFile spectraDirectory = findOrCreateDirectory(dataDirectory,"SPECTRA");
                    if (spectraDirectory == null) {
                        action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                        action.setErrorMessage("Can not find or create SPECTRA directory");
                        m_model.dataChanged(action);
                        continue;
                    }

                    // directory of the name of the local computer
                    ServerFile mgfDirectory = spectraDirectory;
                    String computerDirectoryName = Util.getComputerName();
                    if ((computerDirectoryName != null) && (computerDirectoryName.length()>1)) {
                        mgfDirectory = findOrCreateDirectory(spectraDirectory, computerDirectoryName);
                        if (mgfDirectory == null) {
                            action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                            action.setErrorMessage("Can not find or create computer directory");
                            m_model.dataChanged(action);
                            continue;
                        }
                    }

                    // look for or create sub MGF directory
                    ServerFile submgfDirectory = findOrCreateDirectory(mgfDirectory, action.getDirectory());
                    if (submgfDirectory == null) {
                        action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                        action.setErrorMessage("Can not find or create "+action.getDirectory()+" directory");
                        m_model.dataChanged(action);
                        continue;
                    }

                    // --- UPLOAD to sub mgf directory MGF/submgfDirectory
                    submgfDirectory.uploadto(action.getFile());

                    // --- Write MGF File in Database
                    Date fileDate = new Date(action.getFile().lastModified());
                    double fileInMo = action.getFile().length() / 1000000d;
                    MgfFileInfoJson mgfFileInfoJson = new MgfFileInfoJson(action.getFile().getName(), submgfDirectory.getAbsolutePath(), action.getStudyId(), fileDate, fileInMo);
                    RegisterMgfTask registerMgfTask = new RegisterMgfTask(null, mgfFileInfoJson);
                    boolean success = registerMgfTask.fetchData();
                    if (success) {
                        action.setStatus(MgfFileInfo.StatusEnum.FTP_DONE);
                    } else {
                        action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                    }
                    m_model.dataChanged(action);

                } catch (IOException ioe) {
                    action.setStatus(MgfFileInfo.StatusEnum.FAILED);
                    action.setErrorMessage("Exception Occured:"+ioe.getMessage());
                    m_model.dataChanged(action);
                }

            }


        } catch (Throwable t) {
            m_instance = null; // reset thread
        }

    }

    public void saveMgfFileCache() {
        //Save cache mgf file
        if(m_model!=null) {
            LoggerFactory.getLogger("Epims.Client").debug("Save MGF Cache file");
            MgfFileManager.getSingleton().writeMgfDB(m_model.getMgfInfoList());
        }
    }

    private ServerFile findOrCreateDirectory(ServerFile parentDirectory, String childDirectoryName) {
        File[] files = parentDirectory.listFiles();
        ServerFile childDirectory = null;
        if (files != null) {

            for (File f : files) {
                if (f.isDirectory()) {
                    String name = f.getName();
                    if (name.equals(childDirectoryName)) {
                        childDirectory = (ServerFile) f;
                        break;
                    }
                }
            }
        }
        if (childDirectory == null) {
            childDirectory = (ServerFile) (parentDirectory.createDirectory(childDirectoryName));
        }

        return childDirectory;
    }

    public static boolean isTransfering() {
        if (m_instance == null) {
            return false;
        }
        return ! m_instance.m_transferFinished;
    }


}