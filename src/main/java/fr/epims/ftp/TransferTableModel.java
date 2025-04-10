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

import fr.epims.ui.common.DecoratedTableModelInterface;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * Model for Table to display list of files downloaded or uploaded with their icon status
 *
 * @author JM235353
 *
 */
public class TransferTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public static final int COLTYPE_STEP = 0;
    public static final int COLTYPE_DIRECTION = 1;
    public static final int COLTYPE_SERVER_FILE = 2;
    public static final int COLTYPE_LOCAL_FILE = 3;
    private static final String[] columnNames = {"", "Upload/Download", "Server File", "Local File"};

    private ArrayList<TransferInfo> m_transferInfoList = new ArrayList<>();

    private String m_rootDirectoryName = null;

    public void updateData() {
        fireTableDataChanged();

    }

    public synchronized void addDownloadInfoList(ArrayList<TransferInfo> transferInfoList) {
        m_transferInfoList.addAll(transferInfoList);
        fireTableDataChanged();
    }

    public synchronized void setRootDirectoryName(String rootDirectoryName) {
        m_rootDirectoryName = rootDirectoryName;
    }

    public synchronized TransferInfo getFirstWaiting() {
        for (TransferInfo transferInfo : m_transferInfoList) {
            if (transferInfo.isWaiting()) {
                return transferInfo;
            }
        }
        return null;
    }

    public synchronized void removeFromDownloadList(HashSet<Integer> modelRowsToRemove) {
        boolean mustStopDownload = false;
        ArrayList<TransferInfo> transferInfoList = new ArrayList<>();
        for (int row = 0; row< m_transferInfoList.size(); row++) {
            if (! modelRowsToRemove.contains(Integer.valueOf(row))) {
                transferInfoList.add(m_transferInfoList.get(row));
            } else if (m_transferInfoList.get(row).isRunning()) {
                mustStopDownload = true;
            }
        }
        m_transferInfoList = transferInfoList;

        if (mustStopDownload) {
            FTPTransferThread.getTransferThread().stopDownload();
        }
        fireTableDataChanged();
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_STEP:
            case COLTYPE_DIRECTION:
                return TransferInfo.class;
            case COLTYPE_SERVER_FILE:
            case COLTYPE_LOCAL_FILE:
                return String.class;
        }
        return null; // should not happen
    }

    @Override
    public int getRowCount() {
        if (m_transferInfoList == null) {
            return 0;
        }

        return m_transferInfoList.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        TransferInfo transferInfo = m_transferInfoList.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_STEP:
            case COLTYPE_DIRECTION:
                return transferInfo;
            case COLTYPE_SERVER_FILE:
                String serverFile = transferInfo.getServerFile().getAbsolutePath();
                if (m_rootDirectoryName != null) {
                    int index = serverFile.indexOf(m_rootDirectoryName);
                    if (index != -1) {
                        serverFile = serverFile.substring(index);
                    }
                }
                return serverFile;
            case COLTYPE_LOCAL_FILE:
                return transferInfo.getLocalFile().getAbsolutePath();
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
            return new TransferInfoPanel.TransferInfoRenderer(true);
        }
        if (col == COLTYPE_DIRECTION) {
            return new TransferInfoPanel.TransferInfoRenderer(false);
        }
        return null;
    }


}