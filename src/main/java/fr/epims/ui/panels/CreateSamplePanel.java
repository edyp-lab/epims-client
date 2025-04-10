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


import fr.epims.MainFrame;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.*;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.SampleFromTextDialog;
import fr.epims.ui.dialogs.SelectSampleSpeciesDialog;
import fr.epims.ui.dialogs.SelectSampleTypeDialog;
import fr.epims.ui.dialogs.SetValueDialog;
import fr.epims.ui.panels.model.DefineSampleModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

/**
 *
 * Panel to create a Sample
 *
 * @author JM235353
 *
 */
public class CreateSamplePanel extends JPanel {

    private DefineSampleModel m_defineSampleModel;

    private DefaultDialog m_parentDialog;

    private JComboBox m_speciesComboBox = new JComboBox();
    private JComboBox m_varietyComboBox = new JComboBox();

    private DefaultComboBoxModel m_sampleSpeciesModel;
    private DefaultComboBoxModel m_sampleTypeModel;

    private SampleTable m_table;

    private HashSet<String> m_takenSampleKeys;

    private boolean m_programmaticallyModified = false;

    public CreateSamplePanel(DefaultDialog parent, StudyJson study, ArrayList<SampleJson> samples) {
        super(new GridBagLayout());

        m_parentDialog = parent;

        setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel biologicalOriginPanel = createBiologicalOriginPanel();

        JPanel addSamplePanel = createAddSamplePanel(study, samples);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        add(biologicalOriginPanel, c);


        c.gridy++;
        c.weighty = 1;
        add(addSamplePanel, c);
    }

    public ArrayList<SampleJson> getSamplesToCreate() {
        SampleSpeciesJson sampleSpecies = (SampleSpeciesJson) m_speciesComboBox.getSelectedItem();
        SampleTypeJson sampleType = null;
        Object sampleTypeObject = m_varietyComboBox.getSelectedItem();
        if (sampleTypeObject instanceof SampleTypeJson) {
            sampleType = (SampleTypeJson) sampleTypeObject;
        }

        BiologicOriginJson biologicOriginJson = new BiologicOriginJson(null, null, sampleSpecies.getId(), null, (sampleType == null) ? null : sampleType.getId(), null);

        return m_defineSampleModel.getSamplesToCreate(biologicOriginJson);

    }

    public boolean checkFields(DefaultDialog dialog) {

        Object species = m_speciesComboBox.getSelectedItem();
        if (species instanceof String) {
            dialog.highlight(m_speciesComboBox);
            dialog.setStatus(true, "You must select the Species");
            return false;
        }

        ArrayList<SampleJson> sampleJsonsToCreate = m_defineSampleModel.getSamplesToCreate(null);
        for (SampleJson s : sampleJsonsToCreate) {
            String sampleName = s.getName().trim();
            if (sampleName.isEmpty()) {
                dialog.highlight(m_table);
                dialog.setStatus(true, "Sample nomenclature must be set.");
                return false;
            }
            if (m_takenSampleKeys.contains(s.getName())) {
                dialog.highlight(m_table);
                dialog.setStatus(true, "Sample "+s.getName()+" already exists");
                return false;
            }
        }


        return true;
    }

    private JPanel createBiologicalOriginPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Biological Origin "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel compulsorySpecies = new JLabel(IconManager.getIcon(IconManager.IconType.STAR_COMPULSORY));

        // SampleSpecies Combobox
        ArrayList<SampleSpeciesJson> sampleSpeciesList = DataManager.getRestrictedSampleSpecies();
        Vector sampleSpeciesVector = new Vector();
        sampleSpeciesVector.add("< Species >");
        for (SampleSpeciesJson sampleSpecies : sampleSpeciesList) {
            sampleSpeciesVector.add(sampleSpecies);
        }
        m_sampleSpeciesModel = new DefaultComboBoxModel(sampleSpeciesVector);
        m_speciesComboBox.setModel(m_sampleSpeciesModel);

        // SampleTypes Combobox
        ArrayList<SampleTypeJson> sampleTypeList = DataManager.getRestrictedSampleTypes();
        Vector sampleTypeVector = new Vector();
        sampleTypeVector.add("< Variety >");
        for (SampleTypeJson sampleType : sampleTypeList) {
            sampleTypeVector.add(sampleType);
        }
        m_sampleTypeModel = new DefaultComboBoxModel(sampleTypeVector);
        m_varietyComboBox.setModel(m_sampleTypeModel);


