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

import fr.edyp.epims.json.StudyJson;
import fr.epims.dataaccess.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * Change Study Comment
 *
 * @author JM235353
 *
 */
public class ChangeStudyCommentTask extends AbstractAuthenticateDatabaseTask {


    private String URL;

    private String m_comment;
    private StudyJson[] m_studyJson;


    public ChangeStudyCommentTask(AbstractDatabaseCallback callback, String comment, StudyJson[] studyJson) {
        super(callback, new TaskInfo("Change Comment of "+studyJson[0].getTitle()+" to "+comment, false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/studychangecomment";

        m_studyJson = studyJson;
        m_comment = comment;

    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            HttpEntity<String> requestEntity = new HttpEntity<>(m_comment, entity.getHeaders());

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
