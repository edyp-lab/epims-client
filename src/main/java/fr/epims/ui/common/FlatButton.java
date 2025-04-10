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

/**
 *
 * JButton with a Flat appearance, to be used everywhere in the application
 *
 * @author JM235353
 *
 */
public class FlatButton extends JButton {

    public FlatButton(Icon i, boolean framed) {
        super(i);

        init(framed);

    }

    public FlatButton(Icon i, String text, boolean framed) {
        super(text);
        setIcon(i);

        init(framed);
    }

    private void init(boolean framed) {
        setFocusable(false);
        setContentAreaFilled(framed);
        setMargin(new java.awt.Insets(0, 0, 0, 0));
    }
}