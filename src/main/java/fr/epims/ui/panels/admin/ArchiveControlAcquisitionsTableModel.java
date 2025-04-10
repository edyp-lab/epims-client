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
import fr.epims.ui.common.DecoratedTableModelInterface;
import fr.epims.ui.renderers.IntegerTableCellRenderer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.HashSet;


/**
 *
 * Model for the Table containing archivable Control Acquisitions
 *
 * @author JM235353
 *
 */
public class ArchiveControlAcquisitionsTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_INSTRUMENT = 0;
    public static final int COLTYPE_YEAR = 1;
    public static final int COLTYPE_MONTH = 2;


    private static final String[] m_columnNames = {"Spectroscope", "Year", "Month"};
    private static final String[] m_columnTooltips = {"Spectroscope", "Year", "Month "};

    private ArrayList<ControlAcquisitionArchivableJson> m_controlAcquisitionsData;

    public ArchiveControlAcquisitionsTableModel(JTable table) {
        m_controlAcquisitionsData = new ArrayList<>();


    }

    public ControlAcquisitionArchivableJson getAcquisitionArchivable(int row) {
        return m_controlAcquisitionsData.get(row);
    }

    public void setData(ArrayList<ControlAcquisitionArchivableJson> controlAcquisitionsData) {
        m_controlAcquisitionsData = controlAcquisitionsData;

        fireTableDataChanged();
    }

    public void removeRows(HashSet<Integer> rowsToRemove) {
        ArrayList<ControlAcquisitionArchivableJson> controlAcquisitionsData = new ArrayList<>();
        int row = 0;
        for (ControlAcquisitionArchivableJson data : m_controlAcquisitionsData) {
            if (! rowsToRemove.contains(row)) {
                controlAcquisitionsData.add(data);
            }
            row++;
        }
        m_controlAcquisitionsData = controlAcquisitionsData;
        fireTableDataChanged();
    }

    public ControlAcquisitionArchivableJson getControlAcquisitionData(int index) {
        return m_controlAcquisitionsData.get(index);
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


    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_INSTRUMENT) {
            return String.class;
        }
        return Integer.class;
    }



    @Override
    public int getRowCount() {
        return m_controlAcquisitionsData.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        ControlAcquisitionArchivableJson data = m_controlAcquisitionsData.get(row);

        switch(col) {
            case COLTYPE_INSTRUMENT:
                return data.getInstrument();
            case COLTYPE_YEAR:
                return data.getYear();
            case COLTYPE_MONTH:
                return data.getMonth();
        }

        return null; // should not happen

    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (col == COLTYPE_YEAR) {
            return new IntegerTableCellRenderer();
        }
        if (col == COLTYPE_MONTH) {
            IntegerTableCellRenderer renderer = new IntegerTableCellRenderer();
            renderer.setMonthMode();
            return renderer;
        }
        return null;
    }


}