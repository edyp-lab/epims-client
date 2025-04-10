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

package fr.epims.ui.panels;

import fr.epims.MainFrame;
import fr.epims.dataaccess.DataManager;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.dialogs.DatePickerDialog;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * Panel to filter activities
 *
 * @author JM235353
 *
 */
public class FilterActivitiesPanel extends JPanel {

    private ActivitiesPanel m_activitiesPanel;

    private ActionListener m_searchModificationListener;
    private ActionListener m_searchModificationListenerNoExpand;
    private DocumentListener m_docListener;
    private DocumentListener m_docListenerNoExpand;

    private FocusListener m_focusListener;

    private JCheckBox m_myActivityCB;
    private JCheckBox m_openStudyCB;
    private JTextField m_nameFilter;
    private JTextField m_dateStartTextField;
    private JTextField m_dateEndTextField;

    private boolean m_filterBlocked = false;

    public FilterActivitiesPanel(ActivitiesPanel activitiesPanel) {
        super(new GridBagLayout());

        m_activitiesPanel = activitiesPanel;

        Border titledBorder = BorderFactory.createTitledBorder(" Filter ");
        setBorder(titledBorder);

        init();
    }

    public void filter(boolean expand) {
        if (m_filterBlocked) {
            return;
        }

        String user = m_myActivityCB.isSelected() ? DataManager.getLoggedUser() : null;
        boolean onGoing = m_openStudyCB.isSelected();
        String text = m_nameFilter.getText().trim();

        Date startDate = UtilDate.convertToDateWithoutHour(m_dateStartTextField.getText());
        Date endDate = UtilDate.convertToDateWithoutHour(m_dateEndTextField.getText());
        if (endDate != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(endDate);
            c.add(Calendar.DATE, 1);
            endDate = c.getTime();
        }

        m_activitiesPanel.filter(user, onGoing, text, startDate , endDate, expand);
    }



