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

import fr.edyp.epims.json.AcquisitionStatisticJson;
import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AccessDatabaseThread;
import fr.epims.tasks.StatisticsTask;
import fr.epims.ui.common.HourGlassPanel;
import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.time.Year;
import java.util.*;

/**
 * Panel to display statistics from Proline Server or ePims Server
 */
public class StatisticsAnalysesPanel extends HourGlassPanel {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private static final String STYLE_BIG_BOLD = "style = 'text-align: center;font-family:calibri,arial,helvetica;font-size:20px;font-style:bold;'";
    private static final String STYLE_TITLE = "style = 'text-align: center;color:#ffffff;background:#0a99bd;font-family:calibri,arial,helvetica;font-size:14px;'";
    private static final String STYLE_LEFT_TEXT = "style = 'text-align: left;color:#000000;background:#ffffff;font-family:verdana,arial,helvetica;font-size:10px;font-style:bold;'";
    private static final String STYLE_RIGHT_TEXT = "style = 'text-align: right;color:#000000;background:#ffffff;font-family:verdana,arial,helvetica;font-size:10px;'";


    private ArrayList<ProAnalysisJson> m_analyses = null;
    JEditorPane m_pane = new JEditorPane();

    private String m_displayedHtml = "";

    private static final String PRE_ANALYSIS = "Pre Analysis Grenoble";
    private static final String POST_ANALYSIS = "Post Analysis Grenoble";
    private static final String ACQUISITIONS_STATISTICS = "Acquisitions Statistics";

