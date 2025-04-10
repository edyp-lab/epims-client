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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.tasks.analyses.LoadAnalysisTask;
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.analyserequest.dialogs.SelectAnalyseToImportDialog;
import fr.epims.ui.common.*;
import org.slf4j.LoggerFactory;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

/**
 *
 * Main panel of ModifyAnalyseRequestDialog which allows the user to edit an Analysis Request
 *
 * @author JM235353
 *
 */
public class AnalyseRequestMainPanel extends JPanel {

    private enum CardType {
        TYPE_DATA,
        TYPE_DATA_TO_BE_IMPORTED
    }

    private JList m_panelList;
    private GridBagConstraints m_constraints;

    private HashMap<CardType, CardLayout> m_cardLayoutMap = new HashMap<>();
    private HashMap<CardType, JPanel> m_cardPanelMap = new HashMap<>();



    public static final String PANEL_STEP_1_PART_1 = "Customer";
    public static final String PANEL_STEP_1_PART_2 = "Customer Project";
    public static final String PANEL_STEP_2_PART_1 = "Submitted Samples";
    public static final String PANEL_STEP_2_PART_2 = "Analysis Strategy";
    public static final String PANEL_STEP_2_PART_3 = "Price Setting";

    private HashMap<String, AbstractAnalyseRequestStepPanel> m_panelsMap = new HashMap<>();

    private HashMap<String, AbstractAnalyseRequestStepPanel> m_importPanelsMap = new HashMap<>();

    HashMap<String, String> m_previousValuesMap = null;

    private JScrollPane m_importCardPanelScrollPane = null;
    private int m_oldWidthDialog;

    private boolean m_readOnly;

