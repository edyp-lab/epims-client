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

package fr.epims.ui.analyserequest.panels.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.edyp.epims.json.AnalysisMapJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.ui.analyserequest.panels.AnalysisRequestStep1Part2Panel;
import fr.epims.ui.analyserequest.panels.AnalysisRequestStep2Part3Panel;
import fr.epims.ui.analyserequest.panels.ModifyProgressPanel;
import fr.epims.ui.common.DecoratedTableModelInterface;
import fr.epims.ui.renderers.FloatRenderer;
import fr.epims.ui.renderers.StatusRenderer;
import org.slf4j.LoggerFactory;


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Model of the Table used to export data to Suivi D'analyses excel file
 *
 * @author JM235353
 *
 */
public class ProgressAnalysesTableModel extends AbstractTableModel implements DecoratedTableModelInterface {

    public static final int COLTYPE_STATUS = 0;
    public static final int COLTYPE_DATE = 1;
    public static final int COLTYPE_REF_STUDY = 2;
    public static final int COLTYPE_CUSTOMER_MAIL = 3;
    public static final int COLTYPE_MANAGER_MAIL = 4;
    public static final int COLTYPE_EDYP_RESPONSIBLE = 5;
    public static final int COLTYPE_TYPE = 6;

    public static final int COLTYPE_PRICE = 7;
    public static final int COLTYPE_NON_COLLABORATIVE = 8;
    public static final int COLTYPE_ACCEPTATION_DATE = 9;
    public static final int COLTYPE_SAMPLES_NUMBER = 10;
    public static final int COLTYPE_RECEPTION_DATE = 11;

    public static final int COLTYPE_ROBOT_END_DATE = 12;
    public static final int COLTYPE_INJECTIONS_NUMBER = 13;
    public static final int COLTYPE_LAST_ACQUISITION_DATE = 14;
    public static final int COLTYPE_REPORT_DATE = 15;
    public static final int COLTYPE_ANNOUNCED_DEADLINE = 16;

    public static final int COLTYPE_RESPECTED_DEADLINE = 17;
    public static final int COLTYPE_ORDER_FORM_DATE = 18;
    public static final int COLTYPE_BILLING_DONE = 19;
    public static final int COLTYPE_SEND_TO_ACCOUNT_MANAGER = 20;
    public static final int COLTYPE_RECOVERY_DATE = 21;

    public static final int COLTYPE_BILLING_COMMENTS = 22;

    public enum STATUS {
        STATUS_IN_PROGRESS,
        STATUS_MISSING_ORDER_FORM,
        STATUS_COMPLETED,
        STATUS_READY_TO_INVOICE
    }

    private static final Format FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String[] m_columnNames = {"Status", "Request Date", "Reference", "Customer Mail", "Manager Mail", "EDyP Responsible", "Type",
            "Price", "Non Collaborative", "Acceptation Date", "Samples Number", "Reception Date",
            "Robot End Date", "Injections Number", "Last Acquisition Date", "Report Date", "Announced Deadline Date",
            "Respected Deadline", "Order Form Date", "Billing Done", "Account Manager Dispatch Date", "Recovery Date", "Comments"};

    private static final String[] m_columnTooltips =  {"Status", "Request Date", "Reference", "Customer Mail", "Manager Mail", "EDyP Responsible", "Type",
            "Price", "Non Collaborative", "Acceptation Date", "Samples Number", "Reception Date",
            "Robot End Date", "Injections Number", "Last Acquisition Date", "Report Date", "Announced Deadline Date",
            "Respected Deadline", "Order Form Date", "Billing Done", "Account Manager Dispatch Date", "Recovery Date", "Billing Comments"};

    private List<ProAnalysisJson> m_analysesAll;
    private List<ProAnalysisJson> m_analysesFiltered;
    private HashMap<Integer, AnalysisMapJson> m_analysisMap = new HashMap<>();
    private HashMap<Integer, HashMap<String, String>> m_analysisKey2ValueMap = new HashMap<>();

    private FloatRenderer m_floatRenderer;
    private StatusRenderer m_statusRenderer;


    public ProgressAnalysesTableModel(JTable t) {

        m_analysesAll = new ArrayList<>();
        m_analysesFiltered = new ArrayList<>();

        m_floatRenderer = new FloatRenderer(t.getDefaultRenderer(String.class), 2, false);
        m_statusRenderer = new StatusRenderer(t.getDefaultRenderer(String.class));
    }

