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
import fr.edyp.epims.json.SampleJson;
import fr.edyp.epims.json.StudyJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Add a new Sample
 *
 * @author JM235353
 *
 */
public class AddSampleTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private ArrayList<SampleJson> m_sampleJson = null;


    public AddSampleTask(AbstractDatabaseCallback callback, StudyJson study, ArrayList<SampleJson> sampleJsons) {
        super(callback, new TaskInfo("Add "+sampleJsons.size()+" Samples in"+study.getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/addsample";

        m_sampleJson = sampleJsons;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<List<SampleJson>> requestEntity = new HttpEntity<>(m_sampleJson, entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<SampleJson[]> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, SampleJson[].class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            DataManager.getDatabaseVersion().bumpVersion(StudyJson.class, null);


            SampleJson[] samples = response.getBody();
            m_sampleJson.clear();
            for (SampleJson s : samples) {
                m_sampleJson.add(s);
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
