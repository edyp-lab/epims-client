/*
 * Copyright (C) 2024
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 */

package fr.edyp.epims.tasks.singlecell;

import fr.edyp.epims.dataaccess.*;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.tasks.util.TasksUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;

/** Task to load the Studies available for a CellenOne manipulation import. */
public class LoadCellenOneStudiesTask extends AbstractAuthenticateDatabaseTask {

    private final String URL;
    private final ArrayList<StudyJson> m_studies;

    public LoadCellenOneStudiesTask(AbstractDatabaseCallback callback, ArrayList<StudyJson> studies) {
        super(callback, new TaskInfo("Load Studies for CellenOne Import", false, null), TokenManager.TOKEN_EPIMS_SERVER);
        URL = DataManager.getServerURL() + "/api/cellenonestudies";
        m_studies = studies;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {
        try {
            ResponseEntity<StudyJson[]> response = restTemplate.exchange(URL, HttpMethod.GET, entity, StudyJson[].class);
            m_taskError = TasksUtil.testStatusCode(response, restTemplate, entity, URL);
            if (m_taskError != null) {
                return false;
            }
            StudyJson[] studies = response.getBody();
            if (studies != null) {
                m_studies.addAll(Arrays.asList(studies));
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