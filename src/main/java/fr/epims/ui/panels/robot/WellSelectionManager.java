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

package fr.epims.ui.panels.robot;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Management of selected wells in the edited plate
 *
 * @author JM235353
 *
 */
public class WellSelectionManager {

    private HashMap<String, WellSelectionInterface> m_listeners = new HashMap<>();

    public WellSelectionManager() {

    }

    public void addWellSelectionInterface(String key, WellSelectionInterface selectionInterface) {
        m_listeners.put(key, selectionInterface);
    }

    public void selectionChanged(String sourceKey, HashSet<String> sampleNames) {
        for (String key : m_listeners.keySet()) {
            if (! key.equals(sourceKey)) {
                m_listeners.get(key).selectionChanged(sampleNames);
            }
        }
    }


}
