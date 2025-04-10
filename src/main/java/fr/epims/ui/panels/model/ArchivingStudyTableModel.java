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


import javax.swing.*;
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
public class ArchivingStudyTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_TITLE = 0;
    public static final int COLTYPE_PROJECT = 1;
    public static final int COLTYPE_DESCRIPTION = 2;
    public static final int COLTYPE_DATE = 4;


    private static final String[] m_columnNames = {"Title", "Project", "Description", "Date"};
    private static final String[] m_columnTooltips = {"Titre", "Project", "Description", "Date"};

    private ArrayList<StudyJson> m_studiesList = new ArrayList<>();

    public ArchivingStudyTableModel(JTable table) {


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
            case COLTYPE_PROJECT:
                return DataManager.getProject(s.getProjectId()).getTitle();
        }
        return "";

    }

    public TableCellRenderer getRenderer(int row, int col) {

        return null;
    }




}
