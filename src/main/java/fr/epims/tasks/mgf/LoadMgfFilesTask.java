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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Load from the server all mgf files registered
 *
 * @author JM235353
 *
 */
public class LoadMgfFilesTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<MgfFileInfoJson> m_mgfArrayList;

    public LoadMgfFilesTask(AbstractDatabaseCallback callback, ArrayList<MgfFileInfoJson> mgfArrayList) {
        super(callback, new TaskInfo("Load list of MGF paths", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/mgflist/";

        m_mgfArrayList = mgfArrayList;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<MgfFileInfoJson[]> response = restTemplate.exchange(URL, //
                    HttpMethod.POST, entity, MgfFileInfoJson[].class);

            MgfFileInfoJson[] mgfList = response.getBody();

            for (MgfFileInfoJson mgfFileInfo : mgfList) {
                m_mgfArrayList.add(mgfFileInfo);
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
