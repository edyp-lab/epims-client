package fr.edyp.epims.ui.dialogs;

import fr.edyp.epims.json.CellenOneImportJson;
import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.json.CellenOneRunJson;
import fr.edyp.epims.json.SampleJson;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.json.PlateJson;
import fr.edyp.epims.ui.common.DefaultDialog;
import fr.edyp.epims.ui.common.IconManager;
import fr.edyp.epims.ui.panels.model.PlateMappingTableModel;
import fr.edyp.epims.ui.panels.model.SampleMappingTableModel;
import org.springframework.lang.NonNull;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dialog for configuring a CellenOne manipulation import. */
public class CellenOneImportDialog extends DefaultDialog {

    private static final String UNKNOWN_PLATE = "UNKNOWN";
    private static final int MAX_VISIBLE_TABLE_ROWS = 5;
    private static final String ALL_SAMPLES = "All";
    private static final String ROOT_SAMPLES = "Root only";
    private static final String FATHER_SAMPLES = "Father only";

    private final CellenOneManipJson selectedManip;
    private final StudyJson selectedStudy;
    private final Map<Integer, List<SampleJson>> samplesByStudy;
    private final List<PlateJson> plates;
    private final Map<String, JComboBox<String>> sampleChoices = new LinkedHashMap<>();
    private final Map<String, JComboBox<String>> plateChoices = new LinkedHashMap<>();
    private final List<SampleJson> createdSamples = new ArrayList<>();
    private JRadioButton allSamplesChoice;
    private JRadioButton rootSamplesChoice;
    private JRadioButton fatherSamplesChoice;
    private SampleMappingTableModel sampleMappingTableModel;
    private JTable sampleMappingTable;
    private PlateMappingTableModel plateMappingTableModel;
    private boolean updatingSampleChoices;

    public CellenOneImportDialog(Window parent, CellenOneManipJson selectedManip, StudyJson selectedStudy,
                                 Map<Integer, List<SampleJson>> samplesByStudy, List<PlateJson> plates) {
        super(parent);
        setIconImages(parent.getIconImages());
        this.selectedManip = selectedManip;
        this.selectedStudy = selectedStudy;
        this.samplesByStudy = samplesByStudy;
        this.plates = plates;
        setTitle("Import CellenOne Manipulation");
        setButtonName(BUTTON_OK, "Validate");
        setButtonIcon(BUTTON_OK, IconManager.getIcon(IconManager.IconType.OK));
        setButtonVisible(BUTTON_BACK, false);
        setButtonVisible(BUTTON_HELP, false);
        setInternalComponent(new JPanel());
        showStep();
        setResizable(true);
        repack();
    }

    private void showStep() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(createSampleMappingPanel(), c);
        c.gridy++;
        mainPanel.add(createPlateMappingPanel(), c);

