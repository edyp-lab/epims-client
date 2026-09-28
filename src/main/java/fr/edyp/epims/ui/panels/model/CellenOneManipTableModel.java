/*
 * Copyright (C) 2024
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

package fr.edyp.epims.ui.panels.model;

import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.ui.common.DecoratedTableModelInterface;
import fr.edyp.epims.util.UtilDate;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Table model for CellenOne Manipulations: | Name | Date | Information |
 */
public class CellenOneManipTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_DATE = 1;
    public static final int COLTYPE_INFORMATION = 2;

    private static final String[] m_columnNames = {"Name", "Date", "Information"};
    private static final String[] m_columnTooltips = {"CellenOne Manipulation Name", "Date", "Information"};

    private List<CellenOneManipJson> m_values = new ArrayList<>();

    public CellenOneManipTableModel() {
    }

    public void setValues(List<CellenOneManipJson> values) {
        m_values = values != null ? values : new ArrayList<>();
        fireTableDataChanged();
    }

    public CellenOneManipJson getManipAt(int row) {
        if (row >= 0 && row < m_values.size()) {
            return m_values.get(row);
        }
        return null;
    }

    public void clear() {
        m_values.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return m_values.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        CellenOneManipJson manip = m_values.get(row);
        if (manip == null) {
            return null;
        }
        switch (col) {
            case COLTYPE_NAME:
                return manip.getName();
            case COLTYPE_DATE:
                Date d = manip.getDate();
                return d != null ? UtilDate.dateToString(d) : "";
            case COLTYPE_INFORMATION:
                return manip.getInformation();
        }
        return null;
    }

    @Override
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
}
