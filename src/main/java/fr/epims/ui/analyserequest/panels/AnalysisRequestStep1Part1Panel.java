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
import fr.edyp.epims.json.StudyJson;
import fr.epims.MainFrame;
import fr.epims.dataaccess.DataManager;
import fr.epims.ui.analyserequest.dialogs.ModifyAnalyseRequestDialog;
import fr.epims.ui.analyserequest.dialogs.SearchStudiesRefDialog;
import fr.epims.ui.common.*;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * Sub Panel of AnalyseRequestMainPanel.
 * Possibility to modify Request Analysis information of the client
 *
 * @author JM235353
 *
 */
public class AnalysisRequestStep1Part1Panel extends AbstractAnalyseRequestStepPanel {

    public static final String STUDY_REF = "STUDY_REF";

    private static final String REQUEST_DATE = "REQUEST_DATE";
    public static final String CUSTOMER_NAME = "CUSTOMER_NAME";
    public static final String LABORATORY = "LABORATORY";
    private static final String ADDRESS = "ADDRESS";
    private static final String TEL = "TEL";
    private static final String EMAIL = "EMAIL";
    private static final String EMAIL_TEAM_MANAGER = "EMAIL_TEAM_MANAGER";

    private JTextField m_studyTF;
    private FlatButton m_studyRefButton;

    private JTextField m_requestDateTF;
    private JTextField m_customerNameTF;
    private JTextField m_laboratoryTF;
    private JTextField m_addressTF;
    private JTextField m_telTF;
    private JTextField m_emailTF;
    private JTextField m_emailTeamManagerTF;



