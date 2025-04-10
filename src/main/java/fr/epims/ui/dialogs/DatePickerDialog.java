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
import org.jdesktop.swingx.JXMonthView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 *
 * Dialog to Pick a date
 *
 * @author JM235353
 *
 */
public class DatePickerDialog  extends DefaultDialog {

    private DatePickerDialog m_this;

    private JXMonthView m_datePicker;

    public DatePickerDialog(Window parent, String title, Date d) {
        super(parent);

        setTitle(title);

        setInternalComponent(createDatePicker(d));

        setButtonVisible(DefaultDialog.BUTTON_OK, false);
        setButtonVisible(DefaultDialog.BUTTON_CANCEL, false);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);
        setStatusVisible(false);

        m_this = this;
    }

    public Date getSelectedDate() {
        return m_datePicker.getSelectionDate();
    }

    public JXMonthView createDatePicker(Date date) {


        m_datePicker = new JXMonthView(date);
        m_datePicker.setTraversable(true);
        m_datePicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {



                m_this.setVisible(false);
            }
        });


        return m_datePicker;
    }


}
