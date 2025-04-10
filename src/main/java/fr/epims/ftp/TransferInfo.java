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

package fr.epims.ftp;

import fr.epims.ui.common.IconManager;

import javax.swing.*;
import java.io.File;


/**
 *
 * Source, Destination and status info of the file to download
 *
 * @author JM235353
 *
 */
public class TransferInfo {

    public enum DownloadEnum {
        WAITING(0),
        RUNNING(1),
        SUCCESS(2),
        FAILED(3);

        private DownloadEnum(int index) {
            m_index = index;
        }

        public int getIndex() {
            return m_index;
        }

        private int m_index;
    }

    private DownloadEnum m_state;
    private File m_localFile;
    private ServerFile m_serverFile;

    private boolean m_download;

    private final static ImageIcon[] PUBLIC_STATE_ICONS = { IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16), IconManager.getIcon(IconManager.IconType.ARROW_RIGHT_SMALL), IconManager.getIcon(IconManager.IconType.TICK_SMALL), IconManager.getIcon(IconManager.IconType.CROSS_SMALL16)};


    public TransferInfo(File localFile, ServerFile serverFile, boolean download) {
        m_state = DownloadEnum.WAITING;
        m_serverFile = serverFile;
        m_localFile = localFile;
        m_download = download;
    }



    public void setRunning() {
        m_state = DownloadEnum.RUNNING;
    }

    public void setFailed() {
        m_state = DownloadEnum.FAILED;
    }

    public void setDone() {
        m_state = DownloadEnum.SUCCESS;
    }

    public boolean isWaiting() {
        return m_state.equals(DownloadEnum.WAITING);
    }

    public boolean isRunning() {
        return m_state.equals(DownloadEnum.RUNNING);
    }

    public File getLocalFile() {
        return m_localFile;
    }

    public ServerFile getServerFile() {
        return m_serverFile;
    }

    public boolean isDownload() {
        return m_download;
    }

    public Icon getStateIcon() {
        return PUBLIC_STATE_ICONS[m_state.getIndex()];
    }

    public Icon getTransferIcon() {
        if (m_download) {
            return  IconManager.getIcon(IconManager.IconType.DOWNLOAD_WAY);
        } else {
            return  IconManager.getIcon(IconManager.IconType.UPLOAD_WAY);
        }
    }

}