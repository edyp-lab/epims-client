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

package fr.epims.ui.dialogs;

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.SampleTypeJson;
import fr.epims.tasks.AddSampleTypeTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 * Dialog to Select Sample Type
 *
 * @author JM235353
 *
 */
public class SelectSampleTypeDialog extends DefaultDialog {

    private JTextField m_nameFilter;
    private JList<SampleTypeJson> m_list;

    private DefaultListModel<SampleTypeJson> m_listModel;

    private ArrayList<SampleTypeJson> m_elements;

    public SelectSampleTypeDialog(Window parent, ArrayList<SampleTypeJson> alreadyPresents) {
        super(parent);

        setTitle("Select Variety");

        setInternalComponent(createInternalPanel(alreadyPresents));

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);


        setStatusVisible(false);

        pack();
    }

    public JPanel createInternalPanel(ArrayList<SampleTypeJson> alreadyPresents) {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_nameFilter = new JTextField(20);

        m_elements = DataManager.getSampleTypesWithoutPresents(alreadyPresents);

        m_listModel = new DefaultListModel<>();
        fillModel(m_elements);


        FlatButton createButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD), "Add", true);


        m_list = new JList(m_listModel);
        m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(m_list) {

            private final Dimension preferredSize = new Dimension(220, 300);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };

        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Filter:"), c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        p.add(m_nameFilter, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        p.add(listScrollPane, c);

        c.gridy++;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 1;
        p.add(Box.createGlue(), c);

        c.gridx++;
        p.add(Box.createGlue(), c);

        c.gridx++;
        c.weightx = 0;
        p.add(createButton, c);


        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    AddSampleTypeDialog dialog = new AddSampleTypeDialog(MainFrame.getMainWindow());
                    dialog.centerToWindow(MainFrame.getMainWindow());
                    dialog.setVisible(true);
                    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                        SampleTypeJson[] paramList = new SampleTypeJson[1];
                        paramList[0] = dialog.getSampleTypeToCreate();

                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, boolean finished) {
                                if (success) {
                                    DataManager.sampleTypeAdded(paramList[0]);
                                    m_elements = DataManager.getSampleTypesWithoutPresents(alreadyPresents);
                                    m_listModel.clear();
                                    for (SampleTypeJson sampleTypeJson : m_elements) {
                                        m_listModel.addElement(sampleTypeJson);
                                    }

                                    m_list.setSelectedValue(paramList[0], true);

                                } else {
                                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or Sample Type already exists");
                                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                                    infoDialog.setVisible(true);
                                }
                            }
                        };


                        AddSampleTypeTask task = new AddSampleTypeTask(callback, paramList);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                    }
                }
            });

        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        };

        m_nameFilter.getDocument().addDocumentListener(docListener);

        return p;
    }

    private void filter() {
        String text = m_nameFilter.getText().trim();
        if (text.isEmpty()) {
            fillModel(m_elements);
            return;
        }
        text = text.toLowerCase();

        ArrayList<SampleTypeJson> elements = new ArrayList<>();
        for (SampleTypeJson sampleType : m_elements) {
            if (sampleType.getName().toLowerCase().indexOf(text) != -1) {
                elements.add(sampleType);
            }
        }

        fillModel(elements);
    }

    private void fillModel(ArrayList<SampleTypeJson> elements) {
        m_listModel.removeAllElements();

        for (SampleTypeJson sampleType : elements) {
            m_listModel.addElement(sampleType);
        }
    }

    public SampleTypeJson getSelectedSampleType() {

        return m_list.getSelectedValue();
    }


}
