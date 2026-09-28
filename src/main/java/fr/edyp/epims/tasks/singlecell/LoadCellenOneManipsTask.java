/*
 * Copyright (C) 2024
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

package fr.edyp.epims.tasks.singlecell;

import fr.edyp.epims.dataaccess.*;
import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.tasks.util.TasksUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Task to load SingleCell CellenOne Manipulations from server
 */
public class LoadCellenOneManipsTask extends AbstractAuthenticateDatabaseTask {

    private final String URL;
    private final ArrayList<CellenOneManipJson> m_manips;

    public LoadCellenOneManipsTask(AbstractDatabaseCallback callback, ArrayList<CellenOneManipJson> manips) {
        super(callback, new TaskInfo("Load list of CellenOne Manips", false, null), TokenManager.TOKEN_EPIMS_SERVER);
        URL = DataManager.getServerURL() + "/api/cellenonemanips";
        m_manips = manips;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {
        try {
            ResponseEntity<CellenOneManipJson[]> response = restTemplate.exchange(URL, HttpMethod.POST, entity, CellenOneManipJson[].class);

            m_taskError = TasksUtil.testStatusCode(response, restTemplate, entity, URL);
            if (m_taskError != null) {
                return false;
            }

            CellenOneManipJson[] manipList = response.getBody();
            if (manipList != null) {
                m_manips.addAll(Arrays.asList(manipList));
            }
        } catch (HttpStatusCodeException sce) {
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
