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

package fr.edyp.epims.mgf;

import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.dataaccess.DataManager;
import fr.edyp.epims.preferences.EPimsUserPreferences;
import fr.edyp.epims.preferences.PreferencesKeys;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;


/**
 *
 * Register of the .mgf files found on mgf path locally.
 * @author JM235353
 *
 */
public class MgfFileManager {

    private static MgfFileManager m_singleton = null;

    private static final Logger logger = LoggerFactory.getLogger(MgfFileManager.class);
    private static final String TRANSFERRED_FILE_NAME = "transferedMgfFiles.db";
    private static final String TRANSFERRED_FILE_VERSION = "##VERSION 2.0\n";

    private HashMap<String, ArrayList<File>> m_mgfFilesMap;
    private boolean m_mgfLoaded = false;
    private boolean m_mgfLoading = false;

    private final ArrayList<MgfFilesListener> m_listeners = new ArrayList<>();

    public static MgfFileManager getSingleton() {
        if (m_singleton == null) {
            m_singleton = new MgfFileManager();
        }
        return m_singleton;
    }

    private File m_rootDirectory;

    private MgfFileManager() {

        Preferences preferences = EPimsUserPreferences.root();
        //Use this Path as default on Process PC...
        String defaultDirString = preferences.get(PreferencesKeys.MGF_ROOT_DIR, "D:\\Data\\MGF");
        LoggerFactory.getLogger("Epims.Client").info("Read mgf files from "+defaultDirString);
        m_rootDirectory = (defaultDirString!= null) ? new File(defaultDirString) : null;
        if (m_rootDirectory != null && (! m_rootDirectory.exists() || ! m_rootDirectory.isDirectory())){
            m_rootDirectory = null;
        }
    }

    public synchronized String getRootDirectoryPath() {
        if (m_rootDirectory == null) {
            return "";
        }
        return m_rootDirectory.getAbsolutePath();
    }

    public synchronized void setRoot(File rootDirectory) {
        m_mgfLoaded = false;

        m_rootDirectory = rootDirectory;

        Preferences preferences = EPimsUserPreferences.root();
        preferences.put(PreferencesKeys.MGF_ROOT_DIR, rootDirectory.getAbsolutePath());
        try {
            preferences.flush();
        } catch (Exception prefException) {
            LoggerFactory.getLogger("Epims.Client").error(" Error writing preferences for mgf root directory ", prefException);
        }

    }