    private void init() {

        m_searchModificationListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filter(true);
            }
        };
        m_searchModificationListenerNoExpand = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filter(false);
            }
        };

        m_docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                m_searchModificationListener.actionPerformed(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                m_searchModificationListener.actionPerformed(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                m_searchModificationListener.actionPerformed(null);
            }
        };

        m_docListenerNoExpand = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                m_searchModificationListenerNoExpand.actionPerformed(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                m_searchModificationListenerNoExpand.actionPerformed(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                m_searchModificationListenerNoExpand.actionPerformed(null);
            }
        };

        m_focusListener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField textField = (JTextField) e.getSource();
                if (textField.getText().trim().isEmpty()) {
                    textField.setText("yyyy-mm-dd");
                }

            }
        };

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel upPanel = createUpPanel();
        JPanel searchPanel = createSearchPanel();
        JPanel datePanel = createDatePanel();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        add(upPanel, c);

        c.gridy++;
        add(searchPanel, c);

        c.gridy++;
        add(datePanel, c);



        m_myActivityCB.addActionListener(m_searchModificationListenerNoExpand);
        m_openStudyCB.addActionListener(m_searchModificationListenerNoExpand);
        m_nameFilter.getDocument().addDocumentListener(m_docListener);


    }

    private JPanel createUpPanel() {
        JPanel upPanel = new JPanel(new GridBagLayout());

        GridBagConstraints cp = new GridBagConstraints();
        cp.anchor = GridBagConstraints.NORTHWEST;
        cp.fill = GridBagConstraints.BOTH;
        cp.insets = new java.awt.Insets(0, 5, 0, 5);

        m_myActivityCB = new JCheckBox("My Activities");
        m_myActivityCB.setSelected(true);

        m_openStudyCB = new JCheckBox("Ongoing");
        m_openStudyCB.setSelected(true);

        cp.gridx = 0;
        cp.gridy = 0;
        cp.weightx = 0;
        upPanel.add(m_myActivityCB, cp);

        cp.gridx++;
        upPanel.add(m_openStudyCB, cp);

        cp.gridx++;
        cp.weightx = 1;
        upPanel.add(Box.createHorizontalGlue(), cp);

        return upPanel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints cp = new GridBagConstraints();
        cp.anchor = GridBagConstraints.NORTHWEST;
        cp.fill = GridBagConstraints.BOTH;
        cp.insets = new java.awt.Insets(0, 5, 0, 5);

        m_nameFilter = new JTextField(20);
        m_nameFilter.setMinimumSize(m_nameFilter.getPreferredSize());


        cp.gridx = 0;
        cp.gridy = 0;
        cp.weightx = 0;
        panel.add(new JLabel("Search Text:"), cp);

        cp.gridx++;
        cp.weightx = 1;
        panel.add(m_nameFilter, cp);

        return panel;

    }


    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel(new GridBagLayout());

        GridBagConstraints cp = new GridBagConstraints();
        cp.anchor = GridBagConstraints.NORTHWEST;
        cp.fill = GridBagConstraints.BOTH;
        cp.insets = new java.awt.Insets(0, 5, 0, 5);

        DateFormat format = UtilDate.getDateFormat();

        m_dateStartTextField = new JTextField();
        m_dateStartTextField.setText("yyyy-mm-dd");
        m_dateStartTextField.setColumns(10);
        m_dateStartTextField.setPreferredSize(m_dateStartTextField.getPreferredSize());
        FlatButton dateStartButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CALENDAR), false);

        m_dateEndTextField = new JTextField();
        m_dateEndTextField.setText("yyyy-mm-dd");
        m_dateEndTextField.setColumns(10);
        m_dateEndTextField.setPreferredSize(m_dateEndTextField.getPreferredSize());
        FlatButton dateEndButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CALENDAR), false);


        cp.gridx = 0;
        cp.gridy = 0;
        cp.weightx = 0;

        datePanel.add(new JLabel("From:"), cp);

        cp.gridx++;
        datePanel.add(m_dateStartTextField, cp);

        cp.gridx++;
        datePanel.add(dateStartButton, cp);

        cp.gridx++;
        datePanel.add(Box.createHorizontalStrut(20), cp);

        cp.gridx++;
        datePanel.add(new JLabel("To:"), cp);


        cp.gridx++;
        datePanel.add(m_dateEndTextField, cp);

        cp.gridx++;
        datePanel.add(dateEndButton, cp);

        cp.gridx++;
        cp.weightx = 1;
        datePanel.add(Box.createHorizontalGlue(), cp);
        cp.weightx = 0;


        dateStartButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = UtilDate.convertToDateWithoutHour(m_dateStartTextField.getText());
                if (d == null) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.MONTH, 0);
                    cal.set(Calendar.YEAR, 2000);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    d = cal.getTime();
                }
                DatePickerDialog dialog = new DatePickerDialog(MainFrame.getMainWindow(), "Pick Start Date", d);

                dialog.setLocation(dateStartButton.getLocationOnScreen().x+dateStartButton.getWidth()/2, dateStartButton.getLocationOnScreen().y+dateStartButton.getHeight()/2);
                dialog.setVisible(true);

                Date selectedDate = dialog.getSelectedDate();
                if (selectedDate != null) {
                    DateFormat format = UtilDate.getDateFormat();
                    m_filterBlocked = true; //JPM.WART : avoid filter during setText
                    try {
                        String dateAsText = format.format(selectedDate);
                        m_dateStartTextField.setText(dateAsText);
                    } catch (Exception exp) {
                    }
                    m_filterBlocked = false;
                    filter(false);

                }
            }
        });

        dateEndButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = UtilDate.convertToDateWithoutHour(m_dateEndTextField.getText());
                if (d == null) {
                    d = new Date();
                }
                DatePickerDialog dialog = new DatePickerDialog(MainFrame.getMainWindow(), "Pick End Date", d);
                dialog.setLocation(dateEndButton.getLocationOnScreen().x+dateEndButton.getWidth()/2, dateEndButton.getLocationOnScreen().y+dateEndButton.getHeight()/2);
                dialog.setVisible(true);

                Date selectedDate = dialog.getSelectedDate();
                if (selectedDate != null) {
                    DateFormat format = UtilDate.getDateFormat();
                    m_filterBlocked = true; //JPM.WART : avoid filter during setText
                    try {
                        String dateAsText = format.format(selectedDate);
                        m_dateEndTextField.setText(dateAsText);
                    } catch (Exception exp) {
                    }
                    m_filterBlocked = false;
                    filter(false);

                }

            }
        });


        m_dateStartTextField.getDocument().addDocumentListener(m_docListenerNoExpand);
        m_dateEndTextField.getDocument().addDocumentListener(m_docListenerNoExpand);

        m_dateStartTextField.addFocusListener(m_focusListener);
        m_dateEndTextField.addFocusListener(m_focusListener);

        return datePanel;


    }

    public void reinit() {
        m_filterBlocked = true;
        try {
            m_nameFilter.setText("");
            m_myActivityCB.setSelected(true);
            m_openStudyCB.setSelected(true);

            m_dateStartTextField.setText("yyyy-mm-dd");
            m_dateEndTextField.setText("yyyy-mm-dd");

        } catch (Exception e) {
        }
        m_filterBlocked = false;
    }
}
