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

package fr.epims.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * Useful methods around Dates
 *
 * @author JM235353
 *
 */
public class UtilDate {

    private static SimpleDateFormat formatWithHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat plateFormat = new SimpleDateFormat("yyyyMMdd");

    public static String dateWithoutHour(String dateString) {
        if (dateString == null) {
            return "";
        }
        try {

            Date date = formatWithHour.parse(dateString);
            String formattedDate = format.format(date);
            return formattedDate;
        } catch (Exception e) {
            return dateString;
        }

    }

    public static Date convertToDateWithHour(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            Date date = formatWithHour.parse(dateString);
            return date;
        } catch (Exception e) {
            return null;
        }

    }

    public static Date convertToDateWithoutHour(String dateString) {
        if ((dateString == null) || (dateString.isEmpty())) {
            return null;
        }
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (Exception e) {
            return null;
        }

    }

    public static String dateToString(Date d) {
        if (d == null) {
            return "";
        }
        return format.format(d);
    }

    public static String dateToStringForIHM(Date d) {
        if (d == null) {
            return " /         ";
        }
        return format.format(d);
    }

    public static String dateToPlateName(Date d) {
        return plateFormat.format(d);
    }

    public static SimpleDateFormat getDateFormat() {
        return format;
    }
}
