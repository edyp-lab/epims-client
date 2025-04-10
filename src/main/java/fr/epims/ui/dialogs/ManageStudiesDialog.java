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

import fr.epims.ui.common.DecoratedTable;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.panels.model.ArchivingStudyTableModel;

import javax.swing.*;
import java.awt.*;


/**
 *
 * Dialog not used and not finished for the moment.
 * Its goal is to entice users to close old studies
 *
 * @author JM235353
 *
 */
public class ManageStudiesDialog extends DefaultDialog {


    public ManageStudiesDialog(Window parent) {
        super(parent);

        setTitle("Manage Old Studies");

        setInternalComponent(createTabbedPane());

        setButtonVisible(DefaultDialog.BUTTON_OK, false);

        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setStatusVisible(false);
    }

    public JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Close Studies", createClosePanel());
        tabbedPane.setIconAt(0, IconManager.getIcon(IconManager.IconType.LOCK));


        tabbedPane.addTab("Ask Studies Archiving", createArchivePanel());
        tabbedPane.setIconAt(1, IconManager.getIcon(IconManager.IconType.ARCHIVE_ALLOW));


        return tabbedPane;
    }


    private JPanel createArchivePanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        DecoratedTable table = new DecoratedTable();
        table.setModel(new ArchivingStudyTableModel(table));

        JScrollPane tableScrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        tableScrollPane.setPreferredSize(new Dimension(600,300));
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        JLabel label = new JLabel("<html>You are responsible of old closed Studies. Select the studies which can be archieved and press the button:</html>", null, JLabel.TRAILING);

        FlatButton archiveButton = new FlatButton(IconManager.getIcon(IconManager.IconType.ARCHIVE_ALLOW), "Ask Archiving", true);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        p.add(tableScrollPane, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        c.weightx = 1;
        p.add(label, c);

        c.gridx++;
        c.weightx = 0;
        p.add(archiveButton, c);

        return p;
    }


    private JPanel createClosePanel() {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        DecoratedTable table = new DecoratedTable();
        table.setModel(new ArchivingStudyTableModel(table));

        JScrollPane tableScrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        tableScrollPane.setPreferredSize(new Dimension(600,300));
        tableScrollPane.setViewport(tableScrollPane.getViewport());

        JLabel label = new JLabel("<html>You are responsible of old active Studies. Select the studies which can be closed and press the button:</html>", null, JLabel.TRAILING);

        FlatButton button = new FlatButton(IconManager.getIcon(IconManager.IconType.LOCK), "Close Studies", true);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        p.add(tableScrollPane, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        c.weightx = 1;
        p.add(label, c);

        c.gridx++;
        c.weightx = 0;
        p.add(button, c);

        return p;
    }
}
