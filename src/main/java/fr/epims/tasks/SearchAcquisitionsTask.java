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
import fr.edyp.epims.json.ProtocolApplicationJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * Task used to search acquisitions correspondonding of different parameters like
 * text searched, start dat, end date, instrument, user...
 *
 * @author JM235353
 *
 */
public class SearchAcquisitionsTask  extends AbstractAuthenticateDatabaseTask {

    private String URL;

    private ArrayList<ProtocolApplicationJson> m_protocolApplications;
    private String m_searchText;
    private String m_acquisitionType;
    private int m_instrumentId;
    private String m_sampleOwnerActorKey;
    private String m_startDate;
    private String m_endDate;

    public SearchAcquisitionsTask(AbstractDatabaseCallback callback, String searchText, String acquisitionType, int instrumentId, String sampleOwnerActorKey, ArrayList<ProtocolApplicationJson> protocolApplications, String startDate, String endDate) {
        super(callback, new TaskInfo("Search Acquisitions", false, null), TokenManager.TOKEN_EPIMS_SERVER);

        URL = DataManager.getServerURL()+"/api/acquisitionsSearch/";

        m_protocolApplications = protocolApplications;
        m_searchText = searchText;
        m_acquisitionType = acquisitionType;
        m_instrumentId = instrumentId;
        m_sampleOwnerActorKey = sampleOwnerActorKey;
        m_startDate = startDate;
        m_endDate = endDate;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {

        try {

            if ((m_searchText == null) || (m_searchText.length()<=1)) {
                m_searchText = "_";
            } else {
                m_searchText = URLEncoder.encode(m_searchText, "UTF-8");
            }
            if ((m_acquisitionType == null) || (m_acquisitionType.length()<=1)) {
                m_acquisitionType = "_";
            }
            if ((m_sampleOwnerActorKey == null) || (m_sampleOwnerActorKey.length()<=1)) {
                    m_sampleOwnerActorKey = "_";
            }
            if ((m_startDate == null) || (m_startDate.length()<=1)) {
                m_startDate = "_";
            }
            if ((m_endDate == null) || (m_endDate.length()<=1)) {
                m_endDate = "_";
            }

            // Send request with GET method, and Headers.
            String url = URL+m_searchText+"/"+m_acquisitionType+"/"+m_instrumentId+"/"+m_sampleOwnerActorKey+"/"+m_startDate+"/"+m_endDate;
            ResponseEntity<ProtocolApplicationJson[]> response = restTemplate.exchange(url, //
                    HttpMethod.GET, entity, ProtocolApplicationJson[].class);

            ProtocolApplicationJson[] list = response.getBody();

            for (ProtocolApplicationJson s : list) {
                m_protocolApplications.add(s);
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