    /**
     * Processes the provided map of MGF file information, updating and enriching it with data
     * from a cache file if available.
     * Cache file is structured with one line by file and woth following information
     * full_path \t Study Name \t FTP upload timestamp \t acq Name \t acq specified by yser (False/True)
     *
     *
     * @param mgfFileInfoArrayMap A map where keys are file paths and values are MgfFileInfo objects
     *                            representing metadata and state for MGF files. This map will be
     *                            updated based on the information read from the cache file.
     */
    public synchronized void getExtraInfo(HashMap<String, MgfFileInfo> mgfFileInfoArrayMap) {
        if (m_rootDirectory == null) {
            // Clear data
            return;
        }

        File tranferredFile = new File(m_rootDirectory.getAbsolutePath()+"\\"+ TRANSFERRED_FILE_NAME);

        if (! tranferredFile.exists() || tranferredFile.isDirectory()) {
            // Clear data
            return;
        }
        LoggerFactory.getLogger("Epims.Client").debug("Read mgf files Cache information  from "+tranferredFile.getAbsolutePath());
        // parse file
        try (BufferedReader br = new BufferedReader(new FileReader(tranferredFile))) {
            String line;
            boolean firstLine = true;
            boolean isNewVersion = false;
            while ((line = br.readLine()) != null) {
                if(firstLine) {
                    firstLine = false;
                    isNewVersion = line.startsWith("##VERSION");
                    if(isNewVersion)
                        continue;
                }

                StringTokenizer st = new StringTokenizer(line, "\t");
                String filePath = st.nextToken();
                MgfFileInfo mgfFileInfo = mgfFileInfoArrayMap.get(filePath);
                if (mgfFileInfo != null) {
                    if (mgfFileInfo.getStudyId() == -1) {
                        String study = st.nextToken();
                        StudyJson studyJson = DataManager.getStudyByNomenclature(study);
                        if (studyJson != null) {
                            mgfFileInfo.setStudyId(studyJson.getId());
                        }

                        String timeStamp = st.nextToken();
                        if (! timeStamp.equals("_")) {
                            Date ftpDate = new Date(Long.parseLong(timeStamp));
                            mgfFileInfo.setTransferDate(ftpDate);
                        }

                        if(isNewVersion){
                            String acqName = st.nextToken();
                            if (! acqName.equals("_")) {
                                mgfFileInfo.setAcqName(acqName);
                            }

                            Boolean userAcq = Boolean.valueOf(st.nextToken());
                            mgfFileInfo.setUserAcqName(userAcq);
                        }

                    } else {
                      logger.debug(" STUDY Already defined !!  {}", mgfFileInfo.getStudyId());
                      if(isNewVersion && (mgfFileInfo.getAcqName() == null ||  mgfFileInfo.getAcqName().isEmpty())){
                          st.nextToken(); // study
                          st.nextToken(); // timestamp
                          String acqName = st.nextToken();
                          if (! acqName.equals("_")) {
                              mgfFileInfo.setAcqName(acqName);
                          }
                          Boolean userAcq = Boolean.valueOf(st.nextToken());
                          mgfFileInfo.setUserAcqName(userAcq);
                      }
                    }
                }
            }
        } catch (IOException e) {

        }

    }

    public synchronized void writeMgfDB(ArrayList<MgfFileInfo> mgfFileInfoArrayList) {
        if (m_rootDirectory == null) {
            return;
        }
        File transferredFile = new File(m_rootDirectory.getAbsolutePath()+"\\"+ TRANSFERRED_FILE_NAME);

        // Backup existing file if it exists
        if (transferredFile.exists()) {
            backupExistingFile(transferredFile);
            cleanupOldBackups(transferredFile.getParentFile());
        }

        try {
            FileWriter fw = new FileWriter(transferredFile);
            fw.write(TRANSFERRED_FILE_VERSION);
            for (MgfFileInfo mgfFileInfo : mgfFileInfoArrayList) {

                fw.write(mgfFileInfo.getFile().getAbsolutePath());
                fw.write('\t');

                String study = "_";
                Integer id = mgfFileInfo.getStudyId();
                if ((id != null) && (id != -1)) {
                    StudyJson studyJson = DataManager.getStudy(id);
                    if (studyJson != null) {
                        study = studyJson.getTitle();
                    }
                }
                fw.write(study);
                fw.write('\t');

                Date date = mgfFileInfo.getTransferDate();
                if (date == null) {
                    fw.write('_');
                } else {
                    fw.write(String.valueOf(date.getTime()));
                }
                fw.write('\t');

                String acqName = mgfFileInfo.getAcqName();
                if(acqName == null || acqName.isEmpty()){
                    acqName ="_";
                }
                fw.write(acqName);
                fw.write('\t');

                Boolean usrAcq = mgfFileInfo.isUserAcqName();
                if(usrAcq == null)
                    fw.write("_");
                else
                    fw.write(usrAcq.toString());

                fw.write('\n');
            }
            fw.flush();
            fw.close();
        } catch (java.io.IOException e) {

        }
    }

