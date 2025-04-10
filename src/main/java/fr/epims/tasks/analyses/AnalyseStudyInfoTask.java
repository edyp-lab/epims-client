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

import fr.edyp.epims.json.AnalyseProgressJson;

import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AnalyseStudyInfoTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private AnalyseProgressJson[] m_analysisProgressJson;


    public AnalyseStudyInfoTask(AbstractDatabaseCallback callback, String studyRef, AnalyseProgressJson[] analysisProgressJson) {
        super(callback, new TaskInfo("Load Analyse Request Study Info for "+studyRef, false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/analysisinfo/"+studyRef;

        m_analysisProgressJson = analysisProgressJson;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<AnalyseProgressJson> requestEntity = new HttpEntity<>(null, entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<AnalyseProgressJson> response = restTemplate.exchange(URL, //
                    HttpMethod.POST, requestEntity, AnalyseProgressJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            AnalyseProgressJson analyseProgress = response.getBody();

            m_analysisProgressJson[0] = analyseProgress;

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
