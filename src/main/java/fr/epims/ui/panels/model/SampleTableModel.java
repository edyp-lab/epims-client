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


import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.tasks.ModifySampleTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * Table Model
 *
 * @author JM235353
 *
 */
public class SampleTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_ORIGINAL_NAME = 1;
    public static final int COLTYPE_DESCRIPTION = 2;
    public static final int COLTYPE_SPECIES = 3;
    public static final int COLTYPE_ACQUISITION = 4;
    public static final int COLTYPE_ACQ = 5;
    public static final int COLTYPE_LAST_STEP = 6;
    public static final int COLTYPE_CREATOR = 7;
    public static final int COLTYPE_STATUS = 8;


    private static final String[] m_columnNames = {"Name", "Original Name", "Description", "Specie", "Acquisition", "#acq", "Last Step", "Creator", "Status"};
    private static final String[] m_columnTooltips = {"Name", "Original Name", "Description", "Specie", "Acquisition", "#acq", "Last Step", "Creator", "Status"};

    private ArrayList<SampleJson> m_samples = new ArrayList<>(0);

    private RendererMouseCallback m_callback;

    private boolean m_editable;

    public SampleTableModel(RendererMouseCallback callback, boolean editable) {
        m_callback = callback;

        m_editable = editable;
    }

    public void setSamples(ArrayList<SampleJson> samples) {
        m_samples = samples;
        fireTableDataChanged();
    }

    public SampleJson getSample(int row) {
        return m_samples.get(row);
    }

    public ArrayList<SampleJson> getSamples() {
        return m_samples;
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

        switch (col)  {
            case COLTYPE_NAME:
            case COLTYPE_ORIGINAL_NAME:
            case COLTYPE_DESCRIPTION:
            case COLTYPE_SPECIES:
            case COLTYPE_ACQUISITION:
            case COLTYPE_LAST_STEP:
            case COLTYPE_CREATOR:
            case COLTYPE_STATUS:
                return String.class;
            case COLTYPE_ACQ:
                return Integer.class;
        }

        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_samples.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        SampleJson s = m_samples.get(row);

        switch (col)  {
            case COLTYPE_NAME: {
                return s.getName();
            }
            case COLTYPE_ORIGINAL_NAME: {
                return s.getOriginalName();
            }
            case COLTYPE_DESCRIPTION: {
                String description = s.getDescription();
                return (description == null) ? "" : description;
            }
            case COLTYPE_SPECIES: {
                BiologicOriginJson biologicOrigin = s.getBiologicOriginJson();
                if (biologicOrigin == null) {
                    return "";
                }
                return DataManager.getSampleSpecies(biologicOrigin.getSampleSpecies()).getName();
            }
            case COLTYPE_ACQUISITION: {
                m_sb.setLength(0);
                ArrayList<ProtocolApplicationJson> paList = s.getOrderedProtocolApplications();
                for (ProtocolApplicationJson pa : paList) {
                    AcquisitionJson acquisitionJson = pa.getAcquisitionJson();
                    if (acquisitionJson != null) {
                        if (m_sb.length() != 0) {
                            m_sb.append(", ");
                        }
                        m_sb.append(pa.getName());
                    }
                }
                return m_sb.toString();
            }
            case COLTYPE_ACQ: {
                int acquisitionNumber = 0;
                ArrayList<ProtocolApplicationJson> paList = s.getOrderedProtocolApplications();
                for (ProtocolApplicationJson pa : paList) {
                    AcquisitionJson acquisitionJson = pa.getAcquisitionJson();
                    if (acquisitionJson != null) {
                        acquisitionNumber++;
                    }
                }
                return acquisitionNumber;
            }
            case COLTYPE_LAST_STEP: {
                ArrayList<ProtocolApplicationJson> paList = s.getOrderedProtocolApplications();
                if (paList.isEmpty()) {
                    return "";
                }
                ProtocolApplicationJson pa = paList.get(paList.size()-1);
                AcquisitionJson acquisitionJson = pa.getAcquisitionJson();
                if (acquisitionJson != null) {
                    return pa.getName();
                } else {
                    RunRobotJson runRobotJson = pa.getRunRobotJson();
                    if (runRobotJson != null) {
                        return "Run Robot " + UtilDate.dateToString(pa.getDate());
                    } else {
                        AliquotageJson aliquotageJson = pa.getAliquotageJson();
                        if (aliquotageJson != null) {
                            return pa.getName();
                        }
                    }
                }

                return "";
            }
            case COLTYPE_CREATOR: {
                String actorKey = s.getActorKey();
                return DataManager.getNameFromActorKey(actorKey);
            }
            case COLTYPE_STATUS: {
                return s.getStatus();
            }
        }

        return null;

    }
    private static StringBuffer m_sb = new StringBuffer();


    @Override
    public boolean isCellEditable(int row, int col) {
        return (m_editable && (col == COLTYPE_DESCRIPTION));
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        SampleJson sampleJson = m_samples.get(row);

        String description = sampleJson.getDescription();

        switch (col) {
            case COLTYPE_DESCRIPTION:
                description = (String) aValue;
                if (description==null) {
                    return;
                }
                break;
        }

        final String _description = description;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                setValueInDatabase(sampleJson, _description);

            }
        });

    }

    private void setValueInDatabase( SampleJson sampleJson, String description) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    sampleJson.setDescription(description);
                    fireTableDataChanged();

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }
            }
        };


        SampleJson newSampleJson = new SampleJson(sampleJson.getName(), sampleJson.getActorKey(), sampleJson.getBiologicOriginJson(), sampleJson.getStudy(),
                description, sampleJson.getVolume(), sampleJson.getStatus(), sampleJson.getQuantity(), sampleJson.getOriginalName(), sampleJson.getRadioactivity(), sampleJson.getToxicity(), sampleJson.getCreationDate(), sampleJson.getOrderedProtocolApplications());


        ModifySampleTask task = new ModifySampleTask(callback, newSampleJson);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (col == COLTYPE_LAST_STEP) {
            return new ViewTableCellRenderer(m_callback, COLTYPE_LAST_STEP);
        }


        return null;
    }

    public ArrayList<String> getStepsHistory(int row) {
        SampleJson s = m_samples.get(row);

        ArrayList<String> history = new ArrayList<>();

        ArrayList<ProtocolApplicationJson> paList = s.getOrderedProtocolApplications();
        for (ProtocolApplicationJson pa : paList) {
            AcquisitionJson acquisitionJson = pa.getAcquisitionJson();

            if (acquisitionJson != null) {
                history.add(pa.getName()+" "+UtilDate.dateToString(pa.getDate()));
            } else {
                RunRobotJson runRobotJson = pa.getRunRobotJson();
                if (runRobotJson != null) {
                    history.add( "Run Robot " + UtilDate.dateToString(pa.getDate()));
                } else {
                    AliquotageJson aliquotageJson = pa.getAliquotageJson();
                    if (aliquotageJson != null) {
                        history.add(pa.getName());
                    }
                }
            }
        }
        return history;

    }

    public String getSampleName(int row) {
        SampleJson s = m_samples.get(row);
        return s.getName();
    }

    public Date getLastAcquisition() {
        Date lastDate = null;
        for (int i=0;i<getRowCount();i++) {
            SampleJson s = m_samples.get(i);
            ArrayList<ProtocolApplicationJson> paList = s.getOrderedProtocolApplications();
            for (ProtocolApplicationJson pa : paList) {
                AcquisitionJson acquisitionJson = pa.getAcquisitionJson();
                if (acquisitionJson != null) {
                    Date d = pa.getDate();
                    if ((lastDate == null) || (d.after(lastDate))) {
                        lastDate = d;
                    }
                }
            }
        }
        return lastDate;
    }

}
