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

import fr.edyp.epims.json.AnalysePriceItemJson;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.AnalysisPriceListJson;
import fr.epims.dataaccess.DataManager;
import fr.epims.ui.analyserequest.panels.model.PriceTableModel;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatPanel;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 *
 * Dialog used to select one or multiple prices item from a predined list.
 * The selected prices item are added to the bill part of the Request Analysis
 *
 * @author JM235353
 *
 */
public class PriceListPresetDialog  extends DefaultDialog {

    private JComboBox<AnalysisPriceListJson> m_priceListComboBox;

    private JRadioButton m_choice1;
    private JTextField m_minutesTf1;
    private JRadioButton m_choice2;

    private ButtonGroup m_choicesButtonGroup = new ButtonGroup();

    private JRadioButton m_collaborativePriceRb;
    private JRadioButton m_graalCollaborativePriceRb;
    private JRadioButton m_nonCollaborativePriceRb;
    private JLabel m_sdsPriceLabel;

    private ArrayList<JRadioButton> m_otherPricesRadioButtons = new ArrayList<>();
    private ArrayList<String> m_otherPricesLabel = new ArrayList<>();
    private ArrayList<Float> m_otherPrices = new ArrayList<>();

    private JRadioButton m_userRadioButton;
    private JTextField m_userLabelTF;
    private JTextField m_userPriceTF;

    private JTextField m_numberTF;

    private int m_defaultPriceListId;

    private JPanel m_internalPanel;
    private JPanel m_pricesPanel;
    private GridBagConstraints m_internalComplentConstraints;

    private boolean m_collaborativeService;
    private boolean m_nonCollaborativeService;

    private PriceTableModel.PriceItem m_priceItem;

