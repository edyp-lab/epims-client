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

package fr.epims.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


/**
 *
 * Renderer displaying a button in a cell with the possibility to click on it.
 *
 * @author JM235353
 *
 */
public class IconButtonTableCellRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {

    private final ImageIcon m_icon;
    private final RendererMouseCallback m_callback;
    private final int m_column;

    public enum RowModeEnum {
        MODE_ROW_ALL,
        MODE_ROW_WITHOUT_LAST
    }

    private final RowModeEnum m_rowMode;

    public IconButtonTableCellRenderer(ImageIcon icon, RendererMouseCallback callback, int column, RowModeEnum rowMode) {
        m_icon = icon;
        m_callback = callback;
        m_column = column;
        m_rowMode = rowMode;
    }


    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        checkCursor(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (m_callback == null) {
            return;
        }

        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (e.isShiftDown() || e.isControlDown()) {
            return; // multi selection ongoing
        }

        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);

        int modelCol = table.convertColumnIndexToModel(col);

        if ((modelCol != m_column) || (row == -1)) {
            return;
        }

        if (m_rowMode == RowModeEnum.MODE_ROW_WITHOUT_LAST) {
            if (row >= table.getRowCount()-1) {
                return;
            }
        }

        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();

        int columnStart = 0;
        for (int i=0;i<col;i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }

        // check that the user has clicked on the icon
        if (columnStart+20<e.getX()) {
            return;
        }

        m_callback.mouseAction(e);

    }

    private void checkCursor(MouseEvent e) {

        if (m_callback == null) {
            return;
        }

        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int col = table.columnAtPoint(pt);
        int row = table.rowAtPoint(pt);
        int modelCol = table.convertColumnIndexToModel(col);

        if (modelCol != m_column) {
            //table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        if (row == -1) {
            table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        if (m_rowMode == RowModeEnum.MODE_ROW_WITHOUT_LAST) {
            if (row >= table.getRowCount()-1) {
                table.setCursor(Cursor.getDefaultCursor());
                return;
            }
        }

        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();

        int columnStart = 0;
        for (int i = 0; i < col; i++) {
            TableColumn column = tcm.getColumn(i);
            columnStart += column.getWidth();
        }

        // check that the user is over the icon
        if (columnStart + 20 < e.getX()) {
            table.setCursor(Cursor.getDefaultCursor());
            return;
        }

        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if ((m_rowMode == RowModeEnum.MODE_ROW_ALL) || (row < table.getRowCount()-1)) {
            setIcon(m_icon);
        } else {
            setIcon(null);
        }

        if (value != null) {
            setText(value.toString());
        } else {
            setText("");
        }



        if (isSelected) {
            this.setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
            this.setForeground(Color.WHITE);
        } else {
            this.setBackground(UIManager.getDefaults().getColor("Table.background"));
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}
