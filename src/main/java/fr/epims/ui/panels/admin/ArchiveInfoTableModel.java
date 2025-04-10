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


package fr.epims.ui.panels.admin;

import fr.edyp.epims.json.ControlAcquisitionArchivableJson;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.json.ArchivingInfoJson;
import fr.epims.ui.common.DecoratedTableModelInterface;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * Table Model for the table which displays the list of studies and control acquisitions being archived
 * in different state (finish, error, running, waiting)
 *
 * @author JM235353
 *
 */
public class ArchiveInfoTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public static final int COLTYPE_STEP = 0;
    public static final int COLTYPE_ARCHIVE_NAME = 1;
    public static final int COLTYPE_ERROR_MESSAGE = 2;
    private static final String[] columnNames = {"", "Archive", "Error Status"};

    private ArrayList<ArchivingInfoJson> m_archiveInfoList = new ArrayList<>();

    private boolean m_willBeUpdated = false;

    public void updateData() {
        fireTableDataChanged();

    }

    public synchronized HashSet<String> getArchiveInfoKey(int[] rows) {

        HashSet<String> keySet = new HashSet<>();
        for (int row : rows) {
            ArchivingInfoJson archivingInfoJson = m_archiveInfoList.get(row);
            keySet.add(archivingInfoJson.toString());
        }

        return keySet;
    }

    public synchronized ArrayList<Integer> getSelectedModelRows(HashSet<String> keySet) {

        ArrayList<Integer> rowsSelectedInModel = new ArrayList<>();
        for (int i = 0;i<m_archiveInfoList.size();i++) {
            String key = m_archiveInfoList.get(i).toString();
            if (keySet.contains(key)) {
                rowsSelectedInModel.add(i);
            }
        }

        return rowsSelectedInModel;
    }

    public synchronized void replaceArchiveInfoList(LinkedList<ArchivingInfoJson> archiveInfoList) {
        m_archiveInfoList.clear();
        m_archiveInfoList.addAll(archiveInfoList);
        updateWillBeUpdated();
        fireTableDataChanged();
    }

    public synchronized void addArchiveInfoList(ArrayList<ArchivingInfoJson> archiveInfoList) {
        m_archiveInfoList.addAll(archiveInfoList);
        updateWillBeUpdated();
        fireTableDataChanged();
    }

    private void updateWillBeUpdated() {
        boolean willBeUpdated = false;
        for (ArchivingInfoJson archivingInfoJson :m_archiveInfoList) {
            if (archivingInfoJson.isWaiting() || archivingInfoJson.isRunning()) {
                willBeUpdated = true;
                break;
            }
        }
        m_willBeUpdated = willBeUpdated;
    }

    public synchronized boolean willBeUpdated() {
        return m_willBeUpdated;
    }


    public synchronized LinkedList<ArchivingInfoJson> dataToArchive() {
        LinkedList<ArchivingInfoJson> list = new LinkedList<>();
        for (ArchivingInfoJson archiveInfo : m_archiveInfoList) {
            list.add(archiveInfo);
        }
        return list;
    }



    public synchronized void removeFromArchiveList(HashSet<Integer> modelRowsToRemove) {
        boolean mustStopDownload = false;
        ArrayList<ArchivingInfoJson> archiveInfoList = new ArrayList<>();
        for (int row = 0; row < m_archiveInfoList.size(); row++) {
            if (!modelRowsToRemove.contains(Integer.valueOf(row))) {
                archiveInfoList.add(m_archiveInfoList.get(row));
            } else if (m_archiveInfoList.get(row).isRunning()) {
                mustStopDownload = true;
            }
        }
        m_archiveInfoList = archiveInfoList;

        /*if (mustStopDownload) {
            ArchiveThread.getArchiveThread().stopDownload();
        }*/
        fireTableDataChanged();
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_STEP:
                return ArchivingInfoJson.class;
            case COLTYPE_ARCHIVE_NAME:
            case COLTYPE_ERROR_MESSAGE:
                return String.class;
        }
        return null; // should not happen
    }

    @Override
    public int getRowCount() {
        if (m_archiveInfoList == null) {
            return 0;
        }

        return m_archiveInfoList.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        ArchivingInfoJson archiveInfo = m_archiveInfoList.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_STEP:
                return archiveInfo;
            case COLTYPE_ARCHIVE_NAME: {
                StudyJson s = archiveInfo.getStudyJson();
                if (s != null) {
                    return "Study: "+s.getTitle();
                } else {
                    ControlAcquisitionArchivableJson archivableAcquisition = archiveInfo.getControlAcquisitionArchivableJson();
                    return "Acquisition: "+archivableAcquisition.toString();
                }

            }
            case COLTYPE_ERROR_MESSAGE: {
                return archiveInfo.getMessage();
            }
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
            return new ArchiveInfoPanel.ArchiveInfoRenderer();
        }

        return null;
    }
}


