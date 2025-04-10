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
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Dialog used to set a list of sample names of samples that the user wants to create
 *
 * @author JM235353
 *
 */
public class SampleFromTextDialog extends DefaultDialog {

    private JTextArea m_samplesTextArea;

    public SampleFromTextDialog(Window parent, String sampleText) {
        super(parent);

        setTitle("Samples from Text");

        setInternalComponent(createInternalPanel(sampleText));

        setButtonVisible(DefaultDialog.BUTTON_OK, true);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setResizable(true);

        setStatusVisible(false);
    }

    private JPanel createInternalPanel(String sampleText) {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" Samples Names ");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        JScrollPane scrollPane = new JScrollPane();
        m_samplesTextArea = new JTextArea(40, 30);
        m_samplesTextArea.setText(sampleText);
        m_samplesTextArea.setWrapStyleWord(false);
        m_samplesTextArea.setLineWrap(false);
        scrollPane.setViewportView(m_samplesTextArea);

        FlatButton clearButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ERASER),false);


        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        c.weightx = 1;
        c.weighty = 1;
        p.add(scrollPane, c);

        c.gridx++;
        c.weightx = 0;
        c.weighty = 0;
        c.gridheight = 1;
        p.add(clearButton, c);

        c.gridy++;
        c.weighty = 1;
        p.add(Box.createGlue(), c);

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_samplesTextArea.setText("");
            }
        });

        return p;
    }

    public ArrayList<String> getSampleNames() {
        String text = m_samplesTextArea.getText().trim();

        ArrayList<String> sampleNames = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(text, "\n\t;,");
        while (st.hasMoreTokens()) {
            String sampleName = st.nextToken().trim();
            if (sampleName.length()>2) {
                sampleNames.add(sampleName);
            }
        }

        return sampleNames;
    }

}
