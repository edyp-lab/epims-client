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

import fr.edyp.epims.json.CellenOneRunJson;
import fr.edyp.epims.ui.common.DecoratedTableModelInterface;
import fr.edyp.epims.ui.renderers.IntegerTableCellRenderer;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for Runs in a CellenOne manipulation: | Name | Sources | Plates | Plates size | Samples Count |
 */
public class CellenOneRunTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_SOURCES = 1;
    public static final int COLTYPE_PLATES = 2;
    public static final int COLTYPE_PLATES_SIZE = 3;
    public static final int COLTYPE_SAMPLES_COUNT = 4;

    private static final String[] m_columnNames = {"Name", "Sources", "Plates", "Plates size", "Samples Count"};
    private static final String[] m_columnTooltips = {"Run Name", "Sources", "Plates", "Plates size", "Samples Count"};

    private List<CellenOneRunJson> m_runs = new ArrayList<>();

    public CellenOneRunTableModel() {
    }

    public void setRuns(List<CellenOneRunJson> runs) {
        m_runs = runs != null ? runs : new ArrayList<>();
        fireTableDataChanged();
    }

    public CellenOneRunJson getRunAt(int row) {
        if (row >= 0 && row < m_runs.size()) {
            return m_runs.get(row);
        }
        return null;
    }

    public void clear() {
        m_runs.clear();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return m_runs.size();
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
        if (col == COLTYPE_SAMPLES_COUNT) {
            return Integer.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        CellenOneRunJson run = m_runs.get(row);
        if (run == null) {
            return null;
        }
        switch (col) {
            case COLTYPE_NAME:
                return run.getName();
            case COLTYPE_SOURCES:
                return formatList(run.getSources());
            case COLTYPE_PLATES:
                return formatList(run.getPlates());
            case COLTYPE_PLATES_SIZE:
                return formatPlateSizes(run.getPlateSizes());
            case COLTYPE_SAMPLES_COUNT:
                return run.getSamplesCount();
        }
        return null;
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(", ", list);
    }

    private String formatPlateSizes(List<Point> plateSizes) {
        if (plateSizes == null || plateSizes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plateSizes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Point p = plateSizes.get(i);
            if (p != null) {
                sb.append(p.x).append("x").append(p.y);
            }
        }
        return sb.toString();
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
        if (col == COLTYPE_SAMPLES_COUNT) {
            return new IntegerTableCellRenderer();
        }
        return null;
    }
}
