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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.edyp.epims.util.error.ErrorResponse;
import fr.epims.dataaccess.*;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Create a new Study
 *
 * @author JM235353
 *
 */
public class AddStudyTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private StudyJson[] m_studyJson = null;


    public AddStudyTask(AbstractDatabaseCallback callback, StudyJson[] studyJson) {
        super(callback, new TaskInfo("Create Study "+studyJson[0].getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/addstudy";

        m_studyJson = studyJson;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {


            HttpEntity<StudyJson> requestEntity = new HttpEntity<>(m_studyJson[0], entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<StudyJson> response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, StudyJson.class);

            HttpStatusCode statusCode = response.getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                //Error creating study. Try to get more information
                try {
                    ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, ErrorResponse.class);
                    ErrorResponse error = errorResponse.getBody();
                    if (error != null) {
                        m_taskError = new TaskError("Error " + error.getErrorCode(),
                                error.getMessage() + (error.getDetails() != null ? " - " + error.getDetails() : ""));
                    } else {
                        m_taskError = new TaskError("Failed for unknown reason");
                    }
                } catch (Exception e) {
                    m_taskError = new TaskError("Failed for unknown reason");
                }
                return false;
            }

            DataManager.getDatabaseVersion().bumpVersion(StudyJson.class, null);
            DataManager.getDatabaseVersion().bumpVersion(ProjectJson.class, null);

            StudyJson studyJson = response.getBody();
            m_studyJson[0] = studyJson;


        } catch (HttpStatusCodeException sce) {
            // This catches HTTP errors regardless of server implementation
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                ErrorResponse error = mapper.readValue(sce.getResponseBodyAsString(), ErrorResponse.class);
                m_taskError = new TaskError("Error " + error.getErrorCode(), error.getMessage());
            } catch (Exception parseException) {
                parseException.printStackTrace();
                m_taskError = new TaskError("HTTP " + sce.getStatusCode(), sce.getMessage());
            }
            return false;
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
