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

package fr.epims.ui.panels.robot;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import fr.epims.dataaccess.DataManager;
import fr.epims.ui.common.CyclicColorPalette;
import fr.epims.util.UtilDate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * Export Robot Plate to .xlsx
 *
 * @author JM235353
 *
 */
public class ExcelXMLExporter  {

    private static XSSFWorkbook m_wb;
    private static Sheet m_sheet;
    private static int m_curRow = 0;
    private static int m_curCell = 0;
    private static Row m_row;

    private static String m_filePath = null;

    private boolean m_decorated = false;


    public static void export(RobotPlatePanel.Well[][] wells, int dimX, int dimY, String plateName, String filePath) throws IOException {

        String fileExtension = ".xlsx";
        if (!filePath.endsWith(fileExtension)) {
            filePath = filePath + fileExtension;
        }

        m_wb = new XSSFWorkbook();
        XSSFFont defaultFontBold = m_wb.createFont();
        defaultFontBold.setBold(true);
        DataFormat format = m_wb.createDataFormat();
        m_filePath = filePath;

        m_sheet = m_wb.createSheet(plateName);
        m_curRow = 0;

        // ---------- Line of information
        m_row = m_sheet.createRow(m_curRow);

        Cell cell = m_row.createCell(0);
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,0,1));

        CellStyle turquoiseStyle = m_wb.createCellStyle();
        turquoiseStyle.setFillForegroundColor( IndexedColors.AQUA.getIndex());
        turquoiseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        turquoiseStyle.setFont(defaultFontBold);
        cell.setCellStyle(turquoiseStyle);
        cell.setCellValue("Plate: "+plateName);

        cell = m_row.createCell(2);
        cell.setCellStyle(turquoiseStyle);
        cell.setCellValue(UtilDate.dateToString(new Date()));

        for (int i = 3; i <= dimX; i++) {
            cell = m_row.createCell(i);
            cell.setCellStyle(turquoiseStyle);
        }

        m_curRow++;

        // ---------- Blank line
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        // ---------- Header 1 2 3 ...
        m_row = m_sheet.createRow(m_curRow);
        cell = m_row.createCell(0);
        CellStyle greyStyle = m_wb.createCellStyle();
        greyStyle.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex());
        greyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        greyStyle.setBorderBottom(BorderStyle.THIN);
        greyStyle.setBorderTop(BorderStyle.THIN);
        greyStyle.setBorderRight(BorderStyle.THIN);
        greyStyle.setBorderLeft(BorderStyle.THIN);
        greyStyle.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(greyStyle);
        m_sheet.setColumnWidth(0, 3*255);
        for (int i=1;i<=dimX;i++) {
            cell = m_row.createCell(i);
            cell.setCellStyle(greyStyle);
            cell.setCellValue(i);

            m_sheet.setColumnWidth(i, 22*255); // 22 :number of characters
        }
        m_curRow++;


        // Lines of the plate with A,B,C... column as first column
        //HashMap<String, Color> m_actorsMap = new HashMap<>();
        HashMap<String, Color> m_studyMap = new HashMap<>();
        HashMap<String, ArrayList<String>> m_studyUsersMap = new HashMap<>();
        for (int j=0;j<dimY;j++) {
            m_row = m_sheet.createRow(m_curRow);
            m_curRow++;

            cell = m_row.createCell(0);
            cell.setCellStyle(greyStyle);
            char c = (char) (((int) 'A')+j);
            cell.setCellValue(String.valueOf(c));

            m_curCell = 1;
            for (int i=0;i<dimX;i++) {
                RobotPlatePanel.Well well = wells[i][j];
                ColoredRobotPlanning coloredRobotPlanning = well.getColoredRobotPlanning();
                if (coloredRobotPlanning == null) {
                    addCell("", null);
                } else {
                    String name = coloredRobotPlanning.m_robotPlanning.getSample().getName();
                    String actorKey = coloredRobotPlanning.m_robotPlanning.getSample().getActorKey();
                    String studyName = DataManager.getStudy(coloredRobotPlanning.m_robotPlanning.getSample().getStudy()).getTitle();
                    addCell(name, CyclicColorPalette.getExcelIndexForColor(coloredRobotPlanning.getStudyColor(plateName)) );

                    m_studyMap.put(studyName, coloredRobotPlanning.getStudyColor(plateName));

                    ArrayList<String> userList = m_studyUsersMap.get(studyName);
                    if (userList == null) {
                        userList = new ArrayList<>();
                        m_studyUsersMap.put(studyName, userList);
                    }
                    if (! userList.contains(actorKey)) {
                        userList.add(actorKey);
                    }
                }


            }
        }

        // ---------- Blank line
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        // Users
        m_curCell = 1;
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        // Sort studies according to its actor.
        ArrayList<String> studiesList = new ArrayList(m_studyMap.keySet());
        Collections.sort(studiesList, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                String actor1 =  m_studyUsersMap.get(o1).get(0);
                String actor2 =  m_studyUsersMap.get(o2).get(0);
                return actor1.compareTo(actor2);
            }
        });

        StringBuilder sb = new StringBuilder();
        for (String studyKey : studiesList) {
            ArrayList<String> userList = m_studyUsersMap.get(studyKey);
            sb.setLength(0);
            for (String user : userList) {
                if (sb.length() != 0) {
                    sb.append(" - ");
                }
                sb.append(user);
            }

            addCell(sb.toString(), CyclicColorPalette.getExcelIndexForColor(m_studyMap.get(studyKey)));
        }

        // ---------- Blank line
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        // ---- Analysis Table

        // Line 1 of Analysis Table
        String[] columns = {"Enzyme", "Volume trp (µL)", "Nb colonnes", "Nb ech", "Quantité trypsine\r\n(µg)", "concentration\r\n(µg/µL)", "Volume total", "Volume\r\ntrp stock", "Volume Bc"};

        CellStyle whiteStyle = m_wb.createCellStyle();
        whiteStyle.setWrapText(true);
        whiteStyle.setFillForegroundColor( IndexedColors.WHITE.getIndex());
        whiteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        whiteStyle.setBorderBottom(BorderStyle.THIN);
        whiteStyle.setBorderTop(BorderStyle.THIN);
        whiteStyle.setBorderRight(BorderStyle.THIN);
        whiteStyle.setBorderLeft(BorderStyle.THIN);
        whiteStyle.setAlignment(HorizontalAlignment.CENTER);
        whiteStyle.setVerticalAlignment(VerticalAlignment.TOP);

        CellStyle whiteStyle2Digits = m_wb.createCellStyle();
        whiteStyle2Digits.setWrapText(true);
        whiteStyle2Digits.setFillForegroundColor( IndexedColors.WHITE.getIndex());
        whiteStyle2Digits.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        whiteStyle2Digits.setBorderBottom(BorderStyle.THIN);
        whiteStyle2Digits.setBorderTop(BorderStyle.THIN);
        whiteStyle2Digits.setBorderRight(BorderStyle.THIN);
        whiteStyle2Digits.setBorderLeft(BorderStyle.THIN);
        whiteStyle2Digits.setAlignment(HorizontalAlignment.CENTER);
        whiteStyle2Digits.setVerticalAlignment(VerticalAlignment.TOP);
        whiteStyle2Digits.setDataFormat(format.getFormat("#0.00"));

        CellStyle whiteStyle3Digits = m_wb.createCellStyle();
        whiteStyle3Digits.setWrapText(true);
        whiteStyle3Digits.setFillForegroundColor( IndexedColors.WHITE.getIndex());
        whiteStyle3Digits.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        whiteStyle3Digits.setBorderBottom(BorderStyle.THIN);
        whiteStyle3Digits.setBorderTop(BorderStyle.THIN);
        whiteStyle3Digits.setBorderRight(BorderStyle.THIN);
        whiteStyle3Digits.setBorderLeft(BorderStyle.THIN);
        whiteStyle3Digits.setAlignment(HorizontalAlignment.CENTER);
        whiteStyle3Digits.setVerticalAlignment(VerticalAlignment.TOP);
        whiteStyle3Digits.setDataFormat(format.getFormat("#0.000"));

        CellStyle whiteStyleBold = m_wb.createCellStyle();
        whiteStyleBold.setWrapText(true);
        whiteStyleBold.setFillForegroundColor( IndexedColors.WHITE.getIndex());
        whiteStyleBold.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        whiteStyleBold.setBorderBottom(BorderStyle.THIN);
        whiteStyleBold.setBorderTop(BorderStyle.THIN);
        whiteStyleBold.setBorderRight(BorderStyle.THIN);
        whiteStyleBold.setBorderLeft(BorderStyle.THIN);
        whiteStyleBold.setAlignment(HorizontalAlignment.CENTER);
        whiteStyleBold.setVerticalAlignment(VerticalAlignment.TOP);
        whiteStyleBold.setFont(defaultFontBold);

        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        int rowOftable = m_curRow;

        m_curCell = 2;
        for (String header : columns) {
            cell = m_row.createCell(m_curCell);
            cell.setCellStyle(whiteStyle);
            cell.setCellValue(header);
            m_curCell++;
        }

        // Line 2 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        m_curCell = 1;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("Qté E1");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(15);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue(0);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellFormula("E"+m_curRow+"*8");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(0f);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle3Digits);
        cell.setCellFormula("(G"+m_curRow+")/D"+m_curRow);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("D"+m_curRow+"*(F"+m_curRow+"+7)+200");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"*H"+m_curRow+"/$E$"+(rowOftable+6));
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"-J"+m_curRow);
        m_curCell++;

        // Line 3 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        m_curCell = 1;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("Qté E2");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(15);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue(0);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellFormula("E"+m_curRow+"*8");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(0f);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle3Digits);
        cell.setCellFormula("(G"+m_curRow+")/D"+m_curRow);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("D"+m_curRow+"*(F"+m_curRow+"+7)+200");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"*H"+m_curRow+"/$E$"+(rowOftable+6));
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"-J"+m_curRow);
        m_curCell++;

        // Line 4 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        m_curCell = 1;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("Ajustement 1");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(5);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue(0);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(0f);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle3Digits);
        cell.setCellFormula("(G"+m_curRow+")/D"+m_curRow);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("D"+m_curRow+"*(F"+m_curRow+"+7)");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"*H"+m_curRow+"/$E$"+(rowOftable+6));
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"-J"+m_curRow);
        m_curCell++;

        // Line 5 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        m_curCell = 1;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("Ajustement 2");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(5);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue(0);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue(0f);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle3Digits);
        cell.setCellFormula("(G"+m_curRow+")/D"+m_curRow);
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("D"+m_curRow+"*(F"+m_curRow+"+7)");
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"*H"+m_curRow+"/$E$"+(rowOftable+6));
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle2Digits);
        cell.setCellFormula("I"+m_curRow+"-J"+m_curRow);
        m_curCell++;


        // Line 6 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        m_curCell = 8;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellValue("total trypsine :");
        m_curCell++;

        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyle);
        cell.setCellFormula("J"+(rowOftable+1)+"+J"+(rowOftable+2)+"+J"+(rowOftable+3)+"+J"+(rowOftable+4));
        m_curCell++;

        // Line 7 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        m_curCell = 0;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Stock : 50 µL tampon de resuspension par vial");

        m_curCell+=3;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue("Concentration");

        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue(0.4d);

        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Vérification présence des bouts de gel :");
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,m_curCell,m_curCell+1));

        m_curCell+=2;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("\u2610 OK  \u2610 PAS OK");
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,m_curCell,m_curCell+1));


        // Line 8 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        m_curCell = 0;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("vortex (µg/µL)");

        m_curCell+=3;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellValue("Nb colonnes total");

        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellStyle(whiteStyleBold);
        cell.setCellFormula("E"+(rowOftable+1)+"+E"+(rowOftable+2)+"+E"+(rowOftable+3)+"+E"+(rowOftable+4));
        m_curCell++;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Si pas ok, bouts perdus :");
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,m_curCell,m_curCell+1));

        m_curCell+=2;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Plaque 1 :");

        // Line 9 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Plaque 2 :");

        // Line 10 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        m_curCell = 5;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Vérification dépôt trypsine :");
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,m_curCell,m_curCell+1));

        m_curCell+=2;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("\u2610 OK  \u2610 PAS OK");
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,m_curCell,m_curCell+1));


        // Line 11 of Analysis Table
        m_row = m_sheet.createRow(m_curRow);
        m_curRow++;

        m_curCell = 5;
        cell = m_row.createCell(m_curCell);
        cell.setCellValue("Temps de digestion :");
        m_sheet.addMergedRegion(new CellRangeAddress(m_curRow, m_curRow,m_curCell,m_curCell+1));



        /* for Test : display all possible colors for Apache POI excel
        m_curCell = 1;

        for (short i=0;i<=63;i++) {
            m_row = m_sheet.createRow(m_curRow);
            m_curRow++;

            CellStyle testStyle = m_wb.createCellStyle();
            testStyle.setWrapText(true);
            testStyle.setFillForegroundColor(i);
            testStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            testStyle.setBorderBottom(BorderStyle.THIN);
            testStyle.setBorderTop(BorderStyle.THIN);
            testStyle.setBorderRight(BorderStyle.THIN);
            testStyle.setBorderLeft(BorderStyle.THIN);
            testStyle.setAlignment(HorizontalAlignment.CENTER);
            testStyle.setVerticalAlignment(VerticalAlignment.TOP);
            testStyle.setFont(defaultFontBold);
            cell = m_row.createCell(m_curCell);
            cell.setCellStyle(testStyle);
            cell.setCellValue(i);
        }
        */


        m_wb.setForceFormulaRecalculation(true);

        FileOutputStream fileOut = new FileOutputStream(m_filePath);
        m_wb.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }


    private static void addCell(String t, Short colorIndex) {

        Cell cell = m_row.createCell(m_curCell);
        CellStyle backgroundStyle = m_wb.createCellStyle();
        backgroundStyle.setBorderBottom(BorderStyle.THIN);
        backgroundStyle.setBorderTop(BorderStyle.THIN);
        backgroundStyle.setBorderRight(BorderStyle.THIN);
        backgroundStyle.setBorderLeft(BorderStyle.THIN);
        backgroundStyle.setWrapText(true);


        if (colorIndex != null) {
            backgroundStyle.setFillForegroundColor( colorIndex);
            backgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        cell.setCellStyle(backgroundStyle);
        cell.setCellValue(t);

        m_curCell++;
    }


}
