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

import fr.edyp.epims.json.AnalysePriceItemJson;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.AnalysisPriceListJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.DataManager;
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.analyserequest.dialogs.PriceListPresetDialog;
import fr.epims.ui.analyserequest.panels.model.PriceTableModel;
import fr.epims.ui.common.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Sub Panel of AnalyseRequestMainPanel.
 * Possibility to modify Request Analysis information (pricing)
 *
 * @author JM235353
 *
 */
public class AnalysisRequestStep2Part3Panel extends AbstractAnalyseRequestStepPanel {

    public static final String DELIVERY_TIME = "DELIVERY_TIME";
    public static final String PRICE_TABLE_ANALYSIS = "PRICE_TABLE_ANALYSIS_";
    public static final String PRICE_TABLE_SAMPLES_NUMBER = "PRICE_TABLE_SAMPLES_NUMBER_";
    public static final String PRICE_TABLE_UNIT_PRICE = "PRICE_TABLE_UNIT_PRICE_";
    public static final String PRICE_TABLE_PRICE = "PRICE_TABLE_PRICE_";

    public static final String TOTAL_FACTURE = "TOTAL_FACTURE";

    public static final String SDS_PRICE = "SDS_PRICE";
    public static final String COLLABORATIVE_PRICE = "COLLABORATIVE_PRICE";
    public static final String GRAALCOLLABORATIVE_PRICE = "GRAALCOLLABORATIVE_PRICE";
    public static final String NONCOLLABORATIVE_PRICE = "NONCOLLABORATIVE_PRICE";


    private JTextField m_deliveryTimeTF;
    private JLabel m_collaborativeStatus;

    private DecoratedTable m_table;
    private PriceTableModel m_model;

    private AnalysisMapJson m_analysisMapJson;

    private JDialog m_parentDialog;
    private AnalyseRequestMainPanel m_mainPanel;

    private AnalysisImportButton m_importButton1;

