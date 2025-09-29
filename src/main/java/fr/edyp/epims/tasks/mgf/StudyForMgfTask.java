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

import fr.edyp.epims.dataaccess.*;
import fr.edyp.epims.mgf.MgfFileInfo;
import fr.edyp.epims.tasks.util.TasksUtil;
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

    private final String URL;
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

            HttpEntity<String> requestEntity = new HttpEntity<>(m_mgfFileInfo.getMgfName(), entity.getHeaders());
            ResponseEntity<Integer> responseEntity = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, Integer.class);

            m_taskError  = TasksUtil.testStatusCode(responseEntity, restTemplate, requestEntity,URL);
            if(m_taskError != null)
                return false;

            Integer studyId = responseEntity.getBody();
            m_mgfFileInfo.setStudyId(studyId);

        } catch ( HttpStatusCodeException sce)
        {
            m_taskError = TasksUtil.fromStatusCodeException(sce);
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
