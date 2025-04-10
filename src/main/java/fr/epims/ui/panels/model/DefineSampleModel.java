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
import fr.edyp.epims.json.BiologicOriginJson;
import fr.edyp.epims.json.SampleJson;
import fr.epims.ui.common.*;


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Table Model
 *
 * @author JM235353
 *
 */
public class DefineSampleModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_NOMENCLATURE = 0;
    public static final int COLTYPE_SOURCE_NAME = 1;
    public static final int COLTYPE_VOLUME = 2;
    public static final int COLTYPE_QUANTITY = 3;
    public static final int COLTYPE_DESCRIPTION = 4;


    private static final String[] m_columnNames = {"Nomenclature", "Source Name", "Volume (µl)", "Quantity (ng)", "Description"};
    private static final String[] m_columnTooltips =  {"Nomenclature", "Source Name", "Volume (µl)", "Quantity (ng)", "Description"};


    private SampleJson[] m_values = new SampleJson[1];


    private String m_defaultDescription = "";
    private String m_defaultSourceName = "";
    private Float m_defaultVolume = null;
    private Float m_defaultQuantity = null;
    private String m_defaultSuffix = null;

    private String m_prefix;
    private int m_studyId;

    private HashMap<Integer, String> m_nameEditedByUserMap = new HashMap<>();


    private HashSet<String> m_takenSampleKeys;

    public DefineSampleModel(JTable table, String prefix, int studyId, HashSet<String> takenSampleKeys) {

        m_prefix = prefix;
        m_studyId = studyId;

        m_takenSampleKeys = takenSampleKeys;

        int index = firstAvailableIndex();
        for (int i = 0; i< m_values.length; i++) {

            String name = prefix+"_"+index;

            SampleJson s = new SampleJson(name, DataManager.getLoggedUser(), null, studyId,
                    "", null, "Available", null,
                    null, null, null, null, null);

            m_values[i] = s;
        }

    }

    private int firstAvailableIndex() {

        int valueMin = 0;
        String nameWithoutDigits = (m_defaultSuffix != null) ? m_prefix+"_"+m_defaultSuffix+"_" : m_prefix+"_";
        for (String key : m_takenSampleKeys) {

            if (key.startsWith(nameWithoutDigits)) {

                int indexNumber = nameWithoutDigits.length();
                if (indexNumber<key.length()) {
                    String number = key.substring(indexNumber);
                    while (number.startsWith("0")) {
                        number = number.substring(1);
                    }
                    try {
                        int value = Integer.parseInt(number);
                        if (value>valueMin) {
                            valueMin = value;
                        }
                    } catch (Exception e) {
                        // could happen for specific sample name filled by user
                    }
                }
            }
        }
        return valueMin+1;
    }

    public ArrayList<SampleJson> getSamplesToCreate(BiologicOriginJson biologicOriginJson) {

        Date d = new Date();

        ArrayList<SampleJson> sampleJsonsList = new ArrayList<>();
        for (SampleJson s : m_values ) {
            s.setCreationDate(d);
            s.setBiologicOrigin(biologicOriginJson);

            sampleJsonsList.add(s);
        }

        return sampleJsonsList;
    }


    private void renameAll(boolean forceEditedValues) {
        int index = firstAvailableIndex();

        String nameWithoutDigits = (m_defaultSuffix != null) ? m_prefix+"_"+m_defaultSuffix+"_" : m_prefix+"_";
        int nbDigitsAutoIncrement = (int)(Math.log10(m_values.length+index-1)+1);

        for (int i=0;i<m_values.length;i++) {
            if (! m_nameEditedByUserMap.containsKey(i) || forceEditedValues) {
                m_nameEditedByUserMap.remove(i);
                String digit = String.valueOf(index);
                while (digit.length()<nbDigitsAutoIncrement) {
                    digit = "0"+digit;
                }
                m_values[i].setName(nameWithoutDigits+digit);

            }
            index++;
        }

        fireTableRowsUpdated(0, m_values.length-1);
    }

    public void setNbSamples(int nbSamples) {
        int previousNb = m_values.length;

        if (previousNb < nbSamples) {
            SampleJson[] values = new SampleJson[nbSamples];
            for (int i=0;i<previousNb;i++) {
                values[i] = m_values[i];
            }


            for (int i=previousNb;i<nbSamples;i++) {
                values[i] = new SampleJson(m_prefix+"_", DataManager.getLoggedUser(), null, m_studyId,
                        "", null, "Available", null,
                        null, null, null, null, null);



                if (m_defaultVolume != null) {
                    values[i].setVolume(m_defaultVolume);
                }
                if (m_defaultQuantity != null) {
                    values[i].setQuantity(m_defaultQuantity);
                }
                if (!m_defaultDescription.isEmpty()) {
                    values[i].setDescription(m_defaultDescription);
                }
                if (!m_defaultSourceName.isEmpty()) {
                    values[i].setOriginalName(m_defaultSourceName);
                }

            }

            m_values = values;

            fireTableRowsInserted(previousNb, nbSamples-1);

        } else if (previousNb > nbSamples) {
            SampleJson[] values = new SampleJson[nbSamples];
            for (int i=0;i<nbSamples;i++) {
                values[i] = m_values[i];
            }
            for (int i=nbSamples;i<previousNb;i++) {
                m_nameEditedByUserMap.remove(i);
            }

            m_values = values;
            fireTableRowsDeleted (nbSamples, previousNb-1);
        }

        renameAll(false);


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

        switch (col)  {
            case COLTYPE_NOMENCLATURE:
            case COLTYPE_SOURCE_NAME:
            case COLTYPE_DESCRIPTION:
                return null; // default renderer
            case COLTYPE_VOLUME:
            case COLTYPE_QUANTITY:
                return null; // default renderer
            //case COLTYPE_RADIOACTIVITY:
            //case COLTYPE_TOXICITY:
            //    return null;


        }

        return null;
    }

    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {

        switch (col)  {
            case COLTYPE_NOMENCLATURE:
            case COLTYPE_SOURCE_NAME:
            case COLTYPE_DESCRIPTION:
                return String.class;
            case COLTYPE_VOLUME:
            case COLTYPE_QUANTITY:
                return Float.class;

        }

        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_values.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        SampleJson s = m_values[row];

        switch (col)  {
            case COLTYPE_NOMENCLATURE:
                return s.getName();
            case COLTYPE_SOURCE_NAME:
                return s.getOriginalName();
            case COLTYPE_DESCRIPTION:
                return s.getDescription();
            case COLTYPE_VOLUME: {
                return s.getVolume();
            }
            case COLTYPE_QUANTITY: {
                return s.getQuantity();
            }

        }

        return null; // should never happen

    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col)
    {
        SampleJson s = m_values[row];

        switch (col)  {
            case COLTYPE_NOMENCLATURE: {
                m_nameEditedByUserMap.put(row, aValue.toString().trim());
                s.setName(aValue.toString().trim());
                break;
            }
            case COLTYPE_SOURCE_NAME:
                s.setOriginalName(aValue.toString());
                break;
            case COLTYPE_DESCRIPTION:
                s.setDescription(aValue.toString());
                break;
            case COLTYPE_VOLUME:
                s.setVolume((Float)aValue);
                break;
            case COLTYPE_QUANTITY:
                s.setQuantity((Float)aValue);
                break;


        }
    }

    public void setName(String suffix, boolean forceEditedValues) {
        m_defaultSuffix = suffix;

        renameAll(forceEditedValues);
    }

    public boolean isNameEditedByUser() {
        return ! m_nameEditedByUserMap.isEmpty();
    }

    public void setVolume(Float v) {
        m_defaultVolume = v;

        for (int i=0;i<m_values.length;i++) {
            m_values[i].setVolume(v);
        }

        fireTableDataChanged();
    }


    public void setQuantity(Float v) {
        m_defaultQuantity = v;

        for (int i=0;i<m_values.length;i++) {
            m_values[i].setQuantity(v);
        }

        fireTableDataChanged();
    }

    public void setDescription(String description) {
        m_defaultDescription = description;

        for (int i=0;i<m_values.length;i++) {
            m_values[i].setDescription(description);
        }

        fireTableDataChanged();
    }

    public void setSourceName(String sourceName) {
        m_defaultSourceName = sourceName;

        for (int i=0;i<m_values.length;i++) {
            m_values[i].setOriginalName(sourceName);
        }

        fireTableDataChanged();
    }


    public void setSamples(ArrayList<String> samplesNamesArrayList) {

        int nbSamples = samplesNamesArrayList.size();
        m_values = new SampleJson[nbSamples];
        int i = 0;
        for (String name : samplesNamesArrayList) {
            m_values[i] = new SampleJson(name, DataManager.getLoggedUser(), null, m_studyId,
                    "", null, "Available", null,
                    null, null, null, null, null);

            m_nameEditedByUserMap.put(i ,name);

            if (m_defaultVolume != null) {
                m_values[i].setVolume(m_defaultVolume);
            }
            if (m_defaultQuantity != null) {
                m_values[i].setQuantity(m_defaultQuantity);
            }
            if (!m_defaultDescription.isEmpty()) {
                m_values[i].setDescription(m_defaultDescription);
            }
            i++;
        }


        fireTableDataChanged();

    }

    public String getSamplesText() {
        StringBuilder sb = new StringBuilder();
        for (SampleJson sample : m_values) {
            sb.append(sample.getName()).append("\n");
        }
        return sb.toString();
    }


}
