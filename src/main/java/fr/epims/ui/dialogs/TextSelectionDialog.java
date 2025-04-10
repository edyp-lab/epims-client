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

import fr.epims.ui.common.DefaultDialog;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;

/**
 *
 * Dialog to Select a String from a list of String
 *
 * @author JM235353
 *
 */
public class TextSelectionDialog  extends DefaultDialog {

    private JList<String> m_textList = null;
    private ArrayList<String> m_values = null;

    public TextSelectionDialog(Window parent, String valueTitle, ArrayList<String> values) {
        super(parent);

        m_values = values;

        setTitle("Select "+valueTitle);

        setInternalComponent(createInternalPanel());

        setButtonVisible(DefaultDialog.BUTTON_HELP, false);


    }

    public JPanel createInternalPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String s : m_values) {
            model.addElement(s);
        }
        m_textList = new JList<>(model);
        JScrollPane valuesScrollPane = new JScrollPane(m_textList) {

            private final Dimension preferredSize = new Dimension(120, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };


        // -------------- Place Widgets
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        p.add(valuesScrollPane, c);


        return p;
    }


    public String getSelectedValue() {
        return m_textList.getSelectedValue();
    }


}
