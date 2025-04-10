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

package fr.epims.ui.common;

import javax.swing.*;
import java.awt.*;

/**
 *
 *  Used to display the text of a table cell too long to be viewed directly in the table.
 *
 * @author JM235353
 *
 */
public class DisplayDialog extends DefaultDialog {

    public DisplayDialog(Window parent, String text) {

        super(parent);

        setTitle("Cell Display");


        // hide default and ok button
        setButtonName(BUTTON_OK, "Close");
        setButtonVisible(BUTTON_CANCEL, false);
        setButtonVisible(BUTTON_HELP, false);
        setStatusVisible(false);

        JPanel internalPanel = new JPanel(new GridBagLayout());
        internalPanel.setBackground(Color.white);
        internalPanel.setOpaque(true);
        internalPanel.setBorder(BorderFactory.createTitledBorder(""));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JScrollPane descriptionScrollPane = new JScrollPane();
        JTextArea descriptionTextArea = new JTextArea(10, 60);
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setText(text);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(descriptionScrollPane, c);


        setInternalComponent(internalPanel);

        setResizable(true);

    }


    @Override
    protected boolean okCalled() {
        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }


}
