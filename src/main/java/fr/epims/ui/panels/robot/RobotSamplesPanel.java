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

package fr.epims.ui.panels.robot;

import fr.edyp.epims.json.RobotPlanningJson;
import fr.edyp.epims.json.VirtualWellJson;
import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.panels.model.SampleForPlateModel;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Panel with assigned and unassigned samples
 *
 * @author JM235353
 *
 */
public class RobotSamplesPanel extends JPanel implements WellSelectionInterface{

    private SampleForPlateModel m_unassignedSamplesmodel;
    private SampleForPlateModel m_plateSamplesmodel;

    private WellSelectionManager m_wellSelectionManager;

    private DecoratedTable m_plateSamplesTable;

    private boolean m_plateSampleSelectionBeingChanged = false;

    private RobotPlatePanel m_robotPlatePanel;

    private static final String SELECTION_SOURCE_KEY = RobotSamplesPanel.class.getName();

    public RobotSamplesPanel(WellSelectionManager selectionManager, RobotPlatePanel robotPlatePanel) {
        super(new GridBagLayout());

        m_robotPlatePanel = robotPlatePanel;

        m_wellSelectionManager = selectionManager;
        m_wellSelectionManager.addWellSelectionInterface(SELECTION_SOURCE_KEY, this);

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JTabbedPane samplesTabbedPane = createSamplesTabbedPane();

        c.weightx = 1;
        c.weighty = 1;
        add(samplesTabbedPane, c);

    }

    public void reinit() {
        m_unassignedSamplesmodel.setData(null);
        m_plateSamplesmodel.setData(null);
    }

    public JTabbedPane createSamplesTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Unassigned Samples", createUnassignedTableComponent());
        //tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.LOCK));


        tabbedPane.addTab("Plate Samples", createPlateTableComponent());
        //tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.ARCHIVE));


