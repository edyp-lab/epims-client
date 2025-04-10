package fr.epims.ui.analyserequest.panels;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.edyp.epims.json.AnalyseProgressJson;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.epims.MainFrame;
import fr.epims.ui.analyserequest.dialogs.ModifyProgressAnalyseDialog;
import fr.epims.ui.common.FlatButton;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.dialogs.DatePickerDialog;
import fr.epims.util.UtilDate;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ModifyProgressPanel extends JPanel {


    public static final String TYPE = "TYPE";
    public static final String ACCEPTATION_DATE = "ACCEPTATION_DATE";
    public static final String SAMPLES_NUMBER = "SAMPLES_NUMBER";
    public static final String RECEPTION_DATE = "RECEPTION_DATE";
    public static final String ROBOT_END_DATE = "ROBOT_END_DATE";
    public static final String INJECTIONS_NUMBER = "INJECTIONS_NUMBER";
    public static final String LAST_ACQUISITION_DATE = "LAST_ACQUISITION_DATE";
    public static final String REPORT_DATE = "REPORT_DATE";
    public static final String ANNOUNCED_DEADLINE = "ANNOUNCED_DEADLINE";
    public static final String RESPECTED_DEADLINE = "RESPECTED_DEADLINE";
    public static final String ORDER_FORM_DATE = "ORDER_FORM_DATE";
    public static final String BILLING_DONE = "BILLING_DONE";
    public static final String SEND_TO_ACCOUNT_MANAGER = "SEND_TO_ACCOUNT_MANAGER";
    public static final String RECOVERY_DATE = "RECOVERY_DATE";
    public static final String BILLING_COMMENTS = "BILLING_COMMENTS";

    public static final String[] ALL_KEYS = {TYPE, ACCEPTATION_DATE, SAMPLES_NUMBER, RECEPTION_DATE, ROBOT_END_DATE, INJECTIONS_NUMBER, LAST_ACQUISITION_DATE,
            REPORT_DATE, ANNOUNCED_DEADLINE, RESPECTED_DEADLINE, ORDER_FORM_DATE, BILLING_DONE, SEND_TO_ACCOUNT_MANAGER, RECOVERY_DATE, BILLING_COMMENTS};

    private JComboBox<String> m_typeCombobox;
    private JFormattedTextField m_acceptationDateTextField;
    private JTextField m_samplesNumberTextField;
    private JFormattedTextField m_receptionDateTextField;
    private JFormattedTextField m_robotEndDateTextField;
    private JTextField m_injectionsNumberTextField;
    private JFormattedTextField m_lastAcquisitionDateTextField;
    private JFormattedTextField m_announcedDeadlineTextField;
    private JFormattedTextField m_reportDateTextField;
    private JComboBox<String> m_respectedDeadlineCombobox;
    private JFormattedTextField m_orderFormDateTextField;
    private JComboBox<String> m_billingDoneCombobox;
    private JFormattedTextField m_sendToAccountManagerDateTextField;
    private JFormattedTextField m_recoveryDateTextField;
    private JTextField m_billingCommentTextField;

    private AnalysisMapJson m_analysisMapJson;

    private static final DateFormat FORMAT = UtilDate.getDateFormat();

    public ModifyProgressPanel(AnalyseProgressJson analyseProgressJson, AnalysisMapJson analysisMapJson) {
        super(new GridBagLayout());

        m_analysisMapJson = analysisMapJson;

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;


        JPanel studyPanel = createStudySubPanel(analysisMapJson.getStudyRef());
        JPanel samplesPanel = createSamplesSubPanel();
        JPanel reportPanel = createReportSubPanel();
        JPanel billingPanel = createBillingSubPanel();

        c.gridx = 0;
        c.gridy = 0;
        add(studyPanel, c);

        c.gridy++;
        add(samplesPanel, c);

        c.gridy++;
        add(reportPanel, c);

        c.gridy++;
        add(billingPanel, c);


        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> map = null;
        if ((analysisMapJson != null) && (analysisMapJson.getData() != null)) {
            try {
                map = mapper.readValue(analysisMapJson.getData(), HashMap.class);
            } catch (Exception e) {
                LoggerFactory.getLogger("Epims.Client").debug("Unexpected exception", e);
            }
        }

        loadData(map, analyseProgressJson);
    }

    private JPanel createStudySubPanel(String studyRef) {
        JPanel panel = new JPanel(new GridBagLayout());
        Border titledBorder = BorderFactory.createTitledBorder(" Study "+studyRef);
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_typeCombobox = new JComboBox();
        m_typeCombobox.addItem(" < Select > ");
        m_typeCombobox.addItem(" Discovery ");
        m_typeCombobox.addItem(" PTMs ");
        m_typeCombobox.addItem(" TopDown ");

        m_acceptationDateTextField = new JFormattedTextField(FORMAT);

        addCombobox(panel, c, "Type:", m_typeCombobox);
        addDateField(panel, c, "Acceptation Date:", m_acceptationDateTextField);
        addSpacing(panel, c);

        return panel;
    }

    private JPanel createSamplesSubPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        Border titledBorder = BorderFactory.createTitledBorder(" Samples and Acquisitions ");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_samplesNumberTextField = new JTextField(10);
        m_receptionDateTextField = new JFormattedTextField(FORMAT);
        m_robotEndDateTextField = new JFormattedTextField(FORMAT);
        m_injectionsNumberTextField = new JTextField( 10);
        m_lastAcquisitionDateTextField = new JFormattedTextField(FORMAT);

        addTextField(panel, c, "Samples Number:", m_samplesNumberTextField);
        addDateField(panel, c, "Reception Date:", m_receptionDateTextField);
        addDateField(panel, c, "Robot End Date:", m_robotEndDateTextField);
        addTextField(panel, c, "Injections Number:", m_injectionsNumberTextField);
        addDateField(panel, c, "Last Acquisition Date:", m_lastAcquisitionDateTextField);
        addSpacing(panel, c);

        return panel;
    }

    private JPanel createReportSubPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        Border titledBorder = BorderFactory.createTitledBorder(" Report ");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_reportDateTextField = new JFormattedTextField(FORMAT);
        m_announcedDeadlineTextField = new JFormattedTextField(FORMAT);
        m_respectedDeadlineCombobox = new JComboBox();
        m_respectedDeadlineCombobox.addItem(" < Select > ");
        m_respectedDeadlineCombobox.addItem(" Yes ");
        m_respectedDeadlineCombobox.addItem(" No ");

        addDateField(panel, c, "Report Date:", m_reportDateTextField);
        addDateField(panel, c, "Announced Deadline:", m_announcedDeadlineTextField);
        addCombobox(panel, c, "Respected Deadline:", m_respectedDeadlineCombobox);
        addSpacing(panel, c);

        return panel;
    }

    private JPanel createBillingSubPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        Border titledBorder = BorderFactory.createTitledBorder(" Billing ");
        panel.setBorder(titledBorder);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;

        m_billingDoneCombobox = new JComboBox();
        m_billingDoneCombobox.addItem(" < Select > ");
        m_billingDoneCombobox.addItem(" Yes ");
        m_billingDoneCombobox.addItem(" No ");

        m_orderFormDateTextField = new JFormattedTextField(FORMAT);
        m_sendToAccountManagerDateTextField = new JFormattedTextField(FORMAT);
        m_recoveryDateTextField = new JFormattedTextField(FORMAT);
        m_billingCommentTextField = new JTextField( 10);

        addDateField(panel, c, "Order Form Date:", m_orderFormDateTextField);
        addCombobox(panel, c, "Billing Done:", m_billingDoneCombobox);
        addDateField(panel, c, "Account Manager Dispatch Date:", m_sendToAccountManagerDateTextField);
        addDateField(panel, c, "Recovery Date:", m_recoveryDateTextField);
        addTextField(panel, c, "Billing Comment:", m_billingCommentTextField);
        addSpacing(panel, c);

        return panel;
    }

    private void addSpacing(JPanel p, GridBagConstraints c) {
        c.gridy++;
        c.gridx = 0;
        p.add(Box.createHorizontalStrut(150), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(150), c);
        c.gridx++;
        p.add(Box.createHorizontalGlue(), c);

    }

    private void addCombobox(JPanel p, GridBagConstraints c, String label, JComboBox combobox) {
        c.gridy++;
        c.gridx = 0;
        p.add(new JLabel(label, JLabel.TRAILING), c);
        c.gridx++;
        p.add(combobox, c);
    }
    private void addTextField(JPanel p, GridBagConstraints c, String label, JTextField textField) {

        c.gridy++;
        c.gridx = 0;

        p.add(new JLabel(label, JLabel.TRAILING), c);
        c.gridx++;
        p.add(textField, c);


    }

    private void addDateField(JPanel p, GridBagConstraints c, String label, JFormattedTextField dateTextField) {

        dateTextField.setText("yyyy-mm-dd");

        dateTextField.setColumns(10);
        dateTextField.setPreferredSize(m_acceptationDateTextField.getPreferredSize());

        FlatButton dateButton = new FlatButton(IconManager.getIcon(IconManager.IconType.CALENDAR), false);

        dateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = UtilDate.convertToDateWithoutHour(dateTextField.getText());
                if (d == null) {
                    d = new Date();
                }
                DatePickerDialog dialog = new DatePickerDialog(MainFrame.getMainWindow(), "Pick Date", d);

                dialog.setLocation(dateButton.getLocationOnScreen().x+dateButton.getWidth()/2, dateButton.getLocationOnScreen().y+dateButton.getHeight()/2);
                dialog.setVisible(true);

                Date selectedDate = dialog.getSelectedDate();
                if (selectedDate != null) {
                    DateFormat format = UtilDate.getDateFormat();
                    dateTextField.setText(format.format(selectedDate));
                }

            }
        });


        c.gridy++;
        c.gridx = 0;

        p.add(new JLabel(label, JLabel.TRAILING), c);

        c.gridx++;
        p.add(dateTextField, c);

        c.gridx++;
        p.add(dateButton, c);

        c.gridx++;
        p.add(Box.createGlue(), c);
    }

    public void loadData(HashMap<String, String> valueMap, AnalyseProgressJson analyseProgressJson) {

        DateFormat format = UtilDate.getDateFormat();

        if (valueMap == null) {
            valueMap = new HashMap<>();
        }

        String value = valueMap.get(TYPE);
        if (value != null) {
            m_typeCombobox.setSelectedItem(value);
        } else {
            m_typeCombobox.setSelectedIndex(0);
        }


        fillDateText(m_acceptationDateTextField, valueMap.get(ACCEPTATION_DATE));
        m_samplesNumberTextField.setText(valueMap.get(SAMPLES_NUMBER));


        fillDateText(m_receptionDateTextField, valueMap.get(RECEPTION_DATE));


        value = valueMap.get(ROBOT_END_DATE);
        if (value == null) {
            Date d = analyseProgressJson.getLastRobotDate();
            if (d != null) {
                value = format.format(d);
            } else {
                value = "";
            }
        }
        fillDateText(m_robotEndDateTextField, value);


        value = valueMap.get(INJECTIONS_NUMBER);
        if (value == null) {
            value = String.valueOf(analyseProgressJson.getAcquisitionsNumber());
        }
        m_injectionsNumberTextField.setText(value);

        value = valueMap.get(LAST_ACQUISITION_DATE);
        if (value == null) {
            Date d = analyseProgressJson.getLastAcquisitionDate();
            if (d != null) {
                value = format.format(d);
            } else {
                value = "";
            }
        }
        fillDateText(m_lastAcquisitionDateTextField, value);



        fillDateText(m_reportDateTextField, valueMap.get(REPORT_DATE));

        value = valueMap.get(RESPECTED_DEADLINE);
        if (value != null) {
            m_respectedDeadlineCombobox.setSelectedItem(value);
        } else {
            m_respectedDeadlineCombobox.setSelectedIndex(0);
        }

        fillDateText(m_announcedDeadlineTextField, valueMap.get(ANNOUNCED_DEADLINE));


        fillDateText(m_orderFormDateTextField, valueMap.get(ORDER_FORM_DATE));

        value = valueMap.get(BILLING_DONE);
        if (value != null) {
            m_billingDoneCombobox.setSelectedItem(value);
        } else {
            m_billingDoneCombobox.setSelectedIndex(0);
        }


        fillDateText(m_sendToAccountManagerDateTextField, valueMap.get(SEND_TO_ACCOUNT_MANAGER));
        fillDateText(m_recoveryDateTextField, valueMap.get(RECOVERY_DATE));

        value = valueMap.get(BILLING_COMMENTS);
        if (value == null) {
            value = "";
        }
        m_billingCommentTextField.setText(value.trim());


    }

    private static void fillDateText(JFormattedTextField dateTextField, String value) {
        if ((value == null) || (value.isEmpty())) {
            value = "yyyy-mm-dd";
        }
        dateTextField.setText(value);
    }

    public HashMap<String, String> getTagMap() {

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> data = null;
        if ((m_analysisMapJson != null) && (m_analysisMapJson.getData() != null)) {
            try {
                data = mapper.readValue(m_analysisMapJson.getData(), HashMap.class);
            } catch (Exception e) {
                LoggerFactory.getLogger("Epims.Client").debug("Unexpected exception", e);
            }
        }
        if (data == null) {
            data = new HashMap<>();
        }

        data.put(TYPE, getComboTextToSave(m_typeCombobox));
        data.put(ACCEPTATION_DATE, getDateTextToSave(m_acceptationDateTextField));
        data.put(SAMPLES_NUMBER, m_samplesNumberTextField.getText().trim());
        data.put(RECEPTION_DATE, getDateTextToSave(m_receptionDateTextField));
        data.put(ROBOT_END_DATE, getDateTextToSave(m_robotEndDateTextField));
        data.put(INJECTIONS_NUMBER, m_injectionsNumberTextField.getText().trim());
        data.put(LAST_ACQUISITION_DATE, getDateTextToSave(m_lastAcquisitionDateTextField));
        data.put(REPORT_DATE, getDateTextToSave(m_reportDateTextField));
        data.put(ANNOUNCED_DEADLINE, getDateTextToSave(m_announcedDeadlineTextField));
        data.put(RESPECTED_DEADLINE, getComboTextToSave(m_respectedDeadlineCombobox));
        data.put(ORDER_FORM_DATE, getDateTextToSave(m_orderFormDateTextField));
        data.put(BILLING_DONE, getComboTextToSave(m_billingDoneCombobox));
        data.put(SEND_TO_ACCOUNT_MANAGER, getDateTextToSave(m_sendToAccountManagerDateTextField));
        data.put(RECOVERY_DATE, getDateTextToSave(m_recoveryDateTextField));
        data.put(BILLING_COMMENTS, m_billingCommentTextField.getText().trim());

        return data;
    }

    private static String getComboTextToSave(JComboBox<String> c) {
        if (c.getSelectedIndex() == 0) {
            return "";
        }
        return c.getSelectedItem().toString();
    }

    private static String getDateTextToSave(JFormattedTextField dateTextField) {
        String value = dateTextField.getText().trim();
        if (value.compareTo("yyyy-mm-dd") == 0) {
            value = "";
        }
        return value;
    }

    public boolean checkFields(ModifyProgressAnalyseDialog dialog) {

        if (!checkDate(m_acceptationDateTextField)) {
            dialog.highlight(m_acceptationDateTextField);
            dialog.setStatus(true, "Incorrect format for Acceptation Date.");
            return false;
        }

        if (! m_samplesNumberTextField.getText().isEmpty()) {
            try {
                Integer.parseInt(m_samplesNumberTextField.getText().trim());
            } catch (Exception e) {
                dialog.highlight(m_samplesNumberTextField);
                dialog.setStatus(true, "Incorrect format for Samples Number.");
                return false;
            }
        }


        if (!checkDate(m_receptionDateTextField)) {
            dialog.highlight(m_receptionDateTextField);
            dialog.setStatus(true, "Incorrect format for Reception Date.");
            return false;
        }

        if (!checkDate(m_robotEndDateTextField)) {
            dialog.highlight(m_robotEndDateTextField);
            dialog.setStatus(true, "Incorrect format for End Robot Date.");
            return false;
        }

        //private JTextField m_injectionsNumberTextField;
        if (! m_injectionsNumberTextField.getText().isEmpty()) {
            try {
                Integer.parseInt(m_injectionsNumberTextField.getText().trim());
            } catch (Exception e) {
                dialog.highlight(m_injectionsNumberTextField);
                dialog.setStatus(true, "Incorrect format for Injections Number.");
                return false;
            }
        }

        if (!checkDate(m_lastAcquisitionDateTextField)) {
            dialog.highlight(m_lastAcquisitionDateTextField);
            dialog.setStatus(true, "Incorrect format for Last Acquisition Date.");
            return false;
        }

        if (!checkDate(m_reportDateTextField)) {
            dialog.highlight(m_reportDateTextField);
            dialog.setStatus(true, "Incorrect format for Report Date.");
            return false;
        }

        if (!checkDate(m_announcedDeadlineTextField)) {
            dialog.highlight(m_announcedDeadlineTextField);
            dialog.setStatus(true, "Incorrect format for Announced Deadline.");
            return false;
        }

        if (!checkDate(m_orderFormDateTextField)) {
            dialog.highlight(m_orderFormDateTextField);
            dialog.setStatus(true, "Incorrect format for Order Form Date.");
            return false;
        }

        if (!checkDate(m_sendToAccountManagerDateTextField)) {
            dialog.highlight(m_sendToAccountManagerDateTextField);
            dialog.setStatus(true, "Incorrect format for Account Manager Dispatch Date.");
            return false;
        }

        if (!checkDate(m_recoveryDateTextField)) {
            dialog.highlight(m_recoveryDateTextField);
            dialog.setStatus(true, "Incorrect format for Recovery Date.");
            return false;
        }

        return true;
    }

    private boolean checkDate(JFormattedTextField dateTextField) {
        String value = dateTextField.getText().trim();
        if ((value.compareTo("yyyy-mm-dd") != 0) && (!value.isEmpty())) {
            Date d = UtilDate.convertToDateWithoutHour(value);
            if (d == null) {
                return false;
            }
        }
        return true;
    }
}
