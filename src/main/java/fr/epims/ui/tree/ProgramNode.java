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
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * Node for a Program
 *
 * @author JM235353
 *
 */
public class ProgramNode extends AbstractNode {

    public ProgramNode(ProgramJson data) {
            super(NodeTypes.PROGRAM, new Integer(data.getId()));
        }

    @Override
    public void loadNode(boolean studyNomenclature) {
        ProgramJson program = (ProgramJson)  getData() ;

        ArrayList<ProjectJson>  projects = DataManager.getProjects(program.getProjectsKeys());
        program.setProjects(projects);
        for (ProjectJson p : projects) {
            ProjectNode n = new ProjectNode(p);

            add(n);
            n.loadNode(studyNomenclature);

        }

    }

    @Override
    public Object getData() {
        return DataManager.getProgram((Integer)super.getUserObject());
    }


    @Override
    public ImageIcon getIcon(boolean expanded) {
        return IconManager.getIcon(IconManager.IconType.PROGRAM);

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

        ProgramJson programJson = (ProgramJson) getData();

        // check ownership
        if ((owner != null) && (programJson.getResponsible() != null) && (!programJson.getResponsible().equals(owner))) {
            boolean found = false;
            for (String actor : programJson.getActorsKey()) {
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
        if ((searchText != null) && (!searchText.isEmpty())) {
            String name = getData().toString().toLowerCase();
            String nomenclature = ((ProgramJson) getData()).getNomenclatureTitle().toLowerCase();
            if ((name.indexOf(searchText) == -1) && (nomenclature.indexOf(searchText) == -1)) {
                setVisible(false);
                return;
            }
        }

        Date creationDate = programJson.getCreationDate();
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