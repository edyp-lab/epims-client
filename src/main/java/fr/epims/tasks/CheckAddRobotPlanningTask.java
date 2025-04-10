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
import fr.edyp.epims.json.RobotPlanningJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Used to check if RobotPlannings can be added (can not add 2 RobotPlannings
 * with the same Sample)
 *
 * @author JM235353
 *
 */
public class CheckAddRobotPlanningTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<RobotPlanningJson> m_robotPlanningJsonArrayList;

    private String[] m_result;


    public CheckAddRobotPlanningTask(AbstractDatabaseCallback callback, String[] result, ArrayList<RobotPlanningJson> robotPlanningJsonArrayList) {
        super(callback, new TaskInfo("Check Robot Planning ", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/checkrobotplanning";

        m_result = result;
        m_robotPlanningJsonArrayList = robotPlanningJsonArrayList;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity requestEntity = new HttpEntity<>(m_robotPlanningJsonArrayList, entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<String> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, String.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }


            String result = response.getBody(); // if null : everything is ok
            m_result[0] = result;

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
