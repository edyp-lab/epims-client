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

package fr.epims.ui.panels.admin;

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ContactJson;
import fr.edyp.epims.json.InstrumentJson;
import fr.epims.tasks.CreateInstrumentTask;
import fr.epims.tasks.ModifyInstrumentTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Panel : List of Instruments (Spectrometers), only available for Admin
 *
 * @author JM235353
 *
 */
public class AdminInstrumentPanel extends JPanel implements DataManager.DataManagerListener {

    private DefaultListModel<InstrumentJson> m_instrumentsListModel;
    private JList m_instrumentsList;

    public AdminInstrumentPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Instruments ");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_instrumentsListModel = new DefaultListModel<>();
        m_dataLoaded = false;
        loadData(null);

        m_instrumentsList = new JList(m_instrumentsListModel);
        m_instrumentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_instrumentsList.setCellRenderer(new InstrumentsRenderer());
        JScrollPane listScrollPane = new JScrollPane(m_instrumentsList){

            private final Dimension preferredSize = new Dimension(120, 320);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };

        FlatButton addInstrumentButton = new FlatButton(IconManager.getIcon(IconManager.IconType.INSTRUMENT_ADD), true);
        FlatButton modifyInstrumentButton = new FlatButton(IconManager.getIcon(IconManager.IconType.INSTRUMENT_EDIT), true);
        modifyInstrumentButton.setEnabled(false);

        c.gridx = 0;
        c.gridy = 0;
        add(addInstrumentButton, c);

        c.gridx++;
        add(modifyInstrumentButton, c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);

        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        add(listScrollPane, c);

        c.gridx = 3;
        c.gridwidth = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        add(Box.createGlue(), c);

        m_instrumentsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                modifyInstrumentButton.setEnabled(m_instrumentsList.getSelectedValue() != null);
            }
        });

        addInstrumentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), InstrumentJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                CreateInstrumentDialog dialog = new CreateInstrumentDialog(MainFrame.getMainWindow(), null);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    InstrumentJson[] instrumentList = new InstrumentJson[1];
                    instrumentList[0] = dialog.getInstrumentToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.instrumentAdded(instrumentList[0]);
                                m_dataLoaded = false;
                                loadData(instrumentList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the instrument already exists");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    CreateInstrumentTask task = new CreateInstrumentTask(callback, instrumentList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        modifyInstrumentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), InstrumentJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                InstrumentJson instrumentJson = (InstrumentJson) m_instrumentsList.getSelectedValue();

                CreateInstrumentDialog dialog = new CreateInstrumentDialog(MainFrame.getMainWindow(), instrumentJson);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    InstrumentJson[] instrumentList = new InstrumentJson[1];
                    instrumentList[0] = dialog.getInstrumentToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.instrumentModified(instrumentList[0]);
                                m_dataLoaded = false;
                                loadData(instrumentList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or there is an internal error");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    ModifyInstrumentTask task = new ModifyInstrumentTask(callback, instrumentList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        DataManager.addListener(InstrumentJson.class, this);

    }


    public void reinit() {
        m_dataLoaded = false;
        loadData(null);
    }

    public void loadData(InstrumentJson instrumentJsonToSelect) {
        if (m_dataLoaded) {
            return;
        }

        m_instrumentsListModel.clear();

        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                ArrayList<InstrumentJson> instruments = new ArrayList<>(DataManager.getSpectrometers());

                HashMap<Integer, InstrumentJson> map = DataManager.getInstrumentMap();
                for (InstrumentJson instrument : map.values()) {
                    if (!instrument.getIsSpectrometer()) {
                        instruments.add(instrument);
                    }
                }
                Collections.sort(instruments);

                for (InstrumentJson instrument : instruments) {
                    m_instrumentsListModel.addElement(instrument);
                }

                if (instrumentJsonToSelect != null) {
                    m_instrumentsList.setSelectedValue(instrumentJsonToSelect, true);
                }

            }
        };
        DataManager.dataAvailable(callback, false);
    }
    private boolean m_dataLoaded = false;

    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {
        // nothing to do
    }

    @Override
    public void updateAll(HashSet<Class> c) {

        InstrumentJson prevSelectedInstrument = (InstrumentJson) m_instrumentsList.getSelectedValue();
        InstrumentJson nextSelectedInstrument = null;

        m_dataLoaded = false;
        m_instrumentsListModel.clear();
        for (InstrumentJson instrument : DataManager.getSpectrometers()) {
            m_instrumentsListModel.addElement(instrument);
            if ((prevSelectedInstrument != null) && (prevSelectedInstrument.getId() == instrument.getId())) {
                nextSelectedInstrument = instrument;
            }
        }

        if (nextSelectedInstrument != null) {
            m_instrumentsList.setSelectedValue(nextSelectedInstrument, true);
        }

        m_dataLoaded = true;
    }

    public class InstrumentsRenderer extends DefaultListCellRenderer  {


        public InstrumentsRenderer() {
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            InstrumentJson instrumentJson = (InstrumentJson) value;
            setText(instrumentJson.getName());

            if (instrumentJson.isAvailable()) {
                setForeground(Color.black);
            } else {
                setForeground(Color.gray);
            }

            return this;
        }
    }
}
