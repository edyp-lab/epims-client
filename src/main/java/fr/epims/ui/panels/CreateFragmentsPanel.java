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

package fr.epims.ui.panels;


import fr.edyp.epims.json.*;
import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.dialogs.SetValueDialog;
import fr.epims.ui.panels.model.DefineSampleFragmentModel;
import fr.epims.ui.panels.model.SampleForSelectionTableModel;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Panel to fragment a Sample
 *
 * @author JM235353
 *
 */
public class CreateFragmentsPanel extends JPanel {
    
    private DefaultDialog m_parentDialog;

    private JRadioButton m_volumeRadioButton;
    private JRadioButton m_quantityRadioButton;

    private SampleSelectionTable m_sampleSelectionTable;
    private SampleForSelectionTableModel m_sampleForSelectionTableModel;
    
    
    private FragmentTable m_fragmentTable;
    private DefineSampleFragmentModel m_defineFragmentSampleModel;

    private HashSet<String> m_takenSampleKeys;

    private JTextField m_fragInfixTextField;
    private JTextField m_commentTextField;

    private FlatButton m_downButton;
    private FlatButton m_upButton;

    public CreateFragmentsPanel(DefaultDialog parent, StudyJson study, ArrayList<SampleJson> samples) {
        super(new GridBagLayout());

        m_takenSampleKeys = new HashSet<>();
        for (SampleJson s : samples) {
            m_takenSampleKeys.add(s.getName());
        }

        m_parentDialog = parent;

        setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel sampleSelectionPanel = createSampleSelectionPanel(study, samples);

        JPanel fragmentsParametersPanel = createFragmentationGlobalParametersPanel();

        JPanel fragmentsPanel = createFragmentsPanel();

        JPanel transferPanel = createTransferPanel();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(sampleSelectionPanel, c);

        c.gridy++;
        c.weighty = 0;
        add(transferPanel, c);
        
        c.gridx++;
        c.gridy = 0;
        c.weightx = 0;
        c.gridheight = 2;
        add(fragmentsParametersPanel, c);
        c.gridheight = 1;

        c.gridy=2;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 0;
        c.weighty = 1;
        add(fragmentsPanel, c);
    }

    private JPanel createTransferPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        //p.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(2, 2, 2, 2);

