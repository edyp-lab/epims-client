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

package fr.epims.ui.panels.model;

import fr.edyp.epims.json.*;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.epims.tasks.ModifyProtocolApplicationTask;
import fr.epims.ui.common.DecoratedTableModelInterface;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * Table Model for Acquisitions
 *
 * @author JM235353
 *
 */
public class AcquisitionTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_SAMPLE = 1;
    public static final int COLTYPE_DATE = 2;
    public static final int COLTYPE_INSTRUMENT = 3;
    public static final int COLTYPE_DESCRIPTION = 4;
    public static final int COLTYPE_RESPONSIBLE = 5;



    private static final String[] m_columnNames = {"Name", "Sample", "Date", "Instrument", "Description", "Owner"};
    private static final String[] m_columnTooltips = {"Name", "Sample", "Date", "Instrument", "Description", "Owner"};


    private ArrayList<ProtocolApplicationJson> m_values = new ArrayList<>();

    private boolean m_editable;

    public AcquisitionTableModel(boolean editable) {

        m_editable = editable;
    }

    public void setValues(ArrayList<ProtocolApplicationJson> values) {
        m_values = values;
        fireTableDataChanged();
    }


    @Override
    public int getColumnCount() {
        return m_columnNames.length;

    }

    @Override
    public String getColumnName(int col) {

        return m_columnNames[col];
    }

    public String getToolTipForHeader(int col) {
        return m_columnTooltips[col];
    }

    @Override
    public TableCellEditor getEditor(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return null;
    }

    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_DATE) {
            return Date.class;
        }
        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_values.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        ProtocolApplicationJson p = m_values.get(row);
        switch (col) {
            case COLTYPE_NAME:
                return p.getName();
            case COLTYPE_SAMPLE:
                return p.getSampleKey();
            case COLTYPE_DATE:
                return p.getDate();
            case COLTYPE_INSTRUMENT:
                if (p.getAcquisitionJson() == null) {
                    return "";
                }
                InstrumentJson instrument = DataManager.getInstrument(p.getAcquisitionJson().getInstrumentId());
                if (instrument == null) {
                    return "";
                }
                return instrument.getName();
            case COLTYPE_DESCRIPTION:
                return p.getComment();
            case COLTYPE_RESPONSIBLE: {
                return DataManager.getNameFromActorKey(p.getActorKey());
            }
        }

        return null; // should not happen

    }

    @Override
    public boolean isCellEditable(int row, int col) {

        return (m_editable && (col == COLTYPE_DESCRIPTION));
    }


    @Override
    public void setValueAt(Object aValue, int row, int col) {
        ProtocolApplicationJson protocolApplicationJson = m_values.get(row);

        String comment = protocolApplicationJson.getComment();

        switch (col) {
            case COLTYPE_DESCRIPTION:
                comment = (String) aValue;
                if (comment==null) {
                    return;
                }
                break;
        }

        final String _comment = comment;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                setValueInDatabase(protocolApplicationJson, _comment);

            }
        });

    }

    private void setValueInDatabase(ProtocolApplicationJson protocolApplicationJson, String comment) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    protocolApplicationJson.setComment(comment);
                    fireTableDataChanged();

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }
            }
        };


        ProtocolApplicationJson newProtocolApplicationJson = new ProtocolApplicationJson(protocolApplicationJson.getId(), protocolApplicationJson.getSampleKey(), protocolApplicationJson.getSampleActorKey(), protocolApplicationJson.getActorKey(),
                protocolApplicationJson.getName(), protocolApplicationJson.getDate(), protocolApplicationJson.getComment(), protocolApplicationJson.getStudyId(), protocolApplicationJson.getAcquisitionJson(), protocolApplicationJson.getRunRobotJson(), protocolApplicationJson.getAliquotageJson(), protocolApplicationJson.getRank());

            ModifyProtocolApplicationTask task = new ModifyProtocolApplicationTask(callback, newProtocolApplicationJson);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


    }



}
