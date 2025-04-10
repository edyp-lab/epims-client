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
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.common.FlatPanel;
import fr.epims.ui.common.TextAreaPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.HashMap;

/**
 *
 * Sub Panel of AnalyseRequestMainPanel.
 * Possibility to modify Request Analysis information (request description)
 *
 * @author JM235353
 *
 */
public class AnalysisRequestStep1Part2Panel extends AbstractAnalyseRequestStepPanel {

    private static final String PROJECT_SUMMARY = "PROJECT_SUMMARY";
    public static final String PROBLEMATIC = "PROBLEMATIC";
    private static final String SAMPLE_DESCRIPTION = "SAMPLE_DESCRIPTION";
    private static final String SUPPLEMENTARY_INFORMATION = "SUPPLEMENTARY_INFORMATION";
    public static final String SERVICE_TYPE_COLLABORATIVE = "SERVICE_TYPE_COLLABORATIVE";
    public static final String SERVICE_TYPE_NONCOLLABORATIVE = "SERVICE_TYPE_NONCOLLABORATIVE";

    private JTextArea m_projectSummaryTextArea;
    private JTextArea m_problematicTextArea;
    private JTextArea m_sampleDescriptionTextArea;
    private JTextArea m_supplementaryInformationTextArea;
    private JRadioButton m_collaborativeRadioButton;
    private JRadioButton m_nonCollaborativeRadioButton;




