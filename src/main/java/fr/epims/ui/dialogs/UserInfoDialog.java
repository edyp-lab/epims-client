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

import fr.edyp.epims.json.ContactJson;
import fr.epims.ui.common.DefaultDialog;
import fr.epims.ui.panels.UserInfoPanel;

import java.awt.*;

/**
 *
 * Display information on a Contact
 *
 * @author JM235353
 *
 */
public class UserInfoDialog extends DefaultDialog {

    public UserInfoDialog(Window parent, ContactJson contactJson) {
        super(parent);

        setTitle("Contact Info");

        setInternalComponent(new UserInfoPanel(contactJson));

        setButtonVisible(DefaultDialog.BUTTON_OK, false);
        setButtonVisible(DefaultDialog.BUTTON_HELP, false);

        setButtonName(DefaultDialog.BUTTON_CANCEL, "Close");

        setStatusVisible(false);
    }
}
