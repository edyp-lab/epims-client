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


package fr.epims.tasks.archive;

import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fr.edyp.epims.json.ControlAcquisitionArchivableJson;

import java.util.ArrayList;

/**
 *
 * Load the list of Control Acquisitions which could be archived.
 *
 * @author JM235353
 *
 */
public class LoadControlToArchiveTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<ControlAcquisitionArchivableJson> m_controlAcquisitionArchivable;

    public LoadControlToArchiveTask(AbstractDatabaseCallback callback, ArrayList<ControlAcquisitionArchivableJson> controlAcquisitionArchivable) {
        super(callback, new TaskInfo("Load Controls to Archive information", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/controltoarchivelist";

        m_controlAcquisitionArchivable = controlAcquisitionArchivable;
    }


    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {


        try {

            // Send request with GET method, and Headers.
            ResponseEntity<ControlAcquisitionArchivableJson[]> responseEntity = restTemplate.exchange(URL, //
                    HttpMethod.POST, entity, ControlAcquisitionArchivableJson[].class);

            ControlAcquisitionArchivableJson[] list = responseEntity.getBody();

            for (ControlAcquisitionArchivableJson c : list) {
                m_controlAcquisitionArchivable.add(c);
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
