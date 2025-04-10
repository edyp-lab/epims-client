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
import fr.edyp.epims.json.ActorJson;
import fr.edyp.epims.json.CompanyJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Create a new Company (correspond to the affiliation of a contact)
 *
 * @author JM235353
 *
 */
public class CreateCompanyTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private CompanyJson[] m_companyJson = null;


    public CreateCompanyTask(AbstractDatabaseCallback callback, CompanyJson[] company) {
        super(callback, new TaskInfo("Create Affiliation "+company[0].getName(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/createcompany";

        m_companyJson = company;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<CompanyJson> requestEntity = new HttpEntity<>(m_companyJson[0], entity.getHeaders());

            // Send request with POST method, and Headers.
            ResponseEntity<CompanyJson> response = restTemplate.exchange(URL,
                    HttpMethod.POST, requestEntity, CompanyJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            DataManager.getDatabaseVersion().bumpVersion(CompanyJson.class, null);


            CompanyJson companyJson = response.getBody();
            m_companyJson[0] = companyJson;


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
