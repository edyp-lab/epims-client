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


package fr.epims.tasks.archive;

import fr.edyp.epims.json.ArchivingInfoJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;

/**
 *
 * Task used to retrieve archiving state (data already archivded, waiting for archiving, errors...)
 *
 * @author JM235353
 *
 */
public class ArchivePollingTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private LinkedList<ArchivingInfoJson> m_archivingInfoJsonList;


    public ArchivePollingTask(AbstractDatabaseCallback callback, LinkedList<ArchivingInfoJson> archivingInfoJsonList) {
        super(callback, new TaskInfo("Polls for Archiving Progress", true, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/archivepoll";

        m_archivingInfoJsonList = archivingInfoJsonList;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with POST method, and Headers.
            ResponseEntity<ArchivingInfoJson[]> response = restTemplate.exchange(URL, //
                    HttpMethod.POST, entity, ArchivingInfoJson[].class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            ArchivingInfoJson[] archivingInfoJsonsArray = response.getBody();

            for (ArchivingInfoJson archivingInfoJson : archivingInfoJsonsArray) {
                m_archivingInfoJsonList.add(archivingInfoJson);
            }

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
