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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

/**
 *
 * Sub Panel of AnalyseRequestMainPanel.
 * Possibility to modify Request Analysis information (description of the analysis of samples)
 *
 * @author JM235353
 *
 */
public class AnalysisRequestStep2Part2Panel extends AbstractAnalyseRequestStepPanel {

    private static final String STORAGE_ROOM_TEMPERATURE = "STORAGE_ROOM_TEMPERATURE";
    private static final String STORAGE_ROOM_FRIDGE = "STORAGE_ROOM_FRIDGE";
    private static final String STORAGE_ROOM_MINUS20 = "STORAGE_ROOM_MINUS20";
    private static final String STORAGE_ROOM_MINUS80 = "STORAGE_ROOM_MINUS80";
    private static final String GEL_YES = "GEL_YES";
    private static final String GEL_NO = "GEL_NO";
    private static final String GEL_STACKING = "GEL_STACKING";
    private static final String GEL_PSEUDO_SEPARATING = "GEL_PSEUDO_SEPARATING";
    private static final String GEL_COMPLETE_SEPARATING = "GEL_COMPLETE_SEPARATING";
    private static final String GEL_STAINING_BLUE = "GEL_STAINING_BLUE";
    private static final String GEL_STAINING_NITRATE = "GEL_STAINING_NITRATE";
    private static final String OTHER_PROCESSING = "OTHER_PROCESSING";
    private static final String SHOTGUN_PROTEOMICS = "SHOTGUN_PROTEOMICS";
    private static final String TARGETED_PROTEOMICS = "TARGETED_PROTEOMICS";
    private static final String TOPDOWN_PROTEOMICS = "TOPDOWN_PROTEOMICS";
    private static final String OTHER_PROTEOMICS = "OTHER_PROTEOMICS";
    private static final String OTHER_PROTEOMICS_TEXT = "OTHER_PROTEOMICS_TEXT";
    private static final String ENZYME_TRYPSIN = "ENZYME_TRYPSIN";
    private static final String ENZYME_OTHER = "ENZYME_OTHER";
    private static final String ENZYME_OTHER_TEXT = "ENZYME_OTHER_TEXT";
    private static final String NUMBER_INJECTIONS = "NUMBER_INJECTIONS";
    private static final String LC_GRADIENT_DURATION = "LC_GRADIENT_DURATION";
    private static final String SPECTROMETER = "SPECTROMETER";

    private static final String DATA_ANALYSIS_DB = "DATA_ANALYSIS_DB";
    private static final String DATA_ANALYSIS_MODIFICATION = "DATA_ANALYSIS_MODIFICATION";
    private static final String DATA_ANALYSIS_SOFTWARE = "DATA_ANALYSIS_SOFTWARE";
    private static final String DATA_ANALYSIS_EXPECTED_COVERAGE = "DATA_ANALYSIS_EXPECTED_COVERAGE";
    private static final String ANALYSIS_STRATEGY_OTHER = "ANALYSIS_STRATEGY_OTHER";


    private static final String RESET_GEL = "RESET_GEL";
    private static final String RESET_SEPARATING = "RESET_SEPARATING";

    private static final String RESET_STAINING = "RESET_STAINING";
    private static final String RESET_PROTEOMICS = "RESET_PROTEOMICS";

    private ButtonGroup m_gelGroup;
    private ButtonGroup m_separatingGroup;
    private ButtonGroup m_stainingGroup;
    private ButtonGroup m_proteomicsGroup;


    private JCheckBox m_roomTemperatureCB;
    private JCheckBox m_roomFridgeCB;
    private JCheckBox m_roomMinus20CB;
    private JCheckBox m_roomMinus80CB;
    private JRadioButton m_gelYesRB;
    private JRadioButton m_gelNoRB;
    private JRadioButton m_stackingRB;
    private JRadioButton m_pseudoSeparatingRB;
    private JRadioButton m_completeSeparatingRB;
    private JRadioButton m_stainingCoomassieRB;
    private JRadioButton m_stainingSilverRB;
    private JTextField m_otherProcessingTF;
    private JRadioButton m_shotgunProteomicsRB;
    private JRadioButton m_targetedProteomicsRB;
    private JRadioButton m_topdownProteomicsRB;
    private JRadioButton m_otherProteomicsRB;
    private JTextField m_otherProteomicsTF;
    private JCheckBox m_enzymeTrypsinCB;
    private JCheckBox m_enzymeOtherCB;
    private JTextField m_enzymeOtherTF;
    private JTextField m_numberInjectionsTF;
    private JTextField m_gradientDurationTF;
    private JTextField m_spectrometerTF;
    private JTextField m_dbTF;
    private JTextField m_modificationTF;
    private JTextField m_softwareTF;
    private JTextField m_expectedCoverageTF;
    private JTextField m_analysisOtherTF;


