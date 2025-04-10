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
import fr.edyp.epims.json.RobotPlanningJson;
import fr.edyp.epims.json.SampleJson;
import fr.epims.ui.common.DecoratedTableModelInterface;
import fr.epims.ui.panels.robot.RobotRequestPanel;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 *
 * Table Model
 *
 * @author JM235353
 *
 */
public class RobotRequestModel  extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_SAMPLE = 0;
    public static final int COLTYPE_LOAD_COUNT = 1;
    public static final int COLTYPE_PROTEINS_QUANTITY = 2;
    public static final int COLTYPE_TRYPSIN_QUANTITY = 3;
    public static final int COLTYPE_COMMENTARY = 4;




    private static final String[] m_columnNames = {"Sample", "Load Count", "Proteins Quantity (µg)", "Trypsin Quantity (µg)", "Commentary"};
    private static final String[] m_columnTooltips =  {"Sample", "Load Count", "Proteins Quantity (µg)", "Trypsin Quantity (µg)", "Commentary"};
    private RobotRequestPanel.RobotRequestTable m_table;

    private boolean m_volumeSelected = true;


    private ArrayList<RobotRequestInfo> m_data = new ArrayList<>();

    private String m_defaultCommentary = "";
    private Integer m_defaultLoadCount = null;
    private Float m_defaultProteinQuantity = null;
    private Float m_defaultTrypsinQuantity = null;

    public RobotRequestModel(RobotRequestPanel.RobotRequestTable table, ArrayList<SampleJson> sampleArray) {

        m_table = table;

        addSamples(sampleArray);
    }


    public ArrayList<RobotPlanningJson> getRobotPlannings() {

        ArrayList<RobotPlanningJson> robotPlanningList = new ArrayList<>();

        Date today = new Date();

        for (RobotRequestInfo request : m_data) {
            RobotPlanningJson robotPlanning = new RobotPlanningJson(-1, DataManager.getLoggedUser(), request.getSample(),
                    request.getTrypsinQuantity(), request.getProteinQuantity(), null, today, request.getLoadCount(), request.getCommentary(),
                    null, null, null  );
            robotPlanningList.add(robotPlanning);
        }

        return robotPlanningList;


    }

    public SampleJson getSample(int index) {

        return m_data.get(index).getSample();
    }

    public void addSamples(ArrayList<SampleJson> sampleArray) {


        for (int i = 0; i< sampleArray.size(); i++) {

            RobotRequestInfo robotRequestInfo = new RobotRequestInfo(sampleArray.get(i));

            if (!m_defaultCommentary.isEmpty()) {
                robotRequestInfo.setCommentary(m_defaultCommentary);
            }
            if (m_defaultLoadCount != null) {
                robotRequestInfo.setLoadCount(m_defaultLoadCount);
            }
            if (m_defaultProteinQuantity != null) {
                robotRequestInfo.setProteinQuantity(m_defaultProteinQuantity);
            }
            if (m_defaultTrypsinQuantity != null) {
                robotRequestInfo.setTrypsinQuantity(m_defaultTrypsinQuantity);
            }

            m_data.add(robotRequestInfo);
        }

        Collections.sort(m_data);


        fireTableDataChanged();
    }

    public void removeSamples(ArrayList<SampleJson> sampleArrayToRemove) {

        ArrayList<RobotRequestInfo> data = new ArrayList<>();

        for (RobotRequestInfo robotRequestInfo : m_data) {
            if (! sampleArrayToRemove.contains(robotRequestInfo.getSample())) {
                data.add(robotRequestInfo);
            }
        }

        m_data = data;



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

        switch (col)  {
            case COLTYPE_SAMPLE:
            case COLTYPE_COMMENTARY:
                return String.class;
            case COLTYPE_LOAD_COUNT:
                return Integer.class;
            case COLTYPE_PROTEINS_QUANTITY:
            case COLTYPE_TRYPSIN_QUANTITY:
                return Float.class;

        }


        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_data.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        RobotRequestInfo robotRequestInfo = m_data.get(row);

        switch (col)  {
            case COLTYPE_SAMPLE:
                return robotRequestInfo.getSample().getName();
            case COLTYPE_COMMENTARY:
                return robotRequestInfo.getCommentary();
            case COLTYPE_LOAD_COUNT:
                return robotRequestInfo.getLoadCount();
            case COLTYPE_PROTEINS_QUANTITY:
                return robotRequestInfo.getProteinQuantity();
            case COLTYPE_TRYPSIN_QUANTITY:
                return robotRequestInfo.getTrypsinQuantity();

        }

        return null; // should never happen

    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == COLTYPE_SAMPLE) {
            return false;
        }
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {


        RobotRequestInfo robotRequestInfo = m_data.get(row);

        switch (col)  {
            case COLTYPE_SAMPLE:
                // should not happen
                break;
            case COLTYPE_COMMENTARY:
                robotRequestInfo.setCommentary((String) aValue);
                break;
            case COLTYPE_LOAD_COUNT:
                robotRequestInfo.setLoadCount((Integer) aValue);
                break;
            case COLTYPE_PROTEINS_QUANTITY:
                robotRequestInfo.setProteinQuantity((Float) aValue);
                break;
            case COLTYPE_TRYPSIN_QUANTITY:
                robotRequestInfo.setTrypsinQuantity((Float) aValue);
                break;

        }


    }


    public void setCommentary(String commentary) {
        m_defaultCommentary = commentary;

        for (int i=0;i<m_data.size();i++) {
            m_data.get(i).setCommentary(commentary);
        }

        fireTableDataChanged();
    }

    public void setLoadCount(Integer v) {
        m_defaultLoadCount = v;

        for (int i=0;i<m_data.size();i++) {
            m_data.get(i).setLoadCount(v);
        }

        fireTableDataChanged();
    }

    public void setProteinQuantity(Float v) {
        m_defaultProteinQuantity = v;

        for (int i=0;i<m_data.size();i++) {
            m_data.get(i).setProteinQuantity(v);
        }

        fireTableDataChanged();
    }

    public void setTrypsinQuantity(Float v) {
        m_defaultTrypsinQuantity = v;

        for (int i=0;i<m_data.size();i++) {
            m_data.get(i).setTrypsinQuantity(v);
        }

        fireTableDataChanged();
    }






    public class RobotRequestInfo implements Comparable<RobotRequestInfo> {

        private SampleJson m_sample;

        private Integer m_loadCount = null;
        private Float m_proteinQuantity = null;
        private Float m_trypsinQuantity = null;
        private String m_commentary = "";

        public RobotRequestInfo(SampleJson s) {

            m_sample = s;

        }

        public SampleJson getSample() {
            return m_sample;
        }

        public Integer getLoadCount() {
            return m_loadCount;
        }

        public Float getProteinQuantity() {
            return m_proteinQuantity;
        }

        public Float getTrypsinQuantity() {
            return m_trypsinQuantity;
        }

        public String getCommentary() {
            return m_commentary;
        }

        public void setLoadCount(Integer loadCount) {
            m_loadCount = loadCount;
        }

        public void setProteinQuantity(Float proteinQuantity) {
            m_proteinQuantity = proteinQuantity;
        }

        public void setTrypsinQuantity(Float trypsinQuantity) {
            m_trypsinQuantity = trypsinQuantity;
        }

        public void setCommentary(String commentary) {
            m_commentary = commentary;
        }


        @Override
        public int compareTo(RobotRequestInfo o) {
            return m_sample.compareTo(o.getSample());
        }
    }

}