    public StatisticsAnalysesPanel() {

        setLayout(new GridBagLayout());
        setBackground(Color.white);
        setBorder(BorderFactory.createTitledBorder(""));

        int yearNow = Year.now().getValue();
        JComboBox<Integer> yearCombobox = new JComboBox<>();
        for (int year=yearNow-2;year<=yearNow;year++) {
            yearCombobox.addItem(new Integer(year));
        }
        yearCombobox.setSelectedIndex(1); // we select previous year

        String[] types = { PRE_ANALYSIS, POST_ANALYSIS, ACQUISITIONS_STATISTICS};
        JComboBox<String> analysisTypeCombobox = new JComboBox<>(types);
        analysisTypeCombobox.setSelectedIndex(0);

        ActionListener selectionAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer year = (Integer) yearCombobox.getSelectedItem();
                generateHtmlForAnalysis(year, analysisTypeCombobox.getSelectedIndex());
            }
        };

        analysisTypeCombobox.addActionListener(selectionAction);
        yearCombobox.addActionListener(selectionAction);


        JButton copyForExcelButton = new JButton("Copy for Excel", IconManager.getIcon(IconManager.IconType.EXPORT));
        copyForExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
                CLIPBOARD.setContents(new HtmlSelection(m_displayedHtml), null);
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_pane.setEditable(false);
        m_pane.setPreferredSize(new Dimension(600, 600));
        m_pane.setContentType("text/html");
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(m_pane);

        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Select Year:", SwingConstants.RIGHT), c);

        c.gridx++;
        add(yearCombobox, c);

        c.gridx++;
        add(analysisTypeCombobox, c);

        c.gridx++;
        add(copyForExcelButton, c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);

        c.gridwidth = 5;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        add(scrollPane, c);
    }

    public void setAnalyses(ArrayList<ProAnalysisJson> analyses) {

        m_analyses = analyses;

        int yearNow = Year.now().getValue();
        generateHtmlForAnalysis(yearNow-1, 0);
    }

    private void generateAcquisitionsStatistic(final int year) {
        m_pane.setText("");

        setLoading(getNewLoadingIndex());

        final ArrayList<AcquisitionStatisticJson> acquisitionStatisticList = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {

                    StringBuilder htmlTable = new StringBuilder();
                    htmlTable.append("<html><table>");

                    // Indicateurs Généraux

                    htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td STYLE_TITLE>Nombre d'acquisitions</td><td STYLE_TITLE>Taille Acquisitions</td><td STYLE_TITLE>Nombre d'acquisitions Recherche</td><td STYLE_TITLE>Taille Acquisitions Recherche</td><td STYLE_TITLE>Semaine</td><td STYLE_TITLE>Année</td><td STYLE_TITLE>Instrument</td><td></td><td></td><td></td><td></td></tr>");

                    final String NB_TOTAL = "NB_TOTAL";
                    final String NB_TOTAL_SEMAINE = "NB_TOTAL_SEMAINE";
                    final String NB_RECHERCHE = "NB_RECHERCHE";
                    final String NB_RECHERCHE_SEMAINE = "NB_RECHERCHE_SEMAINE";
                    final String NB_CONTROLE = "NB_CONTROLE";
                    final String NB_CONTROLE_SEMAINE = "NB_CONTROLE_SEMAINE";
                    final String SIZE_TOTAL = "SIZE_TOTAL";
                    final String SIZE_RECHERCHE = "SIZE_RECHERCHE";
                    final String SIZE_CONTROLE = "SIZE_CONTROLE";
                    final String SIZE_TOTAL_SEMAINE = "SIZE_TOTAL_SEMAINE";
                    final String SIZE_RECHERCHE_SEMAINE = "SIZE_RECHERCHE_SEMAINE";
                    final String SIZE_CONTROLE_SEMAINE = "SIZE_CONTROLE_SEMAINE";


                    String previousInstrument = null;
                    int addNbAcquisitions = 0;
                    double addSizeTotal  = 0;
                    int addNbResearchAcquisitions = 0;
                    double addSizeResearchTotal = 0;
                    int i = 0;
                    for (AcquisitionStatisticJson AcquisitionStatistic : acquisitionStatisticList) {
                        String instrument = AcquisitionStatistic.getInstrument();
                        int nbAcquisitions = AcquisitionStatistic.getAcquisitionTotal();
                        double sizeTotal = AcquisitionStatistic.getAcquisitionSizeTotal();
                        int nbResearchAcquisitions = AcquisitionStatistic.getResearchAcquisitionTotal();
                        double sizeResearchTotal = AcquisitionStatistic.getResearchAcquisitionSizeTotal();
                        if ((previousInstrument != null) && (!instrument.equals(previousInstrument))) {
                            // replace values
                            String instrumentTable = htmlTable.toString();
                            instrumentTable = instrumentTable.replace(NB_TOTAL_SEMAINE, decimalFormat.format(addNbAcquisitions/52))
                            .replace(NB_TOTAL, String.valueOf(addNbAcquisitions))
                            .replace(NB_RECHERCHE_SEMAINE, decimalFormat.format(addNbResearchAcquisitions/52))
                            .replace(NB_RECHERCHE, String.valueOf(addNbResearchAcquisitions))
                            .replace(NB_CONTROLE_SEMAINE, decimalFormat.format((addNbAcquisitions-addNbResearchAcquisitions)/52))
                            .replace(NB_CONTROLE, String.valueOf(addNbAcquisitions-addNbResearchAcquisitions))
                            .replace(SIZE_TOTAL_SEMAINE, decimalFormat.format(addSizeTotal/52))
                            .replace(SIZE_TOTAL, decimalFormat.format(addSizeTotal))
                            .replace(SIZE_RECHERCHE_SEMAINE, decimalFormat.format(addSizeResearchTotal/52))
                            .replace(SIZE_RECHERCHE, decimalFormat.format(addSizeResearchTotal))
                            .replace(SIZE_CONTROLE_SEMAINE, decimalFormat.format((addSizeTotal-addSizeResearchTotal)/52))
                            .replace(SIZE_CONTROLE, decimalFormat.format(addSizeTotal-addSizeResearchTotal));

                            htmlTable.setLength(0);
                            htmlTable.append(instrumentTable);




                            // 2 Empty Lines
                            htmlTable.append("<tr><td></td></tr><tr><td></td></tr>");
                            addNbAcquisitions = 0;
                            addSizeTotal  = 0;
                            addNbResearchAcquisitions = 0;
                            addSizeResearchTotal = 0;
                            i = 0;
                        }
                        addNbAcquisitions += nbAcquisitions;
                        addSizeTotal += sizeTotal;
                        addNbResearchAcquisitions += nbResearchAcquisitions;
                        addSizeResearchTotal += sizeResearchTotal;

                        htmlTable.append("<tr STYLE_RIGHT_TEXT>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(nbAcquisitions).append("</td>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(decimalFormat.format(sizeTotal)).append("</td>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(nbResearchAcquisitions).append("</td>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(decimalFormat.format(sizeResearchTotal)).append("</td>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(AcquisitionStatistic.getWeek()).append("</td>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(AcquisitionStatistic.getYear()).append("</td>");
                        htmlTable.append("<td STYLE_RIGHT_TEXT>").append(instrument).append("</td>");
                        if (i == 1) {
                            htmlTable.append("<td>Nb Total</td><td>").append(NB_TOTAL).append("</nb><td>Nb Total par Semaine</td><td>").append(NB_TOTAL_SEMAINE).append("</td>");
                        } else if (i == 2) {
                            htmlTable.append("<td>Nb Recherche</td><td>").append(NB_RECHERCHE).append("</nb><td>Nb Recherche par Semaine</td><td>").append(NB_RECHERCHE_SEMAINE).append("</td>");
                        } else if (i == 3) {
                            htmlTable.append("<td>Nb Contrôle</td><td>").append(NB_CONTROLE).append("</nb><td>Nb Contrôle par Semaine</td><td>").append(NB_CONTROLE_SEMAINE).append("</td>");
                        } else if (i == 4) {
                            htmlTable.append("<td></td>");
                        } else if (i == 5) {
                            htmlTable.append("<td>Taille Total</td><td>").append(SIZE_TOTAL).append("</nb><td>Taille Total par Semaine</td><td>").append(SIZE_TOTAL_SEMAINE).append("</td>");
                        } else if (i == 6) {
                            htmlTable.append("<td>Nb Recherche</td><td>").append(SIZE_RECHERCHE).append("</nb><td>Nb Recherche par Semaine</td><td>").append(SIZE_RECHERCHE_SEMAINE).append("</td>");
                        } else if (i == 7) {
                            htmlTable.append("<td>Nb Contrôle</td><td>").append(SIZE_CONTROLE).append("</nb><td>Nb Contrôle par Semaine</td><td>").append(SIZE_CONTROLE_SEMAINE).append("</td>");
                        }

                        htmlTable.append("</tr>");

                        previousInstrument = instrument;
                        i++;
                    }

                    // Empty Line
                    htmlTable.append("<tr><td></td></tr>");

                    // End Table
                    htmlTable.append("</table></html>");

                    m_displayedHtml = htmlTable.toString().replaceAll("STYLE_BIG_BOLD", STYLE_BIG_BOLD).replaceAll("STYLE_TITLE", STYLE_TITLE).replaceAll("STYLE_RIGHT_TEXT", STYLE_RIGHT_TEXT).replaceAll("STYLE_LEFT_TEXT", STYLE_LEFT_TEXT);


                    m_pane.setText(m_displayedHtml);

                    SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            m_pane.setCaretPosition(0);
                        }
                    });
                } else {
                    m_pane.setText("failed");
                }
                setLoaded(m_id);
            }
        };

        StatisticsTask task = new StatisticsTask(callback, acquisitionStatisticList, year);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    private void generateHtmlForAnalysis(int year, int type) {
        if (type == 2) {
            generateAcquisitionsStatistic(year);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date start = cal.getTime();
        long startSeconds = start.getTime()/1000l;

        cal.set(Calendar.YEAR, year+1);
        cal.set(Calendar.DAY_OF_YEAR, 1);

        Date end = cal.getTime();
        long endSeconds = end.getTime()/1000l;


        HashMap<Integer, AnalysisTypeCount> analysisTypeMap = new HashMap<>();
        String[] ANALYSIS_TYPES = { "Protein identification", "Large-scale relative quantification", "Targeted quantification", "PTMs detection and quantification",
                "Analysis of entire proteins", "Interactomics", "Protein characterization", "Other type of analyses"};

        int id = 0;
        for (String analysTypes : ANALYSIS_TYPES) {
            id++;
            analysisTypeMap.put(id, new AnalysisTypeCount());

        }

        HashMap<Integer, AnalysisTypeCount> analysisProjectTypeMap = new HashMap<>();
        String[] ANALYSIS_PROJECT_TYPES = {"", "Collaboration with contract", "Collaboration with a quotation and an invoice", "Service provided with contract", "Service provided with a quotation and an invoice"};
        id = 0;
        for (String analysProjectTypes : ANALYSIS_PROJECT_TYPES) {
            analysisProjectTypeMap.put(id, new AnalysisTypeCount());
            id++;
        }


        int nbPreAnalyses = 0;
        int nbPreAnalysesRefused = 0;
        int nbPreAnalysesCancelled = 0;
        HashSet<Integer> projects = new HashSet<>();
        HashSet<String> customers = new HashSet<>();
        HashSet<String> customersRefused = new HashSet<>();
        HashSet<String> customersCancelled = new HashSet<>();

        for (ProAnalysisJson analysis : m_analyses) {
            if (analysis.isWaitingValidation()) {
                continue;
            }
            if ((analysis.getAnalyseDateDepotDemande()>=startSeconds) && (analysis.getAnalyseDateDepotDemande()<=endSeconds)) {
                nbPreAnalyses++;

                String preCustomer = analysis.getCustomer();
                customers.add(preCustomer);

                int analysisType = analysis.getTypeAnalyseId();
                AnalysisTypeCount analysisTypeCount = analysisTypeMap.get(analysisType);
                analysisTypeCount.nb++;

                int projectType = analysis.getProjetTypeId();
                AnalysisTypeCount projectTypeCount = analysisProjectTypeMap.get(projectType);
                projectTypeCount.nb++;



                if (analysis.isRefused()) {
                    nbPreAnalysesRefused++;
                    customersRefused.add(preCustomer);
                    analysisTypeCount.nbRefused++;
                    projectTypeCount.nbRefused++;
                } else if (analysis.isCancelled()) {
                    nbPreAnalysesCancelled++;
                    customersCancelled.add(preCustomer);
                    analysisTypeCount.nbCancelled++;
                    projectTypeCount.nbCancelled++;
                }


                int projectId = analysis.getProjetId();
                projects.add(projectId);

            }

        }


        int nbPostAnalyses = 0;
        int nbSamples = 0;
        int nbInjections = 0;
        int nbHours  = 0;
        HashSet<String> postCustomers = new HashSet<>();
        for (ProAnalysisJson analysis : m_analyses) {
            if (!analysis.isCompleted()) {
                continue;
            }
            if ((analysis.getAnalyseDateCloture()>=startSeconds) && (analysis.getAnalyseDateCloture()<=endSeconds)) {
                nbPostAnalyses++;
                nbSamples += analysis.getAnalyseNbEchantillons();
                nbInjections += analysis.getAnalyseInjections();
                nbHours += analysis.getAnalyseHeuresTravail();

                String postCustomer = analysis.getCustomer();
                postCustomers.add(postCustomer);


            }

        }


        StringBuilder htmlTable = new StringBuilder();

        //////////////////// PRE ANALYSIS GRENOBLE

        if (type == 0) {
            htmlTable.append("<html><table>");

            // First line : Title "Statistique preAnalyse GRENOBLE"
            htmlTable.append("<tr><td STYLE_BIG_BOLD colspan='4'>Statistique preAnalyse GRENOBLE</td></tr>");

            // Empty Line
            htmlTable.append("<tr><td></td></tr>");

            // Year
            htmlTable.append("<tr><td STYLE_BIG_BOLD  colspan='4'>Année " + year + "</td></tr>");

            // Empty Line
            htmlTable.append("<tr><td></td></tr>");

            // Indicateurs Généraux

            htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td STYLE_TITLE>Indicateurs généraux</td><td STYLE_TITLE>Total</td><td STYLE_TITLE>Refused</td><td STYLE_TITLE>Cancelled</td></tr>");
            htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td>Nombre de demandes d'analyse</td><td STYLE_RIGHT_TEXT>").append(nbPreAnalyses).append("</td><td STYLE_RIGHT_TEXT>").append(nbPreAnalysesRefused).append("</td><td STYLE_RIGHT_TEXT>").append(nbPreAnalysesCancelled).append("</td></tr>");
            htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td>Nombre de clients différents</td><td STYLE_RIGHT_TEXT>").append(customers.size()).append("</td><td STYLE_RIGHT_TEXT>").append(customersRefused.size()).append("</td><td STYLE_RIGHT_TEXT>").append(customersCancelled.size()).append("</td></tr>");

            // 2 Empty Lines
            htmlTable.append("<tr><td></td></tr>");
            htmlTable.append("<tr><td></td></tr>");

            // Nombre de demandes d'analyses par type d'analyse
            htmlTable.append("<tr><td colspan='2' STYLE_TITLE>Nombre de demandes d'analyses par type d'analyse</td><td STYLE_TITLE>Refused</td><td STYLE_TITLE>Cancelled</td></tr>");


            id = 0;
            for (String analysTypes : ANALYSIS_TYPES) {
                id++;
                AnalysisTypeCount analysisTypeCount = analysisTypeMap.get(id);
                htmlTable.append("<tr>").append("<td  STYLE_LEFT_TEXT>").append(ANALYSIS_TYPES[id - 1]).append("</td><td  STYLE_RIGHT_TEXT>").append(analysisTypeCount.nb).append("</td><td STYLE_RIGHT_TEXT>").append(analysisTypeCount.nbRefused).append("</td><td STYLE_RIGHT_TEXT>").append(analysisTypeCount.nbCancelled).append("</td></tr>");
            }

            // Empty Line
            htmlTable.append("<tr><td></td></tr>");

            // Nombre de demandes d'analyses par type de projet
            htmlTable.append("<tr><td colspan='2' STYLE_TITLE>Nombre de demandes d'analyses par type de projet</td><td STYLE_TITLE>Refused</td><td STYLE_TITLE>Cancelled</td></tr>");

            id = 0;
            for (String analysProjectTypes : ANALYSIS_PROJECT_TYPES) {

                AnalysisTypeCount analysisTypeCount = (AnalysisTypeCount) analysisProjectTypeMap.get(id);
                htmlTable.append("<tr>").append("<td STYLE_LEFT_TEXT>").append(ANALYSIS_PROJECT_TYPES[id]).append("</td><td STYLE_RIGHT_TEXT>").append(analysisTypeCount.nb).append("</td><td STYLE_RIGHT_TEXT>").append(analysisTypeCount.nbRefused).append("</td><td STYLE_RIGHT_TEXT>").append(analysisTypeCount.nbCancelled).append("</td></tr>");
                id++;
            }

            // End Table
            htmlTable.append("</table></html>");

            m_displayedHtml = htmlTable.toString().replaceAll("STYLE_BIG_BOLD", STYLE_BIG_BOLD).replaceAll("STYLE_TITLE", STYLE_TITLE).replaceAll("STYLE_RIGHT_TEXT", STYLE_RIGHT_TEXT).replaceAll("STYLE_LEFT_TEXT", STYLE_LEFT_TEXT);

        } else {

            //////////////////// POST ANALYSIS GRENOBLE

            htmlTable.append("<html><table>");

            // First line : Title "Statistique preAnalyse GRENOBLE"
            htmlTable.append("<tr><td STYLE_BIG_BOLD colspan='4'>Statistique post Analyse GRENOBLE</td></tr>");

            // Empty Line
            htmlTable.append("<tr><td></td></tr>");

            // Year
            htmlTable.append("<tr><td STYLE_BIG_BOLD  colspan='4'>Année " + year + "</td></tr>");

            // Empty Line
            htmlTable.append("<tr><td></td></tr>");

            // Indicateurs Généraux
            int nbCustomers =  postCustomers.size();
            htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td STYLE_TITLE colspan='2'>Indicateurs généraux</td></tr>");
            htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td>Nombre de demandes d'analyse</td><td STYLE_RIGHT_TEXT>").append(nbPostAnalyses).append("</td></tr>");
            htmlTable.append("<tr STYLE_LEFT_TEXT>").append("<td>Nombre de clients différents</td><td STYLE_RIGHT_TEXT>").append(nbCustomers).append("</td></tr>");

            // 2 Empty Lines
            htmlTable.append("<tr><td></td></tr>");
            htmlTable.append("<tr><td></td></tr>");

            // Echantillons		Injections		Heures d'utilisation des spectromètres
            htmlTable.append("<tr><td colspan='2' STYLE_TITLE>Echantillons</td><td colspan='2' STYLE_TITLE>Injections</td><td colspan='2' STYLE_TITLE>Heures d'utilisation des spectromètres</td></tr>");

            // Nombre total d'échantillons	VALUE	Nombre total d'injections	VALUE	Nombre total d'heures	VALUE
            htmlTable.append("<tr>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre total d'échantillons</td><td STYLE_RIGHT_TEXT>").append(nbSamples).append("</td>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre total d'injections</td><td STYLE_RIGHT_TEXT>").append(nbInjections).append("</td>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Heures d'utilisation des spectromètres</td><td STYLE_RIGHT_TEXT>").append(nbHours).append("</td>");
            htmlTable.append("</tr>");

            // Nombre moyen d'échantillons par demande d'analyse	VALUE	Nombre moyen d'injections par demande d'analyse	VALUE	Nombre moyen d'heures par demande d'analyse	VALUE
            float nbSamplesMean = (nbPostAnalyses!=0) ? (((float)(nbSamples))/nbPostAnalyses) : 0f;
            nbSamplesMean = Math.round(nbSamplesMean * 100) / 100f;
            float nbInjectionsMean = (nbPostAnalyses!=0) ? (((float)(nbInjections))/nbPostAnalyses) : 0f;
            nbInjectionsMean = Math.round(nbInjectionsMean * 100) / 100f;
            float nbHoursMean = (nbPostAnalyses!=0) ? (((float)(nbHours))/nbPostAnalyses) : 0f;
            nbHoursMean = Math.round(nbHoursMean * 100) / 100f;

            htmlTable.append("<tr>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre moyen d'échantillons par demande d'analyse</td><td STYLE_RIGHT_TEXT>").append(nbSamplesMean).append("</td>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre total d'Nombre moyen d'injections par demande d'analyse</td><td STYLE_RIGHT_TEXT>").append(nbInjectionsMean).append("</td>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre moyen d'heures par demande d'analyse</td><td STYLE_RIGHT_TEXT>").append(nbHoursMean).append("</td>");
            htmlTable.append("</tr>");


            // Nombre moyen d'échantillons par client	VALUE	Nombre moyen d'injections par client	VALUE	Nombre moyen d'heures par client	VALUE

            nbSamplesMean = (nbPostAnalyses!=0) ? (((float)(nbSamples))/nbCustomers) : 0f;
            nbSamplesMean = Math.round(nbSamplesMean * 100) / 100f;
            nbInjectionsMean = (nbPostAnalyses!=0) ? (((float)(nbInjections))/nbCustomers) : 0f;
            nbInjectionsMean = Math.round(nbInjectionsMean * 100) / 100f;
            nbHoursMean = (nbPostAnalyses!=0) ? (((float)(nbHours))/nbCustomers) : 0f;
            nbHoursMean = Math.round(nbHoursMean * 100) / 100f;

            htmlTable.append("<tr>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre moyen d'échantillons par client</td><td STYLE_RIGHT_TEXT>").append(nbSamplesMean).append("</td>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre moyen d'injections par client</td><td STYLE_RIGHT_TEXT>").append(nbInjectionsMean).append("</td>");
            htmlTable.append("<td STYLE_LEFT_TEXT>Nombre moyen d'heures par client</td><td STYLE_RIGHT_TEXT>").append(nbHoursMean).append("</td>");
            htmlTable.append("</tr>");

            // Empty Line
            htmlTable.append("<tr><td></td></tr>");


            // End Table
            htmlTable.append("</table></html>");

            m_displayedHtml = htmlTable.toString().replaceAll("STYLE_BIG_BOLD", STYLE_BIG_BOLD).replaceAll("STYLE_TITLE", STYLE_TITLE).replaceAll("STYLE_RIGHT_TEXT", STYLE_RIGHT_TEXT).replaceAll("STYLE_LEFT_TEXT", STYLE_LEFT_TEXT);

        }

        m_pane.setText(m_displayedHtml);
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                m_pane.setCaretPosition(0);
            }
        });


    }

    public class AnalysisTypeCount {
        private int nb;
        private int nbRefused;
        private int nbCancelled;
    }

    private static class HtmlSelection implements Transferable {

        private static ArrayList<DataFlavor> htmlFlavors = new ArrayList(3);

        static {

            try {
                htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
                htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
                htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

        }

        private String html;

        public HtmlSelection(String html) {
            this.html = html;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return htmlFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (String.class.equals(flavor.getRepresentationClass())) {
                return html;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(html);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(html);
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

}
