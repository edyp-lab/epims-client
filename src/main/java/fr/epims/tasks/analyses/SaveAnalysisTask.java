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

import fr.edyp.epims.json.AnalysisMapJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


/**
 *
 * Save extra information of an Analysis Request in the ePims Server
 *
 * @author JM235353
 *
 */
public class SaveAnalysisTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private AnalysisMapJson[] m_analysis;


    public SaveAnalysisTask(AbstractDatabaseCallback callback, AnalysisMapJson[] analysis) {
        super(callback, new TaskInfo("Save Analysis Request "+analysis[0].getId(), true, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/saveanalysis";


        m_analysis = analysis;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<AnalysisMapJson> requestEntity = new HttpEntity<>(m_analysis[0], entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<AnalysisMapJson> response = restTemplate.exchange(URL, //
                    HttpMethod.POST, requestEntity, AnalysisMapJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            AnalysisMapJson analysis = response.getBody();

            m_analysis[0] = analysis;

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
