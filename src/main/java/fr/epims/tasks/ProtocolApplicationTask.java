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
import fr.edyp.epims.json.ProtocolApplicationJson;
import fr.edyp.epims.json.StudyJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Load Sample Data for a Study (For instance : Robot Run, Fragmentation (as "aliquotage"))
 *
 * @author JM235353
 *
 */
public class ProtocolApplicationTask  extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<ProtocolApplicationJson> m_protocolApplications;
    private StudyJson m_studyJson;

    public ProtocolApplicationTask(AbstractDatabaseCallback callback, StudyJson studyJson, ArrayList<ProtocolApplicationJson> protocolApplications) {
        super(callback, new TaskInfo("Load Sample Data for Study "+studyJson.getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/protocolApplications/";

        m_protocolApplications = protocolApplications;
        m_studyJson = studyJson;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<ProtocolApplicationJson[]> response = restTemplate.exchange(URL+m_studyJson.getId(), //
                    HttpMethod.GET, entity, ProtocolApplicationJson[].class);

            ProtocolApplicationJson[] list = response.getBody();

            for (ProtocolApplicationJson s : list) {
                m_protocolApplications.add(s);
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
