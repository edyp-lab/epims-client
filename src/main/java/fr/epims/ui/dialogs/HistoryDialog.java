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

import fr.epims.MainFrame;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ActorJson;
import fr.edyp.epims.json.ContactJson;
import fr.epims.tasks.CreateActorTask;
import fr.epims.tasks.ModifyActorTask;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.InfoDialog;
import fr.epims.ui.panels.UserInfoPanel;
import fr.epims.ui.panels.admin.AdminUserPanel;
import fr.epims.ui.panels.admin.CreateOrModifyUserDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 * Display history of a Sample
 *
 * @author JM235353
 *
 */
public class HistoryDialog extends DefaultDialog {

    public HistoryDialog(Window parent, String sampleName, ArrayList<String> history) {
        super(parent);

        setTitle("Sample History");

        setInternalComponent(createPanel(sampleName, history));

        setButtonVisible(DefaultDialog.BUTTON_OK, false);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setStatusVisible(false);
    }

    private JPanel createPanel(String sampleName, ArrayList<String> history) {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder(" "+sampleName+" ");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String s : history) {
            model.addElement(s);
        }

        JList list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScrollPane = new JScrollPane(list) {

            private final Dimension preferredSize = new Dimension(240, 280);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return preferredSize;
            }
        };

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        p.add(listScrollPane, c);

        return p;
    }

}
