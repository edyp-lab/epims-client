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

package fr.epims.tasks.analyses;

import fr.edyp.epims.json.AnalysisPriceListJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Save a new Price List to the ePims Server
 * Only an admin can create a new Price List. It is used to create the bill part
 * of the Analysis Request.
 *
 * @author JM235353
 *
 */
public class SavePriceListTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private ArrayList<AnalysisPriceListJson> m_priceListArray;


    public SavePriceListTask(AbstractDatabaseCallback callback, ArrayList<AnalysisPriceListJson> priceListArray) {
        super(callback, new TaskInfo("Save Analysis Requests Price List ", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/savepricelist";

        m_priceListArray = priceListArray;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<AnalysisPriceListJson> requestEntity = new HttpEntity<>(m_priceListArray.get(0), entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<AnalysisPriceListJson[]> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, AnalysisPriceListJson[].class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            AnalysisPriceListJson[] priceListJson = response.getBody();
            m_priceListArray.clear();
            for (AnalysisPriceListJson priceList : priceListJson) {
                m_priceListArray.add(priceList);
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
