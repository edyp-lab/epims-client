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

package fr.epims.tasks;

import fr.epims.dataaccess.*;
import fr.edyp.epims.json.FtpConfigurationJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * To retrieve the address of the SFTP Server
 *
 * @author JM235353
 *
 */
public class FTPSettingsTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private FtpConfigurationJson[] m_ftpConfigurationJson;

    public FTPSettingsTask(AbstractDatabaseCallback callback, FtpConfigurationJson[] ftpConfigurationJson) {
        super(callback, new TaskInfo("Load FTP Configuration", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/ftpsettings";

        m_ftpConfigurationJson = ftpConfigurationJson;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<FtpConfigurationJson> response = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, FtpConfigurationJson.class);

            FtpConfigurationJson ftpConfigurationJson = response.getBody();

            m_ftpConfigurationJson[0] = ftpConfigurationJson;

        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }

        return true;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }
}
