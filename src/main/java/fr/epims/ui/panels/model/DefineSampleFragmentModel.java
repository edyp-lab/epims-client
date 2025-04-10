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


import fr.edyp.epims.json.FragmentToCreateJson;
import fr.edyp.epims.json.FragmentsGroupToCreateJson;
import fr.edyp.epims.json.SampleJson;
import fr.epims.ui.common.DecoratedTableModelInterface;
import fr.epims.ui.panels.CreateFragmentsPanel;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Table Model
 *
 * @author JM235353
 *
 */
public class DefineSampleFragmentModel  extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_PARENT_SAMPLE = 0;
    public static final int COLTYPE_FRAGMENT_NAME = 1;
    public static final int COLTYPE_VOLUME_OR_QUANTITY = 2;
    public static final int COLTYPE_DESCRIPTION = 3;


    private static final String[] m_columnNames = {"Parent", "Fragment Name", "Volume (µl)" /* OR "Quantity (ng)"*/, "Description"};
    private static final String[] m_columnTooltips =  {"Parent", "Fragment Name", "Volume (µl)" /* OR "Quantity (ng)"*/, "Description"};

    private CreateFragmentsPanel.FragmentTable m_table;

    private int m_nbFragments;

    private boolean m_volumeSelected = true;


    private FragmentsInfoForSample[] m_data = null;

    private Float m_defaultVolume = null;
    private Float m_defaultQuantity = null;
    private String m_defaultDescription = "";

    private String m_defaultFragmentInfix = "_Frag_";

    private HashMap<SampleJson, FragmentsInfoForSample> m_map = new HashMap<>();

    private HashSet<String> m_takenSampleKeys;

    public DefineSampleFragmentModel(CreateFragmentsPanel.FragmentTable table, ArrayList<SampleJson> sampleArray, int nbFragments, HashSet<String> takenSampleKeys) {

        m_table = table;
        m_nbFragments = nbFragments;

        m_takenSampleKeys = takenSampleKeys;

        addSamples(sampleArray);
    }

    public void setFragmentInfix(String fragmentInfix) {
        m_defaultFragmentInfix = fragmentInfix;

        for (FragmentsInfoForSample data : m_data) {
            data.changeFragmentInfix();
        }

        fireTableDataChanged();
    }

    public SampleJson getSample(int index) {
        int sampleIndex = index / m_nbFragments;
        FragmentsInfoForSample sampleInfo = m_data[sampleIndex];
        return sampleInfo.getParentSample();
    }

    private int firstAvailableIndex(String nameWithoutDigits) {

        int valueMin = 0;
        for (String key : m_takenSampleKeys) {

            if (key.startsWith(nameWithoutDigits)) {
                int value = 0;
                int indexNumber = nameWithoutDigits.length();
                if (indexNumber<key.length()) {
                    String number = key.substring(indexNumber);
                    while (number.startsWith("0")) {
                        number = number.substring(1);
                    }
                    try {
                        value = Integer.parseInt(number);
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

    private void renameAll() {

        for (int i=0;i<m_data.length;i++) {
            FragmentsInfoForSample fragmentInfo = m_data[i];
            String nameWithoutDigits = fragmentInfo.getParentSample().getName()+m_defaultFragmentInfix;
            int index = firstAvailableIndex(nameWithoutDigits);
            int nbDigitsAutoIncrement = (int)(Math.log10(m_nbFragments+index-1)+1);
            for (int j=0;j<m_nbFragments;j++) {
                String digit = String.valueOf(index);
                while (digit.length()<nbDigitsAutoIncrement) {
                    digit = "0"+digit;
                }
                fragmentInfo.setFragmentDefault(j, digit);

                index++;
            }

        }
        if (m_data.length>0) {
            fireTableRowsUpdated(0, getRowCount() - 1);
        }
    }

    public FragmentsGroupToCreateJson getFragmentsToCreate(String comment) {

        HashMap<String, ArrayList<FragmentToCreateJson>> parentToFragmentsMap = new HashMap<>();

        for (int i=0;i<m_data.length;i++) {
            FragmentsInfoForSample fragmentInfo = m_data[i];
            String parentKey = fragmentInfo.getParentSample().getName();


            ArrayList<FragmentToCreateJson> fragmentToCreateJsonList = new ArrayList<>();
            parentToFragmentsMap.put(parentKey, fragmentToCreateJsonList);
            for (int j=0;j<m_nbFragments;j++) {
                FragmentToCreateJson fragmentToCreateJson = new FragmentToCreateJson();
                fragmentToCreateJson.setParentSampleKey(parentKey);
                fragmentToCreateJson.setName(fragmentInfo.getFragmentName(j));
                fragmentToCreateJson.setVolume(m_volumeSelected ? fragmentInfo.getVolumeOrQuantity(j) : null);
                fragmentToCreateJson.setQuantity(!m_volumeSelected ? fragmentInfo.getVolumeOrQuantity(j) : null);
                fragmentToCreateJson.setDescription(fragmentInfo.getDescription(j));
                fragmentToCreateJson.setComment(comment);
                fragmentToCreateJsonList.add(fragmentToCreateJson);
            }


        }


        FragmentsGroupToCreateJson fragmentsGroup = new FragmentsGroupToCreateJson();
        fragmentsGroup.setParentToFragmentsMap(parentToFragmentsMap);

        return fragmentsGroup;
    }

    public void addSamples(ArrayList<SampleJson> sampleArray) {

        FragmentsInfoForSample[] data = new FragmentsInfoForSample[sampleArray.size()];

        for (int i = 0; i< data.length; i++) {

            data[i] = m_map.get(sampleArray.get(i));
            if (data[i] == null) {
                data[i] = new FragmentsInfoForSample(sampleArray.get(i), m_nbFragments);
                m_map.put(sampleArray.get(i), data[i]);
            }


            if (!m_defaultDescription.isEmpty()) {
                data[i].setDescription(m_defaultDescription, true);
            }
            if ((m_defaultVolume != null) && (m_volumeSelected)) {
                data[i].setVolumeOrQuantity(m_defaultVolume, true);
            } else if ((m_defaultQuantity != null) && (!m_volumeSelected)) {
                data[i].setVolumeOrQuantity(m_defaultQuantity, true);
            }
        }

        if (m_data == null) {
            m_data = data;
        } else {
            int size = m_data.length + data.length;
            FragmentsInfoForSample[] data1 = m_data;
            m_data = new FragmentsInfoForSample[size];
            for (int i=0;i<data1.length;i++) {
                m_data[i] = data1[i];
            }
            for (int i=0;i<data.length;i++) {
                m_data[i+data1.length] = data[i];
            }
        }

        fireTableDataChanged();
    }

    public void removeSamples(ArrayList<SampleJson> sampleArrayToRemove) {

        ArrayList<FragmentsInfoForSample> dataArray = new ArrayList<>();

        for (int i = 0; i< m_data.length; i++) {
            FragmentsInfoForSample fragmentsInfoForSample = m_data[i];
            if (! sampleArrayToRemove.contains(fragmentsInfoForSample.getParentSample() )) {
                dataArray.add(fragmentsInfoForSample);
            }
        }


        m_data = new FragmentsInfoForSample[dataArray.size()];
        for (int i=0;i<dataArray.size();i++) {
            m_data[i] = dataArray.get(i);
        }

        fireTableDataChanged();
    }

    public void setNbFraments(int nbFragmentsNew) {

        m_nbFragments = nbFragmentsNew;

        for (int i=0;i<m_data.length;i++) {
            m_data[i].setNbFraments(nbFragmentsNew);
        }


        renameAll();

    }

    public void setVolume(Float volume) {
        m_defaultVolume = volume;
        m_defaultQuantity = null;

        for (int i = 0; i< m_data.length; i++) {
            m_data[i].setVolumeOrQuantity(volume, false);
        }
    }

    public void setQuantity(Float quantity) {
        m_defaultVolume = null;
        m_defaultQuantity = quantity;

        for (int i = 0; i< m_data.length; i++) {
            m_data[i].setVolumeOrQuantity(quantity, false);
        }
    }


    public void setDescription(String description) {
        m_defaultDescription = description;

        for (int i = 0; i< m_data.length; i++) {
            m_data[i].setDescription(description, false);
        }
    }

    public void setVolumeOption(boolean isVolume) {
        if (isVolume == m_volumeSelected) {
            return;
        }
        m_volumeSelected = isVolume;

        m_columnNames[COLTYPE_VOLUME_OR_QUANTITY] = m_volumeSelected ? "Volume (µl)" : "Quantity (ng)";
        m_columnTooltips[COLTYPE_VOLUME_OR_QUANTITY] = m_volumeSelected ? "Volume (µl)" : "Quantity (ng)";

        if (m_volumeSelected) {
            setVolume(null);
        } else {
            setQuantity(null);
        }
        for (FragmentsInfoForSample f : m_map.values()) {
            f.setVolumeOrQuantity(null, true);
        }


        fireTableStructureChanged();

        m_table.decorateColumns();
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
            case COLTYPE_PARENT_SAMPLE:
            case COLTYPE_FRAGMENT_NAME:
                return null; // default renderer
            case COLTYPE_VOLUME_OR_QUANTITY:
                return null; // default renderer
            case COLTYPE_DESCRIPTION:
                return null;

        }

        return null;
    }

    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {

        switch (col)  {
            case COLTYPE_PARENT_SAMPLE:
            case COLTYPE_FRAGMENT_NAME:
            case COLTYPE_DESCRIPTION:
                return String.class;
            case COLTYPE_VOLUME_OR_QUANTITY:
                return Float.class;

        }

        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_data.length * m_nbFragments;
    }

    @Override
    public Object getValueAt(int row, int col) {

        int sampleIndex = row / m_nbFragments;
        int fragmentIndex = row % m_nbFragments;

        FragmentsInfoForSample sampleInfo = m_data[sampleIndex];

        switch (col)  {
            case COLTYPE_PARENT_SAMPLE: {
                if (fragmentIndex == 0) {
                    return sampleInfo.getParentSample().getName();
                } else {
                    return "";
                }
            }
            case COLTYPE_FRAGMENT_NAME:
                return sampleInfo.getFragmentName(fragmentIndex);
            case COLTYPE_VOLUME_OR_QUANTITY:
                return sampleInfo.getVolumeOrQuantity(fragmentIndex);
            case COLTYPE_DESCRIPTION:
                return sampleInfo.getDescription(fragmentIndex);


        }

        return null; // should never happen

    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == COLTYPE_PARENT_SAMPLE) {
            return false;
        }
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col)
    {
        int sampleIndex = row / m_nbFragments;
        int fragmentIndex = row % m_nbFragments;

        FragmentsInfoForSample sampleInfo = m_data[sampleIndex];

        switch (col)  {
            case COLTYPE_PARENT_SAMPLE:
                // should not be called
            case COLTYPE_FRAGMENT_NAME:
                sampleInfo.setFragmentName(fragmentIndex, (String) aValue);
                break;
            case COLTYPE_VOLUME_OR_QUANTITY:
                sampleInfo.setVolumeOrQuantity(fragmentIndex, (Float) aValue);
                break;
            case COLTYPE_DESCRIPTION:
                 sampleInfo.setDescription(fragmentIndex, (String) aValue);
                 break;

        }

    }


    public class FragmentsInfoForSample {

        private SampleJson m_parentSample;

        private String[] m_fragmentNames = null;
        private String[] m_fragmentInfixNames = null;
        private String[] m_fragmentNumber = null;
        private Float[] m_volumeOrQuantity = null;
        private String[] m_description = null;

        public FragmentsInfoForSample(SampleJson s, int nbFragments) {

            m_parentSample = s;

            m_fragmentNames = new String[nbFragments];
            m_fragmentInfixNames = new String[nbFragments];
            m_fragmentNumber = new String[nbFragments];
            m_volumeOrQuantity = new Float[nbFragments];
            m_description = new String[nbFragments];

            String nameWithoutDigits = s.getName()+m_defaultFragmentInfix;
            int index = firstAvailableIndex(nameWithoutDigits);
            for (int i=0;i<nbFragments;i++) {
                m_fragmentNumber[i] = String.valueOf(index+i);
                m_fragmentInfixNames[i] = m_defaultFragmentInfix;
                m_fragmentNames[i] = null;
                m_volumeOrQuantity[i] = null;
                m_description[i] = "";
            }
        }

        private void changeFragmentInfix() {
            int nbFragments = m_fragmentNames.length;
            String nameWithoutDigits = m_parentSample.getName()+m_defaultFragmentInfix;
            int index = firstAvailableIndex(nameWithoutDigits);
            for (int i=0;i<nbFragments;i++) {
                if (m_fragmentNames[i] != null) {
                    continue;
                }
                m_fragmentNumber[i] = String.valueOf(index+i);
                m_fragmentInfixNames[i] = m_defaultFragmentInfix;
                m_fragmentNames[i] = null;

            }
        }

        public void setVolumeOrQuantity(Float f, boolean keepDefault) {
            for (int i = 0; i< m_volumeOrQuantity.length; i++) {
                if ((m_volumeOrQuantity[i] == null) || (!keepDefault)) {
                    m_volumeOrQuantity[i] = f;
                }
            }
            fireTableDataChanged();
        }

        public void setDescription(String description, boolean keepDefault) {
            for (int i = 0; i< m_description.length; i++) {
                if ((m_description[i].isEmpty()) || (!keepDefault)) {
                    setDescription(i, description);
                }
            }
            fireTableDataChanged();
        }


        public SampleJson getParentSample() {
            return m_parentSample;
        }

        public String getFragmentName(int i) {
            if (m_fragmentNames[i] == null) {
                return m_parentSample.getName() + m_fragmentInfixNames[i] + m_fragmentNumber[i];
            } else {
                return m_fragmentNames[i];
            }
        }

        public Float getVolumeOrQuantity(int i) {
            return m_volumeOrQuantity[i];
        }

        public String getDescription(int i) {
            return m_description[i];
        }

        public void setFragmentName(int i, String name) {
            m_fragmentNames[i] = name;
        }

        public void setFragmentDefault(int i, String digit) {
            m_fragmentNumber[i] = digit;
            m_fragmentInfixNames[i] = m_defaultFragmentInfix;
            m_fragmentNames[i] = null;
        }

        public void setVolumeOrQuantity(int i, Float f) {
            m_volumeOrQuantity[i] = f;
        }

        public void setDescription(int i, String description) {
            m_description[i] = description;
        }



        public void setNbFraments(int nbFragmentsNew) {
            int previousNb = m_fragmentNames.length;

            if (previousNb < nbFragmentsNew) {

                String[] fragmentNames = new String[nbFragmentsNew];
                String[] fragmentInfixNames = new String[nbFragmentsNew];
                String[] fragmentNumber = new String[nbFragmentsNew];
                Float[] volumeOrQuantity = new Float[nbFragmentsNew];
                String[] description = new String[nbFragmentsNew];

                for (int i=0;i<previousNb;i++) {
                    fragmentNames[i] = m_fragmentNames[i];
                    fragmentInfixNames[i] = m_fragmentInfixNames[i];
                    fragmentNumber[i] = m_fragmentNumber[i];

                    volumeOrQuantity[i] = m_volumeOrQuantity[i];
                    description[i] = m_description[i];
                }
                for (int i=previousNb;i<nbFragmentsNew;i++) {
                    fragmentNames[i] = null; //m_parentSample.getName()+m_defaultFragmentInfix+(i+1);
                    fragmentInfixNames[i] = m_defaultFragmentInfix;
                    fragmentNumber[i] = String.valueOf(i+1);
                    volumeOrQuantity[i] = m_volumeSelected ? m_defaultVolume : m_defaultQuantity;
                    description[i] = m_defaultDescription;
                }

                m_fragmentNames = fragmentNames;
                m_fragmentInfixNames = fragmentInfixNames;
                m_fragmentNumber = fragmentNumber;
                m_volumeOrQuantity = volumeOrQuantity;
                m_description = description;

                fireTableRowsInserted(previousNb, nbFragmentsNew-1);

            } else if (previousNb > nbFragmentsNew) {

                String[] fragmentNames = new String[nbFragmentsNew];
                String[] fragmentInfixNames = new String[nbFragmentsNew];
                String[] fragmentNumber = new String[nbFragmentsNew];
                Float[] volumeOrQuantity = new Float[nbFragmentsNew];
                String[] description = new String[nbFragmentsNew];

                for (int i=0;i<nbFragmentsNew;i++) {
                    fragmentNames[i] = m_fragmentNames[i];
                    fragmentInfixNames[i] = m_fragmentInfixNames[i];
                    fragmentNumber[i] = m_fragmentNumber[i];

                    volumeOrQuantity[i] = m_volumeOrQuantity[i];
                    description[i] = m_description[i];
                }

                m_fragmentNames = fragmentNames;
                m_fragmentInfixNames = fragmentInfixNames;
                m_fragmentNumber = fragmentNumber;
                m_volumeOrQuantity = volumeOrQuantity;
                m_description = description;

                fireTableRowsDeleted (nbFragmentsNew, previousNb-1);
            }
        }


    }

}
