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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * Node for a Project
 *
 * @author JM235353
 *
 */
public class ProjectNode extends AbstractNode {


    public ProjectNode(ProjectJson data) {
        super(NodeTypes.PROJECT, new Integer(data.getId()));
    }

    @Override
    public void loadNode(boolean studyNomenclature) {
        ProjectJson project = (ProjectJson) getData();

        ArrayList<StudyJson> studies = DataManager.getStudies(project.getStudiesKeys());
        project.setStudies(studies);
        for (StudyJson s : studies) {
            StudyNode n = new StudyNode(s);

            add(n);
            n.loadNode(studyNomenclature);
        }
    }

    @Override
    public Object getData() {
        return DataManager.getProject((Integer)super.getUserObject());
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {
        return IconManager.getIcon(IconManager.IconType.PROJECT);
    }


    @Override
    public void filter(String owner, boolean onGoing, String searchText, Date dateFrom, Date dateTo) {

        // if the project has visible children, the project is visible
        if (children != null) {
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
                InvisibleNode node = (InvisibleNode) e.nextElement();
                node.filter(owner, onGoing, searchText, dateFrom, dateTo);
            }


            e = children.elements();
            while (e.hasMoreElements()) {
                InvisibleNode node = (InvisibleNode) e.nextElement();
                if (node.isVisible()) {
                    setVisible(true);
                    return;
                }
            }

        }

        ProjectJson projectJson = (ProjectJson) getData();

        // check ownership
        if ((owner != null) && (projectJson.getActorKey() != null) && (!projectJson.getActorKey().equals(owner))) {
            boolean found = false;
            for (String actor : projectJson.getActorsKey()) {
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
            String nomenclature = ((ProjectJson) getData()).getNomenclatureTitle().toLowerCase();
            if ((name.indexOf(searchText) == -1) && (nomenclature.indexOf(searchText) == -1)) {

                // The Project name fails on text search


                // look the searched text in Program
                int programId = projectJson.getProgramId();
                ProgramJson program = DataManager.getProgram(programId);
                String programName = program.toString().toLowerCase();
                String programNomenclature = program.getNomenclatureTitle().toLowerCase();
                if ((programName.indexOf(searchText) == -1) && (programNomenclature.indexOf(searchText) == -1)) {
                    setVisible(false);
                    return;
                }
            }
        }


        Date creationDate = projectJson.getCreationDate();
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
        return data.toString();
    }
}