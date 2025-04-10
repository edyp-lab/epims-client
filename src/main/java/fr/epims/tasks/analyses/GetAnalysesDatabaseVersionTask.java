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

import fr.epims.dataaccess.AbstractDatabaseCallback;
import fr.epims.dataaccess.AbstractDatabaseTask;
import fr.epims.dataaccess.TaskError;
import fr.epims.dataaccess.TaskInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Get the Analysis Request Server Version (used to do an handshake)
 *
 * @author JM235353
 *
 */
public class GetAnalysesDatabaseVersionTask extends AbstractDatabaseTask {

    private Integer[] m_analysesServerVersion;

    private String URL;

    public GetAnalysesDatabaseVersionTask(AbstractDatabaseCallback callback, String serverURL, Integer[] analysesServerVersion) {
        super(callback, new TaskInfo("Get Analyses Requests Server Version", false, null));

        URL = serverURL+"/api/analysesserverversion";

        m_analysesServerVersion = analysesServerVersion;
    }

    @Override
    public boolean fetchData() {

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> parametersMap = new LinkedMultiValueMap<>();

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parametersMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {

            ResponseEntity<Integer> response =  restTemplate.exchange(URL, HttpMethod.GET, requestEntity, Integer.class);

            m_analysesServerVersion[0] = response.getBody();

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
