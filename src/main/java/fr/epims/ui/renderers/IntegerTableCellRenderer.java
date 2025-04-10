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

package fr.epims.ui.renderers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * Renderer for Integer displayed in Jtable.
 * Two possibilities : 2 digits for months or normal Integer. Localization is France.
 *
 * @author JM235353
 *
 */
public class IntegerTableCellRenderer extends DefaultTableCellRenderer {

    boolean m_monthMode = false;

    public IntegerTableCellRenderer() {
        setHorizontalAlignment(JLabel.LEFT);
    }

    public void setMonthMode() {
        m_monthMode = true;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Integer) {
            if (m_monthMode) {
                value = String.format(Locale.FRANCE, "%02d", (Integer) value);
            } else {
                value = String.format(Locale.FRANCE, "%d", (Integer) value);
            }
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}