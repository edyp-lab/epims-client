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
import fr.edyp.epims.json.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Create a contact to a program or project or study
 *
 * @author JM235353
 *
 */
public class AddContactTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private ProgramJson[] m_programJson = null;
    private ProjectJson[] m_projectJson = null;
    private StudyJson[] m_studyJson = null;

    private List<ContactJson> m_contact;

    /**
     * Add Contact to Program
     */
    public AddContactTask(AbstractDatabaseCallback callback, ProgramJson[] programJson, List<ContactJson> contact) {
        super(callback, new TaskInfo("Add Contact "+contact.get(0).getLastName()+" to "+programJson[0].getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/programaddcontact";


        m_programJson = programJson;
        m_contact = contact;

    }

    /**
     * Add Contact to Project
     */
    public AddContactTask(AbstractDatabaseCallback callback, ProjectJson[] projectJson, List<ContactJson> contact) {
        super(callback, new TaskInfo("Add Contact "+contact.get(0).getLastName()+" to "+projectJson[0].getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/projectaddcontact";

        m_projectJson = projectJson;
        m_contact = contact;

    }

    /**
     * Add Contact to Study
     */
    public AddContactTask(AbstractDatabaseCallback callback, StudyJson[] studyJson, List<ContactJson> contact) {
        super(callback, new TaskInfo("Add Contact "+contact.get(0).getLastName()+" to "+studyJson[0].getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/studyaddcontact";

        m_studyJson = studyJson;
        m_contact = contact;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        if (m_programJson != null) {
            return fetchSecuredDataForProgram(entity, restTemplate);
        } else if (m_projectJson != null) {
            return fetchSecuredDataForProject(entity, restTemplate);
        } else {
            return fetchSecuredDataForStudy(entity, restTemplate);
        }
    }

    private boolean fetchSecuredDataForProgram(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            ArrayList<Integer> keys = new ArrayList<>();
            for (ContactJson contact : m_contact) {
                keys.add(contact.getId());
            }

            HttpEntity<List<Integer>> requestEntity = new HttpEntity<>(keys, entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<ProgramJson> response = restTemplate.exchange(URL+"/"+m_programJson[0].getId(), //
                    HttpMethod.POST, requestEntity, ProgramJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            ProgramJson programJson = response.getBody();
            ArrayList<ProjectJson> projects = DataManager.getProjects(programJson.getProjectsKeys());
            programJson.setProjects(projects);

            DataManager.getDatabaseVersion().bumpVersion(ProgramJson.class, null);

            m_programJson[0] = programJson;

        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }

        return true;
    }


    private boolean fetchSecuredDataForProject(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            ArrayList<Integer> keys = new ArrayList<>();
            for (ContactJson contact : m_contact) {
                keys.add(contact.getId());
            }

            HttpEntity<List<Integer>> requestEntity = new HttpEntity<>(keys, entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<ProjectJson> response = restTemplate.exchange(URL+"/"+m_projectJson[0].getId(), //
                    HttpMethod.POST, requestEntity, ProjectJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            ProjectJson projectJson = response.getBody();
            ArrayList<StudyJson> studies = DataManager.getStudies(projectJson.getStudiesKeys());
            projectJson.setStudies(studies);

            DataManager.getDatabaseVersion().bumpVersion(ProjectJson.class, null);

            m_projectJson[0] = projectJson;

        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }

        return true;
    }


    private boolean fetchSecuredDataForStudy(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            ArrayList<Integer> keys = new ArrayList<>();
            for (ContactJson contact : m_contact) {
                keys.add(contact.getId());
            }

            HttpEntity<List<Integer>> requestEntity = new HttpEntity<>(keys, entity.getHeaders());

            // Send request with GET method, and Headers.
            ResponseEntity<StudyJson> response = restTemplate.exchange(URL+"/"+m_studyJson[0].getId(), //
                    HttpMethod.POST, requestEntity, StudyJson.class);

            HttpStatus statusCode = response.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                m_taskError = new TaskError("Failed for unknown reason");
                return false;
            }

            StudyJson studyJson = response.getBody();

            DataManager.getDatabaseVersion().bumpVersion(StudyJson.class, null);

            m_studyJson[0] = studyJson;

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
