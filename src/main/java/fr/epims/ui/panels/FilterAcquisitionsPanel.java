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
import fr.epims.dataaccess.DataAvailableCallback;
import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.ActorJson;
import fr.edyp.epims.json.InstrumentJson;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.dialogs.DatePickerDialog;
import fr.epims.ui.dialogs.UpdateDataDialog;
import fr.epims.ui.renderers.ActorComboBoxRenderer;
import fr.epims.util.UtilDate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *
 * Panel to filter acquisitions
 *
 * @author JM235353
 *
 */
public class FilterAcquisitionsPanel extends JPanel {

    private final static String SELECT = "< Select >";

    private AcquisitionsSearchListPanel m_resultPanel;

    private JTextField m_nameFilter;
    private JFormattedTextField m_dateStartTextField;
    private JFormattedTextField m_dateEndTextField;

    private JComboBox m_acquisitionTypeComboBox = new JComboBox();
    private JComboBox m_spectrometerComboBox = new JComboBox();
    private JComboBox m_studyMemberComboBox = new JComboBox();

    private DefaultComboBoxModel m_spectrometerModel = null;
    private DefaultComboBoxModel m_acquisitionTypeModel = null;
    private DefaultComboBoxModel m_studyMemberModel = null;

    private JButton m_searchButton;

    private FilterAcquisitionsPanel m_this;

    public FilterAcquisitionsPanel(AcquisitionsSearchListPanel resultPanel) {
        super(new GridBagLayout());

        m_this = this;

        m_resultPanel = resultPanel;

        Border titledBorder = BorderFactory.createTitledBorder(" Filter ");
        setBorder(titledBorder);

        init();

        // Fill comboboxes and let the Search button enabled when data is available
        DataAvailableCallback callback = new DataAvailableCallback() {

            @Override
            public void dataAvailable() {
                fillModels();

            }
        };
        DataManager.dataAvailable(callback, true);

    }

    public void fillModels() {

        // -- Spectrometers --
        ArrayList<InstrumentJson> spectrometers = DataManager.getSpectrometers();

        if (m_spectrometerModel == null) {
            Vector spectrometersVector = new Vector();
            spectrometersVector.add(SELECT);
            for (InstrumentJson spectrometer : spectrometers) {
                spectrometersVector.add(spectrometer);
            }
            m_spectrometerModel = new DefaultComboBoxModel<>( spectrometersVector );
            m_spectrometerComboBox.setModel( m_spectrometerModel );
        } else {
            Object o = m_spectrometerModel.getSelectedItem();
            Integer id = null;
            if (o instanceof InstrumentJson) {
                id = ((InstrumentJson) o).getId();
            }

            m_spectrometerModel.removeAllElements();
            Object selection = SELECT;
            m_spectrometerModel.addElement(SELECT);
            for (InstrumentJson spectrometer : spectrometers) {
                m_spectrometerModel.addElement(spectrometer);
                if ((id != null) && (id == spectrometer.getId())) {
                    selection = spectrometer;
                }
            }
            m_spectrometerModel.setSelectedItem(selection);
        }





        // -- Acquisition Type --
        ArrayList<String> acquisitionTypes = DataManager.getAcquisitionTypes();

        if (m_acquisitionTypeModel == null) {
            Vector acquisitionsTypeVector = new Vector();
            acquisitionsTypeVector.add(SELECT);
            for (String acquisitionType : acquisitionTypes) {
                acquisitionsTypeVector.add(acquisitionType);
            }
            m_acquisitionTypeModel = new DefaultComboBoxModel(acquisitionsTypeVector);
            m_acquisitionTypeComboBox.setModel(m_acquisitionTypeModel);
        } else {

            String o = (String) m_acquisitionTypeModel.getSelectedItem();

            m_acquisitionTypeModel.removeAllElements();
            Object selection = SELECT;
            m_acquisitionTypeModel.addElement(SELECT);
            for (String acquisitionType : acquisitionTypes) {
                m_acquisitionTypeModel.addElement(acquisitionType);
                if (acquisitionType.equals(o)) {
                    selection = o;
                }
            }
            m_acquisitionTypeModel.setSelectedItem(selection);
        }

        // -- Sample Owner --
        ArrayList<ActorJson> actors = DataManager.getActors();

        if (m_studyMemberModel == null) {

            Vector actorsVector = new Vector();
            actorsVector.add(SELECT);
            for (ActorJson actor : actors) {
                actorsVector.add(actor);
            }
            m_studyMemberModel = new DefaultComboBoxModel<>( actorsVector );
            m_studyMemberComboBox.setModel( m_studyMemberModel );
            m_studyMemberComboBox.setRenderer(new ActorComboBoxRenderer());
        } else {
            Object o = m_studyMemberModel.getSelectedItem();
            String login = null;
            if (o instanceof ActorJson) {
                login = ((ActorJson) o).getLogin();
            }

            m_studyMemberModel.removeAllElements();
            Object selection = SELECT;
            m_studyMemberModel.addElement(SELECT);
            for (ActorJson actor : actors) {
                m_studyMemberModel.addElement(actor);
                if ((login != null) && (login.equals(actor.getLogin()))) {
                    selection = actor;
                }
            }
            m_studyMemberModel.setSelectedItem(selection);
        }


        // Search is now possible
        m_searchButton.setEnabled(true);
    }

