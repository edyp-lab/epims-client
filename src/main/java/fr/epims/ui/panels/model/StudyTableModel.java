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
import fr.edyp.epims.json.StudyJson;
import fr.epims.ui.common.DecoratedTableModelInterface;

import fr.epims.ui.common.RendererMouseCallback;
import fr.epims.ui.common.ViewTableCellRenderer;
import fr.epims.util.UtilDate;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;

/**
 *
 * Table Model
 *
 * @author JM235353
 *
 */
public class StudyTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_TITLE = 0;
    public static final int COLTYPE_NOMENCLATURE = 1;
    public static final int COLTYPE_DESCRIPTION = 2;
    public static final int COLTYPE_RESPONSABLE = 3;
    public static final int COLTYPE_DATE = 4;
    public static final int COLTYPE_STATUS = 5;


    private static final String[] m_columnNames = {"Title", "Nomenclature", "Description", "Owner", "Date", "Status"};
    private static final String[] m_columnTooltips = {"Titre", "Nomenclature", "Description of the Project", "Project Responsible", "Creation Date", "Project Status"};

    private ArrayList<StudyJson> m_studiesList = new ArrayList<>();

    private RendererMouseCallback m_callback;

    public StudyTableModel(RendererMouseCallback callback) {
        m_callback = callback;

    }


    public StudyJson getStudy(int row) {
        return m_studiesList.get(row);
    }


    public void setStudies(ArrayList<StudyJson> studiesList) {
        m_studiesList = studiesList;
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
        return m_studiesList.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        StudyJson s = m_studiesList.get(row);

        switch (col) {
            case COLTYPE_TITLE:
                return s.getTitle();
            case COLTYPE_NOMENCLATURE:
                return s.getNomenclatureTitle();
            case  COLTYPE_DESCRIPTION:
                return s.getDescription();
            case COLTYPE_RESPONSABLE:
                return DataManager.getNameFromActorKey(s.getActorKey());
            case COLTYPE_DATE:
                return UtilDate.dateToString(s.getCreationDate());
            case COLTYPE_STATUS:
                if (s.getClosingDate() == null) {
                    return "Open";
                } else {
                    return "Closed";
                }
        }
        return "";

    }

    public TableCellRenderer getRenderer(int row, int col) {

        if (col == 0) {
            return new ViewTableCellRenderer(m_callback, 0);
        }


        return null;
    }




}
