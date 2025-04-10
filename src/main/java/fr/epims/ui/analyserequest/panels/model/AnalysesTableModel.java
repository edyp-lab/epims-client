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
import fr.epims.ui.analyserequest.panels.AnalysisRequestStep1Part1Panel;
import fr.epims.ui.common.DecoratedTableModelInterface;
import org.slf4j.LoggerFactory;


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
 * Model of the Table displayed in the AnalysesListPanel
 *
 * @author JM235353
 *
 */
public class AnalysesTableModel extends AbstractTableModel implements DecoratedTableModelInterface {


    public static final int COLTYPE_REF_STUDY = 0;
    public static final int COLTYPE_CUSTOMER = 1;
    public static final int COLTYPE_LABORATORY = 2;
    public static final int COLTYPE_INTITULE = 3;
    public static final int COLTYPE_REQUEST_DATE = 4;
    public static final int COLTYPE_EDYP_RESPONSIBLE = 5;
    public static final int COLTYPE_SAVE_DATE = 6;
    public static final int COLTYPE_EXPORT_DATE = 7;


    private static final Format FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    private static final String[] m_columnNames = {"Reference", "Customer", "Laboratory", "Analyse Title", "Request Date", "EDyP Responsible", "Save Date", "Export Date"};
    private static final String[] m_columnTooltips = {"Study Reference", "Customer", "Laboratory", "Analyse Title", "Request Date", "EDyP Responsible", "Save Date", "Export to .docx Date"};

    private List<ProAnalysisJson> m_analysesAll;
    private List<ProAnalysisJson> m_analysesFiltered;
    private HashMap<Integer, AnalysisMapJson> m_analysisMap = new HashMap<>();
    private HashMap<Integer, HashMap<String, String>> m_analysisKey2ValueMap = new HashMap<>();

    private boolean m_selectionOnly;

    public AnalysesTableModel(boolean selectionOnly) {

        m_analysesAll = new ArrayList<>();
        m_analysesFiltered = new ArrayList<>();

        m_selectionOnly = selectionOnly;

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

    public boolean canBeEdited(int row, String login) {
        ProAnalysisJson pa = m_analysesFiltered.get(row);
        return (pa.getUserLogin().equals(login));
    }

    public String getStudyRef(int row) {
        ProAnalysisJson p = m_analysesFiltered.get(row);
        AnalysisMapJson a = m_analysisMap.get(p.getAnalyseId());
        if (a != null) {
            return a.getStudyRef();
        }
        return "";
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

    public void exportDone(int analysisId, Date exortDate) {
        AnalysisMapJson analysisMapJson = m_analysisMap.get(analysisId);
        analysisMapJson.setExportDate(exortDate);
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        if (m_selectionOnly) {
            // for import selection, we don't display the last two columns
            return m_columnNames.length - 2;
        }
        return m_columnNames.length;

    }

    @Override
    public String getColumnName(int col) {

        return m_columnNames[col];
    }

    public String getToolTipForHeader(int col) {
        return m_columnTooltips[col];
    }

    @Override
    public TableCellEditor getEditor(int row, int col) {
        return null;
    }

    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public Class getColumnClass(int col) {
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
            case COLTYPE_REF_STUDY: {
                AnalysisMapJson a = m_analysisMap.get(p.getAnalyseId());
                if (a != null) {
                    return a.getStudyRef();
                }
                return "";
            }
            case COLTYPE_CUSTOMER: {
                HashMap<String, String> map = m_analysisKey2ValueMap.get(p.getAnalyseId());
                if (map != null) {
                    return map.get(AnalysisRequestStep1Part1Panel.CUSTOMER_NAME);
                }
                return p.getCustomer();
            }
            case COLTYPE_LABORATORY: {
                HashMap<String, String> map = m_analysisKey2ValueMap.get(p.getAnalyseId());
                if (map != null) {
                    return map.get(AnalysisRequestStep1Part1Panel.LABORATORY);
                }
                return p.getLaboratory();
            }
            case COLTYPE_INTITULE: {
                return p.getAnalyseIntitule();
            }
            case COLTYPE_REQUEST_DATE: {
                return FORMAT.format(new Date(((long) p.getAnalyseDateDepotDemande()) * 1000l));
            }
            case COLTYPE_EDYP_RESPONSIBLE: {
                return p.getUserFullname();
            }
            case COLTYPE_SAVE_DATE: {
                AnalysisMapJson a = m_analysisMap.get(p.getAnalyseId());
                if (a != null) {
                    Date d = a.getSaveDate();
                    if (d != null) {
                        return FORMAT.format(d);
                    }
                }
                return "";
            }
            case COLTYPE_EXPORT_DATE: {
                AnalysisMapJson a = m_analysisMap.get(p.getAnalyseId());
                if (a != null) {
                    Date d = a.getExportDate();
                    if (d != null) {
                        return FORMAT.format(d);
                    }
                }
                return "";
            }
        }


        return null; // should never happen

    }

    public TableCellRenderer getRenderer(int row, int col) {

        /*if (col == 0) {
            return new ViewTableCellRenderer(m_callback, 0);
        }*/

        return null;
    }




}
