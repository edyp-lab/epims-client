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
import fr.edyp.epims.json.CompanyJson;
import fr.epims.tasks.CreateCompanyTask;
import fr.epims.tasks.ModifyCompanyTask;
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
import java.util.HashSet;

/**
 *
 * Panel : List of Companies (affiliations), only available for Admin
 *
 * @author JM235353
 *
 */
public class AdminCompanyPanel extends JPanel implements DataManager.DataManagerListener {

    private DefaultListModel<CompanyJson> m_companiesListModel;
    private JList m_companiesList;

    public AdminCompanyPanel() {
        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Affiliations ");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_companiesListModel = new DefaultListModel<>();
        m_dataLoaded = false;
        loadData(null);

        m_companiesList = new JList(m_companiesListModel);
        m_companiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_companiesList.setCellRenderer(new CompaniesRenderer());
        JScrollPane listScrollPane = new JScrollPane(m_companiesList){

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

        FlatButton addCompanyButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_HOME), true);
        FlatButton modifyCompanyButton = new FlatButton(IconManager.getIcon(IconManager.IconType.EDIT_HOME), true);
        modifyCompanyButton.setEnabled(false);

        c.gridx = 0;
        c.gridy = 0;
        add(addCompanyButton, c);

        c.gridx++;
        add(modifyCompanyButton, c);

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

        m_companiesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                modifyCompanyButton.setEnabled(m_companiesList.getSelectedValue() != null);
            }
        });

        addCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), CompanyJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                CreateOrModifyCompanyDialog dialog = new CreateOrModifyCompanyDialog(MainFrame.getMainWindow(), null);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    CompanyJson[] companyList = new CompanyJson[1];
                    companyList[0] = dialog.getCompanyToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.companyAdded(companyList[0]);
                                m_dataLoaded = false;
                                loadData(companyList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or the company already exists");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    CreateCompanyTask task = new CreateCompanyTask(callback, companyList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        modifyCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), CompanyJson.class.getSimpleName());
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                CompanyJson companyJson = (CompanyJson) m_companiesList.getSelectedValue();

                CreateOrModifyCompanyDialog dialog = new CreateOrModifyCompanyDialog(MainFrame.getMainWindow(), companyJson);
                dialog.centerToWindow(MainFrame.getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    CompanyJson[] companyList = new CompanyJson[1];
                    companyList[0] = dialog.getCompanyToCreate();

                    AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success, long taskId, boolean finished) {
                            if (success) {
                                DataManager.companyModified(companyList[0]);
                                m_dataLoaded = false;
                                loadData(companyList[0]);
                            } else {
                                InfoDialog infoDialog = new InfoDialog(MainFrame.getMainWindow(), InfoDialog.InfoType.WARNING, "Server Error", "Server is down or there is an internal error");
                                infoDialog.centerToWindow(MainFrame.getMainWindow());
                                infoDialog.setVisible(true);
                            }
                        }
                    };


                    ModifyCompanyTask task = new ModifyCompanyTask(callback, companyList);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(task);



                }
            }
        });

        DataManager.addListener(CompanyJson.class, this);

    }

    public void reinit() {
        m_dataLoaded = false;
        loadData(null);
    }

    public void loadData(CompanyJson companyJsonToSelect) {
        if (m_dataLoaded) {
            return;
        }

        m_companiesListModel.clear();

        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {

                for (CompanyJson company : DataManager.getCompanies()) {
                    m_companiesListModel.addElement(company);
                }

                if (companyJsonToSelect != null) {
                    m_companiesList.setSelectedValue(companyJsonToSelect, true);
                }

                m_dataLoaded = true;

            }
        };
        DataManager.dataAvailable(callback, false);
    }
    private boolean m_dataLoaded = false;

    @Override
    public void update(Class c, Object o, DataManager.ActionTypeEnum actionType) {
        updateAll(null);

    }

    @Override
    public void updateAll(HashSet<Class> c) {

        CompanyJson prevSelectedCompany = (CompanyJson) m_companiesList.getSelectedValue();
        CompanyJson nextSelectedCompany = null;

        m_dataLoaded = false;
        m_companiesListModel.clear();
        for (CompanyJson company : DataManager.getCompanies()) {
            m_companiesListModel.addElement(company);
            if ((prevSelectedCompany != null) && (prevSelectedCompany.getName().equals(company.getName()))) {
                nextSelectedCompany = company;
            }
        }

        if (nextSelectedCompany != null) {
            m_companiesList.setSelectedValue(nextSelectedCompany, true);
        }

        m_dataLoaded = true;
    }


    public class CompaniesRenderer extends DefaultListCellRenderer  {


        public CompaniesRenderer() {
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

            CompanyJson companyJson = (CompanyJson) value;
            setText(companyJson.getName());

            return this;
        }
    }
}
