/*
 * Copyright (C) 2024
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 */

package fr.edyp.epims.tasks.singlecell;

import fr.edyp.epims.dataaccess.*;
import fr.edyp.epims.json.CellenOneImportJson;
import fr.edyp.epims.tasks.util.TasksUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/** Task to copy a CellenOne manipulation into a Study repository. */
public class ImportCellenOneManipTask extends AbstractAuthenticateDatabaseTask {

    private final String URL;
    private final CellenOneImportJson m_request;

    public ImportCellenOneManipTask(AbstractDatabaseCallback callback, String manipName, int studyId) {
        this(callback, new CellenOneImportJson(manipName, studyId, java.util.Collections.emptyList()));
    }

    public ImportCellenOneManipTask(AbstractDatabaseCallback callback, CellenOneImportJson request) {
        super(callback, new TaskInfo("Import CellenOne Manipulation", false, null), TokenManager.TOKEN_EPIMS_SERVER);
        URL = DataManager.getServerURL() + "/api/cellenonemanips/import";
        m_request = request;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {
        try {
            HttpEntity<CellenOneImportJson> requestEntity = new HttpEntity<>(m_request, entity.getHeaders());
            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, requestEntity, String.class);
            m_taskError = TasksUtil.testStatusCode(response, restTemplate, entity, URL);
            return m_taskError == null;
        } catch (HttpStatusCodeException sce) {
            m_taskError = TasksUtil.fromStatusCodeException(sce);
            return false;
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }
    }

    @Override
    public boolean needToFetch() {
        return true;
    }
}