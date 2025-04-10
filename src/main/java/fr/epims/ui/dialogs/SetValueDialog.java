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

package fr.epims.ui.dialogs;

import fr.epims.ui.common.DefaultDialog;
import javax.swing.*;
import java.awt.*;

/**
 *
 * Dialog to Set a Value
 *
 * @author JM235353
 *
 */
public class SetValueDialog extends DefaultDialog {

    private JTextField m_valueTextField = null;

    private Class m_valueType;

    public SetValueDialog(Window parent, String valueName, Class valueType) {
        this(parent, valueName, valueType, null, null);
    }

    public SetValueDialog(Window parent, String valueName, Class valueType, String preInfo, String postInfo) {
        super(parent);

        m_valueType = valueType;

        setTitle("Pick "+valueName);

        setInternalComponent(createDataPanel(valueName, preInfo, postInfo));

        setButtonVisible(DefaultDialog.BUTTON_OK, true);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        setStatusVisible(false);
    }

    private JPanel createDataPanel(String valueName, String preInfo, String postInfo) {
        JPanel p = new JPanel(new GridBagLayout());

        p.setBorder(BorderFactory.createTitledBorder(""));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel label = new JLabel((preInfo == null) ? valueName+":" : preInfo);
        m_valueTextField = new JTextField(20);

        JLabel postLabel = (postInfo == null) ? null : new JLabel(postInfo);

        c.gridx = 0;
        c.gridy = 0;
        p.add(label, c);

        c.gridx++;
        p.add(m_valueTextField, c);

        if (postLabel != null) {
            c.gridx++;
            p.add(postLabel, c);
        }

        return p;
    }

    protected boolean okCalled() {
        String value = getStringValue();
        if (value.isEmpty()) {
            return true;
        }
        if (m_valueType.equals(Float.class)) {
            try {
                Float.valueOf(value); // try conversion
            } catch (Exception e) {
                return false;
            }
        } else if (m_valueType.equals(Integer.class)) {
            try {
                Integer.valueOf(value); // try conversion
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public Float getFloatValue() {
        try {
            return Float.valueOf(getStringValue());
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getIntegerValue() {
        try {
            return Integer.valueOf(getStringValue());
        } catch (Exception e) {
            return null;
        }
    }

    public String getStringValue() {
        return m_valueTextField.getText().trim();
    }


}