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

import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ProtocolApplicationJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.ui.common.DecoratedTableModelInterface;
import fr.epims.ui.common.RendererMouseCallback;
import fr.epims.ui.common.ViewTableCellRenderer;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;

/**
 *
 * Table Model for Acquisitions in the Search Table
 *
 * @author JM235353
 *
 */
public class AcquisitionSearchTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_SAMPLE = 1;
    public static final int COLTYPE_DATE = 2;
    public static final int COLTYPE_INSTRUMENT = 3;
    public static final int COLTYPE_DESCRIPTION = 4;
    public static final int COLTYPE_STUDY = 5;
    public static final int COLTYPE_RESPONSIBLE = 6;



    private static final String[] m_columnNames = {"Name", "Sample", "Date", "Instrument", "Description", "Study", "Owner"};
    private static final String[] m_columnTooltips = {"Name", "Sample", "Date", "Instrument", "Description", "Study", "Owner"};

    private RendererMouseCallback m_callbackName;
    private RendererMouseCallback m_callbackStudy;

    private ArrayList<ProtocolApplicationJson> m_values = new ArrayList<>();

    public AcquisitionSearchTableModel(JTable table, RendererMouseCallback callbackName, RendererMouseCallback callbackStudy) {
        m_callbackName = callbackName;
        m_callbackStudy = callbackStudy;

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



    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
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
                return UtilDate.dateToString(p.getDate());
            case COLTYPE_INSTRUMENT:
                if (p.getAcquisitionJson() == null) {
                    return "";
                }
                return DataManager.getInstrument(p.getAcquisitionJson().getInstrumentId()).getName();
            case COLTYPE_DESCRIPTION:
                return p.getComment();
            case COLTYPE_STUDY: {
                StudyJson study = DataManager.getStudy(p.getStudyId());
                return (study != null) ? study.getTitle() : "";
            }
            case COLTYPE_RESPONSIBLE: {
                return DataManager.getNameFromActorKey(p.getSampleActorKey());
            }
        }

        return null; // should not happen

    }

    public StudyJson getStudy(int row) {
        ProtocolApplicationJson p = m_values.get(row);
        return DataManager.getStudy(p.getStudyId());

    }

    public ProtocolApplicationJson getProtocolApplicationJson(int row) {
        return m_values.get(row);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (col == COLTYPE_NAME) {
            return new ViewTableCellRenderer(m_callbackName, col);
        }
        if (col == COLTYPE_STUDY) {
            return new ViewTableCellRenderer(m_callbackStudy, col);
        }

        return null;
    }






}
