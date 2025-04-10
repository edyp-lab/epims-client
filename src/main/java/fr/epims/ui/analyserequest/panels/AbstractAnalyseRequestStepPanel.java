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

package fr.epims.ui.analyserequest.panels;

import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public abstract class AbstractAnalyseRequestStepPanel extends JPanel {

    protected boolean m_readOnly;
    protected ModifyAnalyseRequestDialog m_parentDialog;
    protected AnalyseRequestMainPanel m_mainPanel;
    protected AnalysisMapJson m_AnalysisMapJson;
    protected DocumentListener m_dataChangedListener;
    protected ItemListener m_itemChangedListener;
    protected TableModelListener m_tableModelListener;

    private Rectangle m_selectionZone = null;

    private static final Color SELECTION_COLOR = new Color(30,129,190);

    public AbstractAnalyseRequestStepPanel(boolean readOnly, ModifyAnalyseRequestDialog parentDialog, AnalyseRequestMainPanel mainPanel, AnalysisMapJson AnalysisMapJson, DocumentListener dataChangedListener, ItemListener itemChangedListener, TableModelListener tableModelListener) {
        m_readOnly = readOnly;
        m_parentDialog = parentDialog;
        m_mainPanel = mainPanel;
        m_AnalysisMapJson = AnalysisMapJson;
        m_dataChangedListener = dataChangedListener;
        m_itemChangedListener = itemChangedListener;
        m_tableModelListener = tableModelListener;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);


        if (m_selectionZone != null) {
            g.setColor(SELECTION_COLOR);
            ((Graphics2D)g).draw(m_selectionZone);
        }
    }


    protected void setSelectionZone(Component[] componentList) {
        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = -Integer.MAX_VALUE;
        int y2 = -Integer.MAX_VALUE;


        for (Component c : componentList) {

            Point p1 = SwingUtilities.convertPoint(c.getParent(), c.getX(), c.getY(), this);
            int cX1 = p1.x;
            int cY1 = p1.y;

            Point p2 = SwingUtilities.convertPoint(c.getParent(), c.getX()+c.getWidth(), c.getY()+c.getHeight(), this);
            int cX2 = p2.x;
            int cY2 = p2.y;

            if (x1>cX1) {
                x1 = cX1;
            }
            if (y1>cY1) {
                y1 = cY1;
            }
            if (x2<cX2) {
                x2 = cX2;
            }
            if (y2<cY2) {
                y2 = cY2;
            }
        }

        final int Margin = 6;
        x1 -= Margin;
        y1 -= Margin;
        x2 += Margin;
        y2 += Margin;
        m_selectionZone = new Rectangle(x1, y1,x2-x1, y2-y1);

        repaint();

    }

    protected void resetSelectionZone() {
        m_selectionZone = null;
        repaint();
    }

    public abstract void getTagMap(HashMap<String, String> data);

    public abstract void loadData(ProAnalysisJson proAnalysisJson, String studyRef, HashMap<String, String> valueMap);


    public static class AnalysisImportButton extends FlatButton {

        private String[] m_keysToExport;

        public AnalysisImportButton(AnalyseRequestMainPanel mainPanel, AbstractAnalyseRequestStepPanel panel, String destPanelKey, Component[] components, String[] keysToExport, String[] keysToReset) {
            super(IconManager.getIcon(IconManager.IconType.ARROW_IMPORT), true);

            m_keysToExport = keysToExport;

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    //numberOfSamplesLabel.setForeground(Color.blue);
                    panel.setSelectionZone(components);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    //numberOfSamplesLabel.setForeground(Color.black);
                    panel.resetSelectionZone();
                }
            });

            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    HashMap<String, String> srcDataMap = new HashMap<>();
                    panel.getTagMap(srcDataMap);

                    HashMap<String, String> exportDataMap = new HashMap<>();
                    for (String key : m_keysToExport) {
                        exportDataMap.put(key, srcDataMap.get(key));
                    }
                    for (String key : keysToReset) {
                        exportDataMap.put(key, "true");
                    }



                    mainPanel.importTo(destPanelKey, exportDataMap);
                }
            });
        }

        public void setKeysToExport(String[] keysToExport) {
            m_keysToExport = keysToExport;
        }

    }

    protected final void setText(JTextComponent textField, Object value) {
        if (value != null) {
            textField.setText(value.toString());
        }
    }


}