        FlatButton addSpeciesButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_LIST), true);
        FlatButton addVarietyButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_LIST), true);

        c.gridx = 0;
        c.gridy = 0;
        p.add(compulsorySpecies, c);

        c.gridx++;
        p.add(m_speciesComboBox, c);

        c.gridx++;
        p.add(addSpeciesButton, c);

        c.gridx++;
        c.weightx= 1;
        p.add(Box.createHorizontalGlue(), c);


        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        p.add(m_varietyComboBox, c);

        c.gridx++;
        p.add(addVarietyButton, c);

        c.gridx++;
        c.weightx= 1;
        p.add(Box.createHorizontalGlue(), c);

        addSpeciesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<SampleSpeciesJson> alreadyPresents = new ArrayList();
                for (int i=0;i<m_sampleSpeciesModel.getSize();i++) {
                    Object o = m_sampleSpeciesModel.getElementAt(i);
                    if (! (o instanceof String)) {
                        alreadyPresents.add((SampleSpeciesJson) o);
                    }
                }

                SelectSampleSpeciesDialog dialog = new SelectSampleSpeciesDialog(MainFrame.getMainWindow(), alreadyPresents);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    SampleSpeciesJson sampleSpeciesJson = dialog.getSelectedSampleSpecies();
                    if (sampleSpeciesJson == null) {
                        return;
                    }
                    alreadyPresents.add(sampleSpeciesJson);
                    Collections.sort(alreadyPresents);
                    m_sampleSpeciesModel.removeAllElements();
                    m_sampleSpeciesModel.addElement("< Species >");
                    for (SampleSpeciesJson sampleSpecies : alreadyPresents) {
                        m_sampleSpeciesModel.addElement(sampleSpecies);
                    }
                    m_speciesComboBox.setSelectedItem(sampleSpeciesJson);
                }
            }
        });


        addVarietyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<SampleTypeJson> alreadyPresents = new ArrayList();
                for (int i=0;i<m_sampleTypeModel.getSize();i++) {
                    Object o = m_sampleTypeModel.getElementAt(i);
                    if (! (o instanceof String)) {
                        alreadyPresents.add((SampleTypeJson) o);
                    }
                }

                SelectSampleTypeDialog dialog = new SelectSampleTypeDialog(MainFrame.getMainWindow(), alreadyPresents);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    SampleTypeJson sampleTypeJson = dialog.getSelectedSampleType();
                    if (sampleTypeJson == null) {
                        return;
                    }
                    alreadyPresents.add(sampleTypeJson);
                    Collections.sort(alreadyPresents);
                    m_sampleTypeModel.removeAllElements();
                    m_sampleTypeModel.addElement("< Variety >");
                    for (SampleTypeJson sampleType : alreadyPresents) {
                        m_sampleTypeModel.addElement(sampleType);
                    }
                    m_varietyComboBox.setSelectedItem(sampleTypeJson);
                }
            }
        });

        return p;



    }

    private JPanel createAddSamplePanel(StudyJson study, ArrayList<SampleJson> samples) {
        JPanel p = new JPanel(new GridBagLayout());


        m_takenSampleKeys = new HashSet<>();
        for (SampleJson s : samples) {
            m_takenSampleKeys.add(s.getName());
        }

        p.setBorder(BorderFactory.createTitledBorder(" Add Samples "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel numberSamplesLabel = new JLabel("Number of Samples:");

        SpinnerModel model = new SpinnerNumberModel(1, //initial value
                        1, //min
                        1000, //max
                        1);                //step

        JSpinner numberSamplesSpinner = new JSpinner(model);


        JButton fromTextButton = new JButton("Samples from Text", IconManager.getIcon(IconManager.IconType.ARROW_IMPORT));

        m_table = new SampleTable(study, m_takenSampleKeys);

        JScrollPane tableScrollPane = new JScrollPane(m_table);
        tableScrollPane.setMinimumSize(new Dimension(600,400));
        m_table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        c.gridx = 0;
        c.gridy = 0;
        p.add(numberSamplesLabel, c);

        c.gridx++;
        p.add(numberSamplesSpinner, c);

        c.gridx++;
        c.weightx = 1;
        p.add(Box.createHorizontalGlue(), c);

        c.gridx++;
        c.weightx = 0;
        p.add(fromTextButton, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        c.weightx = 1;
        c.weighty = 1;
        p.add(tableScrollPane, c);


        // set listeners
        numberSamplesSpinner.addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e) {
                if (m_programmaticallyModified) {
                    return;
                }
                Integer nbSamples = ((Integer) ((JSpinner) e.getSource()).getValue());
                m_defineSampleModel.setNbSamples(nbSamples.intValue());
            }
        });


        fromTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SampleFromTextDialog dialog = new SampleFromTextDialog(MainFrame.getMainWindow(), m_defineSampleModel.getSamplesText());
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    m_programmaticallyModified = true;
                    try {
                        ArrayList<String> samples = dialog.getSampleNames();
                        if (!samples.isEmpty()) {
                            int nbSamples = samples.size();
                            numberSamplesSpinner.setValue(new Integer(nbSamples));
                            m_defineSampleModel.setSamples(samples);
                        }
                    } finally {
                        m_programmaticallyModified  = false;
                    }
                }
            }
        });

        return p;
    }


    public class SampleTable extends DecoratedTable {

        private TableCellRenderer m_cellRenderer = null;

        public SampleTable(StudyJson study, HashSet<String> takenSampleKeys) {
            m_defineSampleModel = new DefineSampleModel(this, study.getNomenclatureTitle(), study.getId(), takenSampleKeys);
            setModel(m_defineSampleModel);

            decorateColumns();


            setMinimumSize(new Dimension(600,400));


            getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int col = columnAtPoint(e.getPoint());
                    int modelCol = convertColumnIndexToModel(col);

                    String name = m_defineSampleModel.getColumnName(modelCol);

                    SetValueDialog dialog = null;

                    if (modelCol == DefineSampleModel.COLTYPE_NOMENCLATURE) {
                        dialog = new SetValueDialog(m_parentDialog, name, String.class, "[Study Name]_", "_[Auto-Incrementation]");
                    }
                    else if ((modelCol == DefineSampleModel.COLTYPE_VOLUME) || (modelCol == DefineSampleModel.COLTYPE_QUANTITY)) {
                        dialog = new SetValueDialog(m_parentDialog, name, Float.class);
                    }
                    else if ((modelCol == DefineSampleModel.COLTYPE_DESCRIPTION) || (modelCol == DefineSampleModel.COLTYPE_SOURCE_NAME)) {
                        dialog = new SetValueDialog(m_parentDialog, name, String.class);;
                    }
                    dialog.centerToWindow(m_parentDialog);
                    dialog.setVisible(true);

                    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        if (modelCol == DefineSampleModel.COLTYPE_NOMENCLATURE) {
                            String subname = dialog.getStringValue();
                            if (m_defineSampleModel.isNameEditedByUser()) {
                                QuestionDialog questionDialog = new QuestionDialog(MainFrame.getMainWindow(), "Rename edited values", "Do you want to replace your edited values in the same time ?" );
                                questionDialog.centerToScreen();
                                questionDialog.setVisible(true);

                                if (questionDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                                    m_defineSampleModel.setName(subname, true);
                                } else {
                                    m_defineSampleModel.setName(subname, false);
                                }
                            } else {
                                m_defineSampleModel.setName(subname, false);
                            }
                        }
                        if (modelCol == DefineSampleModel.COLTYPE_VOLUME) {
                            Float v = dialog.getFloatValue();
                            m_defineSampleModel.setVolume(v);
                        } else if (modelCol == DefineSampleModel.COLTYPE_QUANTITY) {
                            Float v = dialog.getFloatValue();
                            m_defineSampleModel.setQuantity(v);

                        } else if (modelCol == DefineSampleModel.COLTYPE_SOURCE_NAME) {
                            String sourceName = dialog.getStringValue();
                            m_defineSampleModel.setSourceName(sourceName);
                        } else if (modelCol == DefineSampleModel.COLTYPE_DESCRIPTION) {
                            String description = dialog.getStringValue();
                            m_defineSampleModel.setDescription(description);
                        }
                    }
                }
            });

            setSortable(false);
        }

        public void decorateColumns() {
            decorateColumn(m_defineSampleModel, DefineSampleModel.COLTYPE_NOMENCLATURE, true);
            decorateColumn(m_defineSampleModel, DefineSampleModel.COLTYPE_SOURCE_NAME, true);
            decorateColumn(m_defineSampleModel, DefineSampleModel.COLTYPE_VOLUME, true);
            decorateColumn(m_defineSampleModel, DefineSampleModel.COLTYPE_QUANTITY, true);
            decorateColumn(m_defineSampleModel, DefineSampleModel.COLTYPE_DESCRIPTION, true);
        }

        private void decorateColumn(DefineSampleModel model, int col, boolean modifiable) {

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
    }





}
