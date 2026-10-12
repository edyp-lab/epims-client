package fr.edyp.epims.ui.panels.model;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlateMappingTableModel extends AbstractTableModel {
    public static final int RUN_COLUMN = 0;
    public static final int SOURCE_PLATE_COLUMN = 1;
    public static final int PLATE_COLUMN = 2;

    private final Map<String, JComboBox<String>> plateChoices;
    private final List<String[]> rows = new ArrayList<>();
    private final String[] columnNames = {"Run", "CellenOne Plate", "Study Plate"};

    public PlateMappingTableModel(Map<String, JComboBox<String>> plateChoices) {
        this.plateChoices = plateChoices;
    }

    public void addRow(String run, String sourcePlate, String key) {
        rows.add(new String[]{run, sourcePlate, key});
    }

    public String getKeyAt(int row) {
        return rows.get(row)[2];
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        String[] mapping = rows.get(row);
        if (column == PLATE_COLUMN) {
            return plateChoices.get(mapping[2]).getSelectedItem();
        }
        return mapping[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == PLATE_COLUMN;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        fireTableCellUpdated(row, column);
    }
}