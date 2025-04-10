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
import fr.edyp.epims.json.ProjectJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/**
 *
 * Close Project : used to close a Project by setting the closeDate.
 *
 * @author JM235353
 *
 */
public class CloseProjectTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private Date m_closeDate;
    private ProjectJson[] m_projectJson;


    public CloseProjectTask(AbstractDatabaseCallback callback, Date closeDate, ProjectJson[] projectJson) {
        super(callback, new TaskInfo("Close Project "+projectJson[0].getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/projectclose";

        m_projectJson = projectJson;
        m_closeDate = closeDate;

    }


    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<Date> requestEntity = new HttpEntity<>(m_closeDate, entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<ProjectJson> response = restTemplate.exchange(URL+"/"+m_projectJson[0].getId(), //
                    HttpMethod.POST, requestEntity, ProjectJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            DataManager.getDatabaseVersion().bumpVersion(ProjectJson.class, null);


            ProjectJson projectJson = response.getBody();


            m_projectJson[0] = projectJson;

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
