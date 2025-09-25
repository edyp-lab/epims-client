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


package fr.edyp.epims.tasks.mgf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.edyp.epims.dataaccess.*;
import fr.edyp.epims.mgf.MgfFileInfo;
import fr.edyp.epims.util.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Find the study corresponding to a mgf file name
 *
 * @author JM235353
 *
 */
public class StudyForMgfTask extends AbstractAuthenticateDatabaseTask {

    private String URL;
    private static final Logger logger   = LoggerFactory.getLogger(StudyForMgfTask.class);

    private final MgfFileInfo m_mgfFileInfo;

    public StudyForMgfTask(AbstractDatabaseCallback callback, TaskInfoCallbackInterface infoCallback, MgfFileInfo mgfFileInfo) {
        super(callback, new TaskInfo(infoCallback, "Find Study for Mgf File ", false, null), TokenManager.TOKEN_EPIMS_SERVER);
        logger.debug("StudyForMgfTask : Find Study for Mgf File {}", mgfFileInfo.getFile().getName());
        URL = DataManager.getServerURL()+"/api/studyformgf";

        m_mgfFileInfo = mgfFileInfo;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<String> requestEntity = new HttpEntity<>(m_mgfFileInfo.getFile().getName(), entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<Integer> responseEntity = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, Integer.class);
            boolean result = testStatusCode(responseEntity, restTemplate, requestEntity);
            if(!result)
                return false;

            Integer studyId = responseEntity.getBody();
            m_mgfFileInfo.setStudyId(studyId);


        } catch ( HttpStatusCodeException sce) {
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

    private boolean testStatusCode(ResponseEntity<?> response, RestTemplate restTemplate, HttpEntity<?> requestEntity) {

        HttpStatusCode statusCode = response.getStatusCode();

        if (!statusCode.is2xxSuccessful()) {
            //Error calling task. Try to get more information
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
        return true;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }
}
