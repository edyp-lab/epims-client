/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.ui.analyserequest.dialogs;

import fr.edyp.epims.json.ProAnalysisJson;
import fr.edyp.epims.ui.analyserequest.panels.AnalysesListPanel;
import fr.edyp.epims.ui.analyserequest.panels.AnalysesRequestsPanel;
import fr.edyp.epims.ui.common.DefaultDialog;

import java.awt.*;

public class SelectAnalyseToImportDialog  extends DefaultDialog {

    private AnalysesListPanel m_mainPanel;

    public SelectAnalyseToImportDialog(Window parent) {
        super(parent);

        setTitle("Select Analyse to Import");

        m_mainPanel = new AnalysesListPanel(true);
        m_mainPanel.setAnalyses(AnalysesRequestsPanel.getPanel().getAnalysisJson(), AnalysesRequestsPanel.getPanel().getAnalysisMapJson(), true);
        setInternalComponent(m_mainPanel);

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);
        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setResizable(true);
    }

    public ProAnalysisJson getSelectedAnalyse() {
        return m_mainPanel.getSelectedAnalyse();
    }
}