    public AnalysisRequestStep2Part3Panel(boolean readOnly, ModifyAnalyseRequestDialog parentDialog, AnalyseRequestMainPanel mainPanel, AnalysisMapJson analysisMapJson, DocumentListener dataChangedListener, ItemListener itemChangedListener, TableModelListener tableModelListener) {
        super( readOnly,  parentDialog,  mainPanel,  analysisMapJson,  dataChangedListener,  itemChangedListener, tableModelListener);
        setLayout(new GridBagLayout());


        m_parentDialog = parentDialog;
        m_mainPanel = mainPanel;

        m_analysisMapJson = analysisMapJson;

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // -------------- Create Widgets
        m_deliveryTimeTF = new JTextField(50);
        m_collaborativeStatus = new JLabel();



        m_table = new DecoratedTable();
        m_model = new PriceTableModel(new DeletePriceRowCallback(), new EditPriceRowCallback());
        m_table.setModel(m_model);

        m_table.getColumnModel().getColumn(PriceTableModel.COLTYPE_ANALYSIS).setMinWidth(360);
        m_table.getColumnModel().getColumn(PriceTableModel.COLTYPE_ACTION).setPreferredWidth(20);
        m_table.getColumnModel().getColumn(PriceTableModel.COLTYPE_ACTION).setMaxWidth(20);

        if (!readOnly) {
            IconButtonTableCellRenderer actionRenderer = (IconButtonTableCellRenderer) m_model.getRenderer(0, PriceTableModel.COLTYPE_ACTION);

            m_table.addMouseListener(actionRenderer);
            m_table.addMouseMotionListener(actionRenderer);

            IconButtonTableCellRenderer editRenderer = (IconButtonTableCellRenderer) m_model.getRenderer(0, PriceTableModel.COLTYPE_ANALYSIS);
            m_table.addMouseListener(editRenderer);
            m_table.addMouseMotionListener(editRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(m_table);
        m_table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        if (readOnly) {
            m_deliveryTimeTF.setEditable(false);
            m_table.setEditable(false);
            m_table.setEnabled(false);
        }


        Component[] componentsToImport1 = { tableScrollPane };
        ArrayList<PriceTableModel.PriceItem> values = m_model.getValues();
        int row = 0;
        ArrayList<String> keysToExportArray = new ArrayList<>();
        for (PriceTableModel.PriceItem priceItem : values) {

            if (priceItem.getNumberOfSamples() == 0) {
                continue;
            }

            String key = String.valueOf(row);
            keysToExportArray.add(PRICE_TABLE_ANALYSIS+key);
            keysToExportArray.add(PRICE_TABLE_SAMPLES_NUMBER+key);
            keysToExportArray.add(PRICE_TABLE_UNIT_PRICE+key);
            keysToExportArray.add(PRICE_TABLE_PRICE+key);
            row++;
        }
        String[] keysToExport1 = {}; // will be set later
        String[] keysToReset1 = {};
        m_importButton1 = new AnalysisImportButton(m_mainPanel, this, AnalyseRequestMainPanel.PANEL_STEP_2_PART_3, componentsToImport1, keysToExport1, keysToReset1);


        // ---------------------------
        m_model.addTableModelListener(tableModelListener);
        m_deliveryTimeTF.getDocument().addDocumentListener(dataChangedListener);



        // -------------- Place Widgets

        // Delivery Time
        Component[] componentsDeliveryTime = { new JLabel("Delivery Time estimated for the Analysis:", SwingConstants.RIGHT),
                m_deliveryTimeTF, Box.createHorizontalGlue()};
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        add(new FlatPanel(componentsDeliveryTime), c);

        // -- Collaborative / Non Collaborative Analysis
        c.gridy++;
        add(Box.createHorizontalStrut(10), c);
        c.gridy++;
        add(m_collaborativeStatus, c);


        // --- Price Table
        c.gridx = 0;
        c.gridy++;
        if (readOnly) {
            add(m_importButton1, c);
        }

        c.gridx++;
        c.gridheight = 2;
        c.weighty = 1;
        add(tableScrollPane, c);

    }

    public void collaborativeStatusChanged(boolean collaborative, boolean nonCollaborative) {
        if (collaborative) {
            m_collaborativeStatus.setText("Service Type : Collaborative");
        } else if (nonCollaborative) {
                m_collaborativeStatus.setText("Service Type : Non Collaborative");
        } else {
            m_collaborativeStatus.setText("Service Type : Unknown");
        }
    }

    @Override
    public void loadData(ProAnalysisJson proAnalysisJson, String studyRef, HashMap<String, String> valueMap) {

        if (valueMap != null) {
            m_deliveryTimeTF.setText(valueMap.get(DELIVERY_TIME));

            ArrayList<PriceTableModel.PriceItem> values = new ArrayList<>();
            int row = 0;
            while (true) {
                String key = String.valueOf(row);
                String analysis = valueMap.get(PRICE_TABLE_ANALYSIS + key);
                if (analysis == null) {
                    // put an empty line if needed
                    values.add(new PriceTableModel.PriceItem("", new Integer(0), new Float(0), new Float(0)));
                    break;
                }
                Integer number = 0;
                try {
                    number = Integer.valueOf(valueMap.get(PRICE_TABLE_SAMPLES_NUMBER + key));
                } catch (Exception e) {

                }
                Float unitPrice = 0f;
                try {
                    unitPrice = Float.valueOf(valueMap.get(PRICE_TABLE_UNIT_PRICE + key));
                } catch (Exception e) {

                }
                Float price = 0f;
                try {
                    price = Float.valueOf(valueMap.get(PRICE_TABLE_PRICE + key));
                } catch (Exception e) {

                }
                values.add(new PriceTableModel.PriceItem(analysis, number, unitPrice, price));

                row++;
            }
            m_model.setValues(values);

            boolean collaborative = false;
            String value = valueMap.get(AnalysisRequestStep1Part2Panel.SERVICE_TYPE_COLLABORATIVE);
            if ((value!=null) && (value.equals("true"))) {
                collaborative = true;
            }

            boolean nonCollaborative = false;
            value = valueMap.get(AnalysisRequestStep1Part2Panel.SERVICE_TYPE_NONCOLLABORATIVE);
            if ((value!=null) && (value.equals("true"))) {
                nonCollaborative = true;
            }
            collaborativeStatusChanged(collaborative, nonCollaborative);


            row = 0;
            ArrayList<String> keysToExportArray = new ArrayList<>();
            for (PriceTableModel.PriceItem priceItem : values) {

                if (priceItem.getNumberOfSamples() == 0) {
                    continue;
                }

                String key = String.valueOf(row);
                keysToExportArray.add(PRICE_TABLE_ANALYSIS+key);
                keysToExportArray.add(PRICE_TABLE_SAMPLES_NUMBER+key);
                keysToExportArray.add(PRICE_TABLE_UNIT_PRICE+key);
                keysToExportArray.add(PRICE_TABLE_PRICE+key);
                row++;
            }
            String[] keysToExport1 = new String[keysToExportArray.size()];
            keysToExport1 = keysToExportArray.toArray(keysToExport1);
            m_importButton1.setKeysToExport(keysToExport1);
        } else {
            ArrayList<PriceTableModel.PriceItem> values = new ArrayList<>();
            values.add(new PriceTableModel.PriceItem("", new Integer(0), new Float(0), new Float(0)));
            m_model.setValues(values);

            collaborativeStatusChanged(false, false);
        }


    }

    public JLabel getCollaborativeStatusLabel() {
        return m_collaborativeStatus;
    }

    @Override
    public void getTagMap(HashMap<String, String> data) {
        data.put(DELIVERY_TIME, m_deliveryTimeTF.getText().trim());

        ArrayList<PriceTableModel.PriceItem> values = m_model.getValues();
        int row = 0;
        float total = 0;
        for (PriceTableModel.PriceItem priceItem : values) {

            if (priceItem.getNumberOfSamples() == 0) {
                continue;
            }

            String key = String.valueOf(row);
            data.put(PRICE_TABLE_ANALYSIS+key, priceItem.getDescription());
            data.put(PRICE_TABLE_SAMPLES_NUMBER+key, String.valueOf(priceItem.getNumberOfSamples()));
            data.put(PRICE_TABLE_UNIT_PRICE+key, String.valueOf(priceItem.getUnitPrice()));
            data.put(PRICE_TABLE_PRICE+key, String.valueOf(priceItem.getPrice()));
            total += priceItem.getPrice();

            row++;
        }


        data.put(TOTAL_FACTURE, String.valueOf(total));

        AnalysisPriceListJson analysisPriceList;
        int priceListId = m_analysisMapJson.getPriceListId();
        if (priceListId == -1) {
            analysisPriceList = DataManager.getLastAnalysisPriceListJson();
        } else {
            analysisPriceList = DataManager.getAnalysisPriceListJson(priceListId);
        }

        HashMap<String, AnalysePriceItemJson> priceMap = analysisPriceList.getPriceMap();
        data.put(SDS_PRICE, String.valueOf(priceMap.get(AnalysePriceItemJson.SDS_PRICE_LABEL).getPrice()));
        data.put(COLLABORATIVE_PRICE, String.valueOf(priceMap.get(AnalysePriceItemJson.MSMS_COLLABORATIVE_PRICE_LABEL).getPrice()));
        data.put(GRAALCOLLABORATIVE_PRICE, String.valueOf(priceMap.get(AnalysePriceItemJson.MSMS_GRAALCOLLABORATIVE_PRICE_LABEL).getPrice()));
        data.put(NONCOLLABORATIVE_PRICE, String.valueOf(priceMap.get(AnalysePriceItemJson.MSMS_NONCOLLABORATIVE_PRICE_LABEL).getPrice()));

    }

    public class DeletePriceRowCallback implements RendererMouseCallback {
        @Override
        public void mouseAction(MouseEvent e) {

            int col = m_table.columnAtPoint(e.getPoint());
            int row = m_table.rowAtPoint(e.getPoint());
            if ((row != -1) && (col != -1)) {

                int colModelIndex = m_table.convertColumnIndexToModel(col);
                if (colModelIndex == PriceTableModel.COLTYPE_ACTION) {

                    int rowModelIndex = m_table.convertRowIndexToModel(row);
                    m_model.deleteRow(rowModelIndex);


                }
            }

        }
    }

    public class EditPriceRowCallback implements RendererMouseCallback {
        @Override
        public void mouseAction(MouseEvent e) {

            int col = m_table.columnAtPoint(e.getPoint());
            int row = m_table.rowAtPoint(e.getPoint());
            if ((row != -1) && (col != -1)) {

                int colModelIndex = m_table.convertColumnIndexToModel(col);
                if (colModelIndex == PriceTableModel.COLTYPE_ANALYSIS) {

                    int rowModelIndex = m_table.convertRowIndexToModel(row);

                    PriceTableModel.PriceItem priceItem = m_model.getPriceItem(rowModelIndex);

                    PriceListPresetDialog dialog = new PriceListPresetDialog(m_parentDialog, m_analysisMapJson, m_mainPanel.isCollaborative(), m_mainPanel.isNonCollaborative(), priceItem);
                    dialog.centerToWindow(MainFrame.getMainWindow());
                    dialog.setVisible(true);
                    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        m_model.addValues(rowModelIndex, dialog.getLinesForBill(), dialog.getPricesForBill(), dialog.getNumberOfSamples(), rowModelIndex);
                    }

                    int priceListId = dialog.getPriceListId();
                    m_analysisMapJson.setPriceListId(priceListId);

                }
            }

        }
    }
}
