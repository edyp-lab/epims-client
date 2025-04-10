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

import fr.epims.MainFrame;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

/**
 *
 *  Generic popup for all tables. There is the possibility to add specific actions.
 *
 * @author JM235353
 *
 */
public class TablePopupMenu extends JPopupMenu {

    private final ArrayList<AbstractTableAction> m_actions = new ArrayList<>();

    private int m_row;
    private int m_col;
    private int[] m_selectedRows;
    private JTable m_table;

    public TablePopupMenu(boolean setDefaultActions) {
        if (setDefaultActions) {
            addAction(new CopyCellAction());
            addAction(new DisplayCellAction());
        }
    }

    public TablePopupMenu() {
        this(true);
    }

    public final void addAction(AbstractTableAction action) {
        m_actions.add(action);
        if (action != null) {
            action.setTablePopupMenu(this);
        }
    }

    public int getRow() {
        return m_row;
    }

    public int getCol() {
        return m_col;
    }

    public int[] getSelectedRows() {
        return m_selectedRows;
    }

    public JTable getTable() {
        return m_table;
    }

    public void preparePopup() {

        removeAll();

        int nbActions = m_actions.size();
        for (int i = 0; i < nbActions; i++) {
            AbstractTableAction action = m_actions.get(i);

            if (action == null) {
                addSeparator();
            } else {
                add(action.getPopupPresenter());
            }
        }

    }


    public void show(int mouseX, int mouseY, JTable table) {

        Point mousePoint = new Point(mouseX, mouseY);

        m_table = table;
        m_selectedRows = table.getSelectedRows();
        m_row = table.rowAtPoint(mousePoint);
        m_col = table.columnAtPoint(mousePoint);

        // update actions
        int nbActions = m_actions.size();
        for (int i = 0; i < nbActions; i++) {
            AbstractTableAction action = m_actions.get(i);
            if (action != null) {
                action.updateEnabled(m_row, m_col, m_selectedRows, table);
            }
        }

        show(table, mouseX, mouseY);

    }

    public static class CopyCellAction extends AbstractTableAction {

        public CopyCellAction() {
            super("Copy cell");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
            TableCellRenderer renderer = table.getCellRenderer(row, col);
            Component c = table.prepareRenderer(renderer, row, col);
            String text = componentToText(c);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(row != -1);
        }

        private String componentToText(Component c) {
            if (c instanceof JLabel) {
                return ((JLabel) c).getText();
            } else if (c instanceof AbstractButton) {
                return ((AbstractButton) c).getText();
            }
            return "";
        }
    }

    public static class DisplayCellAction extends AbstractTableAction {

        public DisplayCellAction() {
            super("Display cell");
        }

        @Override
        public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
            TableCellRenderer renderer = table.getCellRenderer(row, col);
            Component c = table.prepareRenderer(renderer, row, col);
            String text = componentToText(c);

            DisplayDialog dialog = new DisplayDialog(MainFrame.getMainWindow(), text);
            dialog.centerToWindow(MainFrame.getMainWindow());
            dialog.setVisible(true);
        }

        @Override
        public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
            setEnabled(row != -1);
        }

        private String componentToText(Component c) {
            if (c instanceof JLabel) {
                return ((JLabel) c).getText();
            } else if (c instanceof AbstractButton) {
                return ((AbstractButton) c).getText();
            }
            return "";
        }
    }



}