    /**
     * Creates a backup of the existing transferred file with timestamp pattern YYYY_MM_dd_HH_mm
     */
    private void backupExistingFile(File originalFile) {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
            String timestamp = now.format(formatter);

            String backupFileName = TRANSFERRED_FILE_NAME + "." + timestamp;
            File backupFile = new File(originalFile.getParent(), backupFileName);

            java.nio.file.Files.copy(originalFile.toPath(), backupFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            logger.info("Backup created: {}", backupFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to create backup of transferred file", e);
        }
    }

    /**
     * Removes backup files older than one month
     */
    private void cleanupOldBackups(File directory) {
        try {
            java.time.LocalDateTime oneMonthAgo = java.time.LocalDateTime.now().minusMonths(1);

            File[] files = directory.listFiles((dir, name) ->
                    name.startsWith(TRANSFERRED_FILE_NAME + ".") &&
                            name.matches(".*\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}$"));

            if (files != null) {
                for (File backupFile : files) {
                    try {
                        // Extract timestamp from filename
                        String filename = backupFile.getName();
                        String timestampStr = filename.substring(TRANSFERRED_FILE_NAME.length() + 1);

                        java.time.format.DateTimeFormatter formatter =
                                java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
                        java.time.LocalDateTime fileDate = java.time.LocalDateTime.parse(timestampStr, formatter);

                        if (fileDate.isBefore(oneMonthAgo)) {
                            if (backupFile.delete()) {
                                logger.info("Deleted old backup file: {}", backupFile.getName());
                            } else {
                                logger.warn("Failed to delete old backup file: {}", backupFile.getName());
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error processing backup file {}: {}", backupFile.getName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during backup cleanup", e);
        }
    }

    /**
     * Retrieves and initializes the map of MGF files, on local filesystem, if not already loaded. If the files are
     * currently being loaded, the given listener is added to a queue to be notified once loading
     * is complete. If the files have already been loaded, the listener is immediately notified
     * with the map of MGF files.
     *
     * @param mgfFilesListener Instance of MgfFilesListener to be notified when the map of MGF files
     *                         is loaded. Can be null if no listener needs to be notified.
     */
    public synchronized void getMgfMap(MgfFilesListener mgfFilesListener) {
        if (!m_mgfLoaded) {

            if (!m_mgfLoading) {
                updateFiles(mgfFilesListener);
            } else {
                if (mgfFilesListener != null) {
                    m_listeners.add(mgfFilesListener);
                }
            }
        } else {
            mgfFilesListener.mgfFilesMapLoaded(m_mgfFilesMap);
        }

    }

    public synchronized void updateFiles(MgfFilesListener mgfFilesListener) {
        if (mgfFilesListener != null) {
            m_listeners.add(mgfFilesListener);
        }

        Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    _updateFilesImpl();
                }
        });
        t.start();


    }

    // read all mgf files from root dir and all subfolders and fill the m_mgfFilesMap:
    // FolderPath (from root) --> [List of mgfFile]
    private void _updateFilesImpl() {

        m_mgfLoaded = false;
        m_mgfLoading = true;
        m_mgfFilesMap = new HashMap<>();

        if (m_rootDirectory != null) {
            FileUtils.iterateFiles(m_rootDirectory, new String[] {"mgf", "MGF"}, true).forEachRemaining(
                file -> {
                    //Use Linux like separator
                    String parentPath = file.getParentFile().getAbsolutePath();
                    if(parentPath.equals(m_rootDirectory.getAbsolutePath())) {
                        parentPath = ".";
                     } else {
                        parentPath = parentPath.substring(m_rootDirectory.getAbsolutePath().length() + 1);
                        parentPath = parentPath.replace(File.separator, "/");
                    }

                    ArrayList<File> allFiles = m_mgfFilesMap.getOrDefault(parentPath, new ArrayList<>());
                    allFiles.add(file);
                    m_mgfFilesMap.put(parentPath, allFiles);
                   // System.out.println(" - "+file.getAbsolutePath());
                }
            );
        }

        //Notify all listener map is loaded
        final HashMap<String, ArrayList<File>> mgfFilesMap = m_mgfFilesMap;
        for (MgfFilesListener listener : m_listeners ) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.mgfFilesMapLoaded(mgfFilesMap);
                }
            });

        }
        m_listeners.clear();

        m_mgfLoaded = true;
        m_mgfLoading = false;
    }

    public interface MgfFilesListener {
        void mgfFilesMapLoaded(HashMap<String, ArrayList<File>> map);
    }

}
