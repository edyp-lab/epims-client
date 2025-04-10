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

package fr.epims.mgf;

import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.io.File;
import java.util.Date;

/**
 *
 * Informations on a mgf file with the status of the file (deleted, transferred by FTP...)
 * @author JM235353
 *
 */
public class MgfFileInfo {

    public enum StatusEnum {
        NO_INFO(0),
        STUDY_WAITING(1),
        STUDY_RUNNING(2),
        STUDY_DONE(3),
        FTP_WAITING(4),
        FTP_RUNNING(5),
        FTP_DONE(6),
        FAILED(7),
        DELETED(8);



        private StatusEnum(int index) {
            m_index = index;
        }

        public int getIndex() {
            return m_index;
        }

        private int m_index;

    }

    private final static ImageIcon[] PUBLIC_STATUS_ICONS = { null,
            IconManager.getIcon(IconManager.IconType.STUDY_WAITING),
            IconManager.getIcon(IconManager.IconType.STUDY_RUNNING),
            IconManager.getIcon(IconManager.IconType.STUDY_DONE),
            IconManager.getIcon(IconManager.IconType.FTP_WAITING),
            IconManager.getIcon(IconManager.IconType.FTP_RUNNING),
            IconManager.getIcon(IconManager.IconType.FTP_DONE),
            IconManager.getIcon(IconManager.IconType.CROSS_SMALL16),
            IconManager.getIcon(IconManager.IconType.DELETE_MGFFILE)
         };



    private MgfFileInfo.StatusEnum m_status;
    private String m_directory;
    private File m_file;
    private Integer m_studyId;

    private boolean m_studySearched;
    private String m_errorMessage;

    private Date m_transferDate = null;

    public MgfFileInfo(String directory, File file, Integer StudyId) {
        m_status = MgfFileInfo.StatusEnum.NO_INFO;
        m_directory = directory;
        m_file = file;
        m_studyId = StudyId;
        m_errorMessage = "";
        m_studySearched = false;
    }

    public boolean isStudySearched() {
        return m_studySearched;
    }

    public void setStudySearched() {
        m_studySearched = true;
    }

    public String getDirectory() {
        return m_directory;
    }

    public File getFile() {
        return m_file;
    }

    public Integer getStudyId() {
        return m_studyId;
    }

    public void setStudyId(Integer studyId) {
        m_studyId = studyId;
    }

    public StatusEnum getStatus() {
        return m_status;
    }

    public void setStatus(StatusEnum status) {
        m_status = status;
        if ((status == StatusEnum.FTP_DONE) && (m_transferDate == null)) {
            m_transferDate = new Date();
        }
    }

    public Date getTransferDate() {
        return m_transferDate;
    }

    public void setTransferDate(Date d) {
        m_transferDate = d;
    }

    public String getErrorMessage() {
        return m_errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        m_errorMessage = errorMessage;
    }


    public Icon getStateIcon() {
        return PUBLIC_STATUS_ICONS[m_status.getIndex()];
    }

    public static Icon getStateIcon(StatusEnum status) {
        return PUBLIC_STATUS_ICONS[status.getIndex()];
    }
}
