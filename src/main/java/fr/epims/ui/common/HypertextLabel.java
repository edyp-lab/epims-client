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
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Label with clickable hypertext link
 *
 * @author JM235353
 *
 */
public class HypertextLabel extends JLabel implements MouseListener {

    private ActionListener m_actionListener = null;

    public HypertextLabel(String text, ActionListener actionListener) {
        super(text);

        Font font = getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
        setFont(font.deriveFont(attributes));

        setForeground(Color.blue);

        m_actionListener = actionListener;

        if (m_actionListener != null) {
            addMouseListener(this);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (m_actionListener != null) {
            m_actionListener.actionPerformed(null);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
