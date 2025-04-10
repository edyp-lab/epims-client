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
import fr.epims.tasks.ModifyRobotPlanningTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.panels.robot.ColoredRobotPlanning;
import fr.epims.ui.panels.robot.RobotPanel;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;

/**
 * Table Model
 *
 * @author JM235353
 */
public class SampleForPlateModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_SAMPLE = 0;
    public static final int COLTYPE_SPECIES = 1;
    public static final int COLTYPE_LOAD_COUNT = 2;
    public static final int COLTYPE_TRYPSIN_QUANTITY = 3;
    public static final int COLTYPE_QUANTITY = 4;
    public static final int COLTYPE_COMMENTARY = 5;
    public static final int COLTYPE_RESPONSIBLE = 6;
    public static final int COLTYPE_DESCRIPTION = 7;
    public static final int COLTYPE_DATE = 8;


    private static final String[] m_columnNames = {"Sample", "Species", "Load Count", "Trypsin quantity (µg)", "Proteins Quantity (µg)", "Commentary", "Owner", "Description", "Date"};
    private static final String[] m_columnTooltips = {"Sample", "Species", "Load Count", "Trypsin quantity (µg)", "Proteins Quantity (µg)", "Commentary", "Owner", "Description", "Date"};


    private ArrayList<ColoredRobotPlanning> m_values;


    private HashMap<Color, ColoredRenderer> m_coloredStringRendererMap = new HashMap<>();
    private HashMap<Color, ColoredRenderer> m_coloredNumberRendererMap = new HashMap<>();

    private boolean m_rendererForSample;
    private boolean m_editable;
    private boolean m_loadCountEditable;

    public SampleForPlateModel(boolean rendererForSample, boolean editable, boolean loadCountEditable) {
        m_values = new ArrayList<>();
        m_rendererForSample = rendererForSample;
        m_editable = editable;
        m_loadCountEditable = loadCountEditable;

    }

    public ArrayList<ColoredRobotPlanning> getValues() {
        return m_values;
    }

    public void setValues(ArrayList<ColoredRobotPlanning> freeRobotPlanningList) {
        m_values = freeRobotPlanningList;
        Collections.sort(m_values);

        fireTableDataChanged();
    }

    public void setData(ArrayList<RobotPlanningJson> freeRobotPlanningList) {

        if (freeRobotPlanningList == null) {
            m_values = new ArrayList<>();
            fireTableDataChanged();
            return;
        }

        m_values = new ArrayList<>(freeRobotPlanningList.size());
        for (int i = 0; i < freeRobotPlanningList.size(); i++) {
            m_values.add(new ColoredRobotPlanning(freeRobotPlanningList.get(i)));
        }

        Collections.sort(m_values);

        fireTableDataChanged();
    }

    public void addSamples(ArrayList<ColoredRobotPlanning> coloredRobotPlannings) {
        m_values.addAll(coloredRobotPlannings);

        Collections.sort(m_values);

        fireTableDataChanged();
    }

    public void removeSamples(ArrayList<ColoredRobotPlanning> coloredRobotPlannings) {

        HashSet<String> m_sampleNamesSet = new HashSet<>();
        for (ColoredRobotPlanning coloredRobotPlanning : coloredRobotPlannings) {
            m_sampleNamesSet.add(coloredRobotPlanning.m_robotPlanning.getSample().getName());
        }

        ArrayList<ColoredRobotPlanning> newList = new ArrayList<>();
        for (ColoredRobotPlanning coloredRobotPlanning: m_values) {
            if (!m_sampleNamesSet.contains(coloredRobotPlanning.m_robotPlanning.getSample().getName())) {
                newList.add(coloredRobotPlanning);
            }
        }
        m_values = newList;

        fireTableDataChanged();
    }

    public ColoredRobotPlanning removeValue(int row) {

        ColoredRobotPlanning v = m_values.remove(row);


        fireTableRowsDeleted(row, row);

        return v;
    }

    public void addValue(ColoredRobotPlanning v) {
        m_values.add(v);

        fireTableRowsInserted(m_values.size() - 1, m_values.size() - 1);
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

        boolean number = false;
        HashMap<Color, ColoredRenderer> map;
        Color c = null;
        if (col == COLTYPE_SAMPLE) {
            if (!m_rendererForSample) {
                return null;
            }
            c = m_values.get(row).m_color;
            map = m_coloredStringRendererMap;
        } else if (col == COLTYPE_SPECIES) {
            RobotPlanningJson robotPlanning = m_values.get(row).m_robotPlanning;
            SampleJson s = robotPlanning.getSample();
            BiologicOriginJson biologicOrigin = s.getBiologicOriginJson();
            int id = biologicOrigin.getSampleSpecies();
            c = CyclicColorPalette.getColor(id, CyclicColorPalette.GROUP4_PALETTE);
            map = m_coloredStringRendererMap;
        } else if (col == COLTYPE_RESPONSIBLE) {
            c = m_values.get(row).m_userColor;
            map = m_coloredStringRendererMap;
        } else if (col == COLTYPE_TRYPSIN_QUANTITY) {
            c = m_values.get(row).m_trypsinColor;
            number = true;
            map = m_coloredNumberRendererMap;
        } else {
            return null;
        }

        ColoredRenderer renderer = map.get(c);
        if (renderer == null) {
            renderer = new ColoredRenderer(c, number);
            map.put(c, renderer);
        }
        return renderer;
    }

    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {

        switch (col) {
            case COLTYPE_SAMPLE:
            case COLTYPE_SPECIES:
            case COLTYPE_DESCRIPTION:
            case COLTYPE_COMMENTARY:
            case COLTYPE_RESPONSIBLE:
            case COLTYPE_DATE:
                return String.class;
            case COLTYPE_LOAD_COUNT:
                return Integer.class;
            case COLTYPE_TRYPSIN_QUANTITY:
            case COLTYPE_QUANTITY:
                return Float.class;

        }

        return String.class;
    }


    @Override
    public int getRowCount() {
        return m_values.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        RobotPlanningJson robotPlanning = m_values.get(row).m_robotPlanning;
        SampleJson s = robotPlanning.getSample();

        switch (col) {
            case COLTYPE_LOAD_COUNT:
                return robotPlanning.getLoadCount();
            case COLTYPE_SAMPLE: {
                return s.getName();
            }
            case COLTYPE_SPECIES: {
                BiologicOriginJson biologicOrigin = s.getBiologicOriginJson();
                return DataManager.getSampleSpecies(biologicOrigin.getSampleSpecies()).getName();
            }
            case COLTYPE_DESCRIPTION: {
                String description = s.getDescription();
                return (description == null) ? "" : description;
            }
            case COLTYPE_COMMENTARY: {
                String commentary = robotPlanning.getDescription();
                return commentary;
            }
            case COLTYPE_RESPONSIBLE: {
                String actorKey = s.getActorKey();
                return DataManager.getNameFromActorKey(actorKey);
            }
            case COLTYPE_DATE: {
                Date d = robotPlanning.getDate();
                return UtilDate.dateToString(d);
            }
            case COLTYPE_TRYPSIN_QUANTITY: {
                return robotPlanning.getTrypsineVol();
            }
            case COLTYPE_QUANTITY: {
                return robotPlanning.getProteinQty();
            }
        }

        return null; // should never happen

    }

    public ArrayList<Integer> getRowListWithSampleName(HashSet<String> sampleNames) {

        ArrayList<Integer> rowList = new ArrayList<>();
        for (int i = 0; i < m_values.size(); i++) {
            RobotPlanningJson robotPlanning = m_values.get(i).m_robotPlanning;
            SampleJson s = robotPlanning.getSample();
            if (sampleNames.contains(s.getName())) {
                rowList.add(i);
            }
        }
        return rowList;
    }

    public HashSet<String> getSampleNames(ArrayList<Integer> rows) {
        HashSet<String> sampleNamesSet = new HashSet<>();
        for (Integer row : rows) {
            RobotPlanningJson robotPlanning = m_values.get(row).m_robotPlanning;
            SampleJson s = robotPlanning.getSample();
            sampleNamesSet.add(s.getName());
        }

        return sampleNamesSet;
    }

    public ColoredRobotPlanning getColoredRobotPlanning(int row) {
        return m_values.get(row);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return m_editable && ( ((col == COLTYPE_LOAD_COUNT) && m_loadCountEditable) || (col == COLTYPE_TRYPSIN_QUANTITY) || (col == COLTYPE_QUANTITY) || (col == COLTYPE_COMMENTARY) );
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        ColoredRobotPlanning coloredRobotPlanning = m_values.get(row);
        RobotPlanningJson robotPlanning = coloredRobotPlanning.m_robotPlanning;
        Integer loadCount = robotPlanning.getLoadCount();
        Float trypsinVol = robotPlanning.getTrypsineVol();
        Float proteinQuantity = robotPlanning.getProteinQty();
        String commentary = robotPlanning.getDescription();

        switch (col) {
            case COLTYPE_LOAD_COUNT:
                loadCount = (Integer) aValue;
                if ((loadCount==null) || (loadCount<=0)) {
                    return;
                }
                break;
            case COLTYPE_TRYPSIN_QUANTITY:
                trypsinVol = (Float) aValue;
                if ((trypsinVol!=null) && (trypsinVol<=0)) {
                    return;
                }
                break;
            case COLTYPE_QUANTITY:
                proteinQuantity = (Float) aValue;
                if ((proteinQuantity!=null) && (proteinQuantity<=0)) {
                    return;
                }
                break;
            case COLTYPE_COMMENTARY:
                commentary = (String) aValue;
                if (commentary == null) {
                    commentary = "";
                } else {
                    commentary = commentary.trim();
                }
                break;
        }

        final Integer _loadCount = loadCount;
        final Float _trypsinVol = trypsinVol;
        final Float _proteinQuantity = proteinQuantity;
        final String _commentary= commentary;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), RobotDataJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (updateDataDialog.isDataUpdated()) {

                    DatabaseVersionJson serverVersion = updateDataDialog.getServerDatabaseVersion();
                    String login = serverVersion.getLogin(RobotDataJson.class);
                    String user;
                    if ((login == null) || (login.length() == 0)) {
                        user = "Someone";
                    } else {
                        user = DataManager.getLastThenFirstNameFromActorKey(login);
                    }


                    QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Robot Plates Update", user+" has modified Robot Plates. You can not proceed, do you want to reload ?");
                    questionDialog.centerToWindow(MainFrame.getMainWindow());
                    questionDialog.setVisible(true);
                    if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        // reload plates
                        RobotPanel.getPanel().reinit();
                        RobotPanel.getPanel().loadData();
                    }
                    return;
                }


                setValueInDatabase(coloredRobotPlanning, robotPlanning, _loadCount, _trypsinVol, _proteinQuantity, _commentary);

            }
        });

    }

    private void setValueInDatabase( ColoredRobotPlanning coloredRobotPlanning, RobotPlanningJson robotPlanning, Integer loadCount, Float trypsinVol, Float proteinQuantity, String description) {

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    robotPlanning.setLoadCount(loadCount);
                    robotPlanning.setTrypsineVol(trypsinVol);
                    robotPlanning.setProteinQty(proteinQuantity);
                    robotPlanning.setDescription(description);

                    coloredRobotPlanning.updated(robotPlanning);

                    fireTableDataChanged();

                    RobotPanel.getPanel().update(robotPlanning);
                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }
            }
        };


        RobotPlanningJson newRobotPlanning = new RobotPlanningJson(robotPlanning.getId(), robotPlanning.getActorKey(),  robotPlanning.getSample(), trypsinVol, proteinQuantity,
                robotPlanning.getSeparationResultClass(), robotPlanning.getDate(), loadCount, description,
                robotPlanning.getSampleConsumed(), robotPlanning.getName() , robotPlanning.getVirtualWellsId());

        ModifyRobotPlanningTask task = new ModifyRobotPlanningTask(callback, newRobotPlanning);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


    }



    public class ColoredRenderer extends DefaultTableCellRenderer {

        private Icon m_icon;
        private boolean m_number;

        public ColoredRenderer(Color c, boolean number) {
            m_icon = IconManager.createColoredIcon(c);
            m_number = number;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setIcon(m_icon);
            if (m_number) {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setHorizontalTextPosition(SwingConstants.LEFT);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            return label;

        }

    }


}
