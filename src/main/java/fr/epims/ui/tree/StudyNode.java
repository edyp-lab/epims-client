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

package fr.epims.ui.tree;

import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;
import fr.epims.ui.common.IconManager;


import javax.swing.*;
import java.util.Date;

/**
 *
 * Study Node
 *
 * @author JM235353
 *
 */
public class StudyNode extends AbstractNode {


    private String m_extendName = null;
    private boolean m_displayStudyNomenclature;

    public StudyNode(StudyJson data) {
        super(NodeTypes.STUDY, new Integer(data.getId()));
    }

    @Override
    public void loadNode(boolean studyNomenclature) {
        m_displayStudyNomenclature = studyNomenclature;
    }

    @Override
    public Object getData() {
        return DataManager.getStudy((Integer)super.getUserObject());
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {

        return IconManager.getIcon(IconManager.IconType.STUDY);

    }

    @Override
    public void filter(String owner, boolean onGoing, String searchText, Date dateFrom, Date dateTo) {
        StudyJson studyJson = (StudyJson) getData();

        if ((onGoing) && (!studyJson.getStatus().equals("en cours"))) {
            setVisible(false);
            return;
        }

        if ((owner != null) && (studyJson.getActorKey() != null) && (!studyJson.getActorKey().equals(owner))) {

            boolean found = false;
            for (String actor : studyJson.getActorsKey()) {
                if (owner.equals(actor)) {
                    found = true;
                }
            }

            if (! found) {
                setVisible(false);
                return;
            }
        }

        // check searchedText
        if (searchText != null) {
            String name = getData().toString().toLowerCase();
            String nomenclature = ((StudyJson) getData()).getNomenclatureTitle().toLowerCase();
            if ((name.indexOf(searchText) == -1) && (nomenclature.indexOf(searchText) == -1)) {

                // The Study name fails on text search


                // look the searched text in Program or Project
                int projectId = studyJson.getProjectId();
                ProjectJson project = DataManager.getProject(projectId);
                String projectName = project.toString().toLowerCase();
                String projectNomenclature = project.getNomenclatureTitle().toLowerCase();
                int programId = project.getProgramId();
                ProgramJson program = DataManager.getProgram(programId);
                String programName = program.toString().toLowerCase();
                String programNomenclature = program.getNomenclatureTitle().toLowerCase();
                if ((projectName.indexOf(searchText) == -1) && (programName.indexOf(searchText) == -1) && (projectNomenclature.indexOf(searchText) == -1) && (programNomenclature.indexOf(searchText) == -1)) {
                    setVisible(false);
                    return;
                }
            }
        }

        Date creationDate = studyJson.getCreationDate();

        if ( (creationDate!= null) &&
                (( (dateFrom != null) && (!dateFrom.before(creationDate)) ) ||
                 ( (dateTo != null) &&   (!dateTo.after(creationDate)) )) ) {
            setVisible(false);
            return;
        }

        setVisible(true);


    }

    public String toString() {
        Object data = getData();
        if (data == null) {
            return "";
        }

        if (m_extendName == null) {
            if (m_displayStudyNomenclature) {
                SB.setLength(0);
                SB.append(((StudyJson) data).getNomenclatureTitle()).append(" : ").append(data.toString());

                m_extendName = SB.toString();
            } else {
                m_extendName = data.toString();
            }
        }

        return m_extendName;
    }
    private static StringBuilder SB = new StringBuilder();


}