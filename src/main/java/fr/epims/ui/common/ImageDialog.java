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

public class ImageDialog extends DefaultDialog {

    private Image m_image;

    private boolean m_nopack;

    public ImageDialog(Window parent, String title, Image image) {

        super(parent);

        setTitle(title);

        m_image = image;

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        boolean limitSize = false;
        if (width>900) {
            width = 900;
            limitSize = true;
        }
        if (height>800) {
            height = 800;
            limitSize = true;
        }
        if (limitSize) {
            setSize(width+40, height+100);
            m_nopack = true;
        }


        // hide default and ok button
        setButtonName(BUTTON_OK, "Close");
        setButtonVisible(BUTTON_CANCEL, false);
        setButtonVisible(BUTTON_HELP, false);
        setStatusVisible(false);


        JPanel internalPanel = new JPanel(null) {
            public void paint(Graphics g) {
                g.drawImage(m_image,0,0, null);
            }
        };
        internalPanel.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));

        JScrollPane sp = new JScrollPane();
        sp.setViewportView(internalPanel);
        setInternalComponent(sp);

    }

    @Override
    public void pack() {
        if (m_nopack) {
            return;
        } else {
            super.pack();
        }
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