    public AnalysisRequestStep2Part2Panel(boolean readOnly, ModifyAnalyseRequestDialog parentDialog, AnalyseRequestMainPanel mainPanel, AnalysisMapJson analysisMapJson, DocumentListener dataChangedListener, ItemListener itemChangedListener, TableModelListener tableModelListener) {
        super( readOnly,  parentDialog,  mainPanel,  analysisMapJson,  dataChangedListener,  itemChangedListener, tableModelListener);
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        JLabel storageLabel = new JLabel("Storage of sample(s):", SwingConstants.RIGHT);
        JLabel digestingLabel = new JLabel("Digesting enzymes:", SwingConstants.RIGHT);
        JLabel gradientDurationLabel = new JLabel("LC Gradient Duration:", SwingConstants.RIGHT);
        JLabel dataAnalysisLabel = new JLabel("Data analysis:", SwingConstants.RIGHT);


        m_roomTemperatureCB = new JCheckBox("room temperature");
        m_roomFridgeCB = new JCheckBox("fridge");
        m_roomMinus20CB = new JCheckBox("-20°");
        m_roomMinus80CB = new JCheckBox("-80°");

        m_gelYesRB = new JRadioButton("Yes:");
        m_gelNoRB = new JRadioButton("No");
        m_stackingRB = new JRadioButton("stacking");
        m_pseudoSeparatingRB = new JRadioButton("pseudo-separating");
        m_completeSeparatingRB = new JRadioButton("complete separating");
        m_stackingRB.setEnabled(false);
        m_pseudoSeparatingRB.setEnabled(false);
        m_completeSeparatingRB.setEnabled(false);

        m_stainingCoomassieRB = new JRadioButton("Coomassie Blue");
        m_stainingSilverRB = new JRadioButton("Silver Nitrate");

        m_otherProcessingTF = new JTextField(50);

        m_shotgunProteomicsRB = new JRadioButton("shotgun proteomics");
        m_targetedProteomicsRB = new JRadioButton("targeted proteomics");
        m_topdownProteomicsRB = new JRadioButton("top-down proteomics");
        m_otherProteomicsRB = new JRadioButton("other");
        m_otherProteomicsTF = new JTextField(50);

        m_enzymeTrypsinCB = new JCheckBox("trypsin");
        m_enzymeOtherCB = new JCheckBox("other");
        m_enzymeOtherTF = new JTextField(50);

        m_numberInjectionsTF = new JTextField(10);

        m_gradientDurationTF = new JTextField(10);

        m_spectrometerTF = new JTextField(50);

        m_dbTF = new JTextField(50);
        m_modificationTF = new JTextField(50);
        m_softwareTF = new JTextField(50);
        m_expectedCoverageTF = new JTextField(10);

        m_analysisOtherTF = new JTextField(50);




        m_gelGroup = new ButtonGroup();
        m_gelGroup.add(m_gelYesRB);
        m_gelGroup.add(m_gelNoRB);

        m_separatingGroup = new ButtonGroup();
        m_separatingGroup.add(m_stackingRB);
        m_separatingGroup.add(m_pseudoSeparatingRB);
        m_separatingGroup.add(m_completeSeparatingRB);

        m_stainingGroup = new ButtonGroup();
        m_stainingGroup.add(m_stainingCoomassieRB);
        m_stainingGroup.add(m_stainingSilverRB);

        m_proteomicsGroup = new ButtonGroup();
        m_proteomicsGroup.add(m_shotgunProteomicsRB);
        m_proteomicsGroup.add(m_targetedProteomicsRB);
        m_proteomicsGroup.add(m_topdownProteomicsRB);
        m_proteomicsGroup.add(m_otherProteomicsRB);


        if (readOnly) {
            m_roomTemperatureCB.setEnabled(false);
            m_roomFridgeCB.setEnabled(false);
            m_roomMinus20CB.setEnabled(false);
            m_roomMinus80CB.setEnabled(false);

            m_gelYesRB.setEnabled(false);
            m_gelNoRB.setEnabled(false);
            m_stackingRB.setEnabled(false);
            m_pseudoSeparatingRB.setEnabled(false);
            m_completeSeparatingRB.setEnabled(false);
            m_stackingRB.setEnabled(false);


            m_stainingCoomassieRB.setEnabled(false);
            m_stainingSilverRB.setEnabled(false);

            m_otherProcessingTF.setEditable(false);

            m_shotgunProteomicsRB.setEnabled(false);
            m_targetedProteomicsRB.setEnabled(false);
            m_topdownProteomicsRB.setEnabled(false);
            m_otherProteomicsRB.setEnabled(false);
            m_otherProteomicsTF.setEditable(false);

            m_enzymeTrypsinCB.setEnabled(false);
            m_enzymeOtherCB.setEnabled(false);
            m_enzymeOtherTF.setEditable(false);

            m_numberInjectionsTF.setEditable(false);

            m_gradientDurationTF.setEditable(false);

            m_spectrometerTF.setEditable(false);

            m_dbTF.setEditable(false);
            m_modificationTF.setEditable(false);
            m_softwareTF.setEditable(false);
            m_expectedCoverageTF.setEditable(false);

            m_analysisOtherTF.setEditable(false);

        } else {

            // ------------

            ItemListener gelItemListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean yesSelected = m_gelYesRB.isSelected();
                    m_stackingRB.setEnabled(yesSelected);
                    m_pseudoSeparatingRB.setEnabled(yesSelected);
                    m_completeSeparatingRB.setEnabled(yesSelected);

                    if (!yesSelected) {
                        m_separatingGroup.clearSelection();
                    }
                }
            };

