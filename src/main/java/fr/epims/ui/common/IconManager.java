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

package fr.epims.ui.common;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.swing.ImageIcon;


/**
 *
 * Manage all icons needed by the application. Each icon is loaded only one time
 * when it is first needed
 *
 * @author JM235353
 *
 */
public class IconManager {

    public enum IconType {
        EPIMS_LOGO16,
        EPIMS_LOGO32,
        EPIMS_LOGO64,
        EPIMS_LOGO128,
        SEARCH,
        PROGRAM,
        PROJECT,
        STUDY,
        STUDY_WAITING,
        STUDY_RUNNING,
        STUDY_DONE,
        CALENDAR,
        ACTIVITIES,
        ACTIVITIES_FILTERED,
        FAVOURITES,
        ADD_USER,
        ROBOT,
        MGFFILE,
        DELETE_MGFFILE,
        REFRESH,
        ADD_PROGRAM,
        ADD_PROJECT,
        ADD_STUDY,
        HELP,
        LOGOUT,
        ACQUISITIONS,
        INSTRUMENT,
        WAVE,
        SAMPLE,
        ADD_SAMPLE,
        ADD_TO_ROBOT,
        LOCK,
        UNLOCK,
        FTP,
        FTP_WAITING,
        FTP_RUNNING,
        FTP_DONE,
        ADMIN,
        USER,
        OK,
        EMPTY,
        EXCLAMATION,
        INFORMATION,
        BACK,
        QUESTION,
        DEFAULT,
        CANCEL,
        TABLE_PARAMETERS,
        ADD_LIST,
        STAR_COMPULSORY,
        ADD_SAMPLE_CHOICE,
        FRAG_SAMPLE_CHOICE,
        ARCHIVE,
        ARCHIVE_ADD,
        ARCHIVE_ALLOW,
        ARCHIVE_WAITING,
        ADD_HORIZONTAL_ROBOT,
        ADD_VERTICAL_ROBOT,
        ADD_PLATE,
        ARROW_MERGE,
        ARROW_SPLIT,
        INSERT_COLUMN,
        BIG_HELP,
        BIG_WARNING,
        BIG_INFO,
        HOUR_GLASS,
        SAVE,
        UNDO,
        EXPORT,
        FOLDER,
        FILE,
        COMPUTER_NETWORK,
        FOLDER_EXPANDED,
        HOUR_GLASS_MINI16,
        DOWNLOAD,
        UPLOAD,
        UPLOAD_TO_SERVER,
        DOWNLOAD_WAY,
        UPLOAD_WAY,
        CROSS_SMALL16,
        PLUS_SMALL_16,
        TICK_SMALL,
        ARROW_RIGHT_SMALL,
        EDIT_USER,
        ADD_HOME,
        EDIT_HOME,
        HOME,
        LIST,
        LIST_EDIT,
        INSTRUMENT_ADD,
        INSTRUMENT_EDIT,
        PLATE,
        EDIT_PLATE,
        ADD,
        EXPAND_TREE,
        COLLAPSE_TREE,
        ARROW_UP,
        ARROW_DOWN,
        ERASER,
        ANALYSE_REQUEST,
        ANALYSES,
        ANALYSE_EDIT,
        PRICE_TAG,
        HELP_ANALYSIS_STEP1_1,
        HELP_ANALYSIS_STEP1_2,
        HELP_ANALYSIS_STEP2_1,
        HELP_ANALYSIS_STEP2_2,
        HELP_ANALYSIS_STEP2_3,
        SELECT_STUDY_REF,
        ARROW_IMPORT,
        IMPORT_ANALYSE
    }

    private final static HashMap<IconType, ImageIcon> m_iconMap = new HashMap<>();


    public static Image getImage(IconType iconType) {
        ImageIcon icon = getIcon(iconType);
        return icon.getImage();
    }

    public static ImageIcon getIcon(IconType iconType) {

        ImageIcon icon = m_iconMap.get(iconType);
        if (icon == null) {
            String path = getIconFilePath(iconType);
            icon = ImageUtilities.loadImageIcon(path, false);
            m_iconMap.put(iconType, icon);
        }

        return icon;
    }



