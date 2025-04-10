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
import fr.edyp.epims.json.VirtualPlateJson;
import fr.epims.ui.common.CyclicColorPalette;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Used to link a RobotPlanning to a Color. So Each RobotPlanning has a specific
 * color in the Robot Plate
 *
 * @author JM235353
 *
 */
public class ColoredRobotPlanning implements Serializable, Comparable<ColoredRobotPlanning> {

    public RobotPlanningJson m_robotPlanning;
    public Color m_color;
    public Color m_userColor;
    public Color m_trypsinColor;
    public ArrayList<RobotPlatePanel.Well> m_wells = new ArrayList<>();
    public boolean m_linkedWells = true;

    private static HashMap<String, Integer> m_sampleToColorIndexMap = new HashMap<>();
    private static int sampleColorIndexIncrement = 0;

    private static HashMap<String, Integer> m_userToColorIndexMap = new HashMap<>();
    private static int userColorIndexIncrement = 0;

    private static HashMap<String, HashMap<Integer, Integer>> m_plateToStudyToColorIndexMap = new HashMap<>();



    public ColoredRobotPlanning(RobotPlanningJson robotPlanning) {
        m_robotPlanning = robotPlanning;

        String sampleKey = robotPlanning.getSample().getName();
        Integer colorKey = m_sampleToColorIndexMap.get(sampleKey);
        if (colorKey == null) {
            colorKey = sampleColorIndexIncrement++;
            m_sampleToColorIndexMap.put(sampleKey, colorKey);
        }
        m_color = CyclicColorPalette.getColor(colorKey);

        String actorKey = robotPlanning.getSample().getActorKey();
        colorKey = m_userToColorIndexMap.get(actorKey);
        if (colorKey == null) {
            colorKey = userColorIndexIncrement++;
            m_userToColorIndexMap.put(actorKey, colorKey);
        }
        m_userColor = CyclicColorPalette.getExcelColor(colorKey);


        m_trypsinColor = getTrypsinColor(robotPlanning.getTrypsineVol());

    }

    public void updated(RobotPlanningJson newRobotPlanning) {
        m_robotPlanning.setLoadCount(newRobotPlanning.getLoadCount());
        m_robotPlanning.setDescription(newRobotPlanning.getDescription());
        m_robotPlanning.setTrypsineVol(newRobotPlanning.getTrypsineVol());
        m_robotPlanning.setProteinQty(newRobotPlanning.getProteinQty());
        m_trypsinColor = getTrypsinColor(m_robotPlanning.getTrypsineVol());
    }

    public Color getStudyColor(VirtualPlateJson plate) {
        if (plate == null) {
            return null;
        }
        return getStudyColor(plate.getName());
    }
    public Color getStudyColor(String plateName) {
        HashMap<Integer, Integer> studyToColorIndexMap = m_plateToStudyToColorIndexMap.get(plateName);
        if (studyToColorIndexMap == null) {
            studyToColorIndexMap = new HashMap<>();
            m_plateToStudyToColorIndexMap.put(plateName, studyToColorIndexMap);
        }

        Integer studyId = new Integer(m_robotPlanning.getSample().getStudy());
        Integer colorKey = studyToColorIndexMap.get(studyId);
        if (colorKey == null) {
            colorKey = studyToColorIndexMap.size();
            studyToColorIndexMap.put(studyId, colorKey);
        }
        return CyclicColorPalette.getExcelColor(colorKey);
    }

    private static Color getTrypsinColor(Float f) {
        Color c = m_trypsinColors.get(f);
        if (c == null) {
            c = CyclicColorPalette.getColor(m_trypsinColors.size());
            m_trypsinColors.put(f, c);
        }
        return c;
    }

    private static HashMap<Float, Color> m_trypsinColors = new HashMap<>();

    public void setLinkedWells(boolean b) {
        m_linkedWells = b;
    }

    public ColoredRobotPlanning(RobotPlanningJson robotPlanning, Color sampleColor, Color userColor, Color trypsinColor) {
        m_robotPlanning = robotPlanning;
        m_color = sampleColor;
        m_userColor = userColor;
        m_trypsinColor = trypsinColor;
    }

    public void addWell(RobotPlatePanel.Well well) {
        m_wells.add(well);
    }

    public void clearWells() {
        m_wells.clear();
    }

    public int getNbWells() {
        if (m_wells.isEmpty()) {
            return m_robotPlanning.getLoadCount();
        }
        return m_wells.size();
    }

    @Override
    public int compareTo(ColoredRobotPlanning o) {
        if (m_robotPlanning == null) {
            return -1;
        }
        if (o.m_robotPlanning == null) {
            return 1;
        }
        return m_robotPlanning.compareTo(o.m_robotPlanning);
    }
}