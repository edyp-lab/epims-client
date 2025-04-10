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

import fr.epims.ui.analyserequest.panels.model.ProgressAnalysesTableModel;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 *
 * Renderer for the status of a Data Analysis progress
 *
 * @author JM235353
 *
 */
public class StatusRenderer implements TableCellRenderer {

    private final TableCellRenderer m_defaultRenderer;

    private final ImageIcon COMPLETED_ICON = IconManager.createColoredIcon(Color.green);
    private final ImageIcon IN_PROGRESS_ICON = IconManager.createColoredIcon(Color.white);
    private final ImageIcon MISSING_ORDER_ICON = IconManager.createColoredIcon(Color.red);
    private final ImageIcon READY_TO_INVOICE_ICON = IconManager.createColoredIcon(Color.orange);

    public StatusRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        ProgressAnalysesTableModel.STATUS status = (ProgressAnalysesTableModel.STATUS) value;



        String text = "";
        ImageIcon imageIcon = IN_PROGRESS_ICON;
        switch (status) {
            case STATUS_COMPLETED: {
                text = "Completed";
                imageIcon = COMPLETED_ICON;
                break;
            }
            case STATUS_IN_PROGRESS: {
                text = "In Progress";
                imageIcon = IN_PROGRESS_ICON;
                break;
            }
            case STATUS_MISSING_ORDER_FORM: {
                text = "Missing Purchase Order";
                imageIcon = MISSING_ORDER_ICON;
                break;
            }
            case STATUS_READY_TO_INVOICE: {
                text = "Ready to Invoice";
                imageIcon = READY_TO_INVOICE_ICON;
                break;
            }
        }

        Component c = m_defaultRenderer.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

        if (c instanceof JLabel) {
            ((JLabel)c).setHorizontalAlignment(JLabel.LEFT);
            ((JLabel)c).setIcon(imageIcon);
        }

        return c;

    }

}
