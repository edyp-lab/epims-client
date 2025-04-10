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

import fr.edyp.epims.json.ProAnalysisJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Load all Analyses for Grenoble
 *
 * @author JM235353
 *
 */
public class AnalysesTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<ProAnalysisJson> m_analysesJson;


    public AnalysesTask(AbstractDatabaseCallback callback, ArrayList<ProAnalysisJson> analysesJson) {
        super(callback, new TaskInfo("Ask for analyses ", false, null), TokenManager.TOKEN_ANALYSES_SERVER);

        URL = DataManager.getAnalysesServerURL()+"/api/analyses/";

        m_analysesJson = analysesJson;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<ProAnalysisJson[]> responseEntity = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, ProAnalysisJson[].class);

            ProAnalysisJson[] list = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();
            //String versionClass = headers.get("VersionClass").get(0);
            //String version = headers.get("Version").get(0);

            //DataManager.getDatabaseVersion().setVersion(versionClass, Integer.parseInt(version));

            for (ProAnalysisJson c : list) {
                m_analysesJson.add(c);
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
