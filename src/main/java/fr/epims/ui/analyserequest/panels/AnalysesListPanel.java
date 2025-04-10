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

package fr.epims.ui.analyserequest.panels;

import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.dataaccess.DataManager;



import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;


/**
 *
 * Panel listing all analysis Requests assigned to Grenoble
 *
 * @author JM235353
 *
 */
public class AnalysesListPanel extends JPanel {

    private AnalysesTablePanel m_analysesTablePanel;
    private ProgressAnalysesTablePanel m_progressAnalysesTablePanel;
    private StatisticsAnalysesPanel m_statisticsAnalysesPanel;

    private HashSet<String> m_takenStudyRefSet = null;

    private JCheckBox m_filterCheckbox;

    private boolean m_readOnly = true;

    public AnalysesListPanel(boolean importSelection) {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

       JPanel filterPanel = createFilterPanel();

        JTabbedPane tabbedPane = createTabbedPane(importSelection);

       c.weightx = 1;
       add(filterPanel, c);

       c.gridy++;
       c.weighty = 1;
        add(tabbedPane, c);


    }

    private JTabbedPane createTabbedPane(boolean importSelection) {
        JTabbedPane tabbedPane = new JTabbedPane();

        m_analysesTablePanel = new AnalysesTablePanel(this, importSelection);
        m_progressAnalysesTablePanel = new ProgressAnalysesTablePanel(this);
        m_statisticsAnalysesPanel = new StatisticsAnalysesPanel();


        tabbedPane.addTab("Edit/View Analyses", m_analysesTablePanel);
        //tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.LOCK));

        if (!importSelection) {
            tabbedPane.addTab("Analyses Overview", m_progressAnalysesTablePanel);
            //tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.ARCHIVE_ALLOW));

            tabbedPane.addTab("Statistics", m_statisticsAnalysesPanel);
            //tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.ARCHIVE_ALLOW));
        }

        return tabbedPane;
    }


    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        Border titledBorder = BorderFactory.createTitledBorder(" Filter ");
        filterPanel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_filterCheckbox = new JCheckBox("My Analyses Requests");
        m_filterCheckbox.setSelected(true);


        filterPanel.add(m_filterCheckbox, c);

        c.gridx++;
        c.weightx = 1;
        filterPanel.add(Box.createGlue(), c);

        m_filterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                m_analysesTablePanel.filter(m_filterCheckbox.isSelected(), DataManager.getAnalysesLoggedUser());
                m_progressAnalysesTablePanel.filter(m_filterCheckbox.isSelected(), DataManager.getAnalysesLoggedUser());
            }
        });

        return filterPanel;

    }


    public void userUpdated() {
        String fullName = DataManager.getAnalysesFullNameUser();
        if (fullName == null) {
            m_filterCheckbox.setText("My Analyses Requests");
        } else {
            m_filterCheckbox.setText("My Analyses Requests (" + DataManager.getAnalysesFullNameUser() + ")");
        }
    }

    public void setAnalyses(ArrayList<ProAnalysisJson> analyses, ArrayList<AnalysisMapJson> analysesMaps, boolean filterSavedAnalysis) {

        ArrayList<ProAnalysisJson> validatedAnalyses = new ArrayList<>();
        for (ProAnalysisJson a : analyses) {
            if (a.isOK()) {
                validatedAnalyses.add(a);
            }
        }

        m_takenStudyRefSet = new HashSet<>();
        for (AnalysisMapJson analysisMapJson : analysesMaps) {
            String studyRef = analysisMapJson.getStudyRef();
            if (studyRef != null) {
                m_takenStudyRefSet.add(studyRef);
            }
        }

        m_analysesTablePanel.setAnalyses(validatedAnalyses, analysesMaps, m_filterCheckbox.isSelected(), DataManager.getAnalysesLoggedUser(), filterSavedAnalysis);
        m_progressAnalysesTablePanel.setAnalyses(validatedAnalyses, analysesMaps, m_filterCheckbox.isSelected(), DataManager.getAnalysesLoggedUser(), filterSavedAnalysis);
        m_statisticsAnalysesPanel.setAnalyses(analyses);
    }

    public HashSet<String> getTakenStudyRefSet() {
        return m_takenStudyRefSet;
    }

    public void modificationDone(ProAnalysisJson proAnalysisJson, AnalysisMapJson analysisMapJson) {

        AnalysesRequestsPanel.getPanel().modificationDone(proAnalysisJson, analysisMapJson);
        m_analysesTablePanel.modificationDone(proAnalysisJson, analysisMapJson);
        m_progressAnalysesTablePanel.modificationDone(proAnalysisJson, analysisMapJson);
    }

    public void exportDone(int analysisId, Date exportDate) {
        m_analysesTablePanel.exportDone(analysisId, exportDate);
    }



    public ProAnalysisJson getSelectedAnalyse() {
        return m_analysesTablePanel.getSelectedAnalyse();
    }



    public void reinit() {

        setAnalyses(new ArrayList<>(), new ArrayList<>(), false);
    }


}
