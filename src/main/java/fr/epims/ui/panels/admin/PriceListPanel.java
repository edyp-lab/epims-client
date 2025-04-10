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

package fr.epims.ui.panels.admin;

import fr.edyp.epims.json.AnalysePriceItemJson;
import fr.edyp.epims.json.AnalysisPriceListJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.epims.tasks.analyses.SavePriceListTask;
import fr.epims.ui.common.FlatPanel;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Panel to create a new Price List for Analysis Requests
 *
 * @author JM235353
 *
 */
public class PriceListPanel extends JPanel implements DataManager.DataManagerListener {

    private JTextField m_sdsPagePriceTf;
    private JTextField m_lcMsNonCollaborativePriceTf;
    private JTextField m_lcMsGraalCollaborativePriceTf;
    private JTextField m_lcMsCollaborativePriceTf;

    private HashMap<JTextField, AnalysePriceItemJson> m_userPricesTfMap = new HashMap();


    private FlatButton m_saveButton;

    private GridBagConstraints m_c;
    private JScrollPane m_textfieldScrollPane;
    private JPanel m_textfieldPanel;
    private int m_yTextField;

    private DocumentListener m_dataChangedListener;

    private AnalysisPriceListJson m_priceList;

    public PriceListPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Analysis Requests Price List ");
        setBorder(titledBorder);

        m_c = new GridBagConstraints();
        m_c.anchor = GridBagConstraints.NORTHWEST;
        m_c.fill = GridBagConstraints.BOTH;
        m_c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_textfieldPanel = textfieldPanel();
        m_textfieldScrollPane = new JScrollPane();
        m_textfieldScrollPane.setBorder(BorderFactory.createEmptyBorder());
        m_textfieldScrollPane.setViewportView(m_textfieldPanel);


        m_dataLoaded = false;
        loadData();

        // --- Save Button
        m_saveButton = new FlatButton(IconManager.getIcon(IconManager.IconType.SAVE), true);
        m_saveButton.setEnabled(false);
        Component[] components = { m_saveButton, Box.createHorizontalGlue()};

        m_c.gridx = 0;
        m_c.gridy = 0;
        m_c.gridwidth = 2;
        add(new FlatPanel(components), m_c);


        m_c.gridy++;
        m_yTextField = m_c.gridy;
        m_c.gridwidth = 1;
        add(m_textfieldScrollPane, m_c);

        m_c.gridx++;
        m_c.weightx = 1;
        add(Box.createGlue(), m_c);


        m_c.gridx = 1;
        m_c.gridy++;
        m_c.weightx = 1;
        m_c.weighty = 1;
        add(Box.createGlue(), m_c);


