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
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;

/**
 *
 * Load Acquisition Types Task
 *
 * @author JM235353
 *
 */
public class AcquisitionTypesTask extends AbstractDatabaseTask {

    private String URL;

    ArrayList<String> m_acquisitionTypes;

    public AcquisitionTypesTask(AbstractDatabaseCallback callback, ArrayList<String> acquisitionTypes) {
        super(callback, new TaskInfo("Load Acquisition Types", false, null));

        URL = DataManager.getServerURL()+"/api/acquisitiontypes";

        m_acquisitionTypes = acquisitionTypes;
    }

    @Override
    public boolean fetchData() {

        RestTemplate restTemplate = new RestTemplate();

        try {
            String[] list = restTemplate.getForObject(URL, String[].class);

            for (String s : list) {
                m_acquisitionTypes.add(s);
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
