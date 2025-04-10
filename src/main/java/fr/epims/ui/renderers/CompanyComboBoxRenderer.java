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

package fr.epims.ui.renderers;

import fr.edyp.epims.json.CompanyJson;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Render for a Company in a combobox
 *
 * @author JM235353
 *
 */
public class CompanyComboBoxRenderer extends DefaultListCellRenderer {

    public CompanyComboBoxRenderer() {
    }

    /*
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value == null) {
            label.setText("< Select >");
        } else if (value instanceof CompanyJson) {
            label.setText(((CompanyJson)value).getName() );
        } else {
            label.setText(value.toString());
        }

        return label;
    }

}