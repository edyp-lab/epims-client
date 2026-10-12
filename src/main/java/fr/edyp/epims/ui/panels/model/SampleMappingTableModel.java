package fr.edyp.epims.ui.panels.model;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SampleMappingTableModel extends AbstractTableModel {
    public static final int RUN_COLUMN = 0;
    public static final int SOURCE_COLUMN = 1;
    public static final int SAMPLE_COLUMN = 2;
    public static final int ADD_SAMPLE_COLUMN = 3;
    public static final int HIERARCHY_COLUMN = 4;

    private final Map<String, JComboBox<String>> sampleChoices;
    private final List<String[]> rows = new ArrayList<>();
    private final String[] columnNames = {"Run", "Source", "Study Sample", "", ""};

    public SampleMappingTableModel(Map<String, JComboBox<String>> sampleChoices) {
        this.sampleChoices = sampleChoices;
    }

    public void addRow(String run, String source, String key) {
        rows.add(new String[]{run, source, key});
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
        if (column == SAMPLE_COLUMN) {
            return sampleChoices.get(mapping[2]).getSelectedItem();
        }
        return column >= ADD_SAMPLE_COLUMN ? "" : mapping[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == SAMPLE_COLUMN || column == ADD_SAMPLE_COLUMN || column == HIERARCHY_COLUMN;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        fireTableCellUpdated(row, column);
    }
}