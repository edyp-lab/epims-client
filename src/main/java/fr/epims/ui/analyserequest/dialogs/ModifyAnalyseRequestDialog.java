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
import fr.edyp.epims.json.*;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.tasks.analyses.ExportAnalysisDoneTask;
import fr.epims.tasks.analyses.SaveAnalysisTask;
import fr.epims.ui.analyserequest.panels.AnalyseRequestMainPanel;
import fr.epims.ui.analyserequest.panels.AnalysesListPanel;
import fr.epims.ui.analyserequest.panels.AnalysisRequestStep1Part1Panel;
import fr.epims.ui.analyserequest.panels.AnalysisRequestStep2Part3Panel;
import fr.epims.ui.common.*;
import fr.epims.util.UtilZip;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;


/**
 *
 * Dialog to modify and export an analysis Request
 *
 * @author JM235353
 *
 */
public class ModifyAnalyseRequestDialog extends DefaultDialog {

    private AnalyseRequestMainPanel m_mainPanel;

    private ProAnalysisJson m_analysisRequest;

    private AnalysesListPanel m_analysesListPanel;

    private AnalysisMapJson m_analysisMapJson;

    private boolean m_readOnly;

    public ModifyAnalyseRequestDialog(Window parent, AnalysesListPanel analysesListPanel, String studyRef, ProAnalysisJson analysisRequest, AnalysisMapJson analysisMapJson, boolean readOnly) {
        super(parent);

        m_analysesListPanel = analysesListPanel;
        m_analysisRequest = analysisRequest;
        m_analysisMapJson = analysisMapJson;

        m_readOnly = readOnly;

        if (m_analysisMapJson == null) {
            m_analysisMapJson = new AnalysisMapJson(-1, m_analysisRequest.getAnalyseId(), -1, studyRef, null);
        }

        setTitle("Analysis Request "+studyRef+" - "+analysisRequest.getUserFullname());

        m_mainPanel = new AnalyseRequestMainPanel(this, analysisRequest, m_analysisMapJson, readOnly);
        setInternalComponent(m_mainPanel);


        setButtonVisible(DefaultDialog.BUTTON_BACK, true);
        setButtonIcon(DefaultDialog.BUTTON_BACK, IconManager.getIcon(IconManager.IconType.SAVE));
        setButtonName(DefaultDialog.BUTTON_BACK, "Save");

        setButtonVisible(DefaultDialog.BUTTON_OK, true);
        setButtonIcon(DefaultDialog.BUTTON_OK, IconManager.getIcon(IconManager.IconType.EXPORT));
        setButtonName(DefaultDialog.BUTTON_OK, "Export");

        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setButtonEnabled(DefaultDialog.BUTTON_OK, !m_readOnly); // Save Export is disabled in readOnly mode
        setButtonEnabled(DefaultDialog.BUTTON_BACK, false); // Save Button is disabled at the beggining

        setResizable(true);
    }

    public AnalysesListPanel getAnalysesListPanel() {
        return m_analysesListPanel;
    }

    public void dataChanged() {
        setButtonEnabled(DefaultDialog.BUTTON_BACK, !m_readOnly); // Save Button is enabled
        setButtonEnabled(DefaultDialog.BUTTON_OK, false); // Export Button is disabled
    }

    @Override
    public void helpButtonActionPerformed() {

        IconManager.IconType image = null;

        String key = m_mainPanel.getSelectedPanelKey();
        switch (key) {
            case AnalyseRequestMainPanel.PANEL_STEP_1_PART_1:
                image = IconManager.IconType.HELP_ANALYSIS_STEP1_1;
                break;
            case AnalyseRequestMainPanel.PANEL_STEP_1_PART_2:
                image = IconManager.IconType.HELP_ANALYSIS_STEP1_2;
                break;
            case AnalyseRequestMainPanel.PANEL_STEP_2_PART_1:
                image = IconManager.IconType.HELP_ANALYSIS_STEP2_1;
                break;
            case AnalyseRequestMainPanel.PANEL_STEP_2_PART_2:
                image = IconManager.IconType.HELP_ANALYSIS_STEP2_2;
                break;
            case AnalyseRequestMainPanel.PANEL_STEP_2_PART_3:
                image = IconManager.IconType.HELP_ANALYSIS_STEP2_3;
                break;
        }

        if (image != null) {
            ImageDialog helpDialog = new ImageDialog(MainFrame.getMainWindow(), "Help", IconManager.getImage(image));
            helpDialog.centerToWindow(MainFrame.getMainWindow());
            helpDialog.setVisible(true);
        }
    }

