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
import fr.edyp.epims.json.RobotPlanningJson;
import fr.edyp.epims.json.VirtualPlateJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Save a Plate (with corresponding wells and samples)
 *
 * @author JM235353
 *
 */
public class SavePlateTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private VirtualPlateJson m_virtualPlate;


    public SavePlateTask(AbstractDatabaseCallback callback, VirtualPlateJson virtualPlate) {
        super(callback, new TaskInfo("Save Robot Plate "+virtualPlate.getName(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/saveplate";

        m_virtualPlate = virtualPlate;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity requestEntity = new HttpEntity<>(m_virtualPlate, entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<Void> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, Void.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            DataManager.getDatabaseVersion().bumpVersion(RobotDataJson.class, null);

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
