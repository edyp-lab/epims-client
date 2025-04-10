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
import fr.edyp.epims.json.CompanyJson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Load Companies Data
 *
 * @author JM235353
 *
 */
public class CompaniesTask extends AbstractDatabaseTask {

    private String URL;

    private ArrayList<CompanyJson> m_companies;
    private String[] m_version;
    private String[] m_versionClass;

    public CompaniesTask(AbstractDatabaseCallback callback, ArrayList<CompanyJson> companies, String[] version, String[]versionClass) {
        super(callback, new TaskInfo("Load Companies", false, null));

        URL = DataManager.getServerURL()+"/api/companies";

        m_companies = companies;
        m_version = version;
        m_versionClass = versionClass;
    }

    @Override
    public boolean fetchData() {

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<CompanyJson[]> responseEntity =  restTemplate.getForEntity(URL, CompanyJson[].class);
            CompanyJson[] list = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();
            m_version[0] = headers.get("Version").get(0);
            m_versionClass[0] = headers.get("VersionClass").get(0);

            for (CompanyJson c : list) {
                m_companies.add(c);
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