        m_dataChangedListener = new DocumentListener() {
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
                if (! m_dataLoaded) {
                    return;
                }
                m_saveButton.setEnabled(true);
            }
        };




        m_saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String error = null;
                try {
                    Float.valueOf(m_sdsPagePriceTf.getText().trim());
                } catch (NumberFormatException nfe) {
                    error = "Incorrect value for SDS Page Price";
                }
                try {
                    Float.valueOf(m_lcMsNonCollaborativePriceTf.getText().trim());
                } catch (NumberFormatException nfe) {
                    error = "Incorrect value for lc MS/MS Non Collaborative Price";
                }
                try {
                    Float.valueOf(m_lcMsGraalCollaborativePriceTf.getText().trim());
                } catch (NumberFormatException nfe) {
                    error = "Incorrect value for lc MS/MS Graal Collaborative Price";
                }
                try {
                    Float.valueOf(m_lcMsCollaborativePriceTf.getText().trim());
                } catch (NumberFormatException nfe) {
                    error = "Incorrect value for lc MS/MS Collaborative Price";
                }

                for (JTextField tf : m_userPricesTfMap.keySet()) {
                    try {
                        Float.valueOf(tf.getText().trim());
                    } catch (NumberFormatException nfe) {
                        error = "Incorrect value for "+m_userPricesTfMap.get(tf).getLabel()+" value";
                        break;
                    }
                }

                if (error != null) {

                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Incorrect Value", error);
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                }

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), AnalysisPriceListJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                AnalysisPriceListJson priceList = new AnalysisPriceListJson();
                HashMap<String, AnalysePriceItemJson> prices = new HashMap<>();
                priceList.setPriceMap(prices);

                AnalysePriceItemJson sdsPriceItem = new AnalysePriceItemJson();
                sdsPriceItem.setLabel(AnalysePriceItemJson.SDS_PRICE_LABEL);
                sdsPriceItem.setPrice(Float.valueOf(m_sdsPagePriceTf.getText().trim()));
                prices.put(sdsPriceItem.getLabel(), sdsPriceItem);

                AnalysePriceItemJson lcMsNonCollaborativePriceItem = new AnalysePriceItemJson();
                lcMsNonCollaborativePriceItem.setLabel(AnalysePriceItemJson.MSMS_NONCOLLABORATIVE_PRICE_LABEL);
                lcMsNonCollaborativePriceItem.setPrice(Float.valueOf(m_lcMsNonCollaborativePriceTf.getText().trim()));
                prices.put(lcMsNonCollaborativePriceItem.getLabel(), lcMsNonCollaborativePriceItem);

                AnalysePriceItemJson lcMsGraalCollaborativePriceItem = new AnalysePriceItemJson();
                lcMsGraalCollaborativePriceItem.setLabel(AnalysePriceItemJson.MSMS_GRAALCOLLABORATIVE_PRICE_LABEL);
                lcMsGraalCollaborativePriceItem.setPrice(Float.valueOf(m_lcMsGraalCollaborativePriceTf.getText().trim()));
                prices.put(lcMsGraalCollaborativePriceItem.getLabel(), lcMsGraalCollaborativePriceItem);

                AnalysePriceItemJson lcMsCollaborativePriceItem = new AnalysePriceItemJson();
                lcMsCollaborativePriceItem.setLabel(AnalysePriceItemJson.MSMS_COLLABORATIVE_PRICE_LABEL);
                lcMsCollaborativePriceItem.setPrice(Float.valueOf(m_lcMsCollaborativePriceTf.getText().trim()));
                prices.put(lcMsCollaborativePriceItem.getLabel(), lcMsCollaborativePriceItem);

                for (JTextField tf : m_userPricesTfMap.keySet()) {
                    AnalysePriceItemJson priceItem = m_userPricesTfMap.get(tf);
                    priceItem.setPrice(Float.valueOf(tf.getText().trim()));
                    prices.put(priceItem.getLabel(), priceItem);
                }

                priceList.setDate(new Date());

                ArrayList<AnalysisPriceListJson> priceListJsonList = new ArrayList<>();
                priceListJsonList.add(priceList);

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                m_saveButton.setEnabled(false);
                                DataManager.priceListModified(priceListJsonList);
                                m_dataLoaded = false;
                                loadData();
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or there is an internal error");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    SavePriceListTask task = new SavePriceListTask(callback, priceListJsonList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

                }

        });

        DataManager.addListener(AnalysisPriceListJson.class, this);

    }




    public void reinit() {
        m_dataLoaded = false;
        loadData();
    }

    public void loadData() {
        if (m_dataLoaded) {
            return;
        }


        m_sdsPagePriceTf.setText("");
        m_lcMsNonCollaborativePriceTf.setText("");
        m_lcMsGraalCollaborativePriceTf.setText("");
        m_lcMsCollaborativePriceTf.setText("");


        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {
                m_priceList = null;
                updateTextFields();
                m_dataLoaded = true;

            }
        };
        DataManager.dataAvailable(callback, false);
    }
    private boolean m_dataLoaded = false;

    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {
        // nothing to do
    }

    @Override
    public void updateAll(HashSet<Class> c) {

        m_dataLoaded = false;

        updateTextFields();


        m_dataLoaded = true;
    }

    private void updateTextFields() {

        remove(m_textfieldScrollPane);
        m_textfieldScrollPane.remove(m_textfieldPanel);
        m_textfieldPanel = textfieldPanel();
        m_textfieldScrollPane.setViewportView(m_textfieldPanel);

        m_c.gridy = m_yTextField;
        m_c.gridwidth = 1;
        add(m_textfieldScrollPane, m_c);

        updateUI();

    }

    private JPanel textfieldPanel() {

        m_userPricesTfMap.clear();

        JPanel textfieldPanel = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Prices ");
        textfieldPanel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_sdsPagePriceTf = new JTextField(4);
        m_lcMsNonCollaborativePriceTf = new JTextField(4);
        m_lcMsGraalCollaborativePriceTf = new JTextField(4);
        m_lcMsCollaborativePriceTf = new JTextField(4);

        // SDS Page
        c.gridx = 0;
        c.gridy = 0;
        textfieldPanel.add(new JLabel("SDS Page Price:", SwingConstants.RIGHT), c);

        c.gridx++;
        textfieldPanel.add(m_sdsPagePriceTf, c);


        // lc MS/MS Non Collaborative
        c.gridy++;
        c.gridx = 0;
        textfieldPanel.add(new JLabel("lc MS/MS Non Collaborative Price:", SwingConstants.RIGHT), c);

        c.gridx++;
        textfieldPanel.add(m_lcMsNonCollaborativePriceTf, c);

        // lc MS/MS Graal Collaborative Price
        c.gridy++;
        c.gridx = 0;
        textfieldPanel.add(new JLabel("lc MS/MS Graal Collaborative Price:", SwingConstants.RIGHT), c);

        c.gridx++;
        textfieldPanel.add(m_lcMsGraalCollaborativePriceTf, c);

        // lc MS/MS Collaborative Price
        c.gridy++;
        c.gridx = 0;
        textfieldPanel.add(new JLabel("lc MS/MS Collaborative Price:", SwingConstants.RIGHT), c);

        c.gridx++;
        textfieldPanel.add(m_lcMsCollaborativePriceTf, c);

        if (m_priceList == null) {
            m_priceList = DataManager.getLastAnalysisPriceListJson();

            if (m_priceList == null) {
                m_priceList = new AnalysisPriceListJson();
                m_priceList.setId(m_priceList.getId());
                m_priceList.setDate(m_priceList.getDate());
                m_priceList.setPriceMap(new HashMap<>());
            }
        }


            HashMap<String, AnalysePriceItemJson> priceMapCopy = new HashMap<>();
            for (String key : m_priceList.getPriceMap().keySet()) {
                priceMapCopy.put(key, m_priceList.getPriceMap().get(key));
            }
            m_priceList.setPriceMap(priceMapCopy);


            HashMap<String, AnalysePriceItemJson> priceMap = m_priceList.getPriceMap();

            if (priceMap != null) {
                for (AnalysePriceItemJson priceItem : priceMap.values()) {
                    if (priceItem.getLabel().equals(AnalysePriceItemJson.SDS_PRICE_LABEL)) {
                        m_sdsPagePriceTf.setText(String.valueOf(priceItem.getPrice()));
                    } else if (priceItem.getLabel().equals(AnalysePriceItemJson.MSMS_NONCOLLABORATIVE_PRICE_LABEL)) {
                        m_lcMsNonCollaborativePriceTf.setText(String.valueOf(priceItem.getPrice()));
                    } else if (priceItem.getLabel().equals(AnalysePriceItemJson.MSMS_GRAALCOLLABORATIVE_PRICE_LABEL)) {
                        m_lcMsGraalCollaborativePriceTf.setText(String.valueOf(priceItem.getPrice()));
                    } else if (priceItem.getLabel().equals(AnalysePriceItemJson.MSMS_COLLABORATIVE_PRICE_LABEL)) {
                        m_lcMsCollaborativePriceTf.setText(String.valueOf(priceItem.getPrice()));
                    } else {
                        String text = priceItem.getLabel();
                        float price = priceItem.getPrice();
                        JLabel label = new JLabel(text, SwingConstants.RIGHT);
                        JTextField textField = new JTextField();
                        textField.setText(String.valueOf(price));
                        FlatButton deleteButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16), true);

                        c.gridy++;
                        c.gridx = 0;
                        textfieldPanel.add(label, c);

                        c.gridx++;
                        textfieldPanel.add(textField, c);

                        c.gridx++;
                        textfieldPanel.add(deleteButton, c);

                        m_userPricesTfMap.put(textField, priceItem);

                        deleteButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                priceMap.remove(text);
                                updateTextFields();
                                m_saveButton.setEnabled(true);
                            }
                        });



                    }
                }
            }


            JTextField newLabelTextField = new JTextField();
            JTextField newPriceTextField = new JTextField();
            FlatButton addButton = new FlatButton(IconManager.getIcon(IconManager.IconType.PLUS_SMALL_16), true);

            c.gridy++;
            c.gridx = 0;
            textfieldPanel.add(newLabelTextField, c);

            c.gridx++;
            textfieldPanel.add(newPriceTextField, c);

            c.gridx++;
            textfieldPanel.add(addButton, c);

            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String error = null;
                    try {
                        Float.valueOf(newPriceTextField.getText().trim());
                    } catch (NumberFormatException nfe) {
                        error = "Incorrect value for "+newLabelTextField.getText().trim();
                    }

                    if (error != null) {
                        InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Incorrect Value", error);
                        infoDialog.centerToWindow(MainFrame.getMainWindow());
                        infoDialog.setVisible(true);
                        return;
                    }
                    HashMap<String, AnalysePriceItemJson> priceMap = m_priceList.getPriceMap();
                    AnalysePriceItemJson priceItem = new AnalysePriceItemJson();
                    priceItem.setLabel(newLabelTextField.getText().trim());
                    priceItem.setPrice(Float.valueOf(newPriceTextField.getText().trim()));
                    priceMap.put(priceItem.getLabel(), priceItem);
                    updateTextFields();
                    m_saveButton.setEnabled(true);
                }
            });


        c.gridy++;
        c.gridx = 3;
        c.weightx = 1;
        c.weighty = 1;
        textfieldPanel.add(Box.createGlue(), c);

        m_sdsPagePriceTf.getDocument().addDocumentListener(m_dataChangedListener);
        m_lcMsNonCollaborativePriceTf.getDocument().addDocumentListener(m_dataChangedListener);
        m_lcMsGraalCollaborativePriceTf.getDocument().addDocumentListener(m_dataChangedListener);
        m_lcMsCollaborativePriceTf.getDocument().addDocumentListener(m_dataChangedListener);

        return textfieldPanel;
    }

}