    public void filter(boolean filter, String userLogging) {
        if (filter) {
            m_analysesFiltered = new ArrayList<>();
            for (ProAnalysisJson proAnalysisJson : m_analysesAll) {
                if (proAnalysisJson.getUserLogin().equals(userLogging)) {
                    m_analysesFiltered.add(proAnalysisJson);
                }
            }
        } else {
            m_analysesFiltered = m_analysesAll;
        }

        fireTableDataChanged();
    }

    public void setAnalyses(ArrayList<ProAnalysisJson> analyses, ArrayList<AnalysisMapJson> analysesMaps, boolean filterLogging, String userLogging, boolean filterSavedAnalysis) {
        m_analysesAll = analyses;


        ObjectMapper mapper = new ObjectMapper();

        m_analysisMap.clear();

        try {
            for (AnalysisMapJson a : analysesMaps) {
                m_analysisMap.put(a.getProAnalyseId(), a);

                HashMap map = mapper.readValue(a.getData(), HashMap.class);
                m_analysisKey2ValueMap.put(a.getProAnalyseId(), map);


            }
        } catch (Exception e) {
            LoggerFactory.getLogger("Epims.Client").debug("Unexpected exception", e);
        }



        if (filterLogging || filterSavedAnalysis) {
            m_analysesFiltered = new ArrayList<>();
            for (ProAnalysisJson proAnalysisJson : analyses) {
                boolean filterPass = true;
                if ((filterLogging) && (!proAnalysisJson.getUserLogin().equals(userLogging))) {
                    filterPass = false;
                } else if ((filterSavedAnalysis) && (m_analysisKey2ValueMap.get(proAnalysisJson.getAnalyseId()) == null)) {
                    filterPass = false;
                }
                if (filterPass) {
                    m_analysesFiltered.add(proAnalysisJson);
                }
            }
        } else {
            m_analysesFiltered = m_analysesAll;
        }



        fireTableDataChanged();
    }


    public ProAnalysisJson getAnalyse(int row) {
        return m_analysesFiltered.get(row);
    }

    public String getStudyRef(int row) {
        ProAnalysisJson p = m_analysesFiltered.get(row);
        AnalysisMapJson a = m_analysisMap.get(p.getAnalyseId());
        if (a != null) {
            return a.getStudyRef();
        }
        return null;
    }

    public boolean canBeEdited(int row, String login) {
        ProAnalysisJson pa = m_analysesFiltered.get(row);
        return (pa.getUserLogin().equals(login));
    }



