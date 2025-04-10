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

package fr.epims.ui.analyserequest.panels;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 * Panel used to filter Studies of the SearchStudiesRefDialog
 *
 * @author JM235353
 *
 */
public class FilterStudiesPanel extends JPanel {

    private StudiesTreePanel m_studiesTreePanel;

    private ActionListener m_searchModificationListener;
    private ActionListener m_searchModificationListenerNoExpand;
    private DocumentListener m_docListener;
    private DocumentListener m_docListenerNoExpand;


    private JTextField m_nameFilter;


    private boolean m_filterBlocked = false;

    public FilterStudiesPanel(StudiesTreePanel studiesTreePanel) {
        super(new GridBagLayout());

        m_studiesTreePanel = studiesTreePanel;

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        init();
    }

    public void filter(boolean expand) {
        if (m_filterBlocked) {
            return;
        }


        String text = m_nameFilter.getText().trim();



        m_studiesTreePanel.filter(text);
    }



    private void init() {

        m_searchModificationListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filter(true);
            }
        };
        m_searchModificationListenerNoExpand = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filter(false);
            }
        };

        m_docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                m_searchModificationListener.actionPerformed(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                m_searchModificationListener.actionPerformed(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                m_searchModificationListener.actionPerformed(null);
            }
        };

        m_docListenerNoExpand = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                m_searchModificationListenerNoExpand.actionPerformed(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                m_searchModificationListenerNoExpand.actionPerformed(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                m_searchModificationListenerNoExpand.actionPerformed(null);
            }
        };

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel searchPanel = createSearchPanel();


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        add(searchPanel, c);



        m_nameFilter.getDocument().addDocumentListener(m_docListener);


    }



    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints cp = new GridBagConstraints();
        cp.anchor = GridBagConstraints.NORTHWEST;
        cp.fill = GridBagConstraints.BOTH;
        cp.insets = new java.awt.Insets(0, 5, 0, 5);

        m_nameFilter = new JTextField(20);
        m_nameFilter.setMinimumSize(m_nameFilter.getPreferredSize());


        cp.gridx = 0;
        cp.gridy = 0;
        cp.weightx = 0;
        panel.add(new JLabel("Search Text:"), cp);

        cp.gridx++;
        cp.weightx = 1;
        panel.add(m_nameFilter, cp);

        return panel;

    }




    public void reinit() {
        m_filterBlocked = true;
        try {
            m_nameFilter.setText("");


        } catch (Exception e) {
        }
        m_filterBlocked = false;
    }
}
