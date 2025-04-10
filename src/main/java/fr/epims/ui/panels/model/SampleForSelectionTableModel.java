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

import fr.edyp.epims.json.SampleJson;
import fr.epims.ui.common.DecoratedTableModelInterface;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * Table Model
 *
 * @author JM235353
 *
 */
public class SampleForSelectionTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_ORIGINAL_NAME = 1;
    public static final int COLTYPE_DESCRIPTION = 2;


    private static final String[] m_columnNames = {"Name", "Original Name", "Description"};
    private static final String[] m_columnTooltips = {"Name", "Original Name", "Description"};

    private ArrayList<SampleJson> m_samples;

    public SampleForSelectionTableModel(JTable table, ArrayList<SampleJson> samples) {

        m_samples = samples;


    }

    public SampleJson getSample(int index) {
        return m_samples.get(index);
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

        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_samples.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        SampleJson s = m_samples.get(row);

        switch(col) {
            case COLTYPE_NAME:
                return s.getName();
            case COLTYPE_ORIGINAL_NAME:
                return s.getOriginalName();
            case COLTYPE_DESCRIPTION:
                return s.getDescription();
        }

        return null; // should not happen

    }

    public void removeSamples(ArrayList<SampleJson> sampleArrayToRemove) {

        ArrayList<SampleJson> data = new ArrayList<>();

        for (SampleJson sampleJson : m_samples) {
            if (! sampleArrayToRemove.contains(sampleJson)) {
                data.add(sampleJson);
            }
        }

        m_samples = data;



        fireTableDataChanged();
    }

    public void addSamples(ArrayList<SampleJson> sampleArray) {


        for (SampleJson sample : sampleArray) {
            m_samples.add(sample);
        }

        Collections.sort(m_samples);


        fireTableDataChanged();
    }

}
