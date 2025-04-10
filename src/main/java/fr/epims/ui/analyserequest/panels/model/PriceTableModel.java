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

package fr.epims.ui.analyserequest.panels.model;

import fr.epims.ui.common.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;

/**
 *
 * Model of the Bill Table of a Analysis Request
 *
 * @author JM235353
 *
 */
public class PriceTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_ANALYSIS= 0;
    public static final int COLTYPE_NUMBER_OF_SAMPLES = 1;
    public static final int COLTYPE_UNIT_PRICE = 2;
    public static final int COLTYPE_PRICE = 3;
    public static final int COLTYPE_ACTION = 4;
    private static final String[] m_columnNames = { "Analysis", "Number of Samples", "Unit Price exl. Taxes (€)", "Price excl. taxes (€)", ""};
    private static final String[] m_columnTooltips = m_columnNames;

    private ArrayList<PriceItem> m_values = new ArrayList<>();


    private RendererMouseCallback m_deleteCallback;
    private RendererMouseCallback m_editCallback;

    public PriceTableModel(RendererMouseCallback deleteCallback, RendererMouseCallback editCallback) {
        m_deleteCallback = deleteCallback;
        m_editCallback = editCallback;
    }

    public void setValues(ArrayList<PriceItem> values) {
        m_values = values;
        fireTableDataChanged();
    }

    public void deleteRow(int row) {
        m_values.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void addValues(int rowStart, ArrayList<String> values, ArrayList<Float> prices, Integer numberOfSamples, int row) {
        if ((values == null) || (values.isEmpty())) {
            return;
        }


        int indexRow = rowStart;
        for (int indexRead = 0; indexRead<values.size(); indexRead++) {
            PriceItem priceItem;
            if (indexRow<m_values.size()) {
                priceItem = m_values.get(indexRow);
            } else {
                priceItem = new PriceItem();
                m_values.add(priceItem);
            }

            priceItem.setDescription(values.get(indexRead));
            priceItem.setPrice(prices.get(indexRead)*numberOfSamples);
            priceItem.setUnitPrice(prices.get(indexRead));
            priceItem.setNumberOfSamples(numberOfSamples);

            indexRow++;
        }

        if (indexRow == m_values.size()) {
            // we must add a new blank line
            m_values.add(new PriceItem());
        }


        fireTableDataChanged();
    }

    public ArrayList<PriceItem> getValues() {
        return m_values;
    }



    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (col == COLTYPE_ACTION) {
            return new IconButtonTableCellRenderer(IconManager.getIcon(IconManager.IconType.CROSS_SMALL16), m_deleteCallback, COLTYPE_ACTION, IconButtonTableCellRenderer.RowModeEnum.MODE_ROW_WITHOUT_LAST);
        } else if (col == COLTYPE_ANALYSIS) {
            return new IconButtonTableCellRenderer(IconManager.getIcon(IconManager.IconType.LIST_EDIT), m_editCallback, COLTYPE_ANALYSIS, IconButtonTableCellRenderer.RowModeEnum.MODE_ROW_ALL);
        }

        return null;
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_ACTION:
            case COLTYPE_ANALYSIS:
                return String.class;
            case COLTYPE_NUMBER_OF_SAMPLES:
                return Integer.class;
            default:
                return Float.class;
        }
    }



    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }

    public String getToolTipForHeader(int col) {
        return m_columnTooltips[col];
    }

    @Override
    public TableCellEditor getEditor(int row, int col) {
        return null;
    }



    public String getTootlTipValue(int row, int col) {
        return null;
    }


    @Override
    public int getRowCount() {
        return m_values.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        PriceItem p = m_values.get(row);
        switch (col) {
            case COLTYPE_ACTION:
                return "";
            case COLTYPE_ANALYSIS:
                return p.getDescription();
            case COLTYPE_NUMBER_OF_SAMPLES:
                return p.getNumberOfSamples();
            case COLTYPE_UNIT_PRICE:
                return p.getUnitPrice();
            case COLTYPE_PRICE:
                return p.getPrice();
        }

        return null; // should not happen

    }

    public PriceItem getPriceItem(int row) {
        return m_values.get(row);
    }

    @Override
    public boolean isCellEditable(int row, int col) {

        return (col != COLTYPE_PRICE) && (col != COLTYPE_ACTION);
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {


        PriceItem priceItem = m_values.get(row);

        switch (col) {
            case COLTYPE_ANALYSIS:
                priceItem.setDescription(aValue.toString().trim());
                break;
            case COLTYPE_NUMBER_OF_SAMPLES:
                priceItem.setNumberOfSamples((Integer)aValue);
                priceItem.setPrice(priceItem.getNumberOfSamples() *priceItem.getUnitPrice());
                fireTableRowsUpdated(row, row);
                break;
            case COLTYPE_UNIT_PRICE:
                priceItem.setUnitPrice((Float)aValue);
                priceItem.setPrice(priceItem.getNumberOfSamples() *priceItem.getUnitPrice());
                fireTableRowsUpdated(row, row);
                break;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                checkEmptyLines();
            }
        });

    }

    private void checkEmptyLines() {
        // look for first empty line
        int firstEmptyRow = -1;
        int lastEmptyRow = -1;
        int row = 0;
        for (PriceItem priceItem : m_values) {
            if (priceItem.isEmpty()) {
                if (firstEmptyRow == -1) {
                    firstEmptyRow = row;
                }
                lastEmptyRow = row;
            }

            row++;
        }

        if (lastEmptyRow == -1) {
            // we must add a line
            m_values.add(new PriceItem());
            fireTableRowsInserted(row, row);
        } else if (firstEmptyRow != lastEmptyRow) {
            // multiple empty rows : we suppress the first one
            m_values.remove(firstEmptyRow);
            fireTableRowsDeleted(firstEmptyRow, firstEmptyRow);
        }
    }



    public static class PriceItem {

        private String m_description = "";
        private Integer m_numberOfSamples = 0;
        private Float m_unitPrice = 0f;
        private Float m_price = 0f;

        public PriceItem() {
        }

        public PriceItem(String description, Integer numberOfSamples, Float unitPrice, Float price) {
            m_description = description;
            m_numberOfSamples = numberOfSamples;
            m_unitPrice = unitPrice;
            m_price = price;
        }

        public boolean isEmpty() {
            return (m_description.isEmpty() && (m_numberOfSamples.intValue() == 0) && (m_unitPrice.floatValue() == 0f));
        }

        public String getDescription() {
            return m_description;
        }
        public Integer getNumberOfSamples() {
            return m_numberOfSamples;
        }
        public Float getUnitPrice() {
            return m_unitPrice;
        }
        public Float getPrice() {
            return m_price;
        }

        public void setDescription(String d) {
            m_description = d;
        }
        public void setNumberOfSamples(Integer numberOfSamples) {
            m_numberOfSamples = numberOfSamples;
        }
        public void setUnitPrice(Float unitPrice) {
            m_unitPrice = unitPrice;
        }
        public void setPrice(Float price) {
            m_price = price;
        }

    }


}