    public AnalysisRequestStep1Part2Panel(boolean readOnly, ModifyAnalyseRequestDialog parentDialog, AnalyseRequestMainPanel mainPanel, AnalysisMapJson analysisMapJson, DocumentListener dataChangedListener, ItemListener itemChangedListener, TableModelListener tableModelListener) {
        super( readOnly,  parentDialog,  mainPanel,  analysisMapJson,  dataChangedListener,  itemChangedListener, tableModelListener);

        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        m_projectSummaryTextArea = new JTextArea(5, 40);
        m_problematicTextArea = new JTextArea(5, 40);
        m_sampleDescriptionTextArea = new JTextArea(5, 40);
        m_supplementaryInformationTextArea = new JTextArea(5, 40);

        ButtonGroup typeOfServiceGroup = new ButtonGroup();
        m_collaborativeRadioButton = new JRadioButton("Collaborative Service");
        m_nonCollaborativeRadioButton = new JRadioButton("Non Collaborative Service");
        typeOfServiceGroup.add(m_collaborativeRadioButton);
        typeOfServiceGroup.add(m_nonCollaborativeRadioButton);

        m_projectSummaryTextArea.getDocument().addDocumentListener(dataChangedListener);
        m_problematicTextArea.getDocument().addDocumentListener(dataChangedListener);
        m_sampleDescriptionTextArea.getDocument().addDocumentListener(dataChangedListener);
        m_supplementaryInformationTextArea.getDocument().addDocumentListener(dataChangedListener);

        m_collaborativeRadioButton.addItemListener(itemChangedListener);
        m_nonCollaborativeRadioButton.addItemListener(itemChangedListener);

        ActionListener collaborativeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.collaborativeStatusChanged(m_collaborativeRadioButton.isSelected(), m_nonCollaborativeRadioButton.isSelected());
            }
        };

        m_collaborativeRadioButton.addActionListener(collaborativeListener);
        m_nonCollaborativeRadioButton.addActionListener(collaborativeListener);

        if (readOnly) {
            m_projectSummaryTextArea.setEditable(false);
            m_problematicTextArea.setEditable(false);
            m_sampleDescriptionTextArea.setEditable(false);
            m_supplementaryInformationTextArea.setEditable(false);
            m_projectSummaryTextArea.setEnabled(false);
            m_problematicTextArea.setEnabled(false);
            m_sampleDescriptionTextArea.setEnabled(false);
            m_supplementaryInformationTextArea.setEnabled(false);

            m_collaborativeRadioButton.setEnabled(false);
            m_nonCollaborativeRadioButton.setEnabled(false);


        }


        // -------------- Place Widgets

        // --- Scientific Project Summary
        c.gridx = 0;
        c.weightx = 1;
        c.gridy = 0;
        add(new TextAreaPanel("Scientific Project Summary", m_projectSummaryTextArea), c);

        // --- Problematic
        c.gridx = 0;
        c.gridy++;
        add(new TextAreaPanel("Problematic (what you're waiting from us)", m_problematicTextArea), c);

        // --- Sample Description
        c.gridx = 0;
        c.gridy++;
        add(new TextAreaPanel("Description of the samples to be analysed (preparation protocol, solubility...)", m_sampleDescriptionTextArea), c);

        // --- Supplementary Information
        c.gridx = 0;
        c.gridy++;
        add(new TextAreaPanel("Supplementary Information (scans, protein sequence, modification sites, bibliographic references...)", m_supplementaryInformationTextArea), c);

        // --- Type of Service
        Component[] components = { new JLabel("Type of Service:", SwingConstants.RIGHT),
                m_collaborativeRadioButton, m_nonCollaborativeRadioButton, Box.createHorizontalGlue()};
        c.gridx = 0;
        c.gridy++;
        add(new FlatPanel(components), c);


        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        add(Box.createVerticalGlue(), c);

    }


    public boolean isCollaborative() {
        return m_collaborativeRadioButton.isSelected();
    }

    public boolean isNonCollaborative() {
        return m_nonCollaborativeRadioButton.isSelected();
    }

    @Override
    public void loadData(ProAnalysisJson proAnalysisJson, String studyRef, HashMap<String, String> valueMap) {
        if (valueMap == null) {
            m_projectSummaryTextArea.setText(proAnalysisJson.getAnalyseDescriptionProjetScientifique());
            m_sampleDescriptionTextArea.setText(proAnalysisJson.getAnalyseDescriptionEchantillons());
        } else {

            setText(m_projectSummaryTextArea, valueMap.get(PROJECT_SUMMARY));

            setText(m_problematicTextArea, valueMap.get(PROBLEMATIC));
            setText(m_sampleDescriptionTextArea, valueMap.get(SAMPLE_DESCRIPTION));
            setText(m_supplementaryInformationTextArea, valueMap.get(SUPPLEMENTARY_INFORMATION));


            String value = valueMap.get(SERVICE_TYPE_COLLABORATIVE);
            if ((value!=null) && (value.equals("true"))) {
                m_collaborativeRadioButton.setSelected(true);
            }
            value = valueMap.get(SERVICE_TYPE_NONCOLLABORATIVE);
            if ((value!=null) && (value.equals("true"))) {
                m_nonCollaborativeRadioButton.setSelected(true);
            }
        }

    }

    public static void prefillData(ProAnalysisJson proAnalysisJson, HashMap<String, String> valueMap) {

        valueMap.put(PROJECT_SUMMARY, proAnalysisJson.getAnalyseDescriptionProjetScientifique());
        valueMap.put(SAMPLE_DESCRIPTION, proAnalysisJson.getAnalyseDescriptionEchantillons());

        valueMap.put(PROBLEMATIC, proAnalysisJson.getAnalyseIntitule());
    }

    @Override
    public void getTagMap(HashMap<String, String> data) {

        data.put(PROJECT_SUMMARY, m_projectSummaryTextArea.getText());
        data.put(PROBLEMATIC, m_problematicTextArea.getText());
        data.put(SAMPLE_DESCRIPTION, m_sampleDescriptionTextArea.getText());
        data.put(SUPPLEMENTARY_INFORMATION, m_supplementaryInformationTextArea.getText());

        data.put(SERVICE_TYPE_COLLABORATIVE, Boolean.toString(m_collaborativeRadioButton.isSelected()));
        data.put(SERVICE_TYPE_NONCOLLABORATIVE, Boolean.toString(m_nonCollaborativeRadioButton.isSelected()));


    }
}
