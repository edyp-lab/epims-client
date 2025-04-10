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
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.common.FlatPanel;
import fr.epims.ui.common.FramedPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

/**
 *
 * Sub Panel of AnalyseRequestMainPanel.
 * Possibility to modify Request Analysis information (description of the preparation of samples)
 *
 * @author JM235353
 *
 */
public class AnalysisRequestStep2Part1Panel extends AbstractAnalyseRequestStepPanel {

    private static final String RESPONSIBLE_PERSON = "RESPONSIBLE_PERSON";
    private static final String SAMPLES_NUMBER = "SAMPLES_NUMBER";
    private static final String ORGANISM = "ORGANISM";
    private static final String SAMPLE_COMPLIANCE_YES = "SAMPLE_COMPLIANCE_YES";
    private static final String SAMPLE_COMPLIANCE_NO = "SAMPLE_COMPLIANCE_NO";
    private static final String PROTEIN_QUANTITY = "PROTEIN_QUANTITY";

    private static final String SAMPLE_COMPLEXITY_LOW = "SAMPLE_COMPLEXITY_LOW";
    private static final String SAMPLE_COMPLEXITY_MEDIUM = "SAMPLE_COMPLEXITY_MEDIUM";
    private static final String SAMPLE_COMPLEXITY_HIGH = "SAMPLE_COMPLEXITY_HIGH";

    private static final String UNIQUE_PROTEIN_MOLECULAR_WEIGHT= "UNIQUE_PROTEIN_MOLECULAR_WEIGHT";

    private static final String PROTEIN_PREPARATION_SOLUTION = "PROTEIN_PREPARATION_SOLUTION";
    private static final String PROTEIN_SOLUTION_COMPOSITION = "PROTEIN_SOLUTION_COMPOSITION";
    private static final String PROTEIN_PREPARATION_GEL = "PROTEIN_PREPARATION_GEL";
    private static final String PROTEIN_PREPARATION_GEL1D = "PROTEIN_PREPARATION_GEL1D";
    private static final String PROTEIN_PREPARATION_GEL2D = "PROTEIN_PREPARATION_GEL2D";
    private static final String PROTEIN_PREPARATION_GEL_STRAINING_BLUE = "PROTEIN_PREPARATION_GEL_STRAINING_BLUE";
    private static final String PROTEIN_PREPARATION_GEL_STRAINING_SILVER = "PROTEIN_PREPARATION_GEL_STRAINING_SILVER";
    private static final String PROTEIN_PREPARATION_OTHER = "PROTEIN_PREPARATION_OTHER";
    private static final String PROTEIN_PREPARATION_OTHER_INFO = "PROTEIN_PREPARATION_OTHER_INFO";

    private static final String RESET_COMPLIANCE = "RESET_COMPLIANCE";
    private static final String RESET_COMPLEXITY = "RESET_COMPLEXITY";

    private static final String RESET_GEL = "RESET_GEL";
    private static final String RESET_STRAINING = "RESET_STRAINING";

    private ButtonGroup m_complianceGroup;
    private ButtonGroup m_complexityGroup;

    private ButtonGroup m_gelGroup;
    private ButtonGroup m_strainingGroup;

    private JTextField m_responsiblePersonTF;
    private JTextField m_samplesNumberTF;
    private JTextField m_organismTF;
    private JRadioButton m_complianceYesRB;
    private JRadioButton m_complianceNoRB;
    private JTextField m_proteinQuantityTF;

    private JRadioButton m_complexityLowRB;
    private JRadioButton m_complexityMediumRB;
    private JRadioButton m_complexityHighRB;

    private JTextField m_molecularWeightTF;

    private JCheckBox m_preparationSolutionCB;
    private JTextField m_solutionCompositionTF;
    private JCheckBox m_preparationGelCB;
    private JRadioButton m_preparationGel1DRB;
    private JRadioButton m_preparationGel2DRB;
    private JRadioButton m_preparationGelStraingingBlueRB;
    private JRadioButton m_preparationGelStraingingSilverRB;
    private JCheckBox m_preparationOtherCB;
    private JTextField m_preparationOtherTF;