    public PriceListPresetDialog(Window parent, AnalysisMapJson analysisMapJson, boolean collaborativeService, boolean nonCollaborativeService, PriceTableModel.PriceItem priceItem) {
        super(parent);

        setTitle("Default Prices");
        setResizable(true);

        m_priceItem = priceItem;

        m_collaborativeService = collaborativeService;
        m_nonCollaborativeService = nonCollaborativeService;

        m_defaultPriceListId = (analysisMapJson != null) ? analysisMapJson.getPriceListId() : -1;

        m_internalPanel = createInternalComponent();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_internalPanel);
        setInternalComponent(scrollPane);

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        preselectPriceItem();
    }

    private JPanel createInternalComponent() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        m_internalComplentConstraints = new GridBagConstraints();
        m_internalComplentConstraints.anchor = GridBagConstraints.NORTHWEST;
        m_internalComplentConstraints.fill = GridBagConstraints.BOTH;
        m_internalComplentConstraints.insets = new java.awt.Insets(5, 5, 5, 5);

        m_priceListComboBox = new JComboBox<>();
        m_priceListComboBox.setRenderer(new PriceListComboBoxRenderer());
        Vector priceListVector = new Vector();
        List<AnalysisPriceListJson> priceListArray = DataManager.getAnalysisPriceListJsonArray();

        int i = 0;
        int defaultSelection = priceListArray.size()-1;
        for (AnalysisPriceListJson priceList : priceListArray) {
            priceListVector.add(priceList);
            if (priceList.getId() == m_defaultPriceListId) {
                defaultSelection = i;
            }
            i++;
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(priceListVector);
        m_priceListComboBox.setModel(model);

        m_priceListComboBox.setSelectedIndex(defaultSelection);



        m_priceListComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePricesUI();
            }
        });


        Component[] componentsSelectPriceList = { new JLabel("Price List:"), m_priceListComboBox, Box.createHorizontalGlue()};

        m_internalComplentConstraints.gridx = 0;
        m_internalComplentConstraints.gridy = 0;
        m_internalComplentConstraints.weightx = 1;
        p.add(new FlatPanel(componentsSelectPriceList), m_internalComplentConstraints);

        m_pricesPanel = createPricesPanel();

        m_internalComplentConstraints.gridy++;
        p.add(m_pricesPanel, m_internalComplentConstraints);

        JPanel numberPanel = createNumberPanel();

        m_internalComplentConstraints.gridy++;
        p.add(numberPanel, m_internalComplentConstraints);

        m_internalComplentConstraints.gridy++;
        m_internalComplentConstraints.weighty = 1;
        p.add(Box.createGlue(), m_internalComplentConstraints);

        // need to put back previous value : needed to add/remove m_pricesPanel for updating it.
        m_internalComplentConstraints.gridy -= 2;
        m_internalComplentConstraints.weighty = 0;

        return p;
    }

    private void preselectPriceItem() {
        if (!m_priceItem.getDescription().trim().isEmpty()) {
            m_userLabelTF.setText(m_priceItem.getDescription().trim());
            m_userPriceTF.setText(m_priceItem.getUnitPrice().toString());

            m_userRadioButton.setSelected(true);
            m_userLabelTF.setEnabled(true);
            m_userPriceTF.setEnabled(true);

            m_numberTF.setText(m_priceItem.getNumberOfSamples().toString());
        }
    }

    private JPanel createPricesPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_collaborativePriceRb = new JRadioButton();
        m_graalCollaborativePriceRb = new JRadioButton();
        m_nonCollaborativePriceRb = new JRadioButton();
        if (m_collaborativeService) {
            m_collaborativePriceRb.setSelected(true);
        }
        if (m_nonCollaborativeService) {
            m_nonCollaborativePriceRb.setSelected(true);
        }

        m_collaborativePriceRb.setEnabled(false);
        m_graalCollaborativePriceRb.setEnabled(false);
        m_nonCollaborativePriceRb.setEnabled(false);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(m_collaborativePriceRb);
        buttonGroup.add(m_graalCollaborativePriceRb);
        buttonGroup.add(m_nonCollaborativePriceRb);

        m_sdsPriceLabel = new JLabel();
        m_sdsPriceLabel.setEnabled(false);


        m_otherPricesRadioButtons.clear();
        m_otherPricesLabel.clear();
        m_otherPrices.clear();
        AnalysisPriceListJson analysisPriceList = DataManager.getAnalysisPriceListJson(getPriceListId());
        if (analysisPriceList != null) {
            for (AnalysePriceItemJson priceItem : analysisPriceList.getPriceMap().values()) {
                String label = priceItem.getLabel();
                if ((!label.equals(AnalysePriceItemJson.SDS_PRICE_LABEL)) &&
                        (!label.equals(AnalysePriceItemJson.MSMS_NONCOLLABORATIVE_PRICE_LABEL)) &&
                        (!label.equals(AnalysePriceItemJson.MSMS_GRAALCOLLABORATIVE_PRICE_LABEL)) &&
                        (!label.equals(AnalysePriceItemJson.MSMS_COLLABORATIVE_PRICE_LABEL))) {
                    JRadioButton radioButton = new JRadioButton(label+": "+priceItem.getPrice());
                    m_choicesButtonGroup.add(radioButton);
                    m_otherPricesRadioButtons.add(radioButton);
                    m_otherPricesLabel.add(label);
                    m_otherPrices.add(priceItem.getPrice());

                    radioButton.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            updateNumberTF();
                        }
                    });
                }
            }
        }

        m_userRadioButton = new JRadioButton();
        m_choicesButtonGroup.add(m_userRadioButton);
        m_userLabelTF = new JTextField();
        m_userPriceTF = new JTextField();



        fillPrices();




        m_choice1 = new JRadioButton("Nano-LC-MS/MS analysis (chromatographic gradient of ");
        m_choicesButtonGroup.add(m_choice1);
        m_minutesTf1 = new JTextField(3);
        m_minutesTf1.setEnabled(false);
        Component[] componentsLCMSMS = { m_choice1, m_minutesTf1, new JLabel(" minutes)"), Box.createHorizontalGlue()};

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        p.add(new FlatPanel(componentsLCMSMS), c);

        Component[] componentsChoice1Rb = { Box.createHorizontalStrut(40), m_collaborativePriceRb, Box.createHorizontalGlue()};
        c.gridy++;
        p.add(new FlatPanel(componentsChoice1Rb), c);

        Component[] componentsChoice2Rb = { Box.createHorizontalStrut(40), m_graalCollaborativePriceRb, Box.createHorizontalGlue()};
        c.gridy++;
        p.add(new FlatPanel(componentsChoice2Rb), c);

        Component[] componentsChoice3Rb = { Box.createHorizontalStrut(40), m_nonCollaborativePriceRb, Box.createHorizontalGlue()};
        c.gridy++;
        p.add(new FlatPanel(componentsChoice3Rb), c);


        m_choice2 = new JRadioButton("SDS-PAGE (stacking separation, Coomassie blue staining)");
        m_choicesButtonGroup.add(m_choice2);
        c.gridy++;
        p.add(m_choice2, c);

        Component[] componentsSds = { Box.createHorizontalStrut(40), m_sdsPriceLabel, Box.createHorizontalGlue()};
        c.gridy++;
        p.add(new FlatPanel(componentsSds), c);

        for (JRadioButton cb : m_otherPricesRadioButtons) {
            c.gridy++;
            p.add(cb, c);
        }

        c.gridy++;
        m_userRadioButton = new JRadioButton("Label:");
        m_choicesButtonGroup.add(m_userRadioButton);
        m_userLabelTF = new JTextField(30);
        m_userPriceTF = new JTextField(4);
        m_userLabelTF.setEnabled(false);
        m_userPriceTF.setEnabled(false);
        Component[] componentsUser = { m_userRadioButton, m_userLabelTF, new JLabel("Price:"), m_userPriceTF, Box.createHorizontalGlue()};
        c.gridy++;
        p.add(new FlatPanel(componentsUser), c);



        c.gridy++;
        c.weighty = 1;
        p.add(Box.createGlue(), c);


        m_choice1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                m_minutesTf1.setEnabled(m_choice1.isSelected());
                m_collaborativePriceRb.setEnabled(m_choice1.isSelected());
                m_graalCollaborativePriceRb.setEnabled(m_choice1.isSelected());
                m_nonCollaborativePriceRb.setEnabled(m_choice1.isSelected());
                updateNumberTF();
            }
        });

        m_choice2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                m_sdsPriceLabel.setEnabled(m_choice2.isSelected());
                updateNumberTF();
            }
        });

        m_userRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                m_userLabelTF.setEnabled(m_userRadioButton.isSelected());
                m_userPriceTF.setEnabled(m_userRadioButton.isSelected());
                updateNumberTF();
            }
        });

        return p;
    }

    private void updateNumberTF() {
        boolean enabled = m_choice1.isSelected() || m_choice2.isSelected() || m_userRadioButton.isSelected();
        if (!enabled) {
            for (JRadioButton rb : m_otherPricesRadioButtons) {
                if (rb.isSelected()) {
                    enabled = true;
                    break;
                }
            }
        }
        m_numberTF.setEnabled(enabled);
    }

    private void fillPrices() {
        AnalysisPriceListJson analysisPriceList = DataManager.getAnalysisPriceListJson(getPriceListId());
        if (analysisPriceList != null) {

            for (AnalysePriceItemJson priceItem : analysisPriceList.getPriceMap().values()) {
                if (priceItem.getLabel().equals(AnalysePriceItemJson.SDS_PRICE_LABEL)) {
                    m_sdsPriceLabel.setText("Price: " + priceItem.getPrice()+" per gel");
                } else if (priceItem.getLabel().equals(AnalysePriceItemJson.MSMS_NONCOLLABORATIVE_PRICE_LABEL)) {
                    m_nonCollaborativePriceRb.setText("Non Collaborative Service : " + priceItem.getPrice()+" per hour");
                } else if (priceItem.getLabel().equals(AnalysePriceItemJson.MSMS_GRAALCOLLABORATIVE_PRICE_LABEL)) {
                    m_graalCollaborativePriceRb.setText("Collaborative Service for Graal : " + priceItem.getPrice()+" per hour");
                } else if (priceItem.getLabel().equals(AnalysePriceItemJson.MSMS_COLLABORATIVE_PRICE_LABEL)) {
                    m_collaborativePriceRb.setText("Collaborative Service : " + priceItem.getPrice()+" per hour");
                }
            }
        }
    }

    private JPanel createNumberPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel numberLabel = new JLabel("Number of Samples:");
        m_numberTF = new JTextField(3);
        m_numberTF.setEnabled(false);
        m_numberTF.setText("1");


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        p.add(Box.createHorizontalGlue(), c);

        c.gridx++;
        c.weightx = 0;
        p.add(numberLabel, c);

        c.gridx++;
        p.add(m_numberTF, c);




        return p;
    }



    private void updatePricesUI() {
        m_internalPanel.remove(m_pricesPanel);
        m_pricesPanel = createPricesPanel();

        m_internalPanel.add(m_pricesPanel, m_internalComplentConstraints);

        m_internalPanel.updateUI();

        preselectPriceItem();
    }

    @Override
    public boolean okCalled() {

        if (m_choice1.isSelected()) {
            String minutes = m_minutesTf1.getText().trim();
            if (minutes.isEmpty()) {
                highlight(m_minutesTf1);
                setStatus(true, "You must fill the Number of Minutes.");
                return false;
            }
            try {
                int value = Integer.parseInt(minutes);
                if (value<1) {
                    highlight(m_minutesTf1);
                    setStatus(true, "Incorrect value for the Number of Minutes.");
                    return false;
                }
            } catch (NumberFormatException nfe) {
                highlight(m_minutesTf1);
                setStatus(true, "Incorrect value for the Number of Minutes.");
                return false;
            }

            if ((! m_nonCollaborativePriceRb.isSelected()) && (! m_collaborativePriceRb.isSelected()) &&  (! m_graalCollaborativePriceRb.isSelected())) {
                highlight(m_choice1);
                setStatus(true, "You must select Collaborative/Non Collaborative Price.");
                return false;
            }

        } else if (m_userRadioButton.isSelected()) {
            if (m_userLabelTF.getText().trim().isEmpty()) {
                highlight(m_userLabelTF);
                setStatus(true, "You must fill the Label.");
                return false;
            }
            String price = m_userPriceTF.getText().trim();
            if (m_userPriceTF.getText().trim().isEmpty()) {
                highlight(m_userPriceTF);
                setStatus(true, "You must fill the Price.");
                return false;
            }
            try {
                Float.valueOf(price);
            } catch (Exception e) {
                highlight(m_userPriceTF);
                setStatus(true, "Incorrect Price.");
                return false;
            }
        }

        try {
            Integer.valueOf(m_numberTF.getText().trim());
        } catch (Exception e) {
            highlight(m_numberTF);
            setStatus(true, "Incorrect Number of Samples.");
            return false;
        }
            m_numberTF.setEnabled(false);
       return true;
    }

    public ArrayList<String> getLinesForBill() {
        ArrayList<String> billLines = new ArrayList<>();
        if (m_choice1.isSelected()) {
            billLines.add(m_choice1.getText()+m_minutesTf1.getText().trim()+" minutes)");
        }
        if (m_choice2.isSelected()) {
            billLines.add(m_choice2.getText());
        }

        for (int i = 0; i< m_otherPricesRadioButtons.size(); i++) {
            JRadioButton cb = m_otherPricesRadioButtons.get(i);
            if (cb.isSelected()) {
                billLines.add(m_otherPricesLabel.get(i));
            }

        }

        if (m_userRadioButton.isSelected()) {
            billLines.add(m_userLabelTF.getText().trim());
        }


        return billLines;
    }

    public ArrayList<Float> getPricesForBill() {

        AnalysisPriceListJson priceList = ((AnalysisPriceListJson) m_priceListComboBox.getSelectedItem());
        HashMap<String, AnalysePriceItemJson> priceMap = priceList.getPriceMap();

        ArrayList<Float> pricesLines = new ArrayList<>();
        if (m_choice1.isSelected()) {

            String minutes = m_minutesTf1.getText().trim();
            int minutesInt = Integer.parseInt(minutes);
            float p = minutesInt/60f;

            if (m_collaborativePriceRb.isSelected()) {
                float price = Math.round(((priceMap.get(AnalysePriceItemJson.MSMS_COLLABORATIVE_PRICE_LABEL).getPrice() * p) * 100))/100;
                pricesLines.add(price);
            } else if (m_graalCollaborativePriceRb.isSelected()) {
                float price = Math.round(((priceMap.get(AnalysePriceItemJson.MSMS_GRAALCOLLABORATIVE_PRICE_LABEL).getPrice() * p) * 100))/100;
                pricesLines.add(price);
            } else if (m_nonCollaborativePriceRb.isSelected()) {
                float price = Math.round(((priceMap.get(AnalysePriceItemJson.MSMS_NONCOLLABORATIVE_PRICE_LABEL).getPrice() * p) * 100))/100;
                pricesLines.add(price);
            }
        }
        if (m_choice2.isSelected()) {
            pricesLines.add(priceMap.get(AnalysePriceItemJson.SDS_PRICE_LABEL).getPrice());
        }

        for (int i = 0; i< m_otherPricesRadioButtons.size(); i++) {
            JRadioButton cb = m_otherPricesRadioButtons.get(i);
            if (cb.isSelected()) {
                pricesLines.add(m_otherPrices.get(i));
            }

        }

        if (m_userRadioButton.isSelected()) {
            pricesLines.add(Float.valueOf(m_userPriceTF.getText().trim()));
        }

        return pricesLines;
    }

    public Integer getNumberOfSamples() {
        return Integer.valueOf(m_numberTF.getText().trim());
    }

    public int getPriceListId() {
        return ((AnalysisPriceListJson) m_priceListComboBox.getSelectedItem()).getId();
    }


    public class PriceListComboBoxRenderer extends DefaultListCellRenderer {

        public PriceListComboBoxRenderer() {
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            label.setText(UtilDate.dateToStringForIHM(((AnalysisPriceListJson) value).getDate()));

            return label;
        }

    }
}
