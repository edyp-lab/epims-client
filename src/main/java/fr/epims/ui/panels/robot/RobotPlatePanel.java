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


import fr.epims.MainFrame;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.ui.common.CyclicColorPalette;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.panels.robot.gesture.SelectionGestureSquare;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/**
 *
 * Robot Plate editor Panel
 *
 * @author JM235353
 *
 */
public class RobotPlatePanel extends JPanel implements DragGestureListener, DragSourceListener, MouseListener, MouseMotionListener, WellSelectionInterface {

    private final int BOX_WIDTH_MIN = 60;
    private final int BOX_HEIGHT_MIN = 46;

    private final int BLANK_SPACE_HORIZONTAL = 10;
    private final int BLANK_SPACE_VERTICAL = 10;


    private int m_dimX;
    private int m_dimY;

    private Well[][] m_wells;

    private final SelectionGestureSquare m_selectionGesture = new SelectionGestureSquare();

    private PanelTransferHandler m_transferHandler;


    private static MovingGroup m_movingGroup;

    private WellSelectionManager m_wellSelectionManager;
    private static final String SELECTION_SOURCE_KEY = RobotPlatePanel.class.getName();

    private EditablePlate m_currentPlate = null;
    private HashMap<EditablePlate, Well[][]> m_plateWellsMap = new HashMap<>();

    private RobotPanel m_robotPanel;

    public enum COLOR_MODE {
        SAMPLE_COLOUR,
        TRYPSIN_COLOUR,
        USER_COLOUR,
        STUDY_COLOUR
    }
    private COLOR_MODE m_colorMode = COLOR_MODE.SAMPLE_COLOUR;

    private static Color ERROR_COLOR = new Color(192,30,30);

    public RobotPlatePanel(RobotPanel robotPanel, int dimX, int dimY, WellSelectionManager selectionManager) {

        setPlateSize(dimX, dimY);

        m_robotPanel = robotPanel;

        m_wellSelectionManager = selectionManager;
        m_wellSelectionManager.addWellSelectionInterface(SELECTION_SOURCE_KEY, this);

        m_transferHandler = new PanelTransferHandler(this);
        setTransferHandler(m_transferHandler);

        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);

