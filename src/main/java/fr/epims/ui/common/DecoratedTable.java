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


import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 *
 * Table which has a bi-color striping, an ability to select columns viewed and
 * the possibility to display an "histogram" on a column
 *
 * @author JM235353
 *
 */
public class DecoratedTable extends JXTable {

    private RelativePainterHighlighter.NumberRelativizer m_relativizer = null;


    private Highlighter m_stripingHighlighter = null;


    public DecoratedTable() {

        // allow user to hide/show columns
        setColumnControlVisible(true);

        // highlight one line of two
        m_stripingHighlighter = HighlighterFactory.createSimpleStriping();
        addHighlighter(m_stripingHighlighter);

        setGridColor(Color.lightGray);
        setRowHeight(20);
        setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED);


        addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    DecoratedTable source = (DecoratedTable) e.getSource();
                    int row = source.rowAtPoint( e.getPoint() );
                    int column = source.columnAtPoint( e.getPoint() );

                    if (! source.isRowSelected(row))
                        source.changeSelection(row, column, false, false);

                    TablePopupMenu popup = new TablePopupMenu(true);
                    popup.preparePopup();
                    popup.show(e.getX(), e.getY(), source);
                }
            }
        });



    }




    public void removeStriping() {
        removeHighlighter(m_stripingHighlighter);
    }

    public String getToolTipForHeader(int modelColumn) {
        return ((DecoratedTableModelInterface) getModel()).getToolTipForHeader(modelColumn);
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new CustomTooltipTableHeader(this);
    }


    public ArrayList<Long> getSelection() {

        ArrayList<Long> selectionList = new ArrayList<>();

        return selectionList;
    }

    private class CustomTooltipTableHeader extends JXTableHeader {

        private DecoratedTable m_table;

        public CustomTooltipTableHeader(DecoratedTable table) {
            super(table.getColumnModel());
            m_table = table;
        }

        @Override
        public String getToolTipText(MouseEvent e) {

            Point p = e.getPoint();
            int column = columnAtPoint(p);
            if (column != -1) {
                column = m_table.convertColumnIndexToModel(column);
                String tooltip = getToolTipForHeader(column);
                if (tooltip != null) {
                    return tooltip;
                }
            }

            return super.getToolTipText(e);
        }
    }


    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        TableModel model = getModel();
        if (model instanceof DecoratedTableModelInterface) {
            int columnInModel = convertColumnIndexToModel(column);
            int rowInModel = convertRowIndexToModel(row);

            TableCellRenderer renderer = ((DecoratedTableModelInterface) model).getRenderer(rowInModel, columnInModel);
            if (renderer != null) {
                return renderer;

            }
        }

        return super.getCellRenderer(row, column);
    }

    public TableCellEditor getCellEditor(int row, int column) {

        TableModel model = getModel();
        if (model instanceof DecoratedTableModelInterface) {
            int columnInModel = convertColumnIndexToModel(column);
            int rowInModel = convertRowIndexToModel(row);

            TableCellEditor editor = ((DecoratedTableModelInterface) model).getEditor(rowInModel, columnInModel);
            if (editor != null) {
                return editor;

            }
        }

        return super.getCellEditor(row, column);
    }


}
