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


package fr.epims.tasks.mgf;

import fr.edyp.epims.json.MgfFileInfoJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Register a mgf file to the server
 *
 * @author JM235353
 *
 */
public class RegisterMgfTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private MgfFileInfoJson m_mgfFileInfo;

    public RegisterMgfTask(AbstractDatabaseCallback callback, MgfFileInfoJson mgfFileInfo) {
        super(callback, new TaskInfo("Register Mgf File "+mgfFileInfo.getName(), true, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/createmgf";

        m_mgfFileInfo = mgfFileInfo;

        m_prioritary = true; // must be done as soon as possible

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<MgfFileInfoJson> requestEntity = new HttpEntity<>(m_mgfFileInfo, entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<Integer> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, Integer.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            Integer id = response.getBody(); // not used for the moment


        } catch (RestClientResponseException e) {

            m_taskError = new TaskError(e);

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
