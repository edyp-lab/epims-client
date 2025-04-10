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
import fr.edyp.epims.json.DatabaseVersionJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * To Retrieve the Versions of the different Data in the server side.
 * Data can be for instance StudyJson.class, ActorJson.class
 * If the data version on the server side and on ePims client are not the same,
 * a partial loading of out-of-date data is needed.
 *
 * @author JM235353
 *
 */
public class GetDatabaseVersionTask extends AbstractDatabaseTask {

    private DatabaseVersionJson[] m_databaseVersion;

    private String URL;

    public GetDatabaseVersionTask(AbstractDatabaseCallback callback, String serverURL, DatabaseVersionJson[] databaseVersion) {
        super(callback, new TaskInfo("Get Database Version", false, null));

        URL = serverURL+"/api/databaseversion";

        m_databaseVersion = databaseVersion;
    }

    @Override
    public boolean fetchData() {

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> parametersMap = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parametersMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {

            ResponseEntity<DatabaseVersionJson> response =  restTemplate.exchange(URL, HttpMethod.GET, requestEntity, DatabaseVersionJson.class);

            m_databaseVersion[0] = response.getBody();

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
