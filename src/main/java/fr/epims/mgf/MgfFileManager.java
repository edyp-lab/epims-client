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

import fr.edyp.epims.json.StudyJson;
import fr.epims.dataaccess.DataManager;
import fr.epims.preferences.EpimsPreferences;
import fr.epims.preferences.PreferencesKeys;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
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

    private static String TRANSFERED_FILE_NAME = "transferedMgfFiles.db";

    private HashMap<String, ArrayList<File>> m_mgfFilesMap;
    private boolean m_mgfLoaded = false;
    private boolean m_mgfLoading = false;

    private ArrayList<MgfFilesListener> m_listeners = new ArrayList<>();

    private static final Format FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static MgfFileManager getSingleton() {
        if (m_singleton == null) {
            m_singleton = new MgfFileManager();
        }
        return m_singleton;
    }

    private File m_rootDirectory;
    private FilenameFilter m_mgfFilter;

    private MgfFileManager() {

        Preferences preferences = EpimsPreferences.root();
        //Use this Path as default on Process PC...
        String defaultDirString = preferences.get(PreferencesKeys.MGF_ROOT_DIR, "D:\\Data\\MGF");
        LoggerFactory.getLogger("Epims.Client").info("Read mgf files from "+defaultDirString);
        m_rootDirectory = (defaultDirString!= null) ? new File(defaultDirString) : null;
        if (! m_rootDirectory.exists() || ! m_rootDirectory.isDirectory()) {
            m_rootDirectory = null;
        }


        m_mgfFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                return fileName.endsWith(".mgf");
            }
        };
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

        Preferences preferences = EpimsPreferences.root();
        preferences.put(PreferencesKeys.MGF_ROOT_DIR, rootDirectory.getAbsolutePath());
        try {
            preferences.flush();
        } catch (Exception prefException) {
            LoggerFactory.getLogger("Epims.Client").error(" Error writing preferences for mgf root directory ", prefException);
        }

    }


    public synchronized void getExtraInfo(HashMap<String, MgfFileInfo> mgfFileInfoArrayMap) {
        if (m_rootDirectory == null) {
            // Clear data
            return;
        }

        File tranferredFile = new File(m_rootDirectory.getAbsolutePath()+"\\"+TRANSFERED_FILE_NAME);

        if (! tranferredFile.exists() || tranferredFile.isDirectory()) {
            // Clear data
            return;
        }
        LoggerFactory.getLogger("Epims.Client").debug("Read mgf files Cache information  from "+tranferredFile.getAbsolutePath());
        // parse file
        try (BufferedReader br = new BufferedReader(new FileReader(tranferredFile))) {
            String line;
            while ((line = br.readLine()) != null) {

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
        File tranferredFile = new File(m_rootDirectory.getAbsolutePath()+"\\"+TRANSFERED_FILE_NAME);

        try {
            FileWriter fw = new FileWriter(tranferredFile);
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
                fw.write('\n');
            }
            fw.flush();
            fw.close();
        } catch (java.io.IOException e) {

        }
    }



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

    private void _updateFilesImpl() {

        m_mgfLoaded = false;
        m_mgfLoading = true;

        m_mgfFilesMap = new HashMap();

        if (m_rootDirectory != null) {
            File[] files = m_rootDirectory.listFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    continue;
                }

                ArrayList<File> mgfFilesArrayList = new ArrayList<>();
                File[] mgfFiles = lookForMgfInDirectory(f);
                if (mgfFiles !=null) {
                    for (File mgfFile : mgfFiles) {
                        mgfFilesArrayList.add(mgfFile);
                    }
                    m_mgfFilesMap.put(f.getName(), mgfFilesArrayList);
                }


            }

        }

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

    private File[] lookForMgfInDirectory(File directory) {
        File[] mgfFiles = directory.listFiles(m_mgfFilter);
        return mgfFiles;
    }


    public interface MgfFilesListener {
        public void mgfFilesMapLoaded(HashMap<String, ArrayList<File>> map);
    }

}