        replaceInternalComponent(mainPanel);
        repack();
    }

    private JPanel createPlateMappingPanel(){

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(" Plates mapping "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel title = new JLabel("Define mapping between CellenOne target Plate and ePims plate");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(title, c);

        plateMappingTableModel = new PlateMappingTableModel(plateChoices);
        for (CellenOneRunJson run : selectedManip.getRuns()) {
            for (String sourcePlate : run.getPlates()) {
                String key = createKey(run.getName(), sourcePlate);
                JComboBox<String> choice = plateChoices.get(key);
                if (choice == null) {
                    JComboBox<String> newChoice = new JComboBox<>();
                    newChoice.addItem(UNKNOWN_PLATE);
                    plates.forEach(plate -> newChoice.addItem(plate.getName()));
                    choice = newChoice;
                    plateChoices.put(key, choice);
                }
                plateMappingTableModel.addRow(run.getName(), sourcePlate, key);
            }
        }
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 1;
        JTable plateMappingTable = createPlateMappingTable();
        plateMappingTable.getColumnModel().getColumn(PlateMappingTableModel.PLATE_COLUMN).setCellRenderer(new PlateChoiceRenderer());
        plateMappingTable.getColumnModel().getColumn(PlateMappingTableModel.RUN_COLUMN).setPreferredWidth(120);
        plateMappingTable.getColumnModel().getColumn(PlateMappingTableModel.SOURCE_PLATE_COLUMN).setPreferredWidth(180);
        plateMappingTable.getColumnModel().getColumn(PlateMappingTableModel.PLATE_COLUMN).setPreferredWidth(220);
        setTableViewportHeight(plateMappingTable);
        p.add(new JScrollPane(plateMappingTable), c);
        return p;
    }

    @NonNull
    private JTable createPlateMappingTable() {
        JTable plateMappingTable = new JTable(plateMappingTableModel) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == PlateMappingTableModel.PLATE_COLUMN) {
                    return new DefaultCellEditor(plateChoices.get(
                            plateMappingTableModel.getKeyAt(convertRowIndexToModel(row))));
                }
                return super.getCellEditor(row, column);
            }
        };
        plateMappingTable.setRowHeight(26);
        return plateMappingTable;
    }

    private JPanel createSampleMappingPanel(){
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Source - Sample mapping "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel title = new JLabel("Define mapping between CellenOne Manip Source and ePims study's sample");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(title, c);

        allSamplesChoice = new JRadioButton(ALL_SAMPLES, true);
        rootSamplesChoice = new JRadioButton(ROOT_SAMPLES);
        fatherSamplesChoice = new JRadioButton(FATHER_SAMPLES);
        ButtonGroup sampleFilterChoices = new ButtonGroup();
        sampleFilterChoices.add(allSamplesChoice);
        sampleFilterChoices.add(rootSamplesChoice);
        sampleFilterChoices.add(fatherSamplesChoice);
        allSamplesChoice.addActionListener(event -> updateSampleChoices());
        rootSamplesChoice.addActionListener(event -> updateSampleChoices());
        fatherSamplesChoice.addActionListener(event -> updateSampleChoices());
        JPanel sampleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sampleFilterPanel.add(allSamplesChoice);
        sampleFilterPanel.add(rootSamplesChoice);
        sampleFilterPanel.add(fatherSamplesChoice);
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(new JLabel("Samples to list"), c);
        c.gridx++;
        c.weightx = 1;
        p.add(sampleFilterPanel, c);

        sampleMappingTableModel = new SampleMappingTableModel(sampleChoices);
        for (CellenOneRunJson run : selectedManip.getRuns()) {
            for (String source : run.getSources()) {
                String key = createKey(run.getName(), source);
                JComboBox<String> choice = sampleChoices.get(key);
                if (choice == null) {
                    JComboBox<String> newChoice = new JComboBox<>();
                    addSampleChoiceItems(newChoice, samplesForSelectedStudy());
                    selectFirstSample(newChoice);
                    choice = newChoice;
                    sampleChoices.put(key, choice);
                }
                sampleMappingTableModel.addRow(run.getName(), source, key);
            }
        }
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.5;
        sampleMappingTable = new JTable(sampleMappingTableModel) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == SampleMappingTableModel.SAMPLE_COLUMN) {
                    return new DefaultCellEditor(sampleChoices.get(sampleMappingTableModel.getKeyAt(convertRowIndexToModel(row))));
                }
                return super.getCellEditor(row, column);
            }
        };
        sampleMappingTable.setRowHeight(26);
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.SAMPLE_COLUMN).setCellRenderer(new SampleChoiceRenderer());
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.ADD_SAMPLE_COLUMN).setCellRenderer(new AddSampleButtonRenderer());
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.ADD_SAMPLE_COLUMN).setCellEditor(new AddSampleButtonEditor());
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.HIERARCHY_COLUMN).setCellRenderer(new HierarchyButtonRenderer());
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.HIERARCHY_COLUMN).setCellEditor(new HierarchyButtonEditor());
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.RUN_COLUMN).setPreferredWidth(120);
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.SOURCE_COLUMN).setPreferredWidth(180);
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.SAMPLE_COLUMN).setPreferredWidth(220);
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.ADD_SAMPLE_COLUMN).setPreferredWidth(40);
        sampleMappingTable.getColumnModel().getColumn(SampleMappingTableModel.HIERARCHY_COLUMN).setPreferredWidth(40);
        setTableViewportHeight(sampleMappingTable);
        p.add(new JScrollPane(sampleMappingTable), c);
        return p;
    }

    private void setTableViewportHeight(JTable table) {
        int visibleRows = Math.max(1, Math.min(table.getRowCount(), MAX_VISIBLE_TABLE_ROWS));
        int height = visibleRows * table.getRowHeight();
        table.setPreferredScrollableViewportSize(new Dimension(700, height));
    }

    private void addSampleChoiceItems(JComboBox<String> choice, List<SampleJson> samples) {
        samples.stream().sorted().forEach(sample -> choice.addItem(sample.getName()));
    }

    private void updateSampleChoices() {
        if (updatingSampleChoices) return;
        updatingSampleChoices = true;
        try {
            String filter = selectedSampleFilter();
            List<SampleJson> samples = samplesForSelectedStudy().stream()
                    .filter(sample -> ALL_SAMPLES.equals(filter)
                            || ROOT_SAMPLES.equals(filter) && Boolean.TRUE.equals(sample.getIsRoot())
                            || FATHER_SAMPLES.equals(filter) && Boolean.TRUE.equals(sample.getIsFather())).sorted().toList();
            for (JComboBox<String> choice : sampleChoices.values()) {
                String previousSample = selectedSampleName(choice);
                choice.removeAllItems();
                addSampleChoiceItems(choice, samples);
                if (previousSample != null && contains(choice, previousSample)) {
                    choice.setSelectedItem(previousSample);
                } else {
                    selectFirstSample(choice);
                }
            }
            sampleMappingTableModel.fireTableDataChanged();
        } finally {
            updatingSampleChoices = false;
        }
    }

    private String selectedSampleName(JComboBox<String> choice) {
        Object selected = choice.getSelectedItem();
        return selected == null ? null : selected.toString();
    }

    private void selectFirstSample(JComboBox<String> choice) {
        if (choice.getItemCount() > 0) choice.setSelectedIndex(0);
    }

    private String selectedSampleFilter() {
        if (rootSamplesChoice.isSelected()) return ROOT_SAMPLES;
        if (fatherSamplesChoice.isSelected()) return FATHER_SAMPLES;
        return ALL_SAMPLES;
    }

    private boolean contains(JComboBox<String> choice, String value) {
        for (int i = 0; i < choice.getItemCount(); i++) {
            if (value.equals(choice.getItemAt(i))) return true;
        }
        return false;
    }

    private String chooseSampleFromHierarchy() {
        Map<String, SampleJson> samples = new HashMap<>();
        samplesForSelectedStudy().forEach(sample -> samples.put(sample.getName(), sample));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(selectedStudy.getTitle());
        samples.values().stream().filter(sample -> Boolean.TRUE.equals(sample.getIsRoot())).sorted().forEach(sample -> addSampleTreeNode(root, sample, samples));
        JTree tree = new JTree(root);
        tree.setRootVisible(true);
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(500, 350));
        if (JOptionPane.showConfirmDialog(this, scrollPane, "Sample hierarchy", JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        Object selected = tree.getLastSelectedPathComponent();
        return selected instanceof DefaultMutableTreeNode node && !node.equals(root) ? node.getUserObject().toString() : null;
    }

    private void addSampleTreeNode(DefaultMutableTreeNode parent, SampleJson sample, Map<String, SampleJson> samples) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sample.getName());
        parent.add(node);
        sample.getSonSamples().stream().map(samples::get).filter(java.util.Objects::nonNull).sorted(Comparator.naturalOrder()).forEach(son -> addSampleTreeNode(node, son, samples));
    }

    private void createSample(JComboBox<String> choice) {
        ArrayList<SampleJson> samples = new ArrayList<>(samplesForSelectedStudy());
        CreateSamplesDialog dialog = new CreateSamplesDialog(this, selectedStudy, samples);
        dialog.centerToWindow(this);
        dialog.setVisible(true);
        if (dialog.getButtonClicked() != BUTTON_OK) {
            selectFirstSample(choice);
            return;
        }
        List<SampleJson> samplesToCreate = dialog.getSamplesToCreate();
        if (samplesToCreate.isEmpty()) {
            selectFirstSample(choice);
            return;
        }
        createdSamples.addAll(samplesToCreate);
        samplesForSelectedStudy().addAll(samplesToCreate);
        String createdSample = samplesToCreate.get(0).getName();
        updateSampleChoices();
        if (!contains(choice, createdSample)) {
            allSamplesChoice.setSelected(true);
            updateSampleChoices();
        }
        if (contains(choice, createdSample)) choice.setSelectedItem(createdSample);
    }

    @Override
    protected boolean okCalled() {
        if (!allSamplesMapped()) {
            setStatus(true, "Select or create a Sample for every source.");
            return false;
        }
        return true;
    }

    private boolean allSamplesMapped() {
        for (JComboBox<String> choice : sampleChoices.values()) {
            if (choice.getSelectedItem() == null) return false;
        }
        return true;
    }


    public CellenOneImportJson getRequest() {
        List<CellenOneImportJson.Assignment> assignments = new ArrayList<>();
        for (CellenOneRunJson run : selectedManip.getRuns()) {
            for (String source : run.getSources()) {
                Object value = sampleChoices.get(createKey(run.getName(), source)).getSelectedItem();
                assert value != null;
                assignments.add(new CellenOneImportJson.Assignment(run.getName(), source, value.toString()));
            }
        }
        List<CellenOneImportJson.PlateAssignment> plateAssignments = new ArrayList<>();
        for (CellenOneRunJson run : selectedManip.getRuns()) {
            for (String sourcePlate : run.getPlates()) {
                JComboBox<String> choice = plateChoices.get(createKey(run.getName(), sourcePlate));
                String value = choice == null || choice.getSelectedItem() == null
                        ? UNKNOWN_PLATE : choice.getSelectedItem().toString();
                plateAssignments.add(new CellenOneImportJson.PlateAssignment(run.getName(), value));
            }
        }
        return new CellenOneImportJson(selectedManip.getName(), selectedStudy.getId(), assignments, plateAssignments);
    }

    public List<SampleJson> getSamplesToCreate() {
        return new ArrayList<>(createdSamples);
    }


    private List<SampleJson> samplesForSelectedStudy() {
        return samplesByStudy.computeIfAbsent(selectedStudy.getId(), id -> new ArrayList<>());
    }


    private String createKey(String run, String value) {
        return run + "\u0000" + value;
    }


    private class SampleChoiceRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            JComboBox<String> choice = sampleChoices.get(sampleMappingTableModel.getKeyAt(modelRow));
            choice.setSelectedItem(value);
            return choice;
        }
    }

    private class PlateChoiceRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            JComboBox<String> choice = plateChoices.get(plateMappingTableModel.getKeyAt(modelRow));
            choice.setSelectedItem(value);
            return choice;
        }
    }

    private static class AddSampleButtonRenderer extends JButton implements TableCellRenderer {
        private AddSampleButtonRenderer() {
            setIcon(IconManager.getIcon(IconManager.IconType.ADD_SAMPLE));
            setToolTipText("Create a new sample");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            return this;
        }
    }

    private class AddSampleButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton(IconManager.getIcon(IconManager.IconType.ADD_SAMPLE));
        private int row;

        private AddSampleButtonEditor() {
            button.setToolTipText("Create a new sample");
            button.addActionListener(event -> {
                int modelRow = sampleMappingTable.convertRowIndexToModel(row);
                JComboBox<String> choice = sampleChoices.get(sampleMappingTableModel.getKeyAt(modelRow));
                createSample(choice);
                sampleMappingTableModel.fireTableRowsUpdated(modelRow, modelRow);
                stopCellEditing();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
    
    private static class HierarchyButtonRenderer extends JButton implements TableCellRenderer {
        private HierarchyButtonRenderer() {
            setIcon(IconManager.getIcon(IconManager.IconType.SAMPLE_HIERARCHY));
            setToolTipText("Select a sample from hierarchy view");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            return this;
        }
    }

    private class HierarchyButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton(IconManager.getIcon(IconManager.IconType.SAMPLE_HIERARCHY));
        private int row;

        private HierarchyButtonEditor() {
            button.setToolTipText("Select a sample from hierarchy view");
            button.addActionListener(event -> {
                int modelRow = sampleMappingTable.convertRowIndexToModel(row);
                String selectedSample = chooseSampleFromHierarchy();
                if (selectedSample != null) {
                    JComboBox<String> choice = sampleChoices.get(sampleMappingTableModel.getKeyAt(modelRow));
                    if (!contains(choice, selectedSample)) {
                        allSamplesChoice.setSelected(true);
                        updateSampleChoices();
                    }
                    if (contains(choice, selectedSample)) choice.setSelectedItem(selectedSample);
                    sampleMappingTableModel.fireTableRowsUpdated(modelRow, modelRow);
                }
                stopCellEditing();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
    
}