    public AnalysisRequestStep2Part1Panel(boolean readOnly, ModifyAnalyseRequestDialog parentDialog, AnalyseRequestMainPanel mainPanel, AnalysisMapJson analysisMapJson, DocumentListener dataChangedListener, ItemListener itemChangedListener, TableModelListener tableModelListener) {
        super( readOnly,  parentDialog,  mainPanel,  analysisMapJson,  dataChangedListener,  itemChangedListener, tableModelListener);
        setLayout(new GridBagLayout());

        m_readOnly = readOnly;

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        m_responsiblePersonTF = new JTextField(50);
        m_samplesNumberTF = new JTextField(50);
        m_organismTF = new JTextField(50);
        m_complianceYesRB = new JRadioButton("Yes");
        m_complianceNoRB = new JRadioButton("No");
        m_proteinQuantityTF = new JTextField(50);
        m_complexityLowRB = new JRadioButton("Low");
        m_complexityMediumRB = new JRadioButton("Medium");
        m_complexityHighRB = new JRadioButton("High");
        m_molecularWeightTF = new JTextField(50);
        m_preparationSolutionCB = new JCheckBox("Solution");
        m_solutionCompositionTF = new JTextField(50);
        m_solutionCompositionTF.setEnabled(false);

        m_preparationGelCB = new JCheckBox("Gel:");
        m_preparationGel1DRB = new JRadioButton("1D");
        m_preparationGel2DRB = new JRadioButton("2D");
        m_preparationGel1DRB.setEnabled(false);
        m_preparationGel2DRB.setEnabled(false);

        m_preparationGelStraingingBlueRB = new JRadioButton("Coomasie Blue");
        m_preparationGelStraingingSilverRB = new JRadioButton("silver nitrate");
        m_preparationGelStraingingBlueRB.setEnabled(false);
        m_preparationGelStraingingSilverRB.setEnabled(false);

        m_preparationOtherCB = new JCheckBox("Other");
        m_preparationOtherTF = new JTextField(50);
        m_preparationOtherTF.setEnabled(false);

        m_complianceGroup = new ButtonGroup();
        m_complianceGroup.add(m_complianceYesRB);
        m_complianceGroup.add(m_complianceNoRB);

        m_complexityGroup = new ButtonGroup();
        m_complexityGroup.add(m_complexityLowRB);
        m_complexityGroup.add(m_complexityMediumRB);
        m_complexityGroup.add(m_complexityHighRB);


        m_gelGroup = new ButtonGroup();
        m_gelGroup.add(m_preparationGel1DRB);
        m_gelGroup.add(m_preparationGel2DRB);

        m_strainingGroup = new ButtonGroup();
        m_strainingGroup.add(m_preparationGelStraingingBlueRB);
        m_strainingGroup.add(m_preparationGelStraingingSilverRB);

        if (readOnly) {
            m_responsiblePersonTF.setEditable(false);
            m_samplesNumberTF.setEditable(false);
            m_organismTF.setEditable(false);
            m_complianceYesRB.setEnabled(false);
            m_complianceNoRB.setEnabled(false);
            m_proteinQuantityTF.setEditable(false);
            m_complexityLowRB.setEnabled(false);
            m_complexityMediumRB.setEnabled(false);
            m_complexityHighRB.setEnabled(false);
            m_molecularWeightTF.setEditable(false);
            m_preparationSolutionCB.setEnabled(false);
            m_solutionCompositionTF.setEditable(false);
            m_solutionCompositionTF.setEditable(false);

            m_preparationGelCB.setEnabled(false);
            m_preparationGel1DRB.setEnabled(false);
            m_preparationGel2DRB.setEnabled(false);

            m_preparationGelStraingingBlueRB.setEnabled(false);
            m_preparationGelStraingingSilverRB.setEnabled(false);

            m_preparationOtherCB.setEnabled(false);
            m_preparationOtherTF.setEditable(false);

        }



        ItemListener gelItemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                boolean solutionIsSelected = m_preparationSolutionCB.isSelected();
                m_solutionCompositionTF.setEnabled(solutionIsSelected);
                if (!solutionIsSelected) {
                    m_solutionCompositionTF.setText("");
                }

                boolean gelIsSelected = m_preparationGelCB.isSelected();
                m_preparationGel1DRB.setEnabled(gelIsSelected);
                m_preparationGel2DRB.setEnabled(gelIsSelected);
                m_preparationGelStraingingBlueRB.setEnabled(gelIsSelected);
                m_preparationGelStraingingSilverRB.setEnabled(gelIsSelected);

                if (!gelIsSelected) {
                    m_gelGroup.clearSelection();
                    m_strainingGroup.clearSelection();
                }

                boolean otherIsSelected = m_preparationOtherCB.isSelected();
                m_preparationOtherTF.setEnabled(otherIsSelected);
                if (!otherIsSelected) {
                    m_preparationOtherTF.setText("");
                }
            }
        };

        m_preparationSolutionCB.addItemListener(gelItemListener);
        m_preparationGelCB.addItemListener(gelItemListener);
        m_preparationOtherCB.addItemListener(gelItemListener);


        m_responsiblePersonTF.getDocument().addDocumentListener(dataChangedListener);
        m_samplesNumberTF.getDocument().addDocumentListener(dataChangedListener);
        m_organismTF.getDocument().addDocumentListener(dataChangedListener);
        m_complianceYesRB.addItemListener(itemChangedListener);
        m_complianceNoRB.addItemListener(itemChangedListener);
        m_proteinQuantityTF.getDocument().addDocumentListener(dataChangedListener);

        m_complexityLowRB.addItemListener(itemChangedListener);
        m_complexityMediumRB.addItemListener(itemChangedListener);
        m_complexityHighRB.addItemListener(itemChangedListener);

        m_molecularWeightTF.getDocument().addDocumentListener(dataChangedListener);

        m_preparationSolutionCB.addItemListener(itemChangedListener);
        m_solutionCompositionTF.getDocument().addDocumentListener(dataChangedListener);
        m_preparationGelCB.addItemListener(itemChangedListener);
        m_preparationGel1DRB.addItemListener(itemChangedListener);
        m_preparationGel2DRB.addItemListener(itemChangedListener);
        m_preparationGelStraingingBlueRB.addItemListener(itemChangedListener);
        m_preparationGelStraingingSilverRB.addItemListener(itemChangedListener);
        m_preparationOtherCB.addItemListener(itemChangedListener);
        m_preparationOtherTF.getDocument().addDocumentListener(dataChangedListener);


        // -------------- Place Widgets

        // ---Responsible Person:
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        Component[] responsiblePersonComponents = {
                new JLabel("Responsible Person:", SwingConstants.RIGHT),
                m_responsiblePersonTF
        };
        add(new FramedPanel("Responsible Person for the Analysis in EDyP-Service", new FlatPanel(responsiblePersonComponents)), c);

        c.gridy++;
        add(createSampleSubmittedPanel(readOnly), c);


        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        add(Box.createVerticalGlue(), c);

    }

    private JPanel createSampleSubmittedPanel(boolean readOnly) {
        JPanel sampleSubmittedPanel = new JPanel();
        sampleSubmittedPanel.setLayout(new GridBagLayout());


        JLabel numberOfSamplesLabel = new JLabel("Number of Samples:", SwingConstants.RIGHT);
        JLabel organismLabel = new JLabel("Organism:", SwingConstants.RIGHT);
        JLabel samplesComplianceLabel = new JLabel("Samples Compliance:", SwingConstants.RIGHT);
        JLabel proteinQuantityLabel = new JLabel("Protein Quantity (in \u00B5g):", SwingConstants.RIGHT);
        JLabel samplesComplexityLabel = new JLabel("Samples Complexity:", SwingConstants.RIGHT);
        JLabel uniqueProteinLabel = new JLabel("If unique Protein, Molecular Weight:", SwingConstants.RIGHT);


        JPanel proteinPreparationPanel = createProteinPreparationPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        Component[] componentsToImport1 = { numberOfSamplesLabel, m_samplesNumberTF, organismLabel, m_organismTF, samplesComplianceLabel, proteinQuantityLabel, m_proteinQuantityTF, samplesComplexityLabel, uniqueProteinLabel, m_molecularWeightTF };
        String[] keysToExport1 = { SAMPLES_NUMBER, ORGANISM, SAMPLE_COMPLIANCE_YES, SAMPLE_COMPLIANCE_NO, PROTEIN_QUANTITY, SAMPLE_COMPLEXITY_LOW, SAMPLE_COMPLEXITY_MEDIUM, SAMPLE_COMPLEXITY_HIGH, UNIQUE_PROTEIN_MOLECULAR_WEIGHT };
        String[] keysToReset1 = { RESET_COMPLIANCE, RESET_COMPLEXITY};
        AnalysisImportButton importButton1 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_1, componentsToImport1, keysToExport1, keysToReset1);





        Component[] componentsToImport2 = { proteinPreparationPanel };
        String[] keysToExport2 = { PROTEIN_PREPARATION_SOLUTION, PROTEIN_SOLUTION_COMPOSITION, PROTEIN_PREPARATION_GEL, PROTEIN_PREPARATION_GEL1D, PROTEIN_PREPARATION_GEL2D, PROTEIN_PREPARATION_GEL_STRAINING_BLUE, PROTEIN_PREPARATION_GEL_STRAINING_SILVER, PROTEIN_PREPARATION_OTHER, PROTEIN_PREPARATION_OTHER_INFO  };
        String[] keysToReset2 = { RESET_GEL, RESET_STRAINING};
        AnalysisImportButton importButton2 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_1, componentsToImport2, keysToExport2, keysToReset2);


        // --- Number of Samples
        c.gridx = 0;
        c.gridy = 0;
        if (readOnly) {
            sampleSubmittedPanel.add(importButton1, c);
        }
        c.gridx++;
        sampleSubmittedPanel.add(numberOfSamplesLabel, c);

        c.gridx++;
        c.weightx = 1;
        sampleSubmittedPanel.add(m_samplesNumberTF, c);
        c.weightx = 0;

        // --- Organism
        c.gridx = 1;
        c.gridy++;
        sampleSubmittedPanel.add(organismLabel, c);

        c.gridx++;
        c.weightx = 1;
        sampleSubmittedPanel.add(m_organismTF, c);
        c.weightx = 0;

        // --- Samples Compliance
        c.gridx = 1;
        c.gridy++;
        sampleSubmittedPanel.add(samplesComplianceLabel, c);

        c.gridx++;
        Component[] components = { m_complianceYesRB, m_complianceNoRB, Box.createHorizontalGlue() };
        c.weightx = 1;
        sampleSubmittedPanel.add(new FlatPanel(components), c);
        c.weightx = 0;

        // --- Protein Quantity
        c.gridx = 1;
        c.gridy++;
        sampleSubmittedPanel.add(proteinQuantityLabel, c);

        c.gridx++;
        c.weightx = 1;
        sampleSubmittedPanel.add(m_proteinQuantityTF, c);
        c.weightx = 0;

        // --- Samples Complexity
        c.gridx = 1;
        c.gridy++;
        sampleSubmittedPanel.add(samplesComplexityLabel, c);

        c.gridx++;
        Component[] complexityComponents = { m_complexityLowRB, m_complexityMediumRB, m_complexityHighRB, Box.createHorizontalGlue() };
        sampleSubmittedPanel.add(new FlatPanel(complexityComponents), c);

        // --- Protein Molecular Weight
        c.gridx = 1;
        c.gridy++;
        sampleSubmittedPanel.add(uniqueProteinLabel, c);

        c.gridx++;
        c.weightx = 1;
        sampleSubmittedPanel.add(m_molecularWeightTF, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        if (readOnly) {
            sampleSubmittedPanel.add(importButton2, c);
            c.gridy++;
            c.weighty = 1;
            sampleSubmittedPanel.add(Box.createGlue(), c);
            c.weighty = 0;
            c.gridy--;
        }


        c.gridx = 1;
        c.gridheight = 2;
        c.gridwidth = 5;
        c.weightx = 1;
        sampleSubmittedPanel.add(proteinPreparationPanel, c);

        return new FramedPanel("Submitted Sample(s)", sampleSubmittedPanel);
    }

    private JPanel createProteinPreparationPanel() {
        JPanel proteinPreparationPanel = new JPanel();
        proteinPreparationPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // --- Solution
        c.gridx = 0;
        c.gridy = 0;
        proteinPreparationPanel.add(m_preparationSolutionCB, c);

        c.gridx++;
        c.weightx = 1;
        c.gridwidth = 4;
        Component[] solutionComponents = {
                new JLabel("Composition of the Solution:", SwingConstants.RIGHT),
                m_solutionCompositionTF
        };
        proteinPreparationPanel.add(new FlatPanel(solutionComponents), c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 1;
        proteinPreparationPanel.add(m_preparationGelCB, c);

        c.gridx++;
        proteinPreparationPanel.add(m_preparationGel1DRB, c);

        c.gridx++;
        proteinPreparationPanel.add(Box.createHorizontalStrut(20), c);

        c.gridx++;
        proteinPreparationPanel.add(new JLabel("Protein staining:", SwingConstants.RIGHT), c);

        c.gridx++;
        proteinPreparationPanel.add(m_preparationGelStraingingBlueRB, c);

        c.gridx = 1;
        c.gridy++;
        proteinPreparationPanel.add(m_preparationGel2DRB, c);

        c.gridx = 4;
        proteinPreparationPanel.add(m_preparationGelStraingingSilverRB, c);

        c.gridx = 0;
        c.gridy++;
        proteinPreparationPanel.add(m_preparationOtherCB, c);

        c.gridx++;
        c.gridwidth = 4;
        c.weightx = 1;
        proteinPreparationPanel.add(m_preparationOtherTF, c);


        return new FramedPanel("Protein Preparation", proteinPreparationPanel);
    }

    @Override
    public void loadData(ProAnalysisJson proAnalysisJson, String studyRef, HashMap<String, String> valueMap) {

        if (valueMap != null) {

            // Reset commands : used for import
            if (valueMap.get(RESET_COMPLIANCE) != null) {
                m_complianceGroup.clearSelection();
            }
            if (valueMap.get(RESET_COMPLEXITY) != null) {
                m_complexityGroup.clearSelection();
            }
            if (valueMap.get(RESET_GEL) != null) {
                m_gelGroup.clearSelection();
            }
            if (valueMap.get(RESET_STRAINING) != null) {
                m_strainingGroup.clearSelection();
            }

            setText(m_responsiblePersonTF, valueMap.get(RESPONSIBLE_PERSON));
            setText(m_samplesNumberTF, valueMap.get(SAMPLES_NUMBER));
            setText(m_organismTF, valueMap.get(ORGANISM));


            String value = valueMap.get(SAMPLE_COMPLIANCE_YES);
            if ((value!=null) && (value.equals("true"))) {
                m_complianceYesRB.setSelected(true);
            }

            value = valueMap.get(SAMPLE_COMPLIANCE_NO);
            if ((value!=null) && (value.equals("true"))) {
                m_complianceNoRB.setSelected(true);
            }

            setText(m_proteinQuantityTF, valueMap.get(PROTEIN_QUANTITY));


            value = valueMap.get(SAMPLE_COMPLEXITY_LOW);
            if ((value!=null) && (value.equals("true"))) {
                m_complexityLowRB.setSelected(true);
            }

            value = valueMap.get(SAMPLE_COMPLEXITY_MEDIUM);
            if ((value!=null) && (value.equals("true"))) {
                m_complexityMediumRB.setSelected(true);
            }

            value = valueMap.get(SAMPLE_COMPLEXITY_HIGH);
            if ((value!=null) && (value.equals("true"))) {
                m_complexityHighRB.setSelected(true);
            }

            setText(m_molecularWeightTF, valueMap.get(UNIQUE_PROTEIN_MOLECULAR_WEIGHT));


            value = valueMap.get(PROTEIN_PREPARATION_SOLUTION);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationSolutionCB.setSelected(true);
            } else {
                m_preparationSolutionCB.setSelected(false);
            }

            setText(m_solutionCompositionTF, valueMap.get(PROTEIN_SOLUTION_COMPOSITION));


            value = valueMap.get(PROTEIN_PREPARATION_GEL);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationGelCB.setSelected(true);
            } else {
                m_preparationGelCB.setSelected(false);
            }

            value = valueMap.get(PROTEIN_PREPARATION_GEL1D);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationGel1DRB.setSelected(true);
            }

            value = valueMap.get(PROTEIN_PREPARATION_GEL2D);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationGel2DRB.setSelected(true);
            }

            value = valueMap.get(PROTEIN_PREPARATION_GEL_STRAINING_BLUE);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationGelStraingingBlueRB.setSelected(true);
            }

            value = valueMap.get(PROTEIN_PREPARATION_GEL_STRAINING_SILVER);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationGelStraingingSilverRB.setSelected(true);
            }

            value = valueMap.get(PROTEIN_PREPARATION_OTHER);
            if ((value!=null) && (value.equals("true"))) {
                m_preparationOtherCB.setSelected(true);
            } else {
                m_preparationOtherCB.setSelected(false);
            }

            setText(m_preparationOtherTF, valueMap.get(PROTEIN_PREPARATION_OTHER_INFO));



        } else {
            if (!m_readOnly) {
                m_responsiblePersonTF.setText(DataManager.getAnalysesFullNameUser());
            }
        }
    }

    public static void prefillData(ProAnalysisJson proAnalysisJson, HashMap<String, String> valueMap) {

        valueMap.put(RESPONSIBLE_PERSON, DataManager.getAnalysesFullNameUser());
    }

    @Override
    public void getTagMap(HashMap<String, String> data) {

        data.put(RESPONSIBLE_PERSON, m_responsiblePersonTF.getText().trim());
        data.put(SAMPLES_NUMBER, m_samplesNumberTF.getText().trim());
        data.put(ORGANISM, m_organismTF.getText().trim());

        data.put(SAMPLE_COMPLIANCE_YES, Boolean.toString(m_complianceYesRB.isSelected()));
        data.put(SAMPLE_COMPLIANCE_NO, Boolean.toString(m_complianceNoRB.isSelected()));

        data.put(PROTEIN_QUANTITY, m_proteinQuantityTF.getText().trim());

        data.put(SAMPLE_COMPLEXITY_LOW, Boolean.toString(m_complexityLowRB.isSelected()));
        data.put(SAMPLE_COMPLEXITY_MEDIUM, Boolean.toString(m_complexityMediumRB.isSelected()));
        data.put(SAMPLE_COMPLEXITY_HIGH, Boolean.toString(m_complexityHighRB.isSelected()));

        data.put(UNIQUE_PROTEIN_MOLECULAR_WEIGHT, m_molecularWeightTF.getText().trim());

        data.put(PROTEIN_PREPARATION_SOLUTION, Boolean.toString(m_preparationSolutionCB.isSelected()));

        data.put(PROTEIN_SOLUTION_COMPOSITION, m_solutionCompositionTF.getText().trim());

        data.put(PROTEIN_PREPARATION_GEL, Boolean.toString(m_preparationGelCB.isSelected()));
        data.put(PROTEIN_PREPARATION_GEL1D, Boolean.toString(m_preparationGel1DRB.isSelected()));
        data.put(PROTEIN_PREPARATION_GEL2D, Boolean.toString(m_preparationGel2DRB.isSelected()));
        data.put(PROTEIN_PREPARATION_GEL_STRAINING_BLUE, Boolean.toString(m_preparationGelStraingingBlueRB.isSelected()));
        data.put(PROTEIN_PREPARATION_GEL_STRAINING_SILVER, Boolean.toString(m_preparationGelStraingingSilverRB.isSelected()));
        data.put(PROTEIN_PREPARATION_OTHER, Boolean.toString(m_preparationOtherCB.isSelected()));

        data.put(PROTEIN_PREPARATION_OTHER_INFO, m_preparationOtherTF.getText().trim());

    }
}