    private void init() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(createPanel(), c);




    }

    public void reinit() {
        m_nameFilter.setText("");

        m_dateStartTextField.setText("yyyy-mm-dd");
        m_dateEndTextField.setText("yyyy-mm-dd");

        m_acquisitionTypeComboBox.setSelectedItem(SELECT);
        m_spectrometerComboBox.setSelectedItem(SELECT);
        m_studyMemberComboBox.setSelectedItem(SELECT);
    }



    public void searchDone() {
        m_searchButton.setEnabled(true);
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints cp = new GridBagConstraints();
        cp.anchor = GridBagConstraints.NORTHWEST;
        cp.fill = GridBagConstraints.HORIZONTAL;
        cp.insets = new java.awt.Insets(5, 5, 5, 5);


        m_nameFilter = new JTextField(20);
        m_nameFilter.setMinimumSize(m_nameFilter.getPreferredSize());

        DateFormat format = UtilDate.getDateFormat();

        m_dateStartTextField = new JFormattedTextField(format);
        m_dateStartTextField.setText("yyyy-mm-dd");
        m_dateStartTextField.setColumns(10);
        m_dateStartTextField.setPreferredSize(m_dateStartTextField.getPreferredSize());
        FlatButton dateStartButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CALENDAR), false);


        m_dateEndTextField = new JFormattedTextField(format);
        m_dateEndTextField.setText("yyyy-mm-dd");
        m_dateEndTextField.setColumns(10);
        m_dateEndTextField.setPreferredSize(m_dateEndTextField.getPreferredSize());
        FlatButton dateEndButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CALENDAR), false);

        m_searchButton = new JButton("Search", IconManager.getIcon(IconManager.IconType.SEARCH));
        m_searchButton.setEnabled(false);

        cp.gridx = 0;
        cp.gridy = 0;
        panel.add(new JLabel("Search Text:", JLabel.TRAILING), cp);

        cp.gridx++;
        cp.gridwidth = 3;
        panel.add(m_nameFilter, cp);


        // -----------------
        cp.gridx = 0;
        cp.gridy++;
        cp.gridwidth = 1;
        panel.add(new JLabel("From:", JLabel.TRAILING), cp);

        cp.gridx++;
        panel.add(m_dateStartTextField, cp);

        cp.gridx++;
        panel.add(dateStartButton, cp);

        cp.gridx++;
        panel.add(Box.createGlue(), cp);

        cp.gridx = 0;
        cp.gridy++;
        panel.add(new JLabel("To:", JLabel.TRAILING), cp);

        cp.gridx++;
        panel.add(m_dateEndTextField, cp);

        cp.gridx++;
        panel.add(dateEndButton, cp);

        cp.gridx++;
        panel.add(Box.createGlue(), cp);

        //-----------------

        cp.gridy = 0;
        cp.gridx++;
        panel.add(Box.createHorizontalStrut(10), cp);

        //-----------------
        cp.gridx++;
        cp.gridy = 0;
        panel.add(new JLabel("Acquisition Type:", JLabel.TRAILING), cp);

        cp.gridx++;
        panel.add(m_acquisitionTypeComboBox, cp);

        cp.gridx = 5;
        cp.gridy++;
        panel.add(new JLabel("Study Member:", JLabel.TRAILING), cp);

        cp.gridx++;
        panel.add(m_studyMemberComboBox, cp);

        cp.gridx = 5;
        cp.gridy++;
        panel.add(new JLabel("Instrument:", JLabel.TRAILING), cp);

        cp.gridx++;
        panel.add(m_spectrometerComboBox, cp);

        //-----------------

        cp.gridx++;
        panel.add(Box.createHorizontalStrut(10), cp);

        cp.gridx++;
        panel.add(m_searchButton, cp);

        //-----------------
        cp.gridx++;
        cp.weightx = 1;
        panel.add(Box.createGlue(), cp);




        //-----------------

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
                    m_dateStartTextField.setText(format.format(selectedDate));
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
                    m_dateEndTextField.setText(format.format(selectedDate));
                }

            }
        });

        m_searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                m_resultPanel.reinitData();

                UpdateDataDialog updateDataDialog = new UpdateDataDialog(MainFrame.getMainWindow(), null);
                updateDataDialog.centerToWindow(MainFrame.getMainWindow());
                updateDataDialog.setVisible(true);

                if (updateDataDialog.isServerDown()) {
                    return;
                }

                String searchText = m_nameFilter.getText().trim();

                String acquisitionType = null;
                if (m_acquisitionTypeComboBox.getSelectedIndex()>0) {
                    acquisitionType = (String)  m_acquisitionTypeComboBox.getSelectedItem();
                }

                int instrumentId = -1;
                if (m_spectrometerComboBox.getSelectedIndex()>0) {
                    instrumentId = ((InstrumentJson) m_spectrometerComboBox.getSelectedItem()).getId();
                }

                String sampleOwnerActorKey = null;
                if (m_studyMemberComboBox.getSelectedIndex()>0) {
                    sampleOwnerActorKey = ((ActorJson) m_studyMemberComboBox.getSelectedItem()).getLogin();
                }

                DateFormat format = UtilDate.getDateFormat();
                Date startDate = UtilDate.convertToDateWithoutHour(m_dateStartTextField.getText());
                Date endDate = UtilDate.convertToDateWithoutHour(m_dateEndTextField.getText());
                String startDateString = (startDate != null) ? format.format(startDate) : null ;
                String endDateString = (endDate != null) ? format.format(endDate) : null ;

                m_searchButton.setEnabled(false);

                m_resultPanel.loadData(m_this, searchText, acquisitionType, instrumentId, sampleOwnerActorKey, startDateString, endDateString);

            }
        });

        return panel;

    }


}