        m_downButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARROW_DOWN), true);
        m_downButton.setEnabled(false);
        m_upButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARROW_UP), true);
        m_upButton.setEnabled(false);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        p.add(Box.createHorizontalGlue(), c);

        c.gridx++;
        c.weightx = 0;
        p.add(m_downButton, c);

        c.gridx++;
        p.add(Box.createHorizontalStrut(20), c);

        c.gridx++;
        p.add(m_upButton, c);

        c.gridx++;
        c.weightx = 1;
        p.add(Box.createHorizontalGlue(), c);

        m_downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<SampleJson> sampleArray = new ArrayList<>();
                int[] rows = m_sampleSelectionTable.getSelectedRows();
                for (int row : rows) {
                    SampleJson s = m_sampleForSelectionTableModel.getSample(m_sampleSelectionTable.convertRowIndexToModel(row));
                    sampleArray.add(s);
                }

                m_defineFragmentSampleModel.addSamples(sampleArray);
                m_sampleForSelectionTableModel.removeSamples(sampleArray);
                m_sampleSelectionTable.clearSelection();
                m_fragmentTable.select(sampleArray);
            }
        });

        m_upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                HashSet<SampleJson> sampleSet = new HashSet<>();
                int[] rows = m_fragmentTable.getSelectedRows();
                for (int row : rows) {
                    SampleJson s = m_defineFragmentSampleModel.getSample(m_fragmentTable.convertRowIndexToModel(row));
                    sampleSet.add(s);
                }

                ArrayList<SampleJson> sampleArray = new ArrayList<>(sampleSet);

                m_sampleForSelectionTableModel.addSamples(sampleArray);
                m_defineFragmentSampleModel.removeSamples(sampleArray);
                m_fragmentTable.clearSelection();
                m_sampleSelectionTable.select(sampleArray);

            }
        });

        return p;
    }

    private JPanel createSampleSelectionPanel(StudyJson study, ArrayList<SampleJson> samples) {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Select Samples "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_sampleSelectionTable = new SampleSelectionTable(study, samples);

        JScrollPane tableScrollPane = new JScrollPane(m_sampleSelectionTable);
        m_sampleSelectionTable.setFillsViewportHeight(true);
        //tableScrollPane.setPreferredSize(new Dimension(200,250)); // to limit the height
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        p.add(tableScrollPane, c);

        return p;
    }


    private JPanel createFragmentsPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Fragments "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_fragmentTable = new FragmentTable(m_takenSampleKeys);

        JScrollPane tableScrollPane = new JScrollPane(m_fragmentTable);
        //tableScrollPane.setMinimumSize(new Dimension(600,250));
        m_fragmentTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());



        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        p.add(tableScrollPane, c);



        return p;
    }



    private JPanel createFragmentationGlobalParametersPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Fragmentations Parameters "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel numberFragmentsLabel = new JLabel("Number of Fragments:", SwingConstants.RIGHT);

        SpinnerModel model = new SpinnerNumberModel(2, //initial value
                2, //min
                1000, //max
                1);                //step

        JSpinner numberFragmentsSpinner = new JSpinner(model);

        JLabel fragLabel = new JLabel("Fragment Infix:", SwingConstants.RIGHT);
        m_fragInfixTextField = new JTextField(30);
        m_fragInfixTextField.setText("_Frag_");

        JLabel commentLabel = new JLabel("Comment:", SwingConstants.RIGHT);
        m_commentTextField = new JTextField(30);

        m_volumeRadioButton = new JRadioButton("Volume (µl)");
        m_quantityRadioButton = new JRadioButton("Quantity (ng)");
        ButtonGroup group = new ButtonGroup();
        group.add(m_volumeRadioButton);
        group.add(m_quantityRadioButton);
        m_volumeRadioButton.setSelected(true);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(m_volumeRadioButton);
        buttonsPanel.add(m_quantityRadioButton);


        c.gridx = 0;
        c.gridy = 0;
        p.add(numberFragmentsLabel, c);

        c.gridx++;
        p.add(numberFragmentsSpinner, c);

        c.gridx++;
        p.add(Box.createHorizontalStrut(40), c);

        c.gridx = 0;
        c.gridy++;
        p.add(fragLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        p.add(m_fragInfixTextField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        p.add(commentLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        p.add(m_commentTextField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        p.add(buttonsPanel, c);


        c.gridx = 3;
        c.gridwidth = 1;
        c.weightx= 1;
        p.add(Box.createHorizontalGlue(), c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        p.add(Box.createVerticalGlue(), c);


        // set listeners
        numberFragmentsSpinner.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e){
                Integer nbSamples = ((Integer) ((JSpinner) e.getSource()).getValue());
                m_defineFragmentSampleModel.setNbFraments(nbSamples.intValue());

                m_fragmentTable.clearSelection();
                m_upButton.setEnabled(false);
            }

        });

        ActionListener radioButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean valueSelected = m_volumeRadioButton.isSelected();
                m_defineFragmentSampleModel.setVolumeOption(valueSelected);
            }
        };

        m_volumeRadioButton.addActionListener(radioButtonListener);
        m_quantityRadioButton.addActionListener(radioButtonListener);


        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                fragInfixChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fragInfixChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fragInfixChanged();
            }
        };

        m_fragInfixTextField.getDocument().addDocumentListener(docListener);


        return p;

    }

    private void fragInfixChanged() {
        m_defineFragmentSampleModel.setFragmentInfix(m_fragInfixTextField.getText().trim());
    }


    public FragmentsGroupToCreateJson getFragmentsToCreate() {


        return m_defineFragmentSampleModel.getFragmentsToCreate(m_commentTextField.getText().trim());

    }

    public boolean checkFields(DefaultDialog dialog) {

        FragmentsGroupToCreateJson fragmentsGroupToCreateJson = m_defineFragmentSampleModel.getFragmentsToCreate(null);

        HashMap<String, ArrayList<FragmentToCreateJson>> map = fragmentsGroupToCreateJson.getParentToFragmentsMap();

        if (map.isEmpty()) {
            dialog.highlight(m_sampleSelectionTable);
            dialog.setStatus(true, "You have selected no Sample to fragment.");
            return false;
        }

        for (ArrayList<FragmentToCreateJson> fragmentList : map.values()) {
            for (FragmentToCreateJson fragment : fragmentList) {
                if (! fragment.getName().startsWith(fragment.getParentSampleKey())) {
                    dialog.highlight(m_fragmentTable);
                    dialog.setStatus(true, "Fragment "+fragment.getName()+" should be named "+fragment.getParentSampleKey()+"[...].");
                    return false;
                }
                if (m_takenSampleKeys.contains(fragment.getName())) {
                    dialog.highlight(m_fragmentTable);
                    dialog.setStatus(true, "Fragment "+fragment.getName()+" already exists");
                    return false;
                }
            }
        }

        return true;
    }

    private void selectionChanged(boolean fragmentsTable) {

        if (m_selectionIsChanging) {
            return;
        }
        m_selectionIsChanging = true;

        if (fragmentsTable) {
            int[] rows = m_fragmentTable.getSelectedRows();

            if (rows.length > 0) {
                m_downButton.setEnabled(false);
                m_upButton.setEnabled(true);
                m_sampleSelectionTable.clearSelection();
            }
        } else {
            int[] rows = m_sampleSelectionTable.getSelectedRows();
            if (rows.length > 0) {
                m_downButton.setEnabled(true);
                m_upButton.setEnabled(false);
                m_fragmentTable.clearSelection();
            }
        }


        m_selectionIsChanging = false;

    }
    private boolean m_selectionIsChanging = false;

    public class SampleSelectionTable extends DecoratedTable {



        public SampleSelectionTable(StudyJson study, ArrayList<SampleJson> samples) {
            m_sampleForSelectionTableModel = new SampleForSelectionTableModel(this, samples);
            setModel(m_sampleForSelectionTableModel);

            setMinimumSize(new Dimension(600,300));

            getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    selectionChanged(false);
                }
            });
        }

        public void select(ArrayList<SampleJson> sampleArray) {
            ListSelectionModel selectionModel = getSelectionModel();
            selectionModel.clearSelection();
            for (int i=0;i<m_sampleForSelectionTableModel.getRowCount();i++) {
                SampleJson s = m_sampleForSelectionTableModel.getSample(i);
                if (sampleArray.contains(s)) {
                    int row = m_sampleSelectionTable.convertRowIndexToView(i);
                    selectionModel.addSelectionInterval(row, row);
                }
            }
        }


    }

    public class FragmentTable extends DecoratedTable {

        private TableCellRenderer m_cellRenderer = null;

        public FragmentTable(HashSet<String> takenSampleKeys) {
            m_defineFragmentSampleModel = new DefineSampleFragmentModel(this, new ArrayList<SampleJson>(0), 2, takenSampleKeys);
            setModel(m_defineFragmentSampleModel);

            decorateColumns();


            setMinimumSize(new Dimension(600,300));


            getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int col = columnAtPoint(e.getPoint());
                    int modelCol = convertColumnIndexToModel(col);

                    if ((modelCol != DefineSampleFragmentModel.COLTYPE_VOLUME_OR_QUANTITY) && (modelCol != DefineSampleFragmentModel.COLTYPE_DESCRIPTION)) {
                        return;
                    }

                    String name = m_defineFragmentSampleModel.getColumnName(modelCol);

                    SetValueDialog dialog = null;

                    if (modelCol == DefineSampleFragmentModel.COLTYPE_VOLUME_OR_QUANTITY) {
                        dialog = new SetValueDialog(m_parentDialog, name, Float.class);
                    } else if (modelCol == DefineSampleFragmentModel.COLTYPE_DESCRIPTION) {
                        dialog = new SetValueDialog(m_parentDialog, name, String.class);;
                    }
                    dialog.centerToWindow(m_parentDialog);
                    dialog.setVisible(true);

                    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        if (modelCol == DefineSampleFragmentModel.COLTYPE_VOLUME_OR_QUANTITY) {
                            Float v = dialog.getFloatValue();
                            if (m_volumeRadioButton.isSelected()) {
                                m_defineFragmentSampleModel.setVolume(v);
                            } else {
                                m_defineFragmentSampleModel.setQuantity(v);
                            }
                        } else if (modelCol == DefineSampleFragmentModel.COLTYPE_DESCRIPTION) {
                            String description = dialog.getStringValue();
                            m_defineFragmentSampleModel.setDescription(description);
                        }
                    }
                }
            });

            setSortable(false);

            getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    selectionChanged(true);
                }
            });
        }

        public void decorateColumns() {
            decorateColumn(m_defineFragmentSampleModel, DefineSampleFragmentModel.COLTYPE_PARENT_SAMPLE, false);
            decorateColumn(m_defineFragmentSampleModel, DefineSampleFragmentModel.COLTYPE_FRAGMENT_NAME, false);
            decorateColumn(m_defineFragmentSampleModel, DefineSampleFragmentModel.COLTYPE_VOLUME_OR_QUANTITY, true);
            decorateColumn(m_defineFragmentSampleModel, DefineSampleFragmentModel.COLTYPE_DESCRIPTION, true);
        }

        private void decorateColumn(DefineSampleFragmentModel model, int col, boolean modifiable) {

            TableColumn column = getColumnModel().getColumn(col);


            if (m_cellRenderer == null) {
                m_cellRenderer = new TableCellRenderer() {
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        return (JComponent) value;
                    }
                };
            }

            Border headerBorder = UIManager.getBorder("TableHeader.cellBorder");

            JLabel label = new JLabel(model.getColumnName(col), modifiable ? IconManager.getIcon(IconManager.IconType.TABLE_PARAMETERS) : null, SwingConstants.CENTER);
            label.setBorder(headerBorder);
            label.setBackground(Color.white);
            label.setOpaque(true);

            column.setHeaderRenderer(m_cellRenderer);
            column.setHeaderValue(label);
        }

        public void select(ArrayList<SampleJson> sampleArray) {
            ListSelectionModel selectionModel = getSelectionModel();
            selectionModel.clearSelection();
            for (int i=0;i<m_defineFragmentSampleModel.getRowCount();i++) {
                SampleJson s = m_defineFragmentSampleModel.getSample(i);
                if (sampleArray.contains(s)) {
                    int row = m_fragmentTable.convertRowIndexToView(i);
                    selectionModel.addSelectionInterval(row, row);
                }
            }
        }
    }


}
