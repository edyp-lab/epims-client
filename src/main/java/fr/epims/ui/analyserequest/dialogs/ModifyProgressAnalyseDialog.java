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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.edyp.epims.json.AnalyseProgressJson;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.tasks.analyses.ExportAnalysisInfoToProfiTask;
import fr.epims.tasks.analyses.SaveAnalysisTask;
import fr.epims.ui.analyserequest.panels.ModifyProgressPanel;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;


import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Dialog to edit informations about the analysis
 */
public class ModifyProgressAnalyseDialog extends DefaultDialog {

    private ModifyProgressPanel m_modifyProgressPanel;
    private AnalysisMapJson m_analysisMapJson;

    private boolean m_saveDone = false;

    public ModifyProgressAnalyseDialog(Window parent, AnalyseProgressJson analyseProgressJson, AnalysisMapJson analysisMapJson) {
        super(parent);

        setTitle("Analysis Progress "+analysisMapJson.getStudyRef());

        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.SAVE));
        setButtonName(DefaultDialog.BUTTON_OK, "Save");

        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);


        m_analysisMapJson = analysisMapJson;

        m_modifyProgressPanel = new ModifyProgressPanel(analyseProgressJson, analysisMapJson);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_modifyProgressPanel);
        setInternalComponent(scrollPane);
    }




    @Override
    public boolean okCalled() {

        if (!m_modifyProgressPanel.checkFields(this)) {
            return false;
        }

        HashMap<String, String> map = m_modifyProgressPanel.getTagMap();

        ObjectMapper mapper = new ObjectMapper();
        String jsonMap;
        try {
            jsonMap = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Json Error", "Json Error\n\nAction has been aborted.");
            infoDialog.centerToWindow(MainFrame.getMainWindow());
            infoDialog.setVisible(true);

            e.printStackTrace();
            return false;
        }

        AnalysisMapJson[] analysis = new AnalysisMapJson[1];
        m_analysisMapJson.setData(jsonMap);

        analysis[0] = m_analysisMapJson;

        String nbInjectionsString = (String) map.get(ModifyProgressPanel.INJECTIONS_NUMBER);
        Integer nbInjections = -1;
        if (nbInjectionsString != null) {
            try {
                nbInjections = Integer.parseInt(nbInjectionsString);
            } catch (Exception e) {

            }
        }
        final Integer _nbInjections = nbInjections;

        String nbSamplesString = map.get(ModifyProgressPanel.SAMPLES_NUMBER);
        Integer nbSamples = -1;
        if (nbSamplesString != null)  {
            try {
            nbSamples = Integer.parseInt(nbSamplesString);
            } catch (Exception e) {

            }
        }
        final Integer _nbSamples = nbSamples;


        final StringBuilder answerMessage = new StringBuilder();

        final JDialog _dialog = this;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {


                    m_saveDone = true;

                    AbstractDatabaseCallback exportCallback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            String message = answerMessage.toString();
                            if (! message.isEmpty()) {
                                success = false; // there is a warning
                            }
                            if (success) {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Saved", "Saving has succeeded.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Saved", "Saving has succeeded.\nBut export to Profi Server has not been done :\n"+message);
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    ExportAnalysisInfoToProfiTask exportTask = new ExportAnalysisInfoToProfiTask(exportCallback, m_analysisMapJson.getProAnalyseId(), _nbInjections, _nbSamples, answerMessage);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(exportTask);

                    _dialog.setVisible(false);




                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

            }
        };

        SaveAnalysisTask task = new SaveAnalysisTask(callback, analysis);


        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        return false;
    }

    public boolean saveDone() {
        return m_saveDone;
    }

    public AnalysisMapJson getAnalysisMapJson() {
        return m_analysisMapJson;
    }

}
