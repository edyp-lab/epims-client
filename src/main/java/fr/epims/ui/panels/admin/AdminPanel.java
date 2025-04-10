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


import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 *
 * Panel : Admin Panel containing a Tabbed Pane with Contacts, Companiers, Intruments...
 *
 * @author JM235353
 *
 */
public class AdminPanel extends JPanel {
    private static AdminPanel m_singleton = null;

    private JTabbedPane m_tabbedPane;

    private AdminCompanyPanel m_adminCompanyPanel;
    private AdminContactPanel m_adminContactPanel;
    private AdminUserPanel m_adminUserPanel;
    private AdminInstrumentPanel m_instrumentPanel;
    private PriceListPanel m_priceListPanel;
    private AdminArchiveStudiesPanel m_adminArchiveStudiesPanel;


    public static AdminPanel getAdminPanel() {
        if (m_singleton == null) {
            m_singleton = new AdminPanel();
        }
        return m_singleton;
    }

    private AdminPanel() {
        super(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JTabbedPane tabbedPane = createTabbedPane();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(tabbedPane, c);



    }

    private JTabbedPane createTabbedPane() {
        m_tabbedPane = new JTabbedPane();

        m_adminCompanyPanel = new AdminCompanyPanel();
        m_tabbedPane.addTab("Affiliations", m_adminCompanyPanel);
        m_tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.HOME));

        m_adminContactPanel = new AdminContactPanel();
        m_tabbedPane.addTab("Contacts", m_adminContactPanel);
        m_tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.USER));

        m_adminUserPanel = new AdminUserPanel();
        m_tabbedPane.addTab("Users", m_adminUserPanel);
        m_tabbedPane.setIconAt(2, IconManager.getIcon(IconManager.IconType.USER));

        m_instrumentPanel = new AdminInstrumentPanel();
        m_tabbedPane.addTab("Instruments", m_instrumentPanel);
        m_tabbedPane.setIconAt(3, IconManager.getIcon(IconManager.IconType.INSTRUMENT));

        m_priceListPanel = new PriceListPanel();
        m_tabbedPane.addTab("Analysis Requests Prices", m_priceListPanel);
        m_tabbedPane.setIconAt(4, IconManager.getIcon(IconManager.IconType.PRICE_TAG));

        m_adminArchiveStudiesPanel = new AdminArchiveStudiesPanel();
        m_tabbedPane.addTab("Archiving", m_adminArchiveStudiesPanel);
        m_tabbedPane.setIconAt(5, IconManager.getIcon(IconManager.IconType.ARCHIVE));

        m_tabbedPane.addChangeListener(new ChangeListener() { //add the Listener

            public void stateChanged(ChangeEvent e) {

                // Analyses Requests
                if(m_tabbedPane.getSelectedIndex() == 5) {
                    m_adminArchiveStudiesPanel.refreshArchiving();
                }
            }
        });


        return m_tabbedPane;
    }

    public void reinit() {
        m_adminCompanyPanel.reinit();
        m_adminContactPanel.reinit();
        m_adminUserPanel.reinit();
        m_instrumentPanel.reinit();
        m_adminArchiveStudiesPanel.reinit();
    }

    public void setArchiveEnabled(boolean enabled) {
        m_tabbedPane.setEnabledAt(5, enabled);

        if (enabled) {
            m_adminArchiveStudiesPanel.loadData();
        }
    }

}