    private static String getIconFilePath(IconType iconType) {
        switch (iconType) {
            case EPIMS_LOGO16:
                return "epimsLogo16.png";
            case EPIMS_LOGO32:
                return "epimsLogo32.png";
            case EPIMS_LOGO64:
                return "epimsLogo64.png";
            case EPIMS_LOGO128:
                return "epimsLogo128.png";
            case PROGRAM:
                return "program.png";
            case PROJECT:
                return "project.png";
            case STUDY:
                return "study.png";
            case STUDY_WAITING:
                return "study-waiting.png";
            case STUDY_RUNNING:
                return "study-running.png";
            case STUDY_DONE:
                return "study-done.png";
            case CALENDAR:
                return "calendar.png";
            case ACTIVITIES:
                return "activities.png";
            case ACTIVITIES_FILTERED:
                return "activities-filtered.png";
            case FAVOURITES:
                return "star.png";
            case ADD_USER: {
                switch (getPeriod()) {
                    case USER:
                        return "add-user.png";
                    case CHRISTMAS:
                        return "snowman-hat-add.png";
                    case SNOW:
                        return "snowman-hat-add.png";
                    case WOMAN_WEEK:
                        return "user-red-female-add.png";
                }
            }
            case ROBOT:
                return "robot.png";
            case MGFFILE:
                return "mgf.png";
            case DELETE_MGFFILE:
                return "delete-mgf.png";
            case REFRESH:
                return "refresh.png";
            case ADD_PROGRAM:
                return "add-program.png";
            case ADD_PROJECT:
                return "add-project.png";
            case ADD_STUDY:
                return "add-study.png";
            case HELP:
                return "help.png";
            case LOGOUT:
                return "logout.png";
            case ACQUISITIONS:
                return "acquisitions.png";
            case INSTRUMENT:
                return "instrument.png";
            case WAVE:
                return "wave.png";
            case SAMPLE:
                return "sample.png";
            case ADD_SAMPLE:
                return "add-sample.png";
            case ADD_TO_ROBOT:
                return "add-to-robot.png";
            case LOCK:
                return "lock.png";
            case UNLOCK:
                return "lock-unlock.png";
            case FTP:
                return "ftp.png";
            case FTP_WAITING:
                return "ftp-waiting.png";
            case FTP_RUNNING:
                return "ftp-running.png";
            case FTP_DONE:
                return "ftp-done.png";
            case ADMIN:
                return "admin.png";
            case USER:
                return "user.png";
            case OK:
                return "tick.png";
            case EMPTY:
                return "empty.png";
            case EXCLAMATION:
                return "exclamation-red.png";
            case INFORMATION:
                return "information.png";
            case BACK:
                return "arrow-180.png";
            case QUESTION:
                return "question.png";
            case DEFAULT:
                return "arrow-circle.png";
            case CANCEL:
                return "cross.png";
            case SEARCH:
                return "search.png";
            case TABLE_PARAMETERS:
                return "table--pencil.png";
            case ADD_LIST:
                return "add-list.png";
            case STAR_COMPULSORY:
                return "star-small-red.png";
            case ADD_SAMPLE_CHOICE:
                return "create_sample.png";
            case FRAG_SAMPLE_CHOICE:
                return "create_fragment.png";
            case ARCHIVE:
                return "cassette.png";
            case ARCHIVE_ALLOW:
                return "cassette--plus.png";
            case ARCHIVE_WAITING:
                return "cassette-waiting.png";
            case ARCHIVE_ADD:
                return "archive-add.png";
            case ADD_HORIZONTAL_ROBOT:
                return "addHorizontalRobot.png";
            case ADD_VERTICAL_ROBOT:
                return "addVerticalRobot.png";
            case ADD_PLATE:
                return "add-plate.png";
            case ARROW_MERGE:
                return "arrow-merge.png";
            case ARROW_SPLIT:
                return "arrow-split.png";
            case INSERT_COLUMN:
                return "insert-column.png";
            case BIG_HELP:
                return "big-help.png";
            case BIG_WARNING:
                return "big-warning.png";
            case BIG_INFO:
                return "big-info.png";
            case HOUR_GLASS:
                return "hourglass.png";
            case SAVE:
                return "save.png";
            case EXPORT:
                return "export.png";
            case UNDO:
                return "undo.png";
            case FOLDER:
                return "folder.png";
            case FILE:
                return "file.png";
            case COMPUTER_NETWORK:
                return "computer-network.png";
            case FOLDER_EXPANDED:
                return "folder-open.png";
            case HOUR_GLASS_MINI16:
                return "hourglass-mini-16x16.png";
            case DOWNLOAD:
                return "download.png";
            case UPLOAD:
                return "upload.png";
            case UPLOAD_TO_SERVER:
                return "upload2server.png";
            case DOWNLOAD_WAY:
                return "arrow-270-medium.png";
            case UPLOAD_WAY:
                return "arrow-090-medium.png";
            case CROSS_SMALL16:
                return "cross-small16x16.png";
            case TICK_SMALL:
                return "tick-small.png";
            case ARROW_RIGHT_SMALL:
                return "arrow-right-small.png";
            case EDIT_USER:
                return "edit-user.png";
            case ADD_HOME:
                return "home--plus.png";
            case EDIT_HOME:
                return "home--pencil.png";
            case HOME:
                return "home.png";
            case LIST:
                return "document-list.png";
            case LIST_EDIT:
                return "document-list-edit.png";
            case INSTRUMENT_ADD:
                return "instrument-add.png";
            case INSTRUMENT_EDIT:
                return "instrument-edit.png";
            case PLATE:
                return "plate.png";
            case EDIT_PLATE:
                return "edit-plate.png";
            case ADD:
                return "plus-button.png";
            case EXPAND_TREE:
                return "expand-tree.png";
            case COLLAPSE_TREE:
                return "collapse-tree.png";
            case ARROW_UP:
                return "arrow-090.png";
            case ARROW_DOWN:
                return "arrow-270.png";
            case ERASER:
                return "eraser.png";
            case ANALYSE_REQUEST:
                return "analyse.png";
            case ANALYSES:
                return "analyses.png";
            case ANALYSE_EDIT:
                return "analyse-edit.png";
            case PRICE_TAG:
                return "price-tag--pencil.png";
            case HELP_ANALYSIS_STEP1_1:
                return "help-step1-1.png";
            case HELP_ANALYSIS_STEP1_2:
                return "help-step1-2.png";
            case HELP_ANALYSIS_STEP2_1:
                return "help-step2-1.png";
            case HELP_ANALYSIS_STEP2_2:
                return "help-step2-2.png";
            case HELP_ANALYSIS_STEP2_3:
                return "help-step2-3.png";
            case PLUS_SMALL_16:
                return "plus-small.png";
            case SELECT_STUDY_REF:
                return "select-study-ref.png";
            case ARROW_IMPORT:
                return "arrow-import.png";
            case IMPORT_ANALYSE:
                return "import-analyse.png";
        }


        return null; // can not happen
    }

    public static ImageIcon createColoredIcon(Color c) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();

        g2d.setColor(c);
        g2d.fillRect(0,0,15,15);

        g2d.setColor(Color.black);
        g2d.drawRect(0,0,15,15);

        ImageIcon icon = new ImageIcon(img);

        return icon;

    }

    private enum IconPeriod {
        USER,
        WOMAN_WEEK,
        SNOW,
        CHRISTMAS
    }

    private static IconPeriod getPeriod() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        if (c.get(Calendar.MONTH) == 11) {
            return IconPeriod.CHRISTMAS;
        }
        if ((c.get(Calendar.MONTH) == 2) && (c.get(Calendar.DAY_OF_MONTH) >=8) && (c.get(Calendar.DAY_OF_MONTH) <=15)) {
            return IconPeriod.WOMAN_WEEK;
        }

        return IconPeriod.USER;
    }


}
