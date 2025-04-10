package fr.epims.ui.analyserequest.dialogs;

import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.ui.analyserequest.panels.AnalysesListPanel;
import fr.epims.ui.analyserequest.panels.AnalysesRequestsPanel;
import fr.epims.ui.common.DefaultDialog;

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
