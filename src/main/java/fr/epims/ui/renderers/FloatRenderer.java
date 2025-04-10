/*
 * Copyright (C) 2019 VD225637
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

package fr.epims.ui.renderers;


import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for a Float Value in a Table Cell which is displayed as 0.00
 *
 * @author JM235353
 *
 */
public class FloatRenderer implements TableCellRenderer {

    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");

    private final TableCellRenderer m_defaultRenderer;

    private int m_digits = 2;
    private boolean m_showNaN = false;


    public FloatRenderer(TableCellRenderer defaultRenderer, int digits, boolean showNaN) {
        m_defaultRenderer = defaultRenderer;
        m_digits = digits;
        m_showNaN = showNaN;
    }

    public FloatRenderer(TableCellRenderer defaultRenderer, int digits) {
        m_defaultRenderer = defaultRenderer;
        m_digits = digits;
    }

    public FloatRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Float f = (Float) value;
        String formatedValue;

        if ((f == null) || (f.isNaN())) {
            if (m_showNaN) {
                formatedValue = "NaN";
            } else {
                formatedValue = "";
            }
        } else {
            formatedValue = format(f.floatValue(), m_digits);
        }

        Component c = m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

        if (c instanceof JLabel) {
            ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
        }

        return c;

    }

    private static String format(float f, int nbFractionDigits) {

        DECIMAL_FORMAT.setMaximumFractionDigits(nbFractionDigits);
        DECIMAL_FORMAT.setMinimumFractionDigits(nbFractionDigits);

        return DECIMAL_FORMAT.format((double) f);
    }
}
