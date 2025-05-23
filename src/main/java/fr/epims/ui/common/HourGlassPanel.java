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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * Panel being able to display a ""Calculating.../Loading..." message
 * during a long processing
 *
 * @author JM235353
 *
 */
public class HourGlassPanel extends JPanel implements ActionListener {

    private static int loadingIndex = 0;

    private boolean m_loading = false;
    private boolean m_calculating = false;
    private boolean m_saving = false;

    protected int m_id = -1;

    public static int getNewLoadingIndex() {
        return loadingIndex++;
    }

    public void setLoading(int id, boolean calculating, boolean saving) {
        m_id = id;
        m_calculating = calculating;
        m_saving = saving;
        setLoading(true);
    }

    public boolean isLoading(){
        return this.m_loading;
    }

    public void setLoading(int id) {
        setLoading(id, false, false);
    }

    public void setLoaded(int id) {
        if (id>=m_id) {
            setLoading(false);
        }
    }

    private void setLoading(boolean loading) {
        boolean needRepaint = m_loading ^ loading;
        m_loading = loading;
        if (needRepaint) {
            if (!m_loading) {
                // loading is finished, we repaint now
                repaint();
            } else {
                // loading has started, we do not repaint now
                // to let time to the loading to be finished when it is rapid
                Timer timer = new Timer(500, this);
                timer.setRepeats(false);
                timer.start();
            }

        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (!m_loading) {
            return;
        }

        int height = getHeight();
        int width = getWidth();


        final int PAD = 10;
        final int INTERNAL_PAD = 5;
        final int ICON_WIDTH = 16;
        final int BOX_HEIGHT = INTERNAL_PAD*2+ICON_WIDTH;
        final int BOX_WIDTH = 130;
        g.setColor(Color.white);
        g.fillRect(PAD, height-BOX_HEIGHT-PAD, BOX_WIDTH, BOX_HEIGHT);
        g.setColor(Color.darkGray);
        g.drawRect(PAD+2, height-BOX_HEIGHT-PAD+2, BOX_WIDTH-4, BOX_HEIGHT-4);

        ImageIcon hourGlassIcon = IconManager.getIcon(IconManager.IconType.HOUR_GLASS);
        g.drawImage(hourGlassIcon. getImage(), PAD+INTERNAL_PAD,  height-BOX_HEIGHT-PAD+INTERNAL_PAD, null);

        if (m_loadingFont == null) {
            m_loadingFont = new Font("SansSerif", Font.BOLD, 12);
            FontMetrics metrics = g.getFontMetrics(m_loadingFont);
            m_fontAscent = metrics.getAscent();

        }
        g.setFont(m_loadingFont);
        g.setColor(Color.black);
        if (m_calculating) {
            g.drawString("Calculating...", PAD+INTERNAL_PAD*2+ICON_WIDTH, height-BOX_HEIGHT-PAD+INTERNAL_PAD+m_fontAscent);
        } else if (m_saving) {
            g.drawString("Saving...", PAD+INTERNAL_PAD*2+ICON_WIDTH, height-BOX_HEIGHT-PAD+INTERNAL_PAD+m_fontAscent);
        } else {
            g.drawString("Loading Data...", PAD+INTERNAL_PAD*2+ICON_WIDTH, height-BOX_HEIGHT-PAD+INTERNAL_PAD+m_fontAscent);
        }

    }
    private static Font m_loadingFont = null;
    private static int m_fontAscent = 0;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (m_loading) {
            // loading is not finished, we repaint to show the loading message
            repaint();
        }
    }




}
