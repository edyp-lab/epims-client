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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Task to export edited injections and sample numbers of an analysis to the Profi server Database.
 * It will be saved on the database only if the analysis has not been closed.
 *
 * @author JM235353
 *
 */
public class ExportAnalysisInfoToProfiTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private int m_analysisId;
    private int m_nbInjections;
    private int m_nbSamples;

    private StringBuilder m_answerMessage;


    public ExportAnalysisInfoToProfiTask(AbstractDatabaseCallback callback, int analysisId, Integer nbInjections, Integer nbSamples, StringBuilder answerMessage) {
        super(callback, new TaskInfo("Export Analysis Info to Profi Server", false, null), TokenManager.TOKEN_ANALYSES_SERVER);

        URL = DataManager.getAnalysesServerURL()+"/api/modifyanalysis/";

        m_analysisId = analysisId;
        m_nbInjections = (nbInjections == null) ? -1 : nbInjections;
        m_nbSamples = (nbSamples == null) ? -1 : nbSamples;
        m_answerMessage = answerMessage;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            String url = URL+m_analysisId+"/"+m_nbSamples+"/"+m_nbInjections;

            // Send request with GET method, and Headers.
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, //
                    HttpMethod.GET, entity, String.class);

            String answer = responseEntity.getBody();
            m_answerMessage.setLength(0);
            if (answer != null) {
                m_answerMessage.append(answer);
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