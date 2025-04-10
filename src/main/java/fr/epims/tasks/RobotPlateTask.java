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
import fr.edyp.epims.json.RobotDataJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Load all Plates with wells and dispatched samples
 *
 * @author JM235353
 *
 */
public class RobotPlateTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private RobotDataJson[] m_robotDataJson;


    public RobotPlateTask(AbstractDatabaseCallback callback, RobotDataJson[] robotDataJson) {
        super(callback, new TaskInfo("Ask for plates ", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/robotdata";

        m_robotDataJson = robotDataJson;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<RobotDataJson> responseEntity = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, RobotDataJson.class);

            RobotDataJson robotDataJson = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();
            String versionClass = headers.get("VersionClass").get(0);
            String version = headers.get("Version").get(0);

            DataManager.getDatabaseVersion().setVersion(versionClass, Integer.parseInt(version));




            m_robotDataJson[0] = robotDataJson;


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