        addMouseListener(this);
        addMouseMotionListener(this);

    }

    public void reinit() {
        m_currentPlate = null;
        setPlateSize(m_dimX, m_dimY);
        m_plateWellsMap = new HashMap<>();
    }

    public void update(RobotPlanningJson robotPlanning) {
        for (int i = 0; i < m_dimX; i++) {
            for (int j = 0; j < m_dimY; j++) {
                Well w = m_wells[i][j];
                ColoredRobotPlanning coloredRobotPlanning = w.getColoredRobotPlanning();
                if (coloredRobotPlanning == null) {
                    continue;
                }
                if (coloredRobotPlanning.m_robotPlanning.getId() == robotPlanning.getId()) {
                    coloredRobotPlanning.updated(robotPlanning);
                }
            }
        }
    }

    public VirtualPlateJson getPlateToSave(VirtualPlateJson previousVirtualPlate) {

        HashSet<VirtualWellJson> wellSet = new HashSet<>();

        VirtualPlateJson plate = new VirtualPlateJson(previousVirtualPlate.getName(), previousVirtualPlate.getActor(),
                previousVirtualPlate.getPlannedDate(), previousVirtualPlate.getLocked(), previousVirtualPlate.getXSize(),
                previousVirtualPlate.getYSize(), wellSet);

        for (int i = 0; i < m_dimX; i++) {
            for (int j = 0; j < m_dimY; j++) {
                Well w = m_wells[i][j];
                ColoredRobotPlanning coloredRobotPlanning = w.getColoredRobotPlanning();
                if (coloredRobotPlanning == null) {
                    continue;
                }
                RobotPlanningJson robotPlanning = coloredRobotPlanning.m_robotPlanning;
                VirtualWellJson virtualWell = new VirtualWellJson(-1, robotPlanning, previousVirtualPlate.getName(), i, j);
                wellSet.add(virtualWell);
            }
        }

        return plate;
    }

    public void setColourMode(COLOR_MODE colorMode) {
        m_colorMode = colorMode;

        repaint();
    }

    public Well[][] getWells() {
        return m_wells;
    }

    public int getDimX() {
        return m_dimX;
    }

    public int getDimY() {
        return m_dimY;
    }

    private void plateModified() {
        //checkIntegrity();
        m_robotPanel.plateModified(true);
    }

    public HashSet<ColoredRobotPlanning> setPlate(EditablePlate plate, boolean reset) {

        HashSet<ColoredRobotPlanning> removedFreeRobotPlanningSet = null;

        if (reset) {

            m_plateWellsMap.remove(plate);
            m_currentPlate = null;

            for (int i = 0; i < m_dimX; i++) {
                for (int j = 0; j < m_dimY; j++) {
                    Well w = m_wells[i][j];
                    ColoredRobotPlanning coloredRobotPlanning = w.getColoredRobotPlanning();
                    if (coloredRobotPlanning != null) {
                        if (coloredRobotPlanning.m_robotPlanning.getVirtualWellsId() == null) {
                            if (removedFreeRobotPlanningSet == null) {
                                removedFreeRobotPlanningSet = new HashSet<>();
                            }
                            removedFreeRobotPlanningSet.add(coloredRobotPlanning);
                        }
                    }
                }
            }
        }

        if ((plate == null) || (m_currentPlate == plate)) {

            return null;
        }

        if (m_currentPlate != null) {
            m_plateWellsMap.put(m_currentPlate, m_wells);
        }
        m_currentPlate = plate;

        VirtualPlateJson virtualPlate = plate.getPlate();

        if (m_plateWellsMap.containsKey(plate)) {
            m_wells = m_plateWellsMap.get(plate);
            m_dimX = virtualPlate.getXSize();
            m_dimY = virtualPlate.getYSize();
            m_movingGroup = new MovingGroup(m_dimX, m_dimY);
        } else {

            setPlateSize(virtualPlate.getXSize(), virtualPlate.getYSize());

            Set<VirtualWellJson> wells = virtualPlate.getVirtualWells();
            for (VirtualWellJson virtualWellJson : wells) {
                int x = virtualWellJson.getXCoord();
                int y = virtualWellJson.getYCoord();
                m_wells[x][y].setVirtualWell(virtualWellJson);
            }

            link();
        }

        repaint();

        return removedFreeRobotPlanningSet;
    }

    public EditablePlate getCurrentPlate() {
        return m_currentPlate;
    }



    private void setPlateSize(int dimX, int dimY) {
        m_dimX = dimX;
        m_dimY = dimY;

        setMinimumSize(new Dimension(BOX_WIDTH_MIN * dimX, BOX_HEIGHT_MIN*dimY));
        setPreferredSize(new Dimension(BOX_WIDTH_MIN * dimX, BOX_HEIGHT_MIN*dimY));

        m_wells = new Well[m_dimX][m_dimY];

        for (int i=0;i<m_dimX;i++) {
            for (int j=0;j<m_dimY;j++) {
                m_wells[i][j] = new Well(i, j);
            }
        }

        m_movingGroup = new MovingGroup(m_dimX, m_dimY);
    }



    public void cleanWells(HashSet<String> sampleKeys) {
        //checkIntegrity();
        for (int i=0;i<m_dimX;i++) {
            for (int j=0;j<m_dimY;j++) {
                ColoredRobotPlanning coloredRobotPlanning = m_wells[i][j].getColoredRobotPlanning();
                if (coloredRobotPlanning != null) {
                    if (sampleKeys.contains(coloredRobotPlanning.m_robotPlanning.getSample().getName())) {
                        m_wells[i][j].m_coloredRobotPlanning = null;
                    }
                }
            }
        }
        //checkIntegrity();
        repaint();
    }


    public void  clearLinks() {
        //checkIntegrity();

        HashSet<ColoredRobotPlanning> coloredRobotPlanningHashSet = new HashSet<>();
        for (int i=0;i<m_dimX*m_dimY;i++) {
            int ix = i / m_dimY;
            int iy = i % m_dimY;
            Well well = m_wells[ix][iy];

            ColoredRobotPlanning coloredRobotPlanning = well.getColoredRobotPlanning();

            if (coloredRobotPlanning != null) {
                if (coloredRobotPlanningHashSet.contains(coloredRobotPlanning)) {
                    coloredRobotPlanning = new ColoredRobotPlanning(coloredRobotPlanning.m_robotPlanning);
                    well.setRobotPlanning(coloredRobotPlanning);
                } else {
                    coloredRobotPlanning.clearWells();
                    coloredRobotPlanning.setLinkedWells(false);
                    coloredRobotPlanningHashSet.add(coloredRobotPlanning);
                }
            }
            well.setLastWell(false);
        }
        //checkIntegrity();
    }

    public void link() {

        //checkIntegrity();

        RobotPlanningJson previousRobotPlanningJson = null;

        // loop along columns to create vertical links
        int count = 0;
        for (int i=0;i<m_dimX*m_dimY;i++) {
            int ix = i / m_dimY;
            int iy = i % m_dimY;
            VirtualWellJson virtualWellJson = m_wells[ix][iy].getVirtualWell();

            if (virtualWellJson != null) {
                RobotPlanningJson robotPlanning = virtualWellJson.getRobotPlanning();
                if ((previousRobotPlanningJson!=null) && (previousRobotPlanningJson.getSample().getName().equals(robotPlanning.getSample().getName()))) {
                    count++;
                } else {
                    if ((previousRobotPlanningJson!=null) && (count>1)) {
                        if (count == previousRobotPlanningJson.getLoadCount()) {
                            ColoredRobotPlanning coloredRobotPlanning = null;
                            for (int k=i-count;k<i;k++) {
                                int ixCur = k / m_dimY;
                                int iyCur = k % m_dimY;

                                if (coloredRobotPlanning == null) {
                                    coloredRobotPlanning = m_wells[ixCur][iyCur].getColoredRobotPlanning();
                                }
                                if (coloredRobotPlanning == null) {
                                    coloredRobotPlanning = new ColoredRobotPlanning(previousRobotPlanningJson);
                                }
                                coloredRobotPlanning.setLinkedWells(true);
                                m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                                m_wells[ixCur][iyCur].setLastWell(k == i-1);
                                coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);
                            }
                        }
                    }
                    previousRobotPlanningJson = robotPlanning;
                    count = 1;
                }
            }  else {
                if ((previousRobotPlanningJson!=null) && (count>1)) {
                    if (count == previousRobotPlanningJson.getLoadCount()) {

                        ColoredRobotPlanning coloredRobotPlanning = null;
                        for (int k=i-count;k<i;k++) {
                            int ixCur = k / m_dimY;
                            int iyCur = k % m_dimY;

                            if (coloredRobotPlanning == null) {
                                coloredRobotPlanning = m_wells[ixCur][iyCur].getColoredRobotPlanning();
                            }
                            if (coloredRobotPlanning == null) {
                                coloredRobotPlanning = new ColoredRobotPlanning(previousRobotPlanningJson);
                            }

                            coloredRobotPlanning.setLinkedWells(true);
                            m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                            m_wells[ixCur][iyCur].setLastWell(k == i-1);
                            coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);

                        }
                    }
                }
                previousRobotPlanningJson = null;
                count = 0;
            }

        }
        if ((previousRobotPlanningJson!=null) && (count>=1)) {
            if (count == previousRobotPlanningJson.getLoadCount()) {

                ColoredRobotPlanning coloredRobotPlanning = null;
                for (int k=m_dimX*m_dimY-count;k<m_dimX*m_dimY;k++) {
                    int ixCur = k / m_dimY;
                    int iyCur = k % m_dimY;

                    if (coloredRobotPlanning == null) {
                        coloredRobotPlanning = m_wells[ixCur][iyCur].getColoredRobotPlanning();
                    }
                    if (coloredRobotPlanning == null) {
                        coloredRobotPlanning = new ColoredRobotPlanning(previousRobotPlanningJson);
                    }
                    coloredRobotPlanning.setLinkedWells(count>1);
                    m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                    m_wells[ixCur][iyCur].setLastWell(k == m_dimY-1);
                    coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);

                }
            }
        }

        // loop along lines to create horizontal links
        previousRobotPlanningJson = null;
        count = 0;
        for (int i=0;i<m_dimX*m_dimY;i++) {
            int ix = i % m_dimX;
            int iy = i / m_dimX;
            VirtualWellJson virtualWellJson = m_wells[ix][iy].getVirtualWell();

            if (virtualWellJson != null) {
                RobotPlanningJson robotPlanning = virtualWellJson.getRobotPlanning();
                if ((previousRobotPlanningJson!=null) && (previousRobotPlanningJson.getSample().getName().equals(robotPlanning.getSample().getName()))) {
                    count++;
                } else {
                    if ((previousRobotPlanningJson!=null) && (count>1)) {
                        if (count == previousRobotPlanningJson.getLoadCount()) {
                            ColoredRobotPlanning coloredRobotPlanning = null;
                            for (int k=i-count;k<i;k++) {
                                int ixCur = k % m_dimX;
                                int iyCur = k / m_dimX;

                                if (coloredRobotPlanning == null) {
                                    coloredRobotPlanning = m_wells[ixCur][iyCur].getColoredRobotPlanning();
                                }
                                if (coloredRobotPlanning == null) {
                                    coloredRobotPlanning = new ColoredRobotPlanning(previousRobotPlanningJson);
                                }
                                coloredRobotPlanning.setLinkedWells(true);
                                m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                                m_wells[ixCur][iyCur].setLastWell(k == i-1);
                                coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);
                            }
                        }
                    }
                    previousRobotPlanningJson = robotPlanning;
                    count = 1;
                }
            }  else {
                if ((previousRobotPlanningJson!=null) && (count>1)) {
                    if (count == previousRobotPlanningJson.getLoadCount()) {

                        ColoredRobotPlanning coloredRobotPlanning = null;
                        for (int k=i-count;k<i;k++) {
                            int ixCur = k % m_dimX;
                            int iyCur = k / m_dimX;

                            if (coloredRobotPlanning == null) {
                                coloredRobotPlanning = m_wells[ixCur][iyCur].getColoredRobotPlanning();
                            }
                            if (coloredRobotPlanning == null) {
                                coloredRobotPlanning = new ColoredRobotPlanning(previousRobotPlanningJson);
                            }

                            coloredRobotPlanning.setLinkedWells(true);
                            m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                            m_wells[ixCur][iyCur].setLastWell(k == i-1);
                            coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);

                        }
                    }
                }
                previousRobotPlanningJson = null;
                count = 0;
            }

        }
        if ((previousRobotPlanningJson!=null) && (count>=1)) {
            if (count == previousRobotPlanningJson.getLoadCount()) {

                ColoredRobotPlanning coloredRobotPlanning = null;
                for (int k=m_dimX*m_dimY-count;k<m_dimX*m_dimY;k++) {
                    int ixCur = k % m_dimX;
                    int iyCur = k / m_dimX;

                    if (coloredRobotPlanning == null) {
                        coloredRobotPlanning = m_wells[ixCur][iyCur].getColoredRobotPlanning();
                    }
                    if (coloredRobotPlanning == null) {
                        coloredRobotPlanning = new ColoredRobotPlanning(previousRobotPlanningJson);
                    }
                    coloredRobotPlanning.setLinkedWells(count>1);
                    m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                    m_wells[ixCur][iyCur].setLastWell(k == m_dimY-1);
                    coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);

                }
            }
        }

        for (int i=0;i<m_dimX;i++) {
            for (int j = 0; j < m_dimY; j++) {
                VirtualWellJson virtualWellJson = m_wells[i][j].getVirtualWell();
                if ((virtualWellJson != null) && (m_wells[i][j].getColoredRobotPlanning() == null)) {
                    RobotPlanningJson robotPlanning = m_wells[i][j].getVirtualWell().getRobotPlanning();
                    ColoredRobotPlanning coloredRobotPlanning = new ColoredRobotPlanning(robotPlanning);
                    coloredRobotPlanning.setLinkedWells(false);
                    coloredRobotPlanning.addWell(m_wells[i][j]);
                    m_wells[i][j].setRobotPlanning(coloredRobotPlanning);
                }
            }
        }

        //checkIntegrity();
    }

    public void relinkAfterDrop() {

        clearLinks();

        HashSet<ColoredRobotPlanning> coloredPlanningSet = new HashSet<>();
        for (int i=0;i<m_dimX;i++) {
            for (int j=0;j<m_dimY;j++) {
                ColoredRobotPlanning coloredRobotPlanningCur = m_wells[i][j].getColoredRobotPlanning();
                if (coloredRobotPlanningCur != null) {
                    coloredPlanningSet.add(coloredRobotPlanningCur);
                }
            }
        }
        for (ColoredRobotPlanning coloredRobotPlanning : coloredPlanningSet) {
            relinkAfterDrop(coloredRobotPlanning);
        }

        for (int i=0;i<m_dimX;i++) {
            for (int j = 0; j < m_dimY; j++) {
                ColoredRobotPlanning coloredRobotPlanningCur = m_wells[i][j].getColoredRobotPlanning();
                if (coloredRobotPlanningCur != null) {
                    if (coloredRobotPlanningCur.m_wells.isEmpty()) {
                        coloredRobotPlanningCur.addWell(m_wells[i][j]);
                    }
                }
            }
        }
    }
    public void relinkAfterDrop(ColoredRobotPlanning coloredRobotPlanning) {

        try {

            RobotPlanningJson robotPlanning = coloredRobotPlanning.m_robotPlanning;
            String sampleKey = robotPlanning.getSample().getName();

            int loadCount = robotPlanning.getLoadCount();

            int iFoundStart = -1;
            int iFoundEnd = -1;
            for (int i = 0; i < m_dimX * m_dimY; i++) {
                int ix = i / m_dimY;
                int iy = i % m_dimY;
                ColoredRobotPlanning coloredRobotPlanningCur = m_wells[ix][iy].getColoredRobotPlanning();
                if (coloredRobotPlanningCur == null) {
                    iFoundStart = -1;
                    iFoundEnd = -1;
                    continue;
                }
                RobotPlanningJson robotPlanningCur = coloredRobotPlanningCur.m_robotPlanning;
                if (robotPlanningCur.getSample().getName().equals(sampleKey)) {
                    if (iFoundStart == -1) {
                        iFoundStart = i;
                    }
                    iFoundEnd = i;
                    if (iFoundEnd - iFoundStart + 1 == loadCount) {
                        break;
                    }
                } else {

                    iFoundStart = -1;
                    iFoundEnd = -1;
                }
            }
            if ((iFoundEnd != -1) && (iFoundEnd - iFoundStart + 1 == loadCount)) {
                // we link wells
                coloredRobotPlanning.clearWells();
                for (int i = iFoundStart; i <= iFoundEnd; i++) {
                    int ixCur = i / m_dimY;
                    int iyCur = i % m_dimY;
                    m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                    m_wells[ixCur][iyCur].setLastWell(i == iFoundEnd);
                    coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);
                }

                return;
            }


            iFoundStart = -1;
            iFoundEnd = -1;
            for (int i = 0; i < m_dimX * m_dimY; i++) {
                int ix = i % m_dimX;
                int iy = i / m_dimX;
                ColoredRobotPlanning coloredRobotPlanningCur = m_wells[ix][iy].getColoredRobotPlanning();
                if (coloredRobotPlanningCur == null) {
                    iFoundStart = -1;
                    iFoundEnd = -1;
                    continue;
                }
                RobotPlanningJson robotPlanningCur = coloredRobotPlanningCur.m_robotPlanning;
                if (robotPlanningCur.getSample().getName().equals(sampleKey)) {
                    if (iFoundStart == -1) {
                        iFoundStart = i;
                    }
                    iFoundEnd = i;
                    if (iFoundEnd - iFoundStart + 1 == loadCount) {
                        break;
                    }
                } else {

                    iFoundStart = -1;
                    iFoundEnd = -1;
                }
            }
            if ((iFoundEnd != -1) && (iFoundEnd - iFoundStart + 1 == loadCount)) {
                // we link wells
                coloredRobotPlanning.clearWells();
                for (int i = iFoundStart; i <= iFoundEnd; i++) {
                    int ixCur = i % m_dimX;
                    int iyCur = i / m_dimX;
                    m_wells[ixCur][iyCur].setRobotPlanning(coloredRobotPlanning);
                    m_wells[ixCur][iyCur].setLastWell(i == iFoundEnd);
                    coloredRobotPlanning.addWell(m_wells[ixCur][iyCur]);
                }

                return;

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void paint(Graphics g) {
        g.setColor(Color.white);

        int width = getWidth();
        int height = getHeight();

        g.fillRect(0, 0, width-1, height-1);

        g.setColor(Color.black);
        g.drawRect(0, 0, width-1, height-1);

        if (m_currentPlate == null) {
            return;
        }

        double wellWidth = ((double)width - BLANK_SPACE_HORIZONTAL) / m_dimX;
        double wellHeight = ((double)height - BLANK_SPACE_VERTICAL) / m_dimY;

        VirtualPlateJson plate = m_currentPlate.getPlate();
        for (int i=0;i<m_dimX;i++) {
            for (int j=0;j<m_dimY;j++) {

                m_wells[i][j].paint(g, BLANK_SPACE_HORIZONTAL, BLANK_SPACE_VERTICAL, wellWidth, wellHeight, plate, m_colorMode);
            }
        }


        m_selectionGesture.paint(g);

    }



    public void drop(int x, int y, ColoredRobotPlanning robotPlanning) {

        //checkIntegrity();

        int loadCount = robotPlanning.getNbWells();

        x -= BLANK_SPACE_HORIZONTAL;
        y -= BLANK_SPACE_VERTICAL;

        double wellWidth = ((double)getWidth() - BLANK_SPACE_HORIZONTAL) / m_dimX;
        double wellHeight = ((double)getHeight() - BLANK_SPACE_VERTICAL) / m_dimY;

        double iDoubleX = (x / wellWidth);
        double iDoubleY = (y / wellHeight);

        clearSelection();

        //checkIntegrity();

        if ((iDoubleX<0) || (iDoubleY<0)) {
            return;
        }

        int ix = (int) iDoubleX;
        int iy = (int) iDoubleY;



        boolean horizontalDrop = (iDoubleX - ix > iDoubleY - iy);
        boolean split = false;
        if (horizontalDrop) {
            // For horizontal drop, we allow ton continue on next line
            int ixCur = ix;
            int iyCur = iy;
            if (m_wells[ixCur][iyCur].getColoredRobotPlanning() != null) {
                return;
            }
            for (int i=0;i<loadCount;i++) {
                if (iyCur>=m_dimY) {
                    return;
                }
                if (m_wells[ixCur][iyCur].getColoredRobotPlanning() != null) {
                    split = true;
                    ixCur++;
                    if (ixCur>=m_dimX) {
                        ixCur = 0;
                        iyCur++;
                    }
                    i--;
                    continue;
                }
                ixCur++;
                if (ixCur>=m_dimX) {
                    ixCur = 0;
                    iyCur++;
                }
            }

            //checkIntegrity();

        } else {
            // For vertical drop, we allow ton continue on next column
            int ixCur = ix;
            int iyCur = iy;
            if (m_wells[ixCur][iyCur].getColoredRobotPlanning() != null) {
                return;
            }
            for (int i=0;i<loadCount;i++) {
                if (ixCur>=m_dimX) {
                    return;
                }
                if (m_wells[ixCur][iyCur].getColoredRobotPlanning() != null) {
                    split = true;
                    iyCur++;
                    if (iyCur>=m_dimY) {
                        iyCur = 0;
                        ixCur++;
                    }
                    i--;
                    continue;
                }
                iyCur++;
                if (iyCur>=m_dimY) {
                    iyCur = 0;
                    ixCur++;
                }
            }

            //checkIntegrity();
        }

        m_movingGroup.clear();

        robotPlanning.clearWells();

        //checkIntegrity();

        if (horizontalDrop) {
            Well well = null;
            int ixCur = ix;
            int iyCur = iy;
            boolean emptySpace = false;
            for (int i=0;i<loadCount;i++) {

                well = m_wells[ixCur][iyCur];


                if (well.getColoredRobotPlanning() == null) {
                    well.setRobotPlanning(robotPlanning);
                    well.setLastWell(false);
                    robotPlanning.addWell(m_wells[ixCur][iyCur]);
                } else {
                    emptySpace = true;
                    ixCur++;
                    if (ixCur>=m_dimX) {
                        ixCur = 0;
                        iyCur++;
                    }
                    i--;
                    continue;
                }

                ixCur++;
                if (ixCur>=m_dimX) {
                    ixCur = 0;
                    iyCur++;
                }

            }
            well.setLastWell(true);

            //checkIntegrity();

            if (split) {
                ColoredRobotPlanning rb = well.getColoredRobotPlanning();

                // let one well to the ColoredRobotPlanning
                rb.clearWells();
                rb.addWell(m_wells[ix][iy]);

                for (int i = 0; i < m_dimX; i++) {
                    for (int j = 0; j < m_dimY; j++) {
                        if (!((i == ix) && (j == iy))) {
                            if (m_wells[i][j].remove(rb)) {
                                ColoredRobotPlanning rbAlone = new ColoredRobotPlanning(rb.m_robotPlanning, rb.m_color, rb.m_userColor, rb.m_trypsinColor);
                                m_wells[i][j].setRobotPlanning(rbAlone);
                                rbAlone.addWell(m_wells[i][j]);
                            }
                        }
                    }
                }
            }

            //checkIntegrity();

        } else {
            Well well = null;
            int ixCur = ix;
            int iyCur = iy;
            boolean emptySpace = false;

            for (int i=0;i<loadCount;i++) {

                well = m_wells[ixCur][iyCur];

                if (well.getColoredRobotPlanning() == null) {
                    well.setRobotPlanning(robotPlanning);
                    well.setLastWell(false);
                    robotPlanning.addWell(m_wells[ixCur][iyCur]);
                } else {
                    emptySpace = true;
                    iyCur++;
                    if (iyCur>=m_dimY) {
                        iyCur = 0;
                        ixCur++;
                    }
                    i--;
                    continue;
                }

                iyCur++;
                if (iyCur>=m_dimY) {
                    iyCur = 0;
                    ixCur++;
                }

            }
            well.setLastWell(true);

            //checkIntegrity();

            if (split) {
                ColoredRobotPlanning rb = well.getColoredRobotPlanning();

                // let one well to the ColoredRobotPlanning
                rb.clearWells();
                rb.addWell(m_wells[ix][iy]);

                for (int i = 0; i < m_dimX; i++) {
                    for (int j = 0; j < m_dimY; j++) {
                        if (!((i == ix) && (j == iy))) {
                            if (m_wells[i][j].remove(rb)) {
                                ColoredRobotPlanning rbAlone = new ColoredRobotPlanning(rb.m_robotPlanning, rb.m_color, rb.m_userColor, rb.m_trypsinColor);
                                m_wells[i][j].setRobotPlanning(rbAlone);
                                rbAlone.addWell(m_wells[i][j]);
                            }
                        }
                    }
                }
            }

            //checkIntegrity();
        }

        if ((robotPlanning.getNbWells() == 1) && (robotPlanning.m_robotPlanning.getLoadCount()>1)) {
            // we try to relink when it is possible
            relinkAfterDrop(robotPlanning);
        }

        //checkIntegrity();

        plateModified();

        repaint();
    }

    public void drop(int x, int y, MovingGroup movingGroup) {

        //checkIntegrity();

        if (!movingGroup.isDropAllowed()) {
            return;
        }
        movingGroup.drop(m_wells);

        m_movingGroup.clear();  //JPM.WART

        //checkIntegrity();

        clearSelection();

        //checkIntegrity();

        relinkAfterDrop();

        //checkIntegrity();

        plateModified();

        //checkIntegrity();

        repaint();
    }

    public boolean canDrop(int x, int y) {

        //checkIntegrity();

        x -= BLANK_SPACE_HORIZONTAL;
        y -= BLANK_SPACE_VERTICAL;

        double wellWidth = ((double)getWidth() - BLANK_SPACE_HORIZONTAL) / m_dimX;
        double wellHeight = ((double)getHeight() - BLANK_SPACE_VERTICAL) / m_dimY;

        double iDoubleX = (x / wellWidth);
        double iDoubleY = (y / wellHeight);

        if ((iDoubleX<0) || (iDoubleY<0)) {
            return false;
        }

        int ix = (int) iDoubleX;
        int iy = (int) iDoubleY;


        clearSelection();


        boolean canDrop = m_movingGroup.canDrop(m_wells, ix, iy, (iDoubleX - ix > iDoubleY - iy));

        //checkIntegrity();

        repaint();

        return canDrop;
    }


    @Override
    public void dragGestureRecognized(DragGestureEvent event) {

        Point point = event.getDragOrigin();

        double wellWidth = ((double)getWidth() - BLANK_SPACE_HORIZONTAL) / m_dimX;
        double wellHeight = ((double)getHeight() - BLANK_SPACE_VERTICAL) / m_dimY;

        double iDoubleX = ((point.getX() - BLANK_SPACE_HORIZONTAL) / wellWidth);
        double iDoubleY = ((point.getY() - BLANK_SPACE_VERTICAL)/ wellHeight);

        if ((iDoubleX<0) || (iDoubleY<0)) {
            return;
        }

        int ix = (int) iDoubleX;
        int iy = (int) iDoubleY;

        if (! m_wells[ix][iy].isSelected()) {
            return;
        }


        m_movingGroup.defineGroup(m_wells);
        m_movingGroup.setOverCenter(ix, iy);


        Cursor cursor = Cursor.getDefaultCursor();


        if (event.getDragAction() == DnDConstants.ACTION_MOVE) {
            cursor = DragSource.DefaultMoveDrop;
        }

        event.startDrag(cursor, new TransferableInnerRobotPlanning(m_movingGroup), this);
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {

        Point p = new Point(dsde.getLocation());

        SwingUtilities.convertPointFromScreen(p, this);
        if ((p.x<0) || (p.y<0) || (p.x>=getWidth()) || (p.y>=getHeight())) {
            return;
        }

        if (canDrop(p.x, p.y)) {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        } else {
            dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        }


    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {

    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor(null);

        clearOverAndHighliting();
        repaint();
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {

        //checkIntegrity();

        if (! m_movingGroup.m_sourceDnDWells.isEmpty()) {
            clearSelection();
            for (Well well : m_movingGroup.m_sourceDnDWells) {
                well.setRobotPlanning(m_movingGroup.m_movingRobotPlanning[well.m_x][well.m_y]);
                well.setHighlited(true);
            }
            m_movingGroup.clear();


        } else {
            plateModified();
        }

        //checkIntegrity();

        m_wellSelectionManager.selectionChanged(SELECTION_SOURCE_KEY, getSampleOfSelectedWells());
        repaint();

    }

    private HashSet<String> getSampleOfSelectedWells() {

        HashSet<String> sampleNameSet = new HashSet<>();

        for (int i = 0; i < m_dimX; i++) {
            for (int j = 0; j < m_dimY; j++) {
                Well well = m_wells[i][j];
                if (well.isSelected()) {
                    ColoredRobotPlanning coloredRobotPlanning = well.getColoredRobotPlanning();
                    if (coloredRobotPlanning != null) {
                        String sampleName = coloredRobotPlanning.m_robotPlanning.getSample().getName();
                        sampleNameSet.add(sampleName);
                    }
                }
            }
        }

        return sampleNameSet;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        double wellWidth = ((double)getWidth() - BLANK_SPACE_HORIZONTAL) / m_dimX;
        double wellHeight = ((double)getHeight() - BLANK_SPACE_VERTICAL) / m_dimY;

        double iDoubleX = ((x-BLANK_SPACE_HORIZONTAL) / wellWidth);
        double iDoubleY = ((y-BLANK_SPACE_VERTICAL) / wellHeight);

        boolean over = false;

        //checkIntegrity();

        if ((iDoubleX>=0) && (iDoubleY>=0)) {


            int ix = (int) iDoubleX;
            int iy = (int) iDoubleY;



            Well well = m_wells[ix][iy];
            if (well.isOverSplitButton(x, y)) {
                ColoredRobotPlanning rb = well.getColoredRobotPlanning();

                // let one well to the ColoredRobotPlanning
                rb.clearWells();
                rb.addWell(m_wells[ix][iy]);

                for (int i = 0; i < m_dimX; i++) {
                    for (int j = 0; j < m_dimY; j++) {
                        if (!((i == ix) && (j == iy))) {
                            if (m_wells[i][j].remove(rb)) {
                                ColoredRobotPlanning rbAlone = new ColoredRobotPlanning(rb.m_robotPlanning, rb.m_color, rb.m_userColor, rb.m_trypsinColor);
                                m_wells[i][j].setRobotPlanning(rbAlone);
                                rbAlone.addWell(m_wells[i][j]);
                            }
                        }
                    }
                }
                over = true;
                repaint();

                //checkIntegrity();
            } else if (well.isOver(x, y)) {

                int modifiers = e.getModifiers();
                boolean ctrlKeyPressed = ((modifiers & KeyEvent.CTRL_MASK) != 0);
                boolean shiftKeyPressed = ((modifiers & KeyEvent.SHIFT_MASK) != 0);
                boolean currentSelection = well.isSelected();

                boolean selection;
                boolean addRemoveSelectionMode = false;
                if (ctrlKeyPressed || shiftKeyPressed) {
                    selection = ! currentSelection;
                    addRemoveSelectionMode = true;
                } else {
                    selection = true;
                    if (!currentSelection) {
                        clearSelection();
                    } else {
                        // do nothing in fact (deselection will be done when the button is released)
                    }
                }

                HashSet<ColoredRobotPlanning> selectedColoredRobotPlanning = new HashSet<>();
                for (int i=0;i<m_dimX;i++) {
                    for (int j=0;j<m_dimY;j++) {
                        if (m_wells[i][j].isSelected()) {
                            selectedColoredRobotPlanning.add(m_wells[i][j].m_coloredRobotPlanning);
                        }
                    }
                }
                if ((selectedColoredRobotPlanning.size()<=1) || (addRemoveSelectionMode)) {
                    // only one type of sample selected.
                    // We select linked wells for this sample.
                    for (Well wellCur : well.getColoredRobotPlanning().m_wells) {
                        wellCur.setSelected(selection);
                    }
                }

                if (shiftKeyPressed) {
                    // if shift key is pressed, we select or unselect all wells correspond to a sample (even if the wells are not linked
                    String sampleKey = well.getColoredRobotPlanning().m_robotPlanning.getSample().getName();
                    for (int i = 0; i < m_dimX; i++) {
                        for (int j = 0; j < m_dimY; j++) {
                            Well wellCur = m_wells[i][j];
                            ColoredRobotPlanning coloredRobotPlanning = wellCur.m_coloredRobotPlanning;
                            if (coloredRobotPlanning != null) {
                                if (coloredRobotPlanning.m_robotPlanning.getSample().getName().equals(sampleKey)) {
                                    wellCur.setSelected(selection);
                                }
                            }
                        }
                    }
                }

                repaint();
                over = true;
                m_wellSelectionManager.selectionChanged(SELECTION_SOURCE_KEY, getSampleOfSelectedWells());

                //checkIntegrity();
            }
        }

        if (!over) {
            int modifiers = e.getModifiers();
            if (((modifiers & KeyEvent.CTRL_MASK) == 0) && ((modifiers & KeyEvent.SHIFT_MASK) == 0)) {
                clearSelection();

                m_wellSelectionManager.selectionChanged(SELECTION_SOURCE_KEY, getSampleOfSelectedWells());
            }
            m_selectionGesture.startSelection(x, y);
        }
    }

    /**
     * Used to check integrity of the data structure of wells and ColoredRobotPlanning
     */
    private void checkIntegrity() {
        for (int i = 0; i < m_dimX; i++) {
            for (int j = 0; j < m_dimY; j++) {
                Well wellCur = m_wells[i][j];
                ColoredRobotPlanning coloredRobotPlanning = wellCur.m_coloredRobotPlanning;
                if (coloredRobotPlanning != null) {
                    if (coloredRobotPlanning.getNbWells() == 1) {
                        // check that the well is the current well
                        Well wellInColorPlanning = coloredRobotPlanning.m_wells.get(0);
                        if ((wellCur.m_x != wellInColorPlanning.m_x) || (wellCur.m_y != wellInColorPlanning.m_y)) {
                            System.out.println("Integrity problem");
                        }

                    }
                }
            }
        }
    }

    private void clearSelection() {

        for (int i=0;i<m_dimX;i++) {
            for (int j=0;j<m_dimY;j++) {
                m_wells[i][j].setOver(false, false, false);
                m_wells[i][j].setSelected(false);
                m_wells[i][j].setHighlited(false);
                m_wells[i][j].setOnError(false);
            }
        }
    }

    private void clearOverAndHighliting() {

        for (int i=0;i<m_dimX;i++) {
            for (int j=0;j<m_dimY;j++) {
                m_wells[i][j].setOver(false, false, false);
                m_wells[i][j].setHighlited(false);
                m_wells[i][j].setOnError(false);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.stopSelection(e.getX(), e.getY());
            int action = m_selectionGesture.getAction();
            if (action == SelectionGestureSquare.ACTION_SURROUND) {

                Path2D.Double selectionShape = m_selectionGesture.getSelectionPath();

                int modifiers = e.getModifiers();
                boolean isCtrlOrShiftDown = ((modifiers & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);
                if (!isCtrlOrShiftDown) {
                    clearSelection();
                }

                Point minPoint = m_selectionGesture.getMinPoint();
                Point maxPoint = m_selectionGesture.getMaxPoint();

                // Reduce selection area to avoid side selection at borders
                int REDUCE_DELTA = 10;
                minPoint.x += REDUCE_DELTA;
                maxPoint.x -= REDUCE_DELTA;
                minPoint.y += REDUCE_DELTA;
                maxPoint.y -= REDUCE_DELTA;

                double wellWidth = ((double)getWidth() - BLANK_SPACE_HORIZONTAL) / m_dimX;
                double wellHeight = ((double)getHeight() - BLANK_SPACE_VERTICAL) / m_dimY;

                double iDoubleX1 = ((minPoint.x - BLANK_SPACE_HORIZONTAL) / wellWidth);
                double iDoubleY1 = ((minPoint.y - BLANK_SPACE_VERTICAL) / wellHeight);

                if (iDoubleX1<0) {
                    iDoubleX1 = 0;
                }
                if (iDoubleY1<0) {
                    iDoubleY1 = 0;
                }
                int ix1 = (int) iDoubleX1;
                int iy1 = (int) iDoubleY1;

                double iDoubleX2 = ((maxPoint.x - BLANK_SPACE_HORIZONTAL) / wellWidth);
                double iDoubleY2 = ((maxPoint.y - BLANK_SPACE_VERTICAL) / wellHeight);

                if (iDoubleX2<0) {
                    iDoubleX2 = -1;
                }
                if (iDoubleY2<0) {
                    iDoubleY2 = -1;
                }

                int ix2 = (int) (iDoubleX2);
                int iy2 = (int) (iDoubleY2);

                if (ix2 >= m_dimX) {
                    ix2 = m_dimX-1;
                }
                if (iy2 >= m_dimY) {
                    iy2 = m_dimY-1;
                }


                for (int i=ix1;i<=ix2;i++) {
                    for (int j=iy1;j<=iy2;j++) {
                        if (m_wells[i][j].getColoredRobotPlanning() != null) {
                            m_wells[i][j].setSelected(true);
                        }
                    }
                }

                m_wellSelectionManager.selectionChanged(SELECTION_SOURCE_KEY, getSampleOfSelectedWells());


            }

            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.continueSelection(e.getX(), e.getY());
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void selectionChanged(HashSet<String> sampleNames) {
        for (int i = 0; i < m_dimX; i++) {
            for (int j = 0; j < m_dimY; j++) {
                Well well = m_wells[i][j];
                well.setSelected(false);
                ColoredRobotPlanning coloredRobotPlanning =  well.getColoredRobotPlanning();
                if (coloredRobotPlanning != null) {
                    String sampleName = coloredRobotPlanning.m_robotPlanning.getSample().getName();
                    if (sampleNames.contains(sampleName)) {
                        well.setSelected(true);
                    }
                }
            }
        }
        repaint();
    }


    public static class Well implements Serializable {

        private static final BasicStroke STROKE3 = new BasicStroke(3);


        private int m_x;
        private int m_y;

        private final int PAD = 5;

        private ColoredRobotPlanning m_coloredRobotPlanning = null;

        private boolean m_over = false;
        private boolean m_overHorizontalDrop = false;
        private boolean m_singleDrop = false;
        private boolean m_selected = false;
        private boolean m_highlited = false;
        private boolean m_error = false;

        private boolean m_lastWell = false;
        private int m_splitX;
        private int m_splitY;

        private int m_innerX1;
        private int m_innerX2;
        private int m_innerY1;
        private int m_innerY2;



        private VirtualWellJson m_virtualWell;

        private static JLabel m_drawingLabel = new JLabel();

        public Well(int x, int y) {
            m_x = x;
            m_y = y;
        }

        public void setVirtualWell(VirtualWellJson virtualWell) {
            m_virtualWell = virtualWell;

        }

        public VirtualWellJson getVirtualWell() {
            return m_virtualWell;
        }



        public void setX(int x) {
            m_x = x;
        }

        public boolean remove(ColoredRobotPlanning coloredRobotPlanning) {
            if (this.m_coloredRobotPlanning == null) {
                return false;
            }
            if (this.m_coloredRobotPlanning.equals(coloredRobotPlanning)) {
                this.m_coloredRobotPlanning = null;
                return true;
            }
            return false;
        }

        public void setOver(boolean over, boolean overHorizontalDrop, boolean multipleWell) {
            m_over = over;
            m_overHorizontalDrop = overHorizontalDrop;
            m_singleDrop = !multipleWell;
        }

        public void setLastWell(boolean lastWell) {
            m_lastWell = lastWell;
        }

        public void setSelected(boolean selected) {
            m_selected = selected;
        }

        public boolean isSelected() {
            return m_selected;
        }

        public void setHighlited(boolean highlited) {
            m_highlited = highlited;
        }
        public void setOnError(boolean error) {
            m_error = error;
        }

        public boolean isHighlited() {
            return m_highlited;
        }

        public void setRobotPlanning(ColoredRobotPlanning robotPlanning) {
            m_coloredRobotPlanning = robotPlanning;
        }

        public ColoredRobotPlanning getColoredRobotPlanning() {
            return m_coloredRobotPlanning;
        }


        public void paint(Graphics g, int startX, int startY, double width, double height, VirtualPlateJson plate, COLOR_MODE colorMode) {

            Graphics2D g2D = (Graphics2D) g;

            int pixX = startX + (int) Math.round(m_x * width);
            int pixY = startY + (int) Math.round(m_y * height);
            int pixWidth = (int) Math.round(width);
            int pixHeight = (int) Math.round(height);


            if (m_over && !m_singleDrop) {
                g.setColor(Color.lightGray);
                g.drawLine(pixX + PAD, pixY + PAD, pixX + pixWidth - PAD, pixY + pixHeight - PAD);

                if (m_overHorizontalDrop) {
                    Image img = IconManager.getImage(IconManager.IconType.ADD_HORIZONTAL_ROBOT);
                    g.drawImage(img, pixX + pixWidth - PAD*2-img.getWidth(null), pixY+PAD*2, null);
                } else {
                    Image img = IconManager.getImage(IconManager.IconType.ADD_VERTICAL_ROBOT);
                    g.drawImage(img, pixX + PAD*2, pixY+pixHeight-2*PAD-img.getHeight(null), null);
                }
            }

            if (m_coloredRobotPlanning != null) {
                switch (colorMode) {
                    case SAMPLE_COLOUR:
                        g.setColor(m_coloredRobotPlanning.m_color);
                        break;
                    case USER_COLOUR:
                        g.setColor(m_coloredRobotPlanning.m_userColor);
                        break;
                    case TRYPSIN_COLOUR: {
                        g.setColor(m_coloredRobotPlanning.m_trypsinColor);
                        break;
                    }
                    case STUDY_COLOUR: {
                        g.setColor(m_coloredRobotPlanning.getStudyColor(plate));
                        break;
                    }
                }

                g.fillRect(pixX+PAD, pixY+PAD, pixWidth-PAD*2, pixHeight-PAD*2);

                SampleJson s = m_coloredRobotPlanning.m_robotPlanning.getSample();
                SampleSpeciesJson species = DataManager.getSampleSpecies(s.getBiologicOriginJson().getSampleSpecies());

                g.setColor(CyclicColorPalette.getColor(species.getId(), CyclicColorPalette.GROUP4_PALETTE));
                g.fillRect(pixX+PAD+2, pixY+PAD+2, 10, 10);
                g.setColor(Color.black);
                g.drawRect(pixX+PAD+2, pixY+PAD+2, 10, 10);

                if ((m_coloredRobotPlanning.getNbWells()>1) && (m_lastWell)) {
                    m_splitX = pixX + pixWidth - PAD - 16 - 4;
                    m_splitY = pixY + PAD + 2;
                    g.setColor(Color.white);
                    g.fillRect(m_splitX, m_splitY, 16 + 2, 16 + 2);
                    g.setColor(Color.black);
                    g.drawRect(m_splitX, m_splitY, 16 + 2, 16 + 2);
                    g.drawImage(IconManager.getImage(IconManager.IconType.ARROW_SPLIT), pixX + pixWidth - PAD - 16 - 2, pixY + 2 * PAD, null);
                }
            }

            g.setColor(m_error ? ERROR_COLOR : Color.black);
            m_innerX1 = pixX+PAD;
            m_innerX2 = m_innerX1+pixWidth-PAD*2;
            m_innerY1 = pixY+PAD;
            m_innerY2 = m_innerY1+pixHeight-PAD*2;
            if (m_over || m_selected || m_highlited) {
                Stroke oldStroke = g2D.getStroke();
                g2D.setStroke(STROKE3);
                g.drawRect(pixX+PAD, pixY+PAD, pixWidth-PAD*2, pixHeight-PAD*2);
                g2D.setStroke(oldStroke);
            } else {
                g.drawRect(pixX+PAD, pixY+PAD, pixWidth-PAD*2, pixHeight-PAD*2);
            }

            if (m_coloredRobotPlanning != null) {
                m_drawingLabel.setText(m_coloredRobotPlanning.m_robotPlanning.getSample().getName());
                g.translate(pixX + PAD*2, pixY + PAD + 16); // 16 : icon size
                m_drawingLabel.setSize(pixWidth - PAD * 4, pixHeight - PAD * 2 - 16);
                m_drawingLabel.paint(g);
                g.translate(-pixX - PAD*2, -pixY - PAD - 16);
            }

        }






        public boolean isOver(int x, int y) {
            if (m_coloredRobotPlanning == null) {
                return false;
            }
            return ! ((x<m_innerX1) || (y<m_innerY1) || (x>m_innerX2) || (y>m_innerY2));
        }

        public boolean isOverSplitButton(int x, int y) {
            if (m_coloredRobotPlanning == null || m_coloredRobotPlanning.getNbWells()<=1 || !m_lastWell) {
                return false;
            }

            return ! ((x<m_splitX) || (y<m_splitY) || (x>m_splitX+18) || (y>m_splitY+18));
        }

        public int getBetweenColumnsIndex(int x, double width, int nbWellsAlongX) {

            int pixX = (int) Math.round(m_x * width);
            if (x<pixX+PAD) {
                if (m_x == 0) {
                    return -1;
                }
                return m_x;
            }
            if (x>pixX + width - PAD) {
                if (m_x == nbWellsAlongX-1) {
                    return -1;
                }
                return m_x+1;
            }

            return -1;
        }

    }



    public class PanelTransferHandler extends TransferHandler {

        private RobotPlatePanel m_robotPlatePanel = null;

        public PanelTransferHandler(RobotPlatePanel robotPlatePanel) {
            m_robotPlatePanel = robotPlatePanel;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return null;

        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {

            boolean canImport = false;

            if (info.isDataFlavorSupported(RobotSamplesPanel.TransferableRobotPlanning.UNUSED_ROBOT_PLANNING_FLAVOR)) {
                canImport = canImportRobotPlanningFromTable(info);
            } else if (info.isDataFlavorSupported(TransferableInnerRobotPlanning.PLATE_ROBOT_PLANNING_FLAVOR)) {
                canImport = canImportInnerRobotPlanning(info);
            }

            return canImport;
        }
        public boolean canImportRobotPlanningFromTable(TransferHandler.TransferSupport info) {

            if (m_currentPlate == null) {
                return false;
            }

            Point point = info.getDropLocation().getDropPoint();

            if (point == null) {
                return false;
            }

            ColoredRobotPlanning coloredRobotPlanning;
            int loadCount;
            try {
                coloredRobotPlanning = (ColoredRobotPlanning) info.getTransferable().getTransferData(RobotSamplesPanel.TransferableRobotPlanning.UNUSED_ROBOT_PLANNING_FLAVOR);
                RobotPlanningJson robotPlanning = coloredRobotPlanning.m_robotPlanning;

                loadCount = robotPlanning.getLoadCount();


            } catch (Exception e) {
                return false;
            }

            if (loadCount == 0) {
                return false;
            }

            m_movingGroup.definGroupForTable(coloredRobotPlanning);


            return  canDrop(point.x, point.y);
        }
        public boolean canImportInnerRobotPlanning(TransferHandler.TransferSupport info) {

            return true; //JPM.WART
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {

            if (m_currentPlate == null) {
                m_movingGroup.clear();
                m_robotPlatePanel.repaint();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {

                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "No Plate Selected", "You must select a Plate beforehand.");
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                    }
                });
                return false;
            }

            DropLocation location = info.getDropLocation();
            Point point = location.getDropPoint();

            try {

                if (info.isDataFlavorSupported(RobotSamplesPanel.TransferableRobotPlanning.UNUSED_ROBOT_PLANNING_FLAVOR)) {
                    ColoredRobotPlanning robotPlanning = (ColoredRobotPlanning) info.getTransferable().getTransferData(RobotSamplesPanel.TransferableRobotPlanning.UNUSED_ROBOT_PLANNING_FLAVOR);
                    m_robotPlatePanel.drop(point.x, point.y, robotPlanning);
                } else if (info.isDataFlavorSupported(TransferableInnerRobotPlanning.PLATE_ROBOT_PLANNING_FLAVOR)) {
                    MovingGroup movingGroup = (MovingGroup) info.getTransferable().getTransferData(TransferableInnerRobotPlanning.PLATE_ROBOT_PLANNING_FLAVOR);
                    m_robotPlatePanel.drop(point.x, point.y, movingGroup);
                }

            } catch (Exception e) {
                return false;
            }



            return true;
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int act) {
        }

    }




    public static class TransferableInnerRobotPlanning implements Transferable {

        private MovingGroup m_movingGroupData;

        public static final DataFlavor PLATE_ROBOT_PLANNING_FLAVOR = new DataFlavor(MovingGroup.class, "Robot Planning");

        DataFlavor[] m_flavors = null;

        public TransferableInnerRobotPlanning(MovingGroup movingGroup) {
            m_movingGroupData = movingGroup;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            if (m_flavors == null) {
                m_flavors = new DataFlavor[1];
                m_flavors[0] = PLATE_ROBOT_PLANNING_FLAVOR;
            }
            return m_flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(PLATE_ROBOT_PLANNING_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                if (flavor.equals(PLATE_ROBOT_PLANNING_FLAVOR)) {
                    return m_movingGroupData;
                }
            }

            return null;
        }

    }

    public static class MovingGroup implements Serializable {
        private ArrayList<Well> m_sourceDnDWells;
        private ColoredRobotPlanning[][] m_movingRobotPlanning = null;
        private int m_dragWellIndexX = 0;
        private int m_dragWellIndexY = 0;

        private ColoredRobotPlanning m_fromTableColoredRobotPlanning;

        private int m_dimX;
        private int m_dimY;

        private int m_minIndexX;
        private int m_maxIndexX;
        private int m_minIndexY;
        private int m_maxIndexY;

        public static int HORIZONTAL_MODE = 0;
        public static int VERTICAL_MODE = 1;
        public static int GROUP_MODE = 2;

        public boolean m_canDrop;

        public MovingGroup(int dimX, int dimY) {
            m_dimX = dimX;
            m_dimY = dimY;

            m_sourceDnDWells = new ArrayList<>();
            m_movingRobotPlanning = new ColoredRobotPlanning[dimX][dimY];

            clear();
        }

        public ArrayList<ColoredRobotPlanning> removeRobotPlanningList() {
            HashMap<String, ColoredRobotPlanning> robotPlanningMap = new HashMap<>();
            for (Well well : m_sourceDnDWells) {
                ColoredRobotPlanning coloredRobotPlanning = m_movingRobotPlanning[well.m_x][well.m_y];
                robotPlanningMap.put(coloredRobotPlanning.m_robotPlanning.getSample().getName(), coloredRobotPlanning);
            }
            ArrayList<ColoredRobotPlanning> coloredRobotPlanningList = new ArrayList<>(robotPlanningMap.values());

            clear();
            m_movingGroup.clear();

            return coloredRobotPlanningList;
        }

        private void clear() {
            m_sourceDnDWells.clear();

            for (int i=0;i<m_dimX;i++) {
                for (int j=0;j<m_dimY;j++) {
                    m_movingRobotPlanning[i][j] = null;
                }
            }


        }

        public void setOverCenter(int ix, int iy) {

            if (getDropMode(true) == GROUP_MODE) {
                m_dragWellIndexX = ix;
                m_dragWellIndexY = iy;
            } else {
                m_dragWellIndexX = m_minIndexX;
                m_dragWellIndexY = m_maxIndexX;
            }

        }

        public void definGroupForTable(ColoredRobotPlanning coloredRobotPlanning) {

            if ((m_fromTableColoredRobotPlanning != null) && (m_fromTableColoredRobotPlanning.equals(coloredRobotPlanning))) {
                return;
            }
            m_fromTableColoredRobotPlanning = coloredRobotPlanning;

            m_sourceDnDWells.clear();
            for (int i=0;i<m_fromTableColoredRobotPlanning.getNbWells();i++) {
                Well w = new Well(i,0);
                m_sourceDnDWells.add(w);
            }

            m_dragWellIndexX = 0;
            m_dragWellIndexY = 0;
        }

        public void defineGroup( Well[][] wells) {

            m_minIndexX = m_dimX;
            m_maxIndexX = 0;
            m_minIndexY = m_dimY;
            m_maxIndexY = 0;

            for (int i=0;i<m_dimX;i++) {
                for (int j = 0; j < m_dimY; j++) {
                    if (wells[i][j].isSelected()) {
                        m_sourceDnDWells.add(wells[i][j]);
                        m_movingRobotPlanning[i][j] = wells[i][j].getColoredRobotPlanning();
                        wells[i][j].setRobotPlanning(null);
                        if (i<m_minIndexX) {
                            m_minIndexX = i;
                        }
                        if (i>m_maxIndexX) {
                            m_maxIndexX = i;
                        }
                        if (j<m_minIndexY) {
                            m_minIndexY = j;
                        }
                        if (j>m_maxIndexY) {
                            m_maxIndexY = j;
                        }
                    } else {
                        m_movingRobotPlanning[i][j] = null;
                    }
                }
            }

        }

        public int getDropMode(boolean overRightZone) {
            if (isFlatGroup()) {
                if (overRightZone) {
                    return HORIZONTAL_MODE;
                } else {
                    return VERTICAL_MODE;
                }
            } else {
                return GROUP_MODE;
            }

        }

        public boolean isFlatGroup() {
            if (m_sourceDnDWells.size()<=1) {
                return false;
            }

            // check if horizontal
            Collections.sort(m_sourceDnDWells, (well1, well2) -> {
                if (well1.m_y!=well2.m_y) {
                    return well1.m_y - well2.m_y;
                }
                return well1.m_x - well2.m_x;
            });

            int ix = -1;
            int iy = -1;
            boolean selectionIsHorizontal = true;
            for (Well well : m_sourceDnDWells) {
                if (ix == -1) {
                    ix = well.m_x;
                    iy = well.m_y;
                    continue;
                }
                if ((well.m_y != iy) || (well.m_x-1 != ix)) {
                    if (ix == m_dimX-1) {
                        iy++;
                        ix = 0;
                        if ((well.m_x != ix) || (well.m_y != iy)) {
                            selectionIsHorizontal = false;
                        }
                    } else {
                        selectionIsHorizontal = false;
                    }
                }


                ix = well.m_x;
                iy = well.m_y;

            }

            // check if vertical
            Collections.sort(m_sourceDnDWells, (well1, well2) -> {
                if (well1.m_x!=well2.m_x) {
                    return well1.m_x - well2.m_x;
                }
                return well1.m_y - well2.m_y;
            });


            ix = -1;
            iy = -1;
            boolean selectionIsVertical = true;
            for (Well well : m_sourceDnDWells) {
                if (ix == -1) {
                    ix = well.m_x;
                    iy = well.m_y;
                    continue;
                }
                if ((well.m_x != ix) || (well.m_y-1 != iy)) {
                    if (iy == m_dimY-1) {
                        ix++;
                        iy = 0;
                        if ((well.m_x != ix) || (well.m_y != iy)) {
                            selectionIsVertical = false;
                        }
                    } else {
                        selectionIsVertical = false;
                    }
                }

                ix = well.m_x;
                iy = well.m_y;

            }


            return (selectionIsHorizontal || selectionIsVertical);
        }

        public boolean isDropAllowed() {
            return m_canDrop;
        }

        public boolean canDrop(Well[][] wells, int overIndexX, int overIndexY, boolean overRightZone) {

            m_canDrop = false;

            int mode = getDropMode(overRightZone);

            wells[overIndexX][overIndexY].setOver(true, true, false);

            if (mode == GROUP_MODE) {
                boolean collision = false;
                for (Well well : m_sourceDnDWells) {
                    int ix = well.m_x - m_dragWellIndexX + overIndexX;
                    int iy = well.m_y - m_dragWellIndexY + overIndexY;
                    if ((ix<0) || (iy<0) || (ix>=m_dimX) || (iy>=m_dimY)) {
                        collision = true;
                        continue;
                    }
                    if (wells[ix][iy].getColoredRobotPlanning() != null) {
                        collision = true;
                    }

                }
                if (collision) {
                    for (Well well : m_sourceDnDWells) {
                        int ix = well.m_x - m_dragWellIndexX + overIndexX;
                        int iy = well.m_y - m_dragWellIndexY + overIndexY;
                        if ((ix<0) || (iy<0) || (ix>=m_dimX) || (iy>=m_dimY)) {
                            continue;
                        }
                        wells[ix][iy].setHighlited(true);
                        wells[ix][iy].setOnError(true);
                    }
                    return false;
                }
                for (Well well : m_sourceDnDWells) {
                    int ix = well.m_x - m_dragWellIndexX + overIndexX;
                    int iy = well.m_y - m_dragWellIndexY + overIndexY;
                    wells[ix][iy].setHighlited(true);
                }

            } else if (mode == HORIZONTAL_MODE) {
                int nb = m_sourceDnDWells.size();
                int ix = overIndexX;
                int iy = overIndexY;


                int emptySpace = 0;
                if (wells[ix][iy].getColoredRobotPlanning() == null) {

                    while (true) {
                        if (wells[ix][iy].getColoredRobotPlanning() == null) {
                            emptySpace ++;
                        }
                        if (emptySpace == nb) {
                            break;
                        }

                        ix++;
                        if (ix >= m_dimX) {
                            ix = 0;
                            iy++;
                        }
                        if (iy >= m_dimY) {
                            break;
                        }
                    }
                    if (emptySpace<nb) {
                        // not enough empty wells found
                        ix = overIndexX;
                        iy = overIndexY;
                        while (true) {
                            if (wells[ix][iy].getColoredRobotPlanning() != null) {
                                wells[ix][iy].setHighlited(true);
                                wells[ix][iy].setOnError(true);
                            }
                            ix++;
                            if (ix >= m_dimX) {
                                ix = 0;
                                iy++;
                            }
                            if (iy >= m_dimY) {
                                break;
                            }
                        }
                        return false;
                    }
                } else {
                    // current well is already filled
                    wells[ix][iy].setHighlited(true);
                    wells[ix][iy].setOnError(true);
                    return false;
                }

                ix = overIndexX;
                iy = overIndexY;
                emptySpace = 0;
                while (true) {
                    if (wells[ix][iy].getColoredRobotPlanning() == null) {
                        wells[ix][iy].setHighlited(true);
                        emptySpace ++;
                    }
                    if (emptySpace == nb) {
                        break;
                    }

                    ix++;
                    if (ix >= m_dimX) {
                        ix = 0;
                        iy++;
                    }
                    if (iy >= m_dimY) {
                        break;
                    }
                }

                wells[overIndexX][overIndexY].setOver(true, true, true);

            } else if (mode == VERTICAL_MODE) {

                int nb = m_sourceDnDWells.size();
                int ix = overIndexX;
                int iy = overIndexY;


                int emptySpace = 0;
                if (wells[ix][iy].getColoredRobotPlanning() == null) {

                    while (true) {
                        if (wells[ix][iy].getColoredRobotPlanning() == null) {
                            emptySpace++;
                        }
                        if (emptySpace == nb) {
                            break;
                        }

                        iy++;
                        if (iy >= m_dimY) {
                            iy = 0;
                            ix++;
                        }

                        if (ix >= m_dimX) {
                            break;
                        }
                    }

                    if (emptySpace<nb) {
                        // not enough empty wells found
                        ix = overIndexX;
                        iy = overIndexY;
                        while (true) {
                            if (wells[ix][iy].getColoredRobotPlanning() != null) {
                                wells[ix][iy].setHighlited(true);
                                wells[ix][iy].setOnError(true);
                            }
                            iy++;
                            if (iy >= m_dimY) {
                                iy = 0;
                                ix++;
                            }

                            if (ix >= m_dimX) {
                                break;
                            }
                        }
                        return false;
                    }
                } else {
                    // current well is already filled
                    wells[ix][iy].setHighlited(true);
                    wells[ix][iy].setOnError(true);
                    return false;
                }

                ix = overIndexX;
                iy = overIndexY;
                emptySpace = 0;
                while (true) {
                    if (wells[ix][iy].getColoredRobotPlanning() == null) {
                        wells[ix][iy].setHighlited(true);
                        emptySpace ++;
                    }
                    if (emptySpace == nb) {
                        break;
                    }

                    iy++;
                    if (iy >= m_dimY) {
                        iy = 0;
                        ix++;
                    }

                    if (ix >= m_dimX) {
                        break;
                    }
                }

                wells[overIndexX][overIndexY].setOver(true, false, true);



            }

            m_canDrop = true;
            return true;
        }

        public void drop(Well[][] wells) {

            int indexSource = 0;
            for (int i=0;i<m_dimX;i++) {
                for (int j = 0; j < m_dimY; j++) {
                    if (wells[i][j].isHighlited()) {
                        Well sourceWell = m_sourceDnDWells.get(indexSource);
                        indexSource++;

                        m_movingRobotPlanning[sourceWell.m_x][sourceWell.m_y].m_wells.remove(sourceWell);
                        wells[i][j].setRobotPlanning(m_movingRobotPlanning[sourceWell.m_x][sourceWell.m_y]);
                        wells[i][j].setLastWell(false);
                        m_movingRobotPlanning[sourceWell.m_x][sourceWell.m_y].addWell(wells[i][j]);
                    }
                }
            }

        }

    }

}