    public AnalyseRequestMainPanel(final ModifyAnalyseRequestDialog modifyAnalyseRequestDialog, ProAnalysisJson analyseRequest, AnalysisMapJson analysisMapJson, boolean readOnly) {
        setLayout(new GridBagLayout());

        m_readOnly = readOnly;

        m_constraints = new GridBagConstraints();
        m_constraints.anchor = GridBagConstraints.NORTHWEST;
        m_constraints.fill = GridBagConstraints.BOTH;


        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        DefaultListModel<String> stepsModel = new DefaultListModel<>();
        stepsModel.addElement(PANEL_STEP_1_PART_1);
        stepsModel.addElement(PANEL_STEP_1_PART_2);
        stepsModel.addElement(PANEL_STEP_2_PART_1);
        stepsModel.addElement(PANEL_STEP_2_PART_2);
        stepsModel.addElement(PANEL_STEP_2_PART_3);

        FlatButton importButton = new FlatButton(IconManager.getIcon(IconManager.IconType.IMPORT_ANALYSE), "Import", true);
        FlatButton stopImportButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CANCEL), "Close Import", true);
        stopImportButton.setVisible(false);
        Component[] components = { importButton, stopImportButton, Box.createHorizontalGlue()};
        FlatPanel buttonPanel = new FlatPanel(components, "");

        m_panelList = new JList(stepsModel);
        m_panelList.setSelectedIndex(0);
        JScrollPane panelListScrollPane = new JScrollPane(m_panelList);


        m_constraints.gridx = 0;
        m_constraints.gridy = 0;
        add(buttonPanel, m_constraints);

        m_constraints.gridy++;
        m_constraints.weighty = 1;
        m_constraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(panelListScrollPane, m_constraints);

        m_constraints.gridy++;
        m_constraints.weighty = 0;
        m_constraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(Box.createHorizontalStrut(200), m_constraints);

        m_constraints.gridy = 0;
        m_constraints.gridheight = 3;
        m_constraints.gridx++;
        m_constraints.weightx = 1;
        m_constraints.weighty = 1;
        m_constraints.insets = new java.awt.Insets(5, 5, 5, 5);
        createCardPanel(CardType.TYPE_DATA, modifyAnalyseRequestDialog, analyseRequest, analysisMapJson);
        JPanel cardPanel = m_cardPanelMap.get(CardType.TYPE_DATA);
        JScrollPane cardPanelScrollPane = new JScrollPane(cardPanel);
        add(cardPanelScrollPane, m_constraints);

        m_panelList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                String s = (String) m_panelList.getSelectedValue();
                m_cardLayoutMap.get(CardType.TYPE_DATA).show(m_cardPanelMap.get(CardType.TYPE_DATA), s);

                CardLayout importCardLayout = m_cardLayoutMap.get(CardType.TYPE_DATA_TO_BE_IMPORTED);
                if (importCardLayout != null) {
                    importCardLayout.show(m_cardPanelMap.get(CardType.TYPE_DATA_TO_BE_IMPORTED), s);
                }
            }});


        // set ready to add the import panel
        m_constraints.gridx++;


        final AnalyseRequestMainPanel _this = this;
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectAnalyseToImportDialog dialog = new SelectAnalyseToImportDialog(MainFrame.getMainWindow());
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    ProAnalysisJson analyseRequest = dialog.getSelectedAnalyse();
                    if (analyseRequest == null) {
                        return;
                    }


                    AnalysisMapJson[] analysis = new AnalysisMapJson[1];


                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {

                                // prepare to resize the dialog
                                int width = modifyAnalyseRequestDialog.getWidth();

                                m_oldWidthDialog = width;
                                JPanel cardPanel = m_cardPanelMap.get(CardType.TYPE_DATA);
                                int extraWidth = cardPanel.getWidth()+25;
                                width += extraWidth;

                                // limit size of the dialog to current screen size (code support multi-screen)
                                int screenId = getActiveScreen(modifyAnalyseRequestDialog);
                                Dimension dimOfScreen = getScreenDimension(screenId);
                                if ((dimOfScreen!=null) && (dimOfScreen.getWidth()>800) && (width>dimOfScreen.getWidth())) {
                                    width = (int) dimOfScreen.getWidth();
                                }

                                // add the import panel
                                addImportCardPanel(analyseRequest, analysis[0]);

                                // select the correct panel for import card panel
                                String s = (String) m_panelList.getSelectedValue();
                                m_cardLayoutMap.get(CardType.TYPE_DATA_TO_BE_IMPORTED).show(m_cardPanelMap.get(CardType.TYPE_DATA_TO_BE_IMPORTED), s);

                                // resize the dialog
                                final int newWidth = width;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        modifyAnalyseRequestDialog.setSize(newWidth, modifyAnalyseRequestDialog.getHeight());
                                        modifyAnalyseRequestDialog.centerToScreen();
                                    }
                                });


                                stopImportButton.setVisible(true);
                                importButton.setVisible(false);

                                updateUI();
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }

                        }
                    };

                    LoadAnalysisTask task = new LoadAnalysisTask(callback, analyseRequest.getAnalyseId(), analysis);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                }
            }
        });

        stopImportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // remove import card panel
                remove(m_importCardPanelScrollPane);
                m_importCardPanelScrollPane = null;

                stopImportButton.setVisible(false);
                importButton.setVisible(true);

                // resize the dialog
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        modifyAnalyseRequestDialog.setSize(m_oldWidthDialog, modifyAnalyseRequestDialog.getHeight());
                        modifyAnalyseRequestDialog.centerToScreen();
                    }
                });


                updateUI();

            }
        });

    }

    private static int getActiveScreen(JDialog dialog) {
        int screenId = 1;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; i++) {
            GraphicsConfiguration gc = gd[i].getDefaultConfiguration();
            Rectangle r = gc.getBounds();
            if (r.contains(dialog.getLocation())) {
                screenId = i + 1;
            }
        }
        return screenId;
    }

    private static Dimension getScreenDimension(int screenId) {

        if (screenId > 0) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            DisplayMode mode = ge.getScreenDevices()[screenId - 1].getDisplayMode();
            return new Dimension(mode.getWidth(), mode.getHeight());
        }
        return null;
    }

    private void addImportCardPanel(ProAnalysisJson analyseRequest, AnalysisMapJson analysisMapJson) {

        // remove previous card panel if needed
        if (m_importCardPanelScrollPane != null) {
            remove(m_importCardPanelScrollPane);
        }

        // create/replace import card panel
        createCardPanel(CardType.TYPE_DATA_TO_BE_IMPORTED, null, analyseRequest, analysisMapJson);
        JPanel cardImportPanel = m_cardPanelMap.get(CardType.TYPE_DATA_TO_BE_IMPORTED);

        // display new card panel
        m_importCardPanelScrollPane = new JScrollPane(cardImportPanel);
        add(m_importCardPanelScrollPane, m_constraints);

        updateUI();
    }

    public String getSelectedPanelKey() {
        return (String) m_panelList.getSelectedValue();
    }

    private void createCardPanel(CardType cardType, ModifyAnalyseRequestDialog d, ProAnalysisJson analyseRequest, AnalysisMapJson analysisMapJson) {

        CardLayout cardLayout = new CardLayout();
        m_cardLayoutMap.put(cardType, cardLayout);

        JPanel cardPanel = new JPanel(cardLayout);
        m_cardPanelMap.put(cardType, cardPanel);

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> map = null;
        if ((analysisMapJson != null) && (analysisMapJson.getData() != null)) {
            try {
                map = mapper.readValue(analysisMapJson.getData(), HashMap.class);
            } catch (Exception e) {
                LoggerFactory.getLogger("Epims.Client").debug("Unexpected exception", e);
            }
        }

        DocumentListener dataChangedListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                change();
            }
            public void removeUpdate(DocumentEvent e) {
                change();
            }
            public void insertUpdate(DocumentEvent e) {
                change();
            }

            public void change() {
                if (m_loading) {
                    return;
                }
                d.dataChanged();
            }
        };

        ItemListener valueChangedListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (m_loading) {
                    return;
                }
                d.dataChanged();
            }

        };

        TableModelListener tableModelListener = new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (m_loading) {
                    return;
                }
                d.dataChanged();
            }
        };

        m_loading = true;
        try {

            String studyRef = (analysisMapJson != null) ? analysisMapJson.getStudyRef() : null;

            HashMap<String, AbstractAnalyseRequestStepPanel> panelsMap = (cardType == CardType.TYPE_DATA) ? m_panelsMap : m_importPanelsMap;

            AnalysisRequestStep1Part1Panel analysisRequestStep1Part1 = new AnalysisRequestStep1Part1Panel(m_readOnly || (cardType == CardType.TYPE_DATA_TO_BE_IMPORTED), d, this, analysisMapJson, dataChangedListener, valueChangedListener, tableModelListener);
            cardPanel.add(PANEL_STEP_1_PART_1, analysisRequestStep1Part1);
            panelsMap.put(PANEL_STEP_1_PART_1, analysisRequestStep1Part1);

            AnalysisRequestStep1Part2Panel analysisRequestStep1Part2 = new AnalysisRequestStep1Part2Panel(m_readOnly || (cardType == CardType.TYPE_DATA_TO_BE_IMPORTED), d, this, analysisMapJson, dataChangedListener, valueChangedListener, tableModelListener);
            cardPanel.add(PANEL_STEP_1_PART_2, analysisRequestStep1Part2);
            panelsMap.put(PANEL_STEP_1_PART_2, analysisRequestStep1Part2);

            AnalysisRequestStep2Part1Panel analysisRequestStep2Part1 = new AnalysisRequestStep2Part1Panel(m_readOnly || (cardType == CardType.TYPE_DATA_TO_BE_IMPORTED), d, this, analysisMapJson, dataChangedListener, valueChangedListener, tableModelListener);
            cardPanel.add(PANEL_STEP_2_PART_1, analysisRequestStep2Part1);
            panelsMap.put(PANEL_STEP_2_PART_1, analysisRequestStep2Part1);

            AnalysisRequestStep2Part2Panel analysisRequestStep2Part2 = new AnalysisRequestStep2Part2Panel(m_readOnly || (cardType == CardType.TYPE_DATA_TO_BE_IMPORTED), d, this, analysisMapJson, dataChangedListener, valueChangedListener, tableModelListener);
            cardPanel.add(PANEL_STEP_2_PART_2, analysisRequestStep2Part2);
            panelsMap.put(PANEL_STEP_2_PART_2, analysisRequestStep2Part2);

            AnalysisRequestStep2Part3Panel analysisRequestStep2Part3 = new AnalysisRequestStep2Part3Panel(m_readOnly || (cardType == CardType.TYPE_DATA_TO_BE_IMPORTED), d, this, analysisMapJson, dataChangedListener, valueChangedListener, tableModelListener);
            cardPanel.add(PANEL_STEP_2_PART_3, analysisRequestStep2Part3);
            panelsMap.put(PANEL_STEP_2_PART_3, analysisRequestStep2Part3);


            m_previousValuesMap = map != null ? new HashMap<>(map) : new HashMap<>();
            analysisRequestStep1Part1.loadData(analyseRequest, studyRef, map);
            analysisRequestStep1Part2.loadData(analyseRequest, studyRef, map);
            analysisRequestStep2Part1.loadData(analyseRequest, studyRef, map);
            analysisRequestStep2Part2.loadData(analyseRequest, studyRef, map);
            analysisRequestStep2Part3.loadData(analyseRequest, studyRef, map);
        } finally {
            m_loading = false;
        }

    }
    private boolean m_loading = false;

    public String getStudyRef() {
        return ((AnalysisRequestStep1Part1Panel) m_panelsMap.get(PANEL_STEP_1_PART_1)).getStudyRef();
    }

    public boolean isCollaborative() {
        return ((AnalysisRequestStep1Part2Panel) m_panelsMap.get(PANEL_STEP_1_PART_2)).isCollaborative();
    }

    public boolean isNonCollaborative() {
        return ((AnalysisRequestStep1Part2Panel) m_panelsMap.get(PANEL_STEP_1_PART_2)).isNonCollaborative();
    }

    public boolean checkFields(ModifyAnalyseRequestDialog dialog) {
        return ((AnalysisRequestStep1Part1Panel) m_panelsMap.get(PANEL_STEP_1_PART_1)).checkFields(dialog, m_cardPanelMap.get(CardType.TYPE_DATA), m_cardLayoutMap.get(CardType.TYPE_DATA), PANEL_STEP_1_PART_1);
    }

    public void collaborativeStatusChanged(boolean collaborative, boolean nonCollaborative) {
        ((AnalysisRequestStep2Part3Panel) m_panelsMap.get(PANEL_STEP_2_PART_3)).collaborativeStatusChanged(collaborative, nonCollaborative);
    }

    public HashMap<String, String> getTagMap() {
        HashMap<String, String> map = new HashMap<>();

        for (AbstractAnalyseRequestStepPanel panel : m_panelsMap.values()) {
            panel.getTagMap(map);
        }

        // keep values which are from analysis overview (progress dialog)
        String[] keysToKept = ModifyProgressPanel.ALL_KEYS;
        for (String key : keysToKept) {
            String value = m_previousValuesMap.get(key);
            if (value != null) {
                map.put(key, value);
            }
        }


        return map;
    }

    public void importTo(String panelKey, HashMap<String, String> exportDataMap) {
        AbstractAnalyseRequestStepPanel panel = m_panelsMap.get(panelKey);
        panel.loadData(null, null, exportDataMap);
    }

}