        return tabbedPane;
    }

    private JScrollPane createPlateTableComponent() {
        m_plateSamplesTable = new DecoratedTable();

        m_plateSamplesmodel = new SampleForPlateModel(true, true,false);
        m_plateSamplesTable.setModel(m_plateSamplesmodel);

        m_plateSamplesTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane tableScrollPane = new JScrollPane(m_plateSamplesTable);

        m_plateSamplesTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        m_plateSamplesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (m_plateSampleSelectionBeingChanged) {
                    return;
                }
                int[] selectedRows = m_plateSamplesTable.getSelectedRows();
                ArrayList<Integer> rowsInModel = new ArrayList<>();
                for (int i : selectedRows) {
                    rowsInModel.add(m_plateSamplesTable.convertRowIndexToModel(i));
                }
                m_wellSelectionManager.selectionChanged(SELECTION_SOURCE_KEY, m_plateSamplesmodel.getSampleNames(rowsInModel));

            }
        });

        return tableScrollPane;

    }

    private JScrollPane createUnassignedTableComponent() {
        DecoratedTable table = new DecoratedTable() ;

        m_unassignedSamplesmodel = new SampleForPlateModel(true, true,true);
        table.setModel(m_unassignedSamplesmodel);
        table.setTransferHandler(new TableRowTransferHandler(table));
        table.setDragEnabled(true);
        table.setDropMode(DropMode.USE_SELECTION);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScrollPane = new JScrollPane(table);

        table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        return tableScrollPane;
    }

    public void setPlate(EditablePlate plate, HashSet<ColoredRobotPlanning> freedPlanningJsonSet) {

        if (plate == null) {
            return;
        }

        HashMap<String, RobotPlanningJson> robotPlanningMap = new HashMap<>();
        for (VirtualWellJson well : plate.getPlate().getVirtualWells()) {
            RobotPlanningJson robotPlanning = well.getRobotPlanning();
            String key = robotPlanning.getSample().getName();
            if (! robotPlanningMap.containsKey(key)) {
                robotPlanningMap.put(key, robotPlanning);
            }
        }

        ArrayList<RobotPlanningJson> robotPlanningList = new ArrayList(robotPlanningMap.values());
        Collections.sort(robotPlanningList);

        setPlatesSamples(robotPlanningList);

        if (freedPlanningJsonSet != null) {
            modifyAvailableSamples(robotPlanningList, freedPlanningJsonSet);
        }
    }


    public void setAvailableSamples(ArrayList<RobotPlanningJson> freeRobotPlanningList) {
       m_unassignedSamplesmodel.setData(freeRobotPlanningList);
    }

    public void modifyAvailableSamples(ArrayList<RobotPlanningJson> usedRobotPlanningList, HashSet<ColoredRobotPlanning> freedPlanningJsonSet) {

        ArrayList<ColoredRobotPlanning> newList = new ArrayList<>();
        for (ColoredRobotPlanning coloredRobotPlanning : freedPlanningJsonSet) {
            newList.add(coloredRobotPlanning);
        }

        HashSet<String> samplesUsed = new HashSet<>();
        for (RobotPlanningJson robotPlanningJson : usedRobotPlanningList) {
            samplesUsed.add(robotPlanningJson.getSample().getName());
        }

        ArrayList<ColoredRobotPlanning> values = m_unassignedSamplesmodel.getValues();
        for (ColoredRobotPlanning coloredRobotPlanning : values) {
            if (! samplesUsed.contains(coloredRobotPlanning.m_robotPlanning.getSample().getName())) {
                newList.add(coloredRobotPlanning);
            }
        }

        Collections.sort(newList);



        m_unassignedSamplesmodel.setValues(newList);
    }


    public void addAvailableSamples(HashSet<ColoredRobotPlanning> freedPlanningJsonSet) {

        m_unassignedSamplesmodel.addSamples(new ArrayList(freedPlanningJsonSet));
    }

    private void setPlatesSamples(ArrayList<RobotPlanningJson> robotPlanningList) {
        m_plateSamplesmodel.setData(robotPlanningList);
    }

    @Override
    public void selectionChanged(HashSet<String> sampleNames) {
        m_plateSampleSelectionBeingChanged = true;
        try {
            ArrayList<Integer> modelRowList = m_plateSamplesmodel.getRowListWithSampleName(sampleNames);


            int minRowInTable = -1;
            m_plateSamplesTable.clearSelection();
            for (Integer rowInModel : modelRowList) {
                int rowInTable = m_plateSamplesTable.convertRowIndexToView(rowInModel);
                m_plateSamplesTable.addRowSelectionInterval(rowInTable, rowInTable);
                if ((minRowInTable == -1) || (rowInTable<minRowInTable)) {
                    minRowInTable = rowInTable;
                }
            }
            if (minRowInTable != -1) {
                m_plateSamplesTable.scrollRowToVisible(minRowInTable);
            }
        } catch (Exception e) {
            m_plateSampleSelectionBeingChanged = false;
            throw e;
        }
        m_plateSampleSelectionBeingChanged = false;

    }


    public static class TransferableRobotPlanning implements Transferable {

        private int m_row;
        private ColoredRobotPlanning m_robotPlanning;

        public static final DataFlavor UNUSED_ROBOT_PLANNING_FLAVOR = new DataFlavor(ColoredRobotPlanning.class, "Sample");
        public static final DataFlavor TABLE_ROW_FLAVOR = new DataFlavor(Integer.class, "TableRow");

        DataFlavor[] m_flavors = null;

        public TransferableRobotPlanning(int row, ColoredRobotPlanning robotPlanning) {
            m_row = row;
            m_robotPlanning = robotPlanning;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            if (m_flavors == null) {
                m_flavors = new DataFlavor[2];
                m_flavors[0] = UNUSED_ROBOT_PLANNING_FLAVOR;
                m_flavors[1] = TABLE_ROW_FLAVOR;
            }
            return m_flavors;
        }



        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(UNUSED_ROBOT_PLANNING_FLAVOR) || flavor.equals(TABLE_ROW_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                if (flavor.equals(UNUSED_ROBOT_PLANNING_FLAVOR)) {
                    return m_robotPlanning;
                }
                if (flavor.equals(TABLE_ROW_FLAVOR)) {
                    return m_row;
                }
            }

            return null;
        }

        public int getRow() {
            return m_row;
        }
    }


    public class TableRowTransferHandler extends TransferHandler {
        private JTable m_table = null;

        public TableRowTransferHandler(JTable table) {
            m_table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            assert (c == m_table);

            int row = m_table.getSelectedRow();
            int modelRow = m_table.convertRowIndexToModel(row);
            ColoredRobotPlanning robotPlanning =  ((SampleForPlateModel) m_table.getModel()).getColoredRobotPlanning(modelRow);


            return new TransferableRobotPlanning(row, robotPlanning);
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {

            if (info.isDataFlavorSupported(RobotPlatePanel.TransferableInnerRobotPlanning.PLATE_ROBOT_PLANNING_FLAVOR)) {
                m_table.setCursor(DragSource.DefaultMoveDrop);
                return true;
            }

            return false;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            try {
                RobotPlatePanel.MovingGroup movingGroup = (RobotPlatePanel.MovingGroup) info.getTransferable().getTransferData(RobotPlatePanel.TransferableInnerRobotPlanning.PLATE_ROBOT_PLANNING_FLAVOR);

                m_table.setCursor(Cursor.getDefaultCursor());

                ArrayList<ColoredRobotPlanning> coloredRobotPlannings = movingGroup.removeRobotPlanningList();
                m_unassignedSamplesmodel.addSamples(coloredRobotPlannings);

                m_plateSamplesmodel.removeSamples(coloredRobotPlannings);

                // clean up potential wells with the same sample in the plate
                HashSet<String> sampleKeys = new HashSet<>();
                for (ColoredRobotPlanning coloredRobotPlanning : coloredRobotPlannings) {
                    sampleKeys.add(coloredRobotPlanning.m_robotPlanning.getSample().getName());
                }
                m_robotPlatePanel.cleanWells(sampleKeys);

                // clean up wells of coloredRobotPlannings
                for (ColoredRobotPlanning coloredRobotPlanning : coloredRobotPlannings) {
                    coloredRobotPlanning.m_wells.clear();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;

        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int act) {
            if (act != MOVE) {
                return;
            }
            try {
                Integer tableRow = (Integer) t.getTransferData(TransferableRobotPlanning.TABLE_ROW_FLAVOR);

                SampleForPlateModel model = (SampleForPlateModel) m_table.getModel();
                ColoredRobotPlanning v = model.removeValue(m_table.convertRowIndexToModel(tableRow));

                m_plateSamplesmodel.addValue(v);

            } catch (Exception e) {
                return;
            }

        }

    }

    public ArrayList<ColoredRobotPlanning> getUnassignedSamples() {

        ArrayList<ColoredRobotPlanning> list = new ArrayList(m_unassignedSamplesmodel.getValues());

        return list;

    }

}
