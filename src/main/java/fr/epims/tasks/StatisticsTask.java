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

import fr.edyp.epims.json.AcquisitionStatisticJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Task to load Statistics Data
 */
public class StatisticsTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<AcquisitionStatisticJson> m_acquisitionStatisticList;
    private int m_year;

    public StatisticsTask(AbstractDatabaseCallback callback, ArrayList<AcquisitionStatisticJson> acquisitionStatisticList, int year) {
        super(callback, new TaskInfo("Get Acquisitions Statistics", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/acquisitionsStatistic/";

        m_acquisitionStatisticList = acquisitionStatisticList;
        m_year = year;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            String url = URL+m_year;
            ResponseEntity<AcquisitionStatisticJson[]> response = restTemplate.exchange(url, //
                    HttpMethod.GET, entity, AcquisitionStatisticJson[].class);

            AcquisitionStatisticJson[] list = response.getBody();

            for (AcquisitionStatisticJson s : list) {
                m_acquisitionStatisticList.add(s);
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