    public AnalysisRequestStep1Part1Panel(boolean readOnly, ModifyAnalyseRequestDialog parentDialog, AnalyseRequestMainPanel mainPanel, AnalysisMapJson analysisMapJson, DocumentListener dataChangedListener, ItemListener itemChangedListener, TableModelListener tableModelListener) {
        super( readOnly,  parentDialog,  mainPanel,  analysisMapJson,  dataChangedListener,  itemChangedListener, tableModelListener);


        setLayout(new GridBagLayout());

        Border titledBorder = BorderFactory.createTitledBorder("");
        setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // -------------- Create Widgets
        m_studyTF = new JTextField(10);
        m_studyRefButton = new FlatButton(IconManager.getIcon(IconManager.IconType.SELECT_STUDY_REF), true);
        m_requestDateTF = new JTextField(50);
        m_requestDateTF.setEditable(false);
        m_customerNameTF = new JTextField(50);
        m_laboratoryTF = new JTextField(50);
        m_addressTF = new JTextField(50);
        m_telTF = new JTextField(50);
        m_emailTF = new JTextField(50);
        m_emailTeamManagerTF = new JTextField(50);

        if (readOnly) {
            m_studyTF.setEditable(false);
            m_studyRefButton.setEnabled(false);
            m_requestDateTF.setEditable(false);
            m_requestDateTF.setEditable(false);
            m_customerNameTF.setEditable(false);
            m_laboratoryTF.setEditable(false);
            m_addressTF.setEditable(false);
            m_telTF.setEditable(false);
            m_emailTF.setEditable(false);
            m_emailTeamManagerTF.setEditable(false);
        } else {


            m_studyTF.getDocument().addDocumentListener(dataChangedListener);
            m_requestDateTF.getDocument().addDocumentListener(dataChangedListener);
            m_customerNameTF.getDocument().addDocumentListener(dataChangedListener);
            m_laboratoryTF.getDocument().addDocumentListener(dataChangedListener);
            m_addressTF.getDocument().addDocumentListener(dataChangedListener);
            m_telTF.getDocument().addDocumentListener(dataChangedListener);
            m_emailTF.getDocument().addDocumentListener(dataChangedListener);
            m_emailTeamManagerTF.getDocument().addDocumentListener(dataChangedListener);


            m_studyRefButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SearchStudiesRefDialog dialog = new SearchStudiesRefDialog(MainFrame.getMainWindow(), m_studyTF.getText().trim(), parentDialog.getAnalysesListPanel().getTakenStudyRefSet());
                    dialog.centerToWindow(MainFrame.getMainWindow());
                    dialog.setVisible(true);

                    if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        m_studyTF.setText(dialog.getStudyRef());
                    }
                }
            });
        }

        // -------------- Place Widgets

        // --- Request Date
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Study Reference:", SwingConstants.RIGHT), c);

        Component[] components = { m_studyTF, m_studyRefButton, Box.createHorizontalGlue()};

        c.gridx++;
        c.weightx = 1;
        add(new FlatPanel(components), c);
        c.weightx = 0;


        // --- Request Date
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Request Date:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_requestDateTF, c);
        c.weightx = 0;

        // --- Customer Name
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Customer Name:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_customerNameTF, c);
        c.weightx = 0;

        // --- Laboratory
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Laboratory:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_laboratoryTF, c);
        c.weightx = 0;

        // --- Address
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Address:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_addressTF, c);
        c.weightx = 0;

        // --- Tel
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Tel:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_telTF, c);
        c.weightx = 0;


        // --- Email
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Email:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_emailTF, c);
        c.weightx = 0;

        // --- Email
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Email Team Manager:", SwingConstants.RIGHT), c);

        c.gridx++;
        c.weightx = 1;
        add(m_emailTeamManagerTF, c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        add(Box.createVerticalGlue(), c);

    }

    public String getStudyRef() {
        return m_studyTF.getText().trim();
    }

    public JTextField getStudyTF() {
        return m_studyTF;
    }

    @Override
    public void loadData(ProAnalysisJson proAnalysisJson, String studyRef, HashMap<String, String> valueMap) {

        if (studyRef == null) {
            studyRef = "";
        }
        m_studyTF.setText(studyRef);

        if (valueMap == null) {
            Format FORMAT = new SimpleDateFormat("yyyy-MM-dd");
            String requestDate = FORMAT.format(new Date(((long)proAnalysisJson.getAnalyseDateDepotDemande())*1000l));
            m_requestDateTF.setText(requestDate);

            m_customerNameTF.setText(proAnalysisJson.getCustomer());
            m_laboratoryTF.setText(proAnalysisJson.getLaboratory());
            m_addressTF.setText(proAnalysisJson.getAddress());
            m_telTF.setText(proAnalysisJson.getTelFixe());
            m_emailTF.setText(proAnalysisJson.getMail());
            m_emailTeamManagerTF.setText(proAnalysisJson.getManagerMail());
        } else {

            setText(m_requestDateTF, valueMap.get(REQUEST_DATE));
            setText(m_customerNameTF, valueMap.get(CUSTOMER_NAME));
            setText(m_laboratoryTF, valueMap.get(LABORATORY));
            setText(m_addressTF, valueMap.get(ADDRESS));
            setText(m_telTF, valueMap.get(TEL));
            setText(m_emailTF, valueMap.get(EMAIL));
            setText(m_emailTeamManagerTF, valueMap.get(EMAIL_TEAM_MANAGER));

        }
    }

    public static void prefillData(ProAnalysisJson proAnalysisJson, HashMap<String, String> valueMap) {
        Format FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        String requestDate = FORMAT.format(new Date(((long)proAnalysisJson.getAnalyseDateDepotDemande())*1000l));

        valueMap.put(REQUEST_DATE, requestDate);
        valueMap.put(CUSTOMER_NAME, proAnalysisJson.getCustomer());
        valueMap.put(LABORATORY, proAnalysisJson.getLaboratory());
        valueMap.put(ADDRESS, proAnalysisJson.getAddress());
        valueMap.put(TEL, proAnalysisJson.getTelFixe());
        valueMap.put(EMAIL, proAnalysisJson.getMail());
        valueMap.put(EMAIL_TEAM_MANAGER, proAnalysisJson.getManagerMail());



    }

    public void importData(HashMap<String, String> valueImportMap) {

    }

    public boolean checkFields(ModifyAnalyseRequestDialog dialog, JPanel parentPanel, CardLayout cardLayout, String identifierInCardLayout) {

        String studyRef = getStudyRef();
        if (studyRef.isEmpty()) {
            cardLayout.show(parentPanel, identifierInCardLayout);
            dialog.highlight(m_studyTF);
            dialog.setStatus(true, "You must fill the Study Reference.");
            return false;
        }
        StudyJson study = DataManager.getStudyByNomenclature(studyRef);
        if (study == null) {
            cardLayout.show(parentPanel, identifierInCardLayout);
            dialog.highlight(m_studyTF);
            QuestionDialog questionDialog = new QuestionDialog(dialog, InfoDialog.InfoType.WARNING, "Study not Found", "Study Reference does not correspond to an already existing study. Do you want to proceed ?");

            questionDialog.centerToWindow(MainFrame.getMainWindow());
            questionDialog.setVisible(true);
            if (questionDialog.getButtonClicked() != DefaultDialog.BUTTON_OK) {
                return false;
            }
        }


        return true;
    }

    @Override
    public void getTagMap(HashMap<String, String> data) {

        data.put(STUDY_REF, m_studyTF.getText().trim());
        data.put(REQUEST_DATE, m_requestDateTF.getText().trim());
        data.put(CUSTOMER_NAME, m_customerNameTF.getText().trim());
        data.put(LABORATORY, m_laboratoryTF.getText().trim());
        data.put(ADDRESS, m_addressTF.getText().trim());
        data.put(TEL, m_telTF.getText().trim());
        data.put(EMAIL, m_emailTF.getText().trim());
        data.put(EMAIL_TEAM_MANAGER, m_emailTeamManagerTF.getText().trim());

    }
}