            m_gelYesRB.addItemListener(gelItemListener);
            m_gelNoRB.addItemListener(gelItemListener);

            // ------------

            ItemListener typeOfAnalysisListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean otherSelected = m_otherProteomicsRB.isSelected();
                    m_otherProteomicsTF.setEnabled(otherSelected);

                    if (!otherSelected) {
                        m_otherProteomicsTF.setText("");
                    }
                }
            };

            m_shotgunProteomicsRB.addItemListener(typeOfAnalysisListener);
            m_targetedProteomicsRB.addItemListener(typeOfAnalysisListener);
            m_topdownProteomicsRB.addItemListener(typeOfAnalysisListener);
            m_otherProteomicsRB.addItemListener(typeOfAnalysisListener);

            // ------------

            ItemListener digestiveEnzymeListener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean otherSelected = m_enzymeOtherCB.isSelected();
                    m_enzymeOtherTF.setEnabled(otherSelected);

                    if (!otherSelected) {
                        m_enzymeOtherTF.setText("");
                    }
                }
            };

            m_enzymeTrypsinCB.addItemListener(digestiveEnzymeListener);
            m_enzymeOtherCB.addItemListener(digestiveEnzymeListener);

            // ------------

            m_roomTemperatureCB.addItemListener(itemChangedListener);
            m_roomFridgeCB.addItemListener(itemChangedListener);
            m_roomMinus20CB.addItemListener(itemChangedListener);
            m_roomMinus80CB.addItemListener(itemChangedListener);
            m_gelYesRB.addItemListener(itemChangedListener);
            m_gelNoRB.addItemListener(itemChangedListener);
            m_stackingRB.addItemListener(itemChangedListener);
            m_pseudoSeparatingRB.addItemListener(itemChangedListener);
            m_completeSeparatingRB.addItemListener(itemChangedListener);
            m_stainingCoomassieRB.addItemListener(itemChangedListener);
            m_stainingSilverRB.addItemListener(itemChangedListener);
            m_otherProcessingTF.getDocument().addDocumentListener(dataChangedListener);
            m_shotgunProteomicsRB.addItemListener(itemChangedListener);
            m_targetedProteomicsRB.addItemListener(itemChangedListener);
            m_topdownProteomicsRB.addItemListener(itemChangedListener);
            m_otherProteomicsRB.addItemListener(itemChangedListener);
            m_otherProteomicsTF.getDocument().addDocumentListener(dataChangedListener);
            m_enzymeTrypsinCB.addItemListener(itemChangedListener);
            m_enzymeOtherCB.addItemListener(itemChangedListener);
            m_enzymeOtherTF.getDocument().addDocumentListener(dataChangedListener);
            m_numberInjectionsTF.getDocument().addDocumentListener(dataChangedListener);
            m_gradientDurationTF.getDocument().addDocumentListener(dataChangedListener);
            m_spectrometerTF.getDocument().addDocumentListener(dataChangedListener);
            m_dbTF.getDocument().addDocumentListener(dataChangedListener);
            m_modificationTF.getDocument().addDocumentListener(dataChangedListener);
            m_softwareTF.getDocument().addDocumentListener(dataChangedListener);
            m_expectedCoverageTF.getDocument().addDocumentListener(dataChangedListener);
            m_analysisOtherTF.getDocument().addDocumentListener(dataChangedListener);

        }
        Component[] componentsToImport1 = { storageLabel, m_otherProteomicsTF };
        String[] keysToExport1 = { STORAGE_ROOM_TEMPERATURE, STORAGE_ROOM_FRIDGE, STORAGE_ROOM_MINUS20, STORAGE_ROOM_MINUS80, GEL_YES, GEL_NO, GEL_STACKING, GEL_PSEUDO_SEPARATING,
                GEL_COMPLETE_SEPARATING, GEL_STAINING_BLUE, GEL_STAINING_NITRATE, OTHER_PROCESSING, SHOTGUN_PROTEOMICS, TARGETED_PROTEOMICS, TOPDOWN_PROTEOMICS, OTHER_PROTEOMICS, OTHER_PROTEOMICS_TEXT };
        String[] keysToReset1 = { RESET_GEL, RESET_SEPARATING, RESET_STAINING, RESET_PROTEOMICS };
        AnalysisImportButton importButton1 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_2, componentsToImport1, keysToExport1, keysToReset1);


        Component[] componentsToImport2 = { digestingLabel, m_enzymeOtherTF };
        String[] keysToExport2 = { ENZYME_TRYPSIN, ENZYME_OTHER, ENZYME_OTHER_TEXT };
        String[] keysToReset2 = {  };
        AnalysisImportButton importButton2 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_2, componentsToImport2, keysToExport2, keysToReset2);


        Component[] componentsToImport3 = { gradientDurationLabel, m_spectrometerTF };
        String[] keysToExport3 = { LC_GRADIENT_DURATION, SPECTROMETER };
        String[] keysToReset3 = {  };
        AnalysisImportButton importButton3 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_2, componentsToImport3, keysToExport3, keysToReset3);

        Component[] componentsToImport4 = { dataAnalysisLabel, m_analysisOtherTF };
        String[] keysToExport4 = { DATA_ANALYSIS_DB, DATA_ANALYSIS_MODIFICATION, DATA_ANALYSIS_SOFTWARE, DATA_ANALYSIS_EXPECTED_COVERAGE, ANALYSIS_STRATEGY_OTHER };
        String[] keysToReset4 = {  };
        AnalysisImportButton importButton4 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_2, componentsToImport4, keysToExport4, keysToReset4);


        // -------------- Place Widgets

        // --- Storage of sample(s)
        c.gridx = 0;
        c.gridy = 0;
        c.gridx = 0;
        c.gridy = 0;
        if (readOnly) {
            add(importButton1, c);
        }

        c.gridx++;
        add(storageLabel, c);

        c.gridx++;
        c.weightx = 1;
        Component[] responsiblePersonComponents = {
                m_roomTemperatureCB,
                m_roomFridgeCB,
                m_roomMinus20CB,
                m_roomMinus80CB,
                Box.createGlue()

        };
        add(new FlatPanel(responsiblePersonComponents), c);

        // --- Gel
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(new JLabel("Gel:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] gelYesComponents = {
                m_gelYesRB,
                m_stackingRB,
                m_pseudoSeparatingRB,
                m_completeSeparatingRB,
                Box.createGlue()

        };
        add(new FlatPanel(gelYesComponents), c);

        c.gridy++;
        Component[] gelNoComponents = {
                m_gelNoRB,
                Box.createGlue()

        };
        add(new FlatPanel(gelNoComponents), c);

        // --- Protein Staining
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(new JLabel("Protein staining:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] gelStainingComponents = {
                m_stainingCoomassieRB,
                m_stainingSilverRB,
                Box.createGlue()

        };
        add(new FlatPanel(gelStainingComponents), c);

        // --- Other Processing
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(new JLabel("Other Processing:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] gelOtherProcessingComponents = {
                m_otherProcessingTF

        };
        add(new FlatPanel(gelOtherProcessingComponents), c);

        // --- Type of analysis
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(new JLabel("Type of analysis:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] shotgunComponents = {
                m_shotgunProteomicsRB,
                Box.createGlue()
        };
        add(new FlatPanel(shotgunComponents), c);

        c.gridy++;
        Component[] targetedComponents = {
                m_targetedProteomicsRB,
                Box.createGlue()
        };
        add(new FlatPanel(targetedComponents), c);

        c.gridy++;
        Component[] topdownComponents = {
                m_topdownProteomicsRB,
                Box.createGlue()
        };
        add(new FlatPanel(topdownComponents), c);

        c.gridy++;
        Component[] otherProteomicsComponents = {
                m_otherProteomicsRB,
                m_otherProteomicsTF
        };
        add(new FlatPanel(otherProteomicsComponents), c);
        c.weightx = 0;

        // --- Digesting enzymes

        c.gridx = 0;
        c.gridy++;
        if (readOnly) {
            add(importButton2, c);
        }

        c.gridx++;
        add(digestingLabel, c);

        c.gridx++;
        c.weightx = 1;
        Component[] trypsineComponents = {
                m_enzymeTrypsinCB,
                Box.createGlue()
        };
        add(new FlatPanel(trypsineComponents), c);

        c.gridy++;
        Component[] otherEnzymeComponents = {
                m_enzymeOtherCB,
                m_enzymeOtherTF
        };
        add(new FlatPanel(otherEnzymeComponents), c);

        // --- Number of Injections
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(new JLabel("Number of Injections:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] numberOfInjectionsComponents = {
                m_numberInjectionsTF,
                Box.createHorizontalGlue()
        };
        add(new FlatPanel(numberOfInjectionsComponents), c);

        // --- LC Gradient Duration

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        if (readOnly) {
            add(importButton3, c);
        }

        c.gridx++;
        add(gradientDurationLabel, c);

        c.gridx++;
        c.weightx = 1;
        Component[] gradientDurationsComponents = {
                m_gradientDurationTF,
                new JLabel("minutes")
        };
        add(new FlatPanel(gradientDurationsComponents), c);

        // --- Spectrometer, method
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(new JLabel("Spectrometer, method:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] sprectrometerComponents = {
                m_spectrometerTF
        };
        add(new FlatPanel(sprectrometerComponents), c);

        // --- Data Analysis

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        if (readOnly) {
            add(importButton4, c);
        }

        c.gridx++;
        add(dataAnalysisLabel, c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createHorizontalGlue(), c);


        JPanel dataAnalysisPanel = createDataAnalysisPanel();
        c.gridx = 1;
        c.gridy++;
        c.weightx = 1;
        c.gridwidth = 2;
        add(dataAnalysisPanel, c);

        // --- Other
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        c.gridwidth = 1;
        add(new JLabel("Other:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] analysisOtherComponents = {
                m_analysisOtherTF
        };
        add(new FlatPanel(analysisOtherComponents), c);



        // ---
        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        add(Box.createVerticalGlue(), c);

    }

    private JPanel createDataAnalysisPanel() {
        JPanel dataAnalysisPanel = new JPanel();
        dataAnalysisPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // --- Choosen database
        c.gridx = 0;
        c.gridy = 0;
        dataAnalysisPanel.add(Box.createHorizontalStrut(40), c);

        c.gridx++;
        dataAnalysisPanel.add(new JLabel("Chosen database(s):", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] components = {
                m_dbTF
        };
        dataAnalysisPanel.add(new FlatPanel(components), c);

        // --- Modification
        c.weightx = 0;
        c.gridx = 0;
        c.gridy++;
        dataAnalysisPanel.add(Box.createHorizontalStrut(40), c);

        c.gridx++;
        dataAnalysisPanel.add(new JLabel("Modifications:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] modificationComponents = {
                m_modificationTF
        };
        dataAnalysisPanel.add(new FlatPanel(modificationComponents), c);

        // --- Chosen software(s)
        c.weightx = 0;
        c.gridx = 0;
        c.gridy++;
        dataAnalysisPanel.add(Box.createHorizontalStrut(40), c);

        c.gridx++;
        dataAnalysisPanel.add(new JLabel("Chosen software(s):", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] softwareComponents = {
                m_softwareTF
        };
        dataAnalysisPanel.add(new FlatPanel(softwareComponents), c);

        // --- Expected Coverage
        c.weightx = 0;
        c.gridx = 0;
        c.gridy++;
        dataAnalysisPanel.add(Box.createHorizontalStrut(40), c);

        c.gridx++;
        dataAnalysisPanel.add(new JLabel("Expected Coverage (unique protein):", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        Component[] expectedComponents = {
                m_expectedCoverageTF,
                new JLabel("%")
        };
        dataAnalysisPanel.add(new FlatPanel(expectedComponents), c);

        return dataAnalysisPanel;
    }

    @Override
    public void loadData(ProAnalysisJson proAnalysisJson, String studyRef, HashMap<String, String> valueMap) {

        if (valueMap != null) {

            // Reset commands : used for import
            if (valueMap.get(RESET_GEL) != null) {
                m_gelGroup.clearSelection();
            }
            if (valueMap.get(RESET_SEPARATING) != null) {
                m_separatingGroup.clearSelection();
            }
            if (valueMap.get(RESET_STAINING) != null) {
                m_stainingGroup.clearSelection();
            }
            if (valueMap.get(RESET_PROTEOMICS) != null) {
                m_proteomicsGroup.clearSelection();
            }


            String value = valueMap.get(STORAGE_ROOM_TEMPERATURE);
            if ((value!=null) && (value.equals("true"))) {
                m_roomTemperatureCB.setSelected(true);
            } else {
                m_roomTemperatureCB.setSelected(false);
            }

            value = valueMap.get(STORAGE_ROOM_FRIDGE);
            if ((value!=null) && (value.equals("true"))) {
                m_roomFridgeCB.setSelected(true);
            } else {
                m_roomFridgeCB.setSelected(false);
            }

            value = valueMap.get(STORAGE_ROOM_MINUS20);
            if ((value!=null) && (value.equals("true"))) {
                m_roomMinus20CB.setSelected(true);
            } else {
                m_roomMinus20CB.setSelected(false);
            }

            value = valueMap.get(STORAGE_ROOM_MINUS80);
            if ((value!=null) && (value.equals("true"))) {
                m_roomMinus80CB.setSelected(true);
            } else {
                m_roomMinus80CB.setSelected(false);
            }

            value = valueMap.get(GEL_YES);
            if ((value!=null) && (value.equals("true"))) {
                m_gelYesRB.setSelected(true);
            }

            value = valueMap.get(GEL_NO);
            if ((value!=null) && (value.equals("true"))) {
                m_gelNoRB.setSelected(true);
            }

            value = valueMap.get(GEL_STACKING);
            if ((value!=null) && (value.equals("true"))) {
                m_stackingRB.setSelected(true);
            }

            value = valueMap.get(GEL_PSEUDO_SEPARATING);
            if ((value!=null) && (value.equals("true"))) {
                m_pseudoSeparatingRB.setSelected(true);
            }

            value = valueMap.get(GEL_COMPLETE_SEPARATING);
            if ((value!=null) && (value.equals("true"))) {
                m_completeSeparatingRB.setSelected(true);
            }

            value = valueMap.get(GEL_STAINING_BLUE);
            if ((value!=null) && (value.equals("true"))) {
                m_stainingCoomassieRB.setSelected(true);
            }

            value = valueMap.get(GEL_STAINING_NITRATE);
            if ((value!=null) && (value.equals("true"))) {
                m_stainingSilverRB.setSelected(true);
            }


            setText(m_otherProcessingTF, valueMap.get(OTHER_PROCESSING));


            value = valueMap.get(SHOTGUN_PROTEOMICS);
            if ((value!=null) && (value.equals("true"))) {
                m_shotgunProteomicsRB.setSelected(true);
            }

            value = valueMap.get(TARGETED_PROTEOMICS);
            if ((value!=null) && (value.equals("true"))) {
                m_targetedProteomicsRB.setSelected(true);
            }

            value = valueMap.get(TOPDOWN_PROTEOMICS);
            if ((value!=null) && (value.equals("true"))) {
                m_topdownProteomicsRB.setSelected(true);
            }

            value = valueMap.get(OTHER_PROTEOMICS);
            if ((value!=null) && (value.equals("true"))) {
                m_otherProteomicsRB.setSelected(true);
            }


            setText(m_otherProteomicsTF, valueMap.get(OTHER_PROTEOMICS_TEXT));


            value = valueMap.get(ENZYME_TRYPSIN);
            if ((value!=null) && (value.equals("true"))) {
                m_enzymeTrypsinCB.setSelected(true);
            } else {
                m_enzymeTrypsinCB.setSelected(false);
            }

            value = valueMap.get(ENZYME_OTHER);
            if ((value!=null) && (value.equals("true"))) {
                m_enzymeOtherCB.setSelected(true);
            } else {
                m_enzymeOtherCB.setSelected(false);
            }


            setText(m_enzymeOtherTF, valueMap.get(ENZYME_OTHER_TEXT));

            setText(m_numberInjectionsTF, valueMap.get(NUMBER_INJECTIONS));
            setText(m_gradientDurationTF, valueMap.get(LC_GRADIENT_DURATION));
            setText(m_spectrometerTF, valueMap.get(SPECTROMETER));
            setText(m_dbTF, valueMap.get(DATA_ANALYSIS_DB));
            setText(m_modificationTF, valueMap.get(DATA_ANALYSIS_MODIFICATION));
            setText(m_softwareTF, valueMap.get(DATA_ANALYSIS_SOFTWARE));
            setText(m_expectedCoverageTF, valueMap.get(DATA_ANALYSIS_EXPECTED_COVERAGE));
            setText(m_analysisOtherTF, valueMap.get(ANALYSIS_STRATEGY_OTHER));



        }
    }



    @Override
    public void getTagMap(HashMap<String, String> data) {

        data.put(STORAGE_ROOM_TEMPERATURE, Boolean.toString(m_roomTemperatureCB.isSelected()));
        data.put(STORAGE_ROOM_FRIDGE, Boolean.toString(m_roomFridgeCB.isSelected()));
        data.put(STORAGE_ROOM_MINUS20, Boolean.toString(m_roomMinus20CB.isSelected()));
        data.put(STORAGE_ROOM_MINUS80, Boolean.toString(m_roomMinus80CB.isSelected()));

        data.put(GEL_YES, Boolean.toString(m_gelYesRB.isSelected()));
        data.put(GEL_NO, Boolean.toString(m_gelNoRB.isSelected()));
        data.put(GEL_STACKING, Boolean.toString(m_stackingRB.isSelected()));
        data.put(GEL_PSEUDO_SEPARATING, Boolean.toString(m_pseudoSeparatingRB.isSelected()));
        data.put(GEL_COMPLETE_SEPARATING, Boolean.toString(m_completeSeparatingRB.isSelected()));
        data.put(GEL_STAINING_BLUE, Boolean.toString(m_stainingCoomassieRB.isSelected()));
        data.put(GEL_STAINING_NITRATE, Boolean.toString(m_stainingSilverRB.isSelected()));

        data.put(OTHER_PROCESSING, m_otherProcessingTF.getText().trim());

        data.put(SHOTGUN_PROTEOMICS, Boolean.toString(m_shotgunProteomicsRB.isSelected()));
        data.put(TARGETED_PROTEOMICS, Boolean.toString(m_targetedProteomicsRB.isSelected()));
        data.put(TOPDOWN_PROTEOMICS, Boolean.toString(m_topdownProteomicsRB.isSelected()));
        data.put(OTHER_PROTEOMICS, Boolean.toString(m_otherProteomicsRB.isSelected()));

        data.put(OTHER_PROTEOMICS_TEXT, m_otherProteomicsTF.getText().trim());

        data.put(ENZYME_TRYPSIN, Boolean.toString(m_enzymeTrypsinCB.isSelected()));
        data.put(ENZYME_OTHER, Boolean.toString(m_enzymeOtherCB.isSelected()));

        data.put(ENZYME_OTHER_TEXT, m_enzymeOtherTF.getText().trim());
        data.put(NUMBER_INJECTIONS, m_numberInjectionsTF.getText().trim());
        data.put(LC_GRADIENT_DURATION, m_gradientDurationTF.getText().trim());
        data.put(SPECTROMETER, m_spectrometerTF.getText().trim());
        data.put(DATA_ANALYSIS_DB, m_dbTF.getText().trim());
        data.put(DATA_ANALYSIS_MODIFICATION, m_modificationTF.getText().trim());
        data.put(DATA_ANALYSIS_SOFTWARE, m_softwareTF.getText().trim());
        data.put(DATA_ANALYSIS_EXPECTED_COVERAGE, m_expectedCoverageTF.getText().trim());
        data.put(ANALYSIS_STRATEGY_OTHER, m_analysisOtherTF.getText().trim());


    }
}
