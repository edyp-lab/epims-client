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

package fr.epims.tasks.analyses;

import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


/**
 *
 * Used to Load information about the newly logged user to the DA Server
 *
 * @author JM235353
 *
 */
public class UserTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private String[] m_fullName;


    public UserTask(AbstractDatabaseCallback callback, String login, String[] fullName) {
        super(callback, new TaskInfo("Load User Information", false, null), TokenManager.TOKEN_ANALYSES_SERVER);

        URL = DataManager.getAnalysesServerURL()+"/api/userfullname/"+login;

        m_fullName = fullName;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {


            // Send request with GET method, and Headers.
            ResponseEntity<String> response = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, String.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            String fullName = response.getBody();

            m_fullName[0] = fullName;


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
