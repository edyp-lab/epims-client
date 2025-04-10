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
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * Dialog with two buttons : One to add a sample, the other to Fragment a Sample
 *
 * @author JM235353
 *
 */
public class AddOrFragmentSampleDialog extends DefaultDialog {

    private boolean m_addSampleSelected = false;

    public AddOrFragmentSampleDialog(Window parent) {
        super(parent);

        setTitle("Add or Fragment Samples");

        setInternalComponent(createInternalPanel());

        setButtonVisible(DefaultDialog.BUTTON_OK, false);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        setStatusVisible(false);
    }

    private JPanel createInternalPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        p.add(addSamplePanel(), c);

        c.gridx++;
        p.add(addFragmentationPanel(), c);

        return p;
    }


    private JPanel addSamplePanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Add Samples "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        FlatButton b = new FlatButton(IconManager.getIcon(IconManager.IconType.ADD_SAMPLE_CHOICE), false);

        c.gridx = 0;
        c.gridy = 0;
        p.add(b, c);



        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_addSampleSelected = true;
                okButtonActionPerformed();
            }
        });




        return p;
    }

    private JPanel addFragmentationPanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(" Fragment Samples "));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        FlatButton b = new FlatButton(IconManager.getIcon(IconManager.IconType.FRAG_SAMPLE_CHOICE), false);

        c.gridx = 0;
        c.gridy = 0;
        p.add(b, c);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_addSampleSelected = false;
                okButtonActionPerformed();
            }
        });

        return p;
    }

    public boolean isAddSampleSelected() {
        return m_addSampleSelected;
    }
}




