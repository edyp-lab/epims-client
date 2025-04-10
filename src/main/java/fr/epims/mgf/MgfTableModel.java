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
import fr.epims.ui.common.DecoratedTableModelInterface;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * Model for the mgf table displayed in MgfPanel
 *
 * @author JM235353
 *
 */
public class MgfTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public static final int COLTYPE_STEP = 0;
    public static final int COLTYPE_DIRECTORY = 1;
    public static final int COLTYPE_MGF_FILE = 2;
    public static final int COLTYPE_STUDY = 3;
    public static final int COLTYPE_SIZE = 4;
    public static final int COLTYPE_FILE_DATE = 5;
    public static final int COLTYPE_FTP_DATE = 6;
    public static final int COLTYPE_ERROR = 7;
    private static final String[] columnNames = {"", "Directory", "MGF File", "Study", "Size", "File Date", "FTP Date", "Error Message"};

    private ArrayList<MgfFileInfo> m_mgfInfoList = new ArrayList<>();
    private ArrayList<MgfFileInfo> m_mgfInfoFilteredList = new ArrayList<>();

    private HashMap<String, MgfFileInfo> m_mgfInfoMap = new HashMap();

    private String m_subdirectoryFilter = "";
    private boolean m_all = true;
    private boolean m_unsavedMgf;
    private boolean m_transferredMgf;
    private boolean m_errorMgf;
    private boolean m_deletedMgf;

    private DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    public void setValues(ArrayList<MgfFileInfo> mgfInfoList) {
        m_mgfInfoList = mgfInfoList;
        m_mgfInfoMap.clear();
        for (MgfFileInfo mgfFileInfo : mgfInfoList) {
            m_mgfInfoMap.put(mgfFileInfo.getFile().getAbsolutePath(), mgfFileInfo);
        }

        MgfFileManager.getSingleton().getExtraInfo(m_mgfInfoMap);

        filter();
    }

    public MgfFileInfo getMgfFileInfo(int row) {
        return m_mgfInfoFilteredList.get(row);
    }

    public ArrayList<MgfFileInfo>  getMgfInfoList() {
        return m_mgfInfoList;
    }

    public HashMap<String, MgfFileInfo>  getMgfInfoMap() {
        return m_mgfInfoMap;
    }

    public void dataChanged(int row) {
        if (row>=getRowCount()) {
            return; // sanity check
        }
        fireTableRowsUpdated(row, row);
    }

    public void filter(String subdirectory, boolean all, boolean unsavedMgf, boolean transferredMgf, boolean errorMgf, boolean deletedMgf) {

        m_subdirectoryFilter = subdirectory;
        m_all = all;
        m_unsavedMgf = unsavedMgf;
        m_transferredMgf = transferredMgf;
        m_errorMgf = errorMgf;
        m_deletedMgf = deletedMgf;

        filter();
    }

    private void filter() {

        if (m_subdirectoryFilter.isEmpty() && m_all) {
            m_mgfInfoFilteredList = m_mgfInfoList;
            fireTableDataChanged();
            return;
        }

        m_mgfInfoFilteredList = new ArrayList<>();
        String directoryFilterUpper = m_subdirectoryFilter.toUpperCase();

        m_mgfInfoFilteredList = new ArrayList<>();
        for (MgfFileInfo mgfFileInfo : m_mgfInfoList) {

            boolean directoryOk = directoryFilterUpper.isEmpty();
            if (!directoryOk) {
                String directory = mgfFileInfo.getDirectory();
                String directoryUpper = directory.toUpperCase();
                if (directoryUpper.indexOf(directoryFilterUpper) != -1) {
                    directoryOk = true;
                }
            }
            if (!directoryOk) {
                continue;
            }

            if (m_all) {
                m_mgfInfoFilteredList.add(mgfFileInfo);
            } else {
                if ((m_unsavedMgf && mgfFileInfo.getStatus().equals(MgfFileInfo.StatusEnum.NO_INFO)) ||
                    (m_transferredMgf && mgfFileInfo.getStatus().equals(MgfFileInfo.StatusEnum.FTP_DONE)) ||
                        (m_errorMgf && mgfFileInfo.getStatus().equals(MgfFileInfo.StatusEnum.FAILED)) ||
                        (m_deletedMgf && mgfFileInfo.getStatus().equals(MgfFileInfo.StatusEnum.DELETED))) {
                    m_mgfInfoFilteredList.add(mgfFileInfo);
                }
            }



        }

        fireTableDataChanged();

    }

    public void reset() {
        setValues(new ArrayList<>());

    }

    public void dataChanged(final MgfFileInfo mgfFileInfo) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int row = m_mgfInfoFilteredList.indexOf(mgfFileInfo);
                if (row >=0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        });

    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_STEP:
                return MgfFileInfo.class;
            case COLTYPE_DIRECTORY:
            case COLTYPE_MGF_FILE:
            case COLTYPE_STUDY:
            case COLTYPE_SIZE:
            case COLTYPE_FILE_DATE:
            case COLTYPE_FTP_DATE:
            case COLTYPE_ERROR:
                return String.class;
        }
        return null; // should not happen
    }

    @Override
    public int getRowCount() {
        if (m_mgfInfoFilteredList == null) {
            return 0;
        }

        return m_mgfInfoFilteredList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        MgfFileInfo mgfFileInfo = m_mgfInfoFilteredList.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_STEP:
                return mgfFileInfo;
            case COLTYPE_DIRECTORY:
                return mgfFileInfo.getDirectory();
            case COLTYPE_MGF_FILE:
                return mgfFileInfo.getFile().getName();
            case COLTYPE_SIZE: {
                long bytes = mgfFileInfo.getFile().length();
                double mo = ((double) bytes) / 1000000;
                return  String.format("%.2f Mo", mo);
            }
            case COLTYPE_FILE_DATE: {
                try {
                    BasicFileAttributes attr = Files.readAttributes(mgfFileInfo.getFile().toPath(), BasicFileAttributes.class);
                    return  DATE_FORMAT.format(attr.lastModifiedTime().toMillis());
                } catch (java.io.IOException e) {

                }
                return "";
            }
            case COLTYPE_FTP_DATE: {
                Date transferDate = mgfFileInfo.getTransferDate();
                if (transferDate == null) {
                    return "";
                }
                return DATE_FORMAT.format(transferDate.getTime());
            }
            case COLTYPE_STUDY: {
                Integer id = mgfFileInfo.getStudyId();
                if ((id == null) || (id == -1)) {
                    if (mgfFileInfo.isStudySearched()) {
                        return "Study Not Found";
                    }
                    return "";
                }
                StudyJson studyJson = DataManager.getStudy(id);
                if (studyJson == null) {
                    return "";
                }
                return studyJson.getTitle();

            }
            case COLTYPE_ERROR:
                return mgfFileInfo.getErrorMessage();
        }

        return null; // should not happen
    }


    @Override
    public String getToolTipForHeader(int col) {
        return null;
    }

    @Override
    public TableCellEditor getEditor(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (col == COLTYPE_STEP) {
            return new MgfPanel.MgfFileInfoRenderer();
        }

        return null;
    }

}
