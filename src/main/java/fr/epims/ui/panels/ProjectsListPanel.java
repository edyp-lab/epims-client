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
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.epims.tasks.AddProjectTask;
import fr.epims.ui.common.*;
import fr.epims.ui.dialogs.AddProjectDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.panels.model.ProjectTableModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 *
 * Panel with list of projects of a program
 *
 * @author JM235353
 *
 */
public class ProjectsListPanel extends JPanel implements RendererMouseCallback {

    private DecoratedTable m_table;

    public ProjectsListPanel(ProgramJson p) {
        super(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Projects ");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        JToolBar toolbar = createTreeToolbar(p);

        m_table = new DecoratedTable();
        ProjectTableModel model = new ProjectTableModel(p, this);
        m_table.setModel(model);

        ViewTableCellRenderer renderer = (ViewTableCellRenderer) model.getRenderer(0, ProjectTableModel.COLTYPE_TITLE);
        m_table.addMouseListener(renderer);
        m_table.addMouseMotionListener(renderer);


        JScrollPane tableScrollPane = new JScrollPane(m_table);

        m_table.setFillsViewportHeight(true);
        tableScrollPane.setViewport(tableScrollPane.getViewport());


        c.weightx = 1;
        add(toolbar, c);

        c.gridy++;
        c.weighty = 1;
        add(tableScrollPane, c);
    }

    private JToolBar createTreeToolbar(final ProgramJson p) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        JButton addButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_PROJECT), false);
        toolbar.add(addButton);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), ProgramJson.class.getSimpleName());
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

                boolean locked = p.getClosingDate() != null;
                if (locked) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Program Closed", "Program is closed. You can not create projects.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                if (!DataManager.checkOwner(p)) {
                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.INFO, "Restricted Action", "You must be Responsible or Member of the Program to create a Project.");
                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                    infoDialog.setVisible(true);
                    return;
                }

                AddProjectDialog dialog = new AddProjectDialog(MainFrame.getMainWindow(), p);
                dialog.centerToScreen();
                dialog.setVisible(true);

                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    ProjectJson p = dialog.getProjectToCreate();
                    ProjectJson[] arrProject = new ProjectJson[1];
                    arrProject[0] = p;



                        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long taskId, boolean finished) {
                                if (success) {
                                    DataManager.createProject(arrProject[0]);
                                } else {
                                    InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server Error\n\nAction has been aborted.");
                                    infoDialog.centerToWindow(MainFrame.getMainWindow());
                                    infoDialog.setVisible(true);
                                }
                            }
                        };

                    AddProjectTask task = new AddProjectTask(callback, arrProject);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
                }

            }
        });

        return toolbar;

    }

    @Override
    public void mouseAction(MouseEvent e) {
        int col = m_table.columnAtPoint(e.getPoint());
        int row = m_table.rowAtPoint(e.getPoint());
        if ((row != -1) && (col != -1)) {

            int colModelIndex = m_table.convertColumnIndexToModel(col);
            if (colModelIndex == ProjectTableModel.COLTYPE_TITLE) {

                int rowModelIndex = m_table.convertRowIndexToModel(row);
                ProjectJson project = ((ProjectTableModel)m_table.getModel()).getProject(rowModelIndex);

                ActivitiesPanel activitiesPanel = ActivitiesPanel.getActivitiesPanel();
                activitiesPanel.getTree().display(project, false);


            }
        }
    }
}