    @Override
    public boolean cancelCalled() {
        if (isButtonEnabled(DefaultDialog.BUTTON_BACK)) {
            // Save Button enabled: the user wants to close the dialog, but nothing is saved
            QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Close without Saving", "Your modifications have not been saved. Do you really want to close the dialog ?");
            questionDialog.centerToWindow(this);
            questionDialog.setVisible(true);
            return (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK);
        }

        return true;
    }

    @Override
    public boolean backCalled() {

        // SAVE ACTION

        boolean checkSave = m_mainPanel.checkFields(this);
        if (!checkSave) {
            return false;
        }


        String studyRef = m_mainPanel.getStudyRef();
        HashMap<String, String> map = m_mainPanel.getTagMap();
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

         m_analysisMapJson.setStudyRef(studyRef);
         m_analysisMapJson.setData(jsonMap);

        analysis[0] = m_analysisMapJson;


        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Saved", "Saving has succeeded.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    m_analysesListPanel.modificationDone(m_analysisRequest, analysis[0]);

                    setButtonEnabled(DefaultDialog.BUTTON_OK, true && !m_readOnly); // Export Button is enabled
                    setButtonEnabled(DefaultDialog.BUTTON_BACK, false); // Save Button is disabled

                } else {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

            }
        };

        SaveAnalysisTask task = new SaveAnalysisTask(callback, analysis);


        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);


        return true;

    }

    @Override
    public boolean okCalled() {
        // EXPORT ACTION

        boolean checkSave = m_mainPanel.checkFields(this);
        if (!checkSave) {
            return false;
        }

        HashMap<String, String> map = m_mainPanel.getTagMap();
        String studyRef = map.get(AnalysisRequestStep1Part1Panel.STUDY_REF);
        if (studyRef == null) {
            studyRef = "";
        }

        JFileChooser fchooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Word", "docx");
        fchooser.setFileFilter(filter);
        fchooser.setSelectedFile(new File(studyRef+"_AnalysisRequest.docx"));


        int result = fchooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File exportFile = fchooser.getSelectedFile();

            if (exportFile.exists()) {
                QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Replace File", exportFile.getName()+" already exists. Do you want to replace it ?");
                questionDialog.centerToWindow(MainFrame.getMainWindow());
                questionDialog.setVisible(true);
                if (questionDialog.getButtonClicked() != DefaultDialog.BUTTON_OK) {
                    return false;
                }

                if (! exportFile.canWrite()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Export Failed", exportFile.getName()+" is write protected. Export has failed.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return false;
                }
            }

            String exportFileTmp = exportFile.getAbsolutePath()+".tmp";
            String exportDirTmp = exportFile.getAbsolutePath()+".dir.tmp";
            String documentXML = exportDirTmp+"\\word\\document.xml";
            String documentXMLTmp = documentXML+".tmp";
            try {
                // VDS TODO : Not in source any more. To be added when new implem will be done
                UtilZip.exportResource("da/AnalysisRequestTemplate.docx", exportFileTmp);
                File dir = new File(exportDirTmp);
                if (! dir.exists()) {
                    dir.mkdir();
                }
                UtilZip.unzip(exportFileTmp, exportDirTmp);

                File documentXMLFile = new File(documentXML);
                File documentXMLTmpFile = new File(documentXMLTmp);

                // VDS TODO : Not in source any more. To be added when new implem will be done
                String factureTableLine = UtilZip.readRessouce("da/LigneTableXML.txt");

                replaceTAGinXML(documentXMLFile, documentXMLTmpFile, map, factureTableLine);

                documentXMLFile.delete();
                documentXMLTmpFile.renameTo(documentXMLFile);
                (new File(documentXMLTmp)).delete();
                UtilZip.zipFileWithoutParentDirectory(dir.getAbsolutePath(), exportFile.getAbsolutePath());

                new File(exportFileTmp).delete();
                UtilZip.deleteDirectory(dir);


                final Date exportDate = new Date();

                AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                    @Override
                    public boolean mustBeCalledInAWT() {
                        return true;
                    }

                    @Override
                    public void run(boolean success, long taskId, boolean finished) {
                        if (success) {

                            m_analysesListPanel.exportDone(m_analysisRequest.getAnalyseId(), exportDate);


                        } else {
                            InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nExport Date not saved in database.");
                            infoDialog.centerToWindow(MainFrame.getMainWindow());
                            infoDialog.setVisible(true);
                        }

                    }
                };

                int id = m_analysisRequest.getAnalyseId();
                ExportAnalysisDoneTask task = new ExportAnalysisDoneTask(callback, id, exportDate);

                AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Export Done", "Export has succeeded.");
                infoDialog.centerToWindow(MainFrame.getMainWindow());
                infoDialog.setVisible(true);

            }
            catch (FileNotFoundException e) {
                // in this case most of the time the file is opened by another processus
                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Export Failed", "Export has failed. Perhaps the file is opened in Word.");
                infoDialog.centerToWindow(MainFrame.getMainWindow());
                infoDialog.setVisible(true);
            }
            catch (Exception e) {
                LoggerFactory.getLogger("Epims.Client").debug("Unexpected exception", e);
                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Export Failed", "Export has failed.");
                infoDialog.centerToWindow(MainFrame.getMainWindow());
                infoDialog.setVisible(true);
            }
        }

        return false;
    }


    private void replaceTAGinXML(File source, File destination, HashMap<String, String> tag2ValueMap, String factureTableLine) throws Exception {

        final String TAG = "#TAGEPIMS#";
        // ENDTAG is "#"

        try (FileInputStream fileReader = new FileInputStream(source);
             InputStreamReader isr = new InputStreamReader(fileReader, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(isr);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter) ) {

            StringBuilder sb = new StringBuilder(8192);

            int c;
            readloop:
            while ((c = bufferedReader.read()) > -1) {

                if (c == '#') {
                    boolean tagfound = true;
                    int pointeur = 1;
                    while (pointeur < TAG.length()) {
                        int ctagInt = bufferedReader.read();
                        char ctag = (char) ctagInt;
                        if (ctagInt == -1) {
                            bufferedWriter.write(TAG.substring(0, pointeur));
                            break readloop;
                        } else if (ctag != TAG.charAt(pointeur)) {
                            bufferedWriter.write(TAG.substring(0, pointeur));
                            bufferedWriter.write(ctag);
                            tagfound = false;
                            break;
                        }
                        pointeur++;
                    }
                    if (tagfound) {
                        sb.setLength(0);
                        while ((c = bufferedReader.read()) > -1) {
                            if (c == '#') {
                                break;
                            } else {
                                sb.append((char) c);
                            }
                        }
                        String tag = sb.toString();
                        String value = tag2ValueMap.get(tag);
                        if (value != null) {
                            if (value.equals("true")) {
                                value = "1"; // checked button
                            } else if (value.equals("false")) {
                                value = "0"; // unchecked button
                            }
                            bufferedWriter.write(escapeXMLCharacters(value));

                        } else if (tag.equals("TABLE_FACTURE")) {
                            int row = 0;
                            while (true) {
                                String v1 = tag2ValueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_ANALYSIS+row);
                                if (v1 == null) {
                                    break;
                                }
                                String v2 = tag2ValueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_SAMPLES_NUMBER+row);
                                String v3 = tag2ValueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_UNIT_PRICE+row);
                                String v4 = tag2ValueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_PRICE+row);
                                String line = factureTableLine;
                                line = line.replace("####EPIMS_CASE1####", escapeXMLCharacters(v1));
                                line = line.replace("####EPIMS_CASE2####", escapeXMLCharacters(v2));
                                line = line.replace("####EPIMS_CASE3####", escapeXMLCharacters(v3));
                                line = line.replace("####EPIMS_CASE4####", escapeXMLCharacters(v4));
                                bufferedWriter.write(line);
                                row++;
                            }

                        }
                    }

                } else {
                    bufferedWriter.write(c);
                }

            }
        }
    }

    private String escapeXMLCharacters(String s) {
        m_sb.setLength(0);
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            if (c == '"') {
                m_sb.append("&quot;");
            } else if (c == '\'') {
                m_sb.append("&apos;");
            } else if (c == '<') {
                m_sb.append("&lt;");
            } else if (c == '>') {
                m_sb.append("&gt;");
            } else if (c == '&') {
                m_sb.append("&amp;");
            } else {
                m_sb.append(c);
            }
        }

        return m_sb.toString();

    }
    private static StringBuilder m_sb = new StringBuilder();

}
