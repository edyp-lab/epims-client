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
import fr.edyp.epims.json.InstrumentJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 *
 * Load instruments (Spectrometers)
 *
 * @author JM235353
 *
 */
public class InstrumentsTask extends AbstractUnsecuredDatabaseTask {

    private String URL;

    private ArrayList<InstrumentJson> m_instruments;
    private String[] m_version;
    private String[] m_versionClass;

    public InstrumentsTask(AbstractDatabaseCallback callback, ArrayList<InstrumentJson> instruments, String[] version, String[] versionClass) {
        super(callback, new TaskInfo("Load Instruments", false, null));

        URL = DataManager.getServerURL()+"/api/spectrometers";

        m_instruments = instruments;
        m_version = version;
        m_versionClass = versionClass;
    }

    @Override
    public boolean fetchUnsecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {


        try {
            ResponseEntity<InstrumentJson[]> responseEntity = restTemplate.exchange(URL, //
                    HttpMethod.GET, entity, InstrumentJson[].class);
            InstrumentJson[] list = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();
            m_version[0] = headers.get("Version").get(0);
            m_versionClass[0] = headers.get("VersionClass").get(0);

            for (InstrumentJson c : list) {
                m_instruments.add(c);
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
