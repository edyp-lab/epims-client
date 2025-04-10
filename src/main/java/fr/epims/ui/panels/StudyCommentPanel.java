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
import fr.epims.MainFrame;

import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;

import fr.epims.tasks.ChangeStudyCommentTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.UpdateDataDialog;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 * Panel to display and modify comments on a study
 *
 * @author JM235353
 *
 */
public class StudyCommentPanel extends HourGlassPanel {

    private JTextArea m_textArea;
    private FlatButton m_saveButton;

    public StudyCommentPanel(StudyJson s) {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JToolBar toolbar = createTreeToolbar(s);


        m_textArea = new JTextArea();
        String comment = s.getComment();
        if (comment == null) {
            comment = "";
        }
        m_textArea.setText(comment);
        m_textArea.setEditable(DataManager.checkOwner(s));


        JScrollPane scrollPane = new JScrollPane(m_textArea);



        c.weightx = 1;
        add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        add(scrollPane, c);


        m_textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                m_saveButton.setEnabled(true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                m_saveButton.setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                m_saveButton.setEnabled(true);
            }
        });

    }




    private JToolBar createTreeToolbar(StudyJson s) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);

        m_saveButton = new FlatButton(IconManager.getIcon(IconManager.IconType.SAVE), false);
        m_saveButton.setToolTipText("Save Study Comment");
        m_saveButton.setEnabled(false);
        toolbar.add(m_saveButton);


        m_saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), StudyJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                if (updateDataDialog.isDataUpdated()) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Retry", "Data were not up-to-date. Please Retry.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);

                    return;
                }

                if (!DataManager.checkOwner(s)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Study to modify the comment.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                StudyJson[] arrStudy = new StudyJson[1];
                arrStudy[0] = s;

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                s.setComment(arrStudy[0].getComment());
                                m_saveButton.setEnabled(false);

                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };

                    ChangeStudyCommentTask task = new ChangeStudyCommentTask(callback, m_textArea.getText(), arrStudy);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



            }
        });

        return toolbar;

    }

}
