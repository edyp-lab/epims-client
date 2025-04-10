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


import fr.edyp.epims.json.ProgramJson;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * Root Node : Programs
 *
 * @author JM235353
 *
 */
public class RootNode  extends AbstractNode {

    private ArrayList<ProgramJson> m_programList;

    public RootNode() {
        super( AbstractNode.NodeTypes.TREE_PARENT, "Programs");
    }


    public void setData(ArrayList<ProgramJson> programList) {
        m_programList = programList;

        removeAllChildren();

    }

    @Override
    public void loadNode(boolean studyNomenclature) {

        for (ProgramJson p : m_programList) {
            ProgramNode n = new ProgramNode(p);

            add(n);
            n.loadNode(studyNomenclature);
        }
    }

    @Override
    public ImageIcon getIcon(boolean expanded) {
        return IconManager.getIcon(IconManager.IconType.ACTIVITIES);
    }

    @Override
    public void filter(String owner, boolean onGoing, String searchText, Date dateFrom, Date dateTo) {
        if(children == null)
            return;
        Enumeration e = children.elements();
        while (e.hasMoreElements()) {
            InvisibleNode node = (InvisibleNode) e.nextElement();
            node.filter(owner, onGoing, searchText, dateFrom, dateTo);
        }

    }


}