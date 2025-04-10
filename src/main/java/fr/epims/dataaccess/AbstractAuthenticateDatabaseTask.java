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

package fr.epims.dataaccess;


import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 *
 * Base class for tasks which need to do a authentified server request
 *
 * @author JM235353
 *
 */
public abstract class AbstractAuthenticateDatabaseTask extends AbstractDatabaseTask {

    private String m_tokenServerKey;

    public AbstractAuthenticateDatabaseTask(AbstractDatabaseCallback callback, TaskInfo taskInfo, String tokenServerKey) {
        super(callback, taskInfo);

        m_tokenServerKey = tokenServerKey;
    }

    public final boolean fetchData() {

        HttpHeaders headers = new HttpHeaders();

        //
        // Authorization string (JWT)
        //
        headers.set("Authorization", TokenManager.getToken(m_tokenServerKey));
        //
        headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));

        // Request to return JSON format
        headers.setContentType(MediaType.APPLICATION_JSON);

        /*ClassVersionJson classVersion = getVersion();
        if (classVersion != null) {
            headers.add("version", classVersion.getVersion().toString());
            headers.add("versionclass", classVersion.getClassAsString());
        }*/
        // HttpEntity<String>: To get result as String.
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        // RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        return fetchSecuredData(entity, restTemplate);
    }


    public abstract boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate);
}
