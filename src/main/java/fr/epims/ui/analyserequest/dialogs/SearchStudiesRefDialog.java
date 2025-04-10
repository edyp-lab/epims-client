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

package fr.epims.ui.analyserequest.dialogs;

import fr.epims.MainFrame;
import fr.epims.ui.analyserequest.panels.StudiesTreePanel;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.InfoDialog;

import java.awt.*;
import java.util.HashSet;

/**
 *
 * Dialog used to select a Study corresponding to the Analysis Request.
 * The Study Reference is then saved in the Analysis Request.
 *
 * @author JM235353
 *
 */
public class SearchStudiesRefDialog extends DefaultDialog {

    private StudiesTreePanel m_mainPanel;

    private String m_previousStudyRef;

    private HashSet<String> m_referencedStudyRefSet;

    public SearchStudiesRefDialog(Window parent, String studyRef, HashSet<String> referencedStudyRefSet) {
        super(parent);

        m_previousStudyRef = studyRef;
        m_referencedStudyRefSet = referencedStudyRefSet;

        setTitle("Select Study Reference");

        m_mainPanel = new StudiesTreePanel(studyRef, this);
        setInternalComponent(m_mainPanel);

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);
        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setResizable(true);
    }

    public String getStudyRef() {
        return m_mainPanel.getStudyRef();
    }

    @Override
    public boolean okCalled() {

        String studyRefSelected = getStudyRef();

        if (studyRefSelected.isEmpty()) {
            return true;
        }

        if ((m_previousStudyRef != null) &&  m_previousStudyRef.equals(studyRefSelected)) {
            return true;
        }

        if (m_referencedStudyRefSet.contains(studyRefSelected)) {
            // this study Ref is already reference

            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Already Referenced Study", "You can not select this Study which is already referenced.");
            infoDialog.centerToWindow(MainFrame.getMainWindow());
            infoDialog.setVisible(true);

            return false;
        }


        return true;
    }
}
