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
import fr.edyp.epims.json.MgfKeysInfoJson;
import fr.edyp.epims.dataaccess.AbstractAuthenticateDatabaseTask;
import fr.edyp.epims.dataaccess.AbstractDatabaseCallback;
import fr.edyp.epims.dataaccess.DataManager;
import fr.edyp.epims.dataaccess.TaskError;
import fr.edyp.epims.dataaccess.TaskInfo;
import fr.edyp.epims.dataaccess.TaskInfoCallbackInterface;
import fr.edyp.epims.dataaccess.TokenManager;
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
 * Find the study/acquisition corresponding to a mgf file name
 *
 * @author JM235353
 *
 */
public class GetInfoForMgfFileTask extends AbstractAuthenticateDatabaseTask {

    private final String URL;
    private static final Logger logger   = LoggerFactory.getLogger(GetInfoForMgfFileTask.class);

    private final MgfFileInfo m_mgfFileInfo;
    private  MgfKeysInfoJson m_mgfKeyInfo;

    public GetInfoForMgfFileTask(AbstractDatabaseCallback callback, TaskInfoCallbackInterface infoCallback, MgfFileInfo mgfInfo) {
        super(callback, new TaskInfo(infoCallback, "Find Information for Mgf File ", false, null), TokenManager.TOKEN_EPIMS_SERVER);
        logger.debug("GetInfoForMgfFileTask : Find Study for Mgf File {}", mgfInfo.getMgfName());
        URL = DataManager.getServerURL()+"/api/mgfkeyinfo";
        m_mgfFileInfo  = mgfInfo;
        m_mgfKeyInfo = new MgfKeysInfoJson(m_mgfFileInfo.getMgfName(), m_mgfFileInfo.getStudyId(), m_mgfFileInfo.getAcqName(), null);
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<MgfKeysInfoJson> requestEntity = new HttpEntity<>(m_mgfKeyInfo, entity.getHeaders());
            ResponseEntity<MgfKeysInfoJson> responseEntity = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, MgfKeysInfoJson.class);

            m_taskError = TasksUtil.testStatusCode(responseEntity, restTemplate, requestEntity, URL);
            if(m_taskError != null){
                return false;
            }

            MgfKeysInfoJson resp = responseEntity.getBody();
            assert resp != null;
            m_mgfFileInfo.setStudyId(resp.getStudyId());
            m_mgfFileInfo.setAcqName(resp.getAcquisitionName());

        } catch (HttpStatusCodeException sce)
        {
            // This catches HTTP errors regardless of server implementation
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
