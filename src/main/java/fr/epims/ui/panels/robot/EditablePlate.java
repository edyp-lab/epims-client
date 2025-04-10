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

import fr.edyp.epims.json.VirtualPlateJson;

/**
 *
 * Wrap a VirtualPlateJson to know if it has been modifieds
 *
 * @author JM235353
 *
 */
public class EditablePlate {

    private VirtualPlateJson m_plate;
    private boolean m_modified;

    public EditablePlate(VirtualPlateJson plate) {
        m_plate = plate;
        m_modified = false;
    }

    public VirtualPlateJson getPlate() {
        return m_plate;
    }
    public void setPlate(VirtualPlateJson plate) {
        m_plate = plate;
    }

    public void setModified(boolean modified) {
        m_modified = modified;
    }
    public boolean isModified() {
        return m_modified;
    }

    public String toString() {
        return m_plate.toString();
    }


}