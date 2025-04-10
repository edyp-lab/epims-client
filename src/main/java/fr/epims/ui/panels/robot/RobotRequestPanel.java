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

package fr.epims.ui.panels.robot;


import fr.edyp.epims.json.RobotPlanningJson;
import fr.edyp.epims.json.SampleJson;
import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.dialogs.SetValueDialog;
import fr.epims.ui.panels.model.RobotRequestModel;
import fr.epims.ui.panels.model.SampleForSelectionTableModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * Panel for the Dialog to ask to add a Sample to a Robot Plate
 *
 * @author JM235353
 *
 */
public class RobotRequestPanel extends JPanel {

    private SampleForSelectionTableModel m_sampleForSelectionTableModel;
    private SampleSelectionTable m_sampleSelectionTable;

    private RobotRequestModel m_robotRequestModel;
    private RobotRequestTable m_robotRequestTable;

    private FlatButton m_downButton;
    private FlatButton m_upButton;


    private DefaultDialog m_parentDialog;

    public RobotRequestPanel(DefaultDialog parent, ArrayList<SampleJson> samples, HashSet<SampleJson> selectedSamples) {
        super(new GridBagLayout());

        m_parentDialog = parent;

        setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel robotRequestPanel = createRobotRequestPanel();
        JPanel transferPanel = createTransferPanel();
        JPanel sampleSelectionPanel = createSampleSelectionPanel(samples, selectedSamples);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(sampleSelectionPanel, c);

        c.gridy++;
        c.weighty = 0;
        add(transferPanel, c);

        c.gridy++;
        c.weighty = 1;
        add(robotRequestPanel, c);

        if (!selectedSamples.isEmpty()) {
            m_downButton.setEnabled(true);
        }
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

                m_robotRequestModel.addSamples(sampleArray);
                m_sampleForSelectionTableModel.removeSamples(sampleArray);
                m_sampleSelectionTable.clearSelection();
                m_robotRequestTable.select(sampleArray);
            }
        });

        m_upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<SampleJson> sampleArray = new ArrayList<>();
                int[] rows = m_robotRequestTable.getSelectedRows();
                for (int row : rows) {
                    SampleJson s = m_robotRequestModel.getSample(m_robotRequestTable.convertRowIndexToModel(row));
                    sampleArray.add(s);
                }

                m_sampleForSelectionTableModel.addSamples(sampleArray);
                m_robotRequestModel.removeSamples(sampleArray);
                m_robotRequestTable.clearSelection();
                m_sampleSelectionTable.select(sampleArray);
            }
        });

        return p;
    }

    private JPanel createSampleSelectionPanel(ArrayList<SampleJson> samples, HashSet<SampleJson> selectedSamples) {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Select Samples "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_selectionIsChanging = true;
        m_sampleSelectionTable = new SampleSelectionTable(samples, selectedSamples);
        m_selectionIsChanging = false;

        JScrollPane tableScrollPane = new JScrollPane(m_sampleSelectionTable);
        m_sampleSelectionTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        p.add(tableScrollPane, c);

        return p;
    }


    private JPanel createRobotRequestPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Robot Requests "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_robotRequestTable = new RobotRequestTable();

        JScrollPane tableScrollPane = new JScrollPane(m_robotRequestTable);
        m_robotRequestTable.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());



        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        p.add(tableScrollPane, c);


        return p;
    }


    private void selectionChanged(boolean robotRequestTable) {

        if (m_selectionIsChanging) {
            return;
        }
        m_selectionIsChanging = true;

        if (robotRequestTable) {
            int[] rows = m_robotRequestTable.getSelectedRows();

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
                m_robotRequestTable.clearSelection();
            }
        }


        m_selectionIsChanging = false;

    }
    private boolean m_selectionIsChanging = false;


    public class SampleSelectionTable extends DecoratedTable {



        public SampleSelectionTable(ArrayList<SampleJson> samples, HashSet<SampleJson> selectedSamples) {
            m_sampleForSelectionTableModel = new SampleForSelectionTableModel(this, samples);
            setModel(m_sampleForSelectionTableModel);

            if (! selectedSamples.isEmpty()) {
                for (int i = 0; i < samples.size(); i++) {
                    if (selectedSamples.contains(samples.get(i))) {
                        getSelectionModel().addSelectionInterval(i, i);
                    }
                }
                selectionChanged(false);
            }


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



    public class RobotRequestTable extends DecoratedTable {

        private TableCellRenderer m_cellRenderer = null;

        public RobotRequestTable() {
            m_robotRequestModel = new RobotRequestModel(this, new ArrayList<SampleJson>(0));
            setModel(m_robotRequestModel);

            decorateColumns();


            setMinimumSize(new Dimension(600,400));


            getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int col = columnAtPoint(e.getPoint());
                    int modelCol = convertColumnIndexToModel(col);

                    if (modelCol == RobotRequestModel.COLTYPE_SAMPLE) {
                        return;
                    }

                    String name = m_robotRequestModel.getColumnName(modelCol);

                    SetValueDialog dialog = null;

                    if (modelCol == RobotRequestModel.COLTYPE_COMMENTARY) {
                        dialog = new SetValueDialog(m_parentDialog, name, String.class);
                    } else if (modelCol == RobotRequestModel.COLTYPE_LOAD_COUNT) {
                        dialog = new SetValueDialog(m_parentDialog, name, Integer.class);
                    } else if ((modelCol == RobotRequestModel.COLTYPE_PROTEINS_QUANTITY) || (modelCol == RobotRequestModel.COLTYPE_TRYPSIN_QUANTITY)) {
                        dialog = new SetValueDialog(m_parentDialog, name, Float.class);
                    }

                    dialog.centerToWindow(m_parentDialog);
                    dialog.setVisible(true);

                    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        if (modelCol == RobotRequestModel.COLTYPE_COMMENTARY) {
                            String commentary = dialog.getStringValue();
                            m_robotRequestModel.setCommentary(commentary);
                        } else if (modelCol == RobotRequestModel.COLTYPE_LOAD_COUNT) {
                            Integer v = dialog.getIntegerValue();
                            m_robotRequestModel.setLoadCount(v);
                        } else if (modelCol == RobotRequestModel.COLTYPE_PROTEINS_QUANTITY) {
                            Float v = dialog.getFloatValue();
                            m_robotRequestModel.setProteinQuantity(v);
                        } else if (modelCol == RobotRequestModel.COLTYPE_TRYPSIN_QUANTITY) {
                            Float v = dialog.getFloatValue();
                            m_robotRequestModel.setTrypsinQuantity(v);
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

        public void select(ArrayList<SampleJson> sampleArray) {
            ListSelectionModel selectionModel = getSelectionModel();
            selectionModel.clearSelection();
            for (int i=0;i<m_robotRequestModel.getRowCount();i++) {
                SampleJson s = m_robotRequestModel.getSample(i);
                if (sampleArray.contains(s)) {
                    int row = m_robotRequestTable.convertRowIndexToView(i);
                    selectionModel.addSelectionInterval(row, row);
                }
            }
        }
        public void decorateColumns() {
            decorateColumn(m_robotRequestModel, RobotRequestModel.COLTYPE_SAMPLE, false);
            decorateColumn(m_robotRequestModel, RobotRequestModel.COLTYPE_COMMENTARY, true);
            decorateColumn(m_robotRequestModel, RobotRequestModel.COLTYPE_LOAD_COUNT, true);
            decorateColumn(m_robotRequestModel, RobotRequestModel.COLTYPE_PROTEINS_QUANTITY, true);
            decorateColumn(m_robotRequestModel, RobotRequestModel.COLTYPE_TRYPSIN_QUANTITY, true);
            //decorateColumn(m_model, RobotRequestModel.COLTYPE_SEPARATION, true);

        }

        private void decorateColumn(RobotRequestModel model, int col, boolean modifiable) {

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

    public ArrayList<RobotPlanningJson> getRobotPlannings() {


        return m_robotRequestModel.getRobotPlannings();

    }

    public boolean checkFields(DefaultDialog dialog) {
        ArrayList<RobotPlanningJson> robotPlanningJsonArrayList = m_robotRequestModel.getRobotPlannings();
        for (RobotPlanningJson robotPlanningJson : robotPlanningJsonArrayList) {
            if ( (robotPlanningJson.getLoadCount() == null) || (robotPlanningJson.getLoadCount() <1) ) {
                dialog.highlight(m_robotRequestTable);
                dialog.setStatus(true, "Load counts is compulsory and must be > 0.");
                return false;
            }
        }
        for (RobotPlanningJson robotPlanningJson : robotPlanningJsonArrayList) {
            if ( (robotPlanningJson.getTrypsineVol() == null) || (robotPlanningJson.getTrypsineVol() <=0) ) {
                dialog.highlight(m_robotRequestTable);
                dialog.setStatus(true, "Trypsine Quantity is comuplsory and must be > 0.");
                return false;
            }
        }
        for (RobotPlanningJson robotPlanningJson : robotPlanningJsonArrayList) {
            if ( (robotPlanningJson.getProteinQty() != null) && (robotPlanningJson.getProteinQty() <=0) ) {
                dialog.highlight(m_robotRequestTable);
                dialog.setStatus(true, "Protein Quantity must be > 0.");
                return false;
            }
        }
        return true;
    }




}
