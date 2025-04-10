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
import fr.edyp.epims.json.ProjectJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Load all Projects
 *
 * @author JM235353
 *
 */
public class ProjectsTask extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<ProjectJson> m_projects;
    private String[] m_version;
    private String[] m_versionClass;

    public ProjectsTask(AbstractDatabaseCallback callback, ArrayList<ProjectJson> projects, String[] version, String[]versionClass) {
        super(callback, new TaskInfo("Load Projects", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/projects";

        m_projects = projects;
        m_version = version;
        m_versionClass = versionClass;
    }


    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            // Send request with GET method, and Headers.
            ResponseEntity<ProjectJson[]> responseEntity = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, ProjectJson[].class);

            ProjectJson[] list = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();
            m_version[0] = headers.get("Version").get(0);
            m_versionClass[0] = headers.get("VersionClass").get(0);


            for (ProjectJson p : list) {
                m_projects.add(p);
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
