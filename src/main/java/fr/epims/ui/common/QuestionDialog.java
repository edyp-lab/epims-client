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
import javax.swing.border.Border;
import java.awt.*;

/**
 *
 * Display a question in a dialog.
 *
 * @author JM235353
 *
 */
public class QuestionDialog extends InfoDialog {

    public QuestionDialog(Window parent, String title, String question) {
        this(parent, InfoType.QUESTION, title, question);
    }
    public QuestionDialog(Window parent, InfoType type, String title, String question) {
        super(parent, type,title, question);

        setTitle(title);

        JPanel p = new JPanel(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        p.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        p.add(new JLabel(question), c);

        setInternalComponent(p);

        setButtonVisible(DefaultDialog.BUTTON_OK, true);
        setButtonVisible(DefaultDialog.BUTTON_CANCEL, true);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        setButtonName(DefaultDialog.BUTTON_OK, "Yes");
        setButtonName(DefaultDialog.BUTTON_CANCEL, "No");

        setStatusVisible(false);
    }
}