    public void modificationDone(ProAnalysisJson proAnalysisJson, AnalysisMapJson analysisMapJson) {
        m_analysisMap.put(proAnalysisJson.getAnalyseId(), analysisMapJson);

        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap map = mapper.readValue(analysisMapJson.getData(), HashMap.class);
            m_analysisKey2ValueMap.put(analysisMapJson.getProAnalyseId(), map);
        } catch (Exception e) {
            LoggerFactory.getLogger("Epims.Client").debug("Unexpected exception", e);
        }
        fireTableDataChanged();
    }

    @Override
    public TableCellEditor getEditor(int row, int col) {
        return null;
    }

    @Override
    public int getColumnCount() {

        return m_columnNames.length;

    }

    @Override
    public String getColumnName(int col) {

        return m_columnNames[col];
    }

    public String getToolTipForHeader(int col) {
        return m_columnTooltips[col];
    }


    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {

        if (col == COLTYPE_PRICE) {
            return Float.class;
        }
        if (col == COLTYPE_STATUS) {
            return STATUS.class;
        }
        return String.class;
    }



    @Override
    public int getRowCount() {
        return m_analysesFiltered.size();
    }



    @Override
    public Object getValueAt(int row, int col) {

        ProAnalysisJson p = m_analysesFiltered.get(row);

        switch (col) {
            case COLTYPE_STATUS: {
                boolean reportDate = ! ((String)getValueAt(row, COLTYPE_REPORT_DATE)).isEmpty();
                boolean orderFormDate = ! ((String)getValueAt(row, COLTYPE_ORDER_FORM_DATE)).isEmpty();
                boolean accountManagerDispatchDate = ! ((String)getValueAt(row, COLTYPE_SEND_TO_ACCOUNT_MANAGER)).isEmpty();
                boolean billingDone = ((String)getValueAt(row, COLTYPE_BILLING_DONE)).equals("Yes");


                boolean readyToInvoice = reportDate && orderFormDate;
                boolean completed = reportDate && ((orderFormDate && accountManagerDispatchDate) || billingDone);
                boolean missingOrderForm = reportDate && !orderFormDate && !billingDone;

                if (missingOrderForm) {
                    return STATUS.STATUS_MISSING_ORDER_FORM;
                } else if (completed) {
                    return STATUS.STATUS_COMPLETED;
                } else if (readyToInvoice) {
                    return STATUS.STATUS_READY_TO_INVOICE;
                } else {
                    return STATUS.STATUS_IN_PROGRESS;
                }
            }
            case COLTYPE_DATE: {
                return FORMAT.format(new Date(((long) p.getAnalyseDateDepotDemande()) * 1000l));
            }
            case COLTYPE_REF_STUDY: {
                AnalysisMapJson a = m_analysisMap.get(p.getAnalyseId());
                if (a != null) {
                    return a.getStudyRef();
                }
                return "";
            }
            case COLTYPE_CUSTOMER_MAIL: {
                return p.getMail();
            }
            case COLTYPE_MANAGER_MAIL: {
                return p.getManagerMail();
            }
            case COLTYPE_EDYP_RESPONSIBLE: {
                return p.getUserFullname();
            }
            case COLTYPE_TYPE: {
                return getValue(p, ModifyProgressPanel.TYPE);
            }
            case COLTYPE_PRICE: {
                return getPrice(p);
            }
            case COLTYPE_NON_COLLABORATIVE: {
                return getNonCollaborative(p);
            }
            case COLTYPE_ACCEPTATION_DATE: {
                return getValue(p, ModifyProgressPanel.ACCEPTATION_DATE);
            }
            case COLTYPE_SAMPLES_NUMBER: {
                return getValue(p, ModifyProgressPanel.SAMPLES_NUMBER);
            }
            case COLTYPE_RECEPTION_DATE: {
                return getValue(p, ModifyProgressPanel.RECEPTION_DATE);
            }
            case COLTYPE_ROBOT_END_DATE: {
                return getValue(p, ModifyProgressPanel.ROBOT_END_DATE);
            }
            case COLTYPE_INJECTIONS_NUMBER: {
                return getValue(p, ModifyProgressPanel.INJECTIONS_NUMBER);
            }
            case COLTYPE_LAST_ACQUISITION_DATE: {
                return getValue(p, ModifyProgressPanel.LAST_ACQUISITION_DATE);
            }
            case COLTYPE_REPORT_DATE: {
                return getValue(p, ModifyProgressPanel.REPORT_DATE);
            }
            case COLTYPE_ANNOUNCED_DEADLINE: {
                return getValue(p, ModifyProgressPanel.ANNOUNCED_DEADLINE);
            }
            case COLTYPE_RESPECTED_DEADLINE: {
                return getValue(p, ModifyProgressPanel.RESPECTED_DEADLINE);
            }
            case COLTYPE_ORDER_FORM_DATE: {
                return getValue(p, ModifyProgressPanel.ORDER_FORM_DATE);
            }
            case COLTYPE_BILLING_DONE: {
                return getValue(p, ModifyProgressPanel.BILLING_DONE);
            }
            case COLTYPE_SEND_TO_ACCOUNT_MANAGER: {
                return getValue(p, ModifyProgressPanel.SEND_TO_ACCOUNT_MANAGER);
            }
            case COLTYPE_RECOVERY_DATE: {
                return getValue(p, ModifyProgressPanel.RECOVERY_DATE);
            }
            case COLTYPE_BILLING_COMMENTS: {
                return getValue(p, ModifyProgressPanel.BILLING_COMMENTS);
            }
        }

        return null; // should never happen

    }


    public int getColumnPreferredSize(int col) {

        final int STATUS_COL_SIZE = 100;
        final int DATE_COL_SIZE = 140;
        final int INT_COL_SIZE = 80;
        final int MAIL_COL_SIZE = 160;
        final int RESPONSIBLE_COL_SIZE = 200;
        final int REF_STUDY_COL_SIZE = 80;
        final int SHORT_STRING_SIZE = 100;
        final int LONG_STRING_SIZE = 140;
        final int PRICE_SIZE = 80;

        switch (col) {
            case COLTYPE_STATUS:
                return STATUS_COL_SIZE;
            case COLTYPE_DATE:
            case COLTYPE_ACCEPTATION_DATE:
            case COLTYPE_RECEPTION_DATE:
            case COLTYPE_ROBOT_END_DATE:
            case COLTYPE_LAST_ACQUISITION_DATE:
            case COLTYPE_REPORT_DATE:
            case COLTYPE_ANNOUNCED_DEADLINE:
            case COLTYPE_ORDER_FORM_DATE:
            case COLTYPE_RECOVERY_DATE:
                return DATE_COL_SIZE;
            case COLTYPE_REF_STUDY:
                return REF_STUDY_COL_SIZE;
            case COLTYPE_CUSTOMER_MAIL:
            case COLTYPE_MANAGER_MAIL:
                return MAIL_COL_SIZE;

            case COLTYPE_EDYP_RESPONSIBLE:
                return RESPONSIBLE_COL_SIZE;
            case COLTYPE_TYPE:
            case COLTYPE_NON_COLLABORATIVE:
            case COLTYPE_RESPECTED_DEADLINE:
            case COLTYPE_BILLING_DONE:
            case COLTYPE_SEND_TO_ACCOUNT_MANAGER:
                return SHORT_STRING_SIZE;
            case COLTYPE_PRICE:
                return PRICE_SIZE;
            case COLTYPE_SAMPLES_NUMBER:
            case COLTYPE_INJECTIONS_NUMBER:
                return INT_COL_SIZE;
            case COLTYPE_BILLING_COMMENTS:
                return LONG_STRING_SIZE;
        }

        return SHORT_STRING_SIZE; // should not be called

    }

    private Float getPrice(ProAnalysisJson proAnalysisJson) {

        HashMap<String, String> valueMap = m_analysisKey2ValueMap.get(proAnalysisJson.getAnalyseId());

        if (valueMap == null) {
            return new Float(0);
        }

        float total = 0;
        int row = 0;
        while (true) {
            String key = String.valueOf(row);
            String analysis = valueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_ANALYSIS + key);
            if (analysis == null) {
                break;
            }
            Integer number = 0;
            try {
                number = Integer.valueOf(valueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_SAMPLES_NUMBER + key));
            } catch (Exception e) {

            }
            Float unitPrice = 0f;
            try {
                unitPrice = Float.valueOf(valueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_UNIT_PRICE + key));
            } catch (Exception e) {

            }
            Float price = 0f;
            try {
                price = Float.valueOf(valueMap.get(AnalysisRequestStep2Part3Panel.PRICE_TABLE_PRICE + key));
            } catch (Exception e) {

            }
            total += price;

            row++;
        }
        return new Float(total);
    }

    private String getValue(ProAnalysisJson proAnalysisJson, String key) {

        HashMap<String, String> valueMap = m_analysisKey2ValueMap.get(proAnalysisJson.getAnalyseId());

        if (valueMap == null) {
            return "";
        }

        String value = valueMap.get(key);
        if (value!=null) {
            return value;
        } else {
            return "";
        }
    }

    private String getNonCollaborative(ProAnalysisJson proAnalysisJson) {

        HashMap<String, String> valueMap = m_analysisKey2ValueMap.get(proAnalysisJson.getAnalyseId());

        if (valueMap == null) {
            return "";
        }

        String value = valueMap.get(AnalysisRequestStep1Part2Panel.SERVICE_TYPE_COLLABORATIVE);
        if ((value!=null) && (value.equals("true"))) {
            return "oui";
        } else {
            return "non";
        }
    }

    public void getExcelString(int row, StringBuilder sb) {
        for (int i=0;i<getColumnCount();i++) {

            if ((i == COLTYPE_DATE) || (i == COLTYPE_STATUS))  {
                continue;
            } else if (i == COLTYPE_EDYP_RESPONSIBLE) {
                String value = getValueAt(row, i).toString();
                for (char c : value.toCharArray()) {
                    if ((c>='A') && (c<='Z')) {
                        sb.append(c); // only initials of the responsible
                    }
                }
                sb.append("\t"); // add an empty column corresponding to "Type"
            } else {
                sb.append(getValueAt(row, i));
            }
            if (i == COLTYPE_RESPECTED_DEADLINE) {
                // add blank column
                sb.append("\t");
            }
            if (i != COLTYPE_BILLING_COMMENTS) {
                sb.append("\t");
            }
        }
        sb.append(System.lineSeparator());
    }

    public TableCellRenderer getRenderer(int row, int col) {
        if (col == COLTYPE_PRICE) {
            return m_floatRenderer;
        } else if (col == COLTYPE_STATUS) {
            return m_statusRenderer;
        }
        return null;
    }




}
