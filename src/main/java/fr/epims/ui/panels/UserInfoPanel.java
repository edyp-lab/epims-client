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

package fr.epims.ui.panels;

import fr.epims.dataaccess.DataManager;
import fr.edyp.epims.json.CompanyJson;
import fr.edyp.epims.json.ContactJson;
import fr.epims.ui.common.IconManager;
import fr.epims.ui.common.TitlePanel;

import javax.swing.*;
import java.awt.*;

/**
 *
 * Panel with user (actor or contact) info
 *
 * @author JM235353
 *
 */
public class UserInfoPanel extends JPanel {

    public UserInfoPanel(ContactJson contactJson) {
        super(new GridBagLayout());

        setBorder(BorderFactory.createTitledBorder(""));


        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        TitlePanel title = new TitlePanel(contactJson.getFirstName()+" "+contactJson.getLastName(), IconManager.getIcon(IconManager.IconType.USER), null);
        JLabel emailLabel = new JLabel("eMail:");
        JLabel telephoneLabel = new JLabel("Telephone:");
        JLabel faxLabel = new JLabel("Fax:");
        JLabel affiliationLabel = new JLabel("Affiliation:");

        JLabel emailInfoLabel = new JLabel(contactJson.getEmail());
        JLabel telephoneInfoLabel = new JLabel(contactJson.getTelephoneNumber());
        JLabel faxInfoLabel = new JLabel(contactJson.getFaxNumber());

        CompanyJson companyJson = DataManager.getCompany(contactJson);


        JLabel affiliationInfo1Label = new JLabel((companyJson != null) ? companyJson.getName() : "");
        JLabel affiliationInfo2Label = new JLabel((companyJson != null) ? companyJson.getManager() : "");
        JLabel affiliationInfo3Label = new JLabel((companyJson != null) ? companyJson.getAddress(): "");
        JLabel affiliationInfo4Label = new JLabel((companyJson != null) ? companyJson.getPostalCode() : "");

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        add(title, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        add(emailLabel, c);

        c.gridx++;
        add(emailInfoLabel,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        add(telephoneLabel, c);

        c.gridx++;
        add(telephoneInfoLabel,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        add(faxLabel, c);

        c.gridx++;
        add(faxInfoLabel,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        add(affiliationLabel, c);

        c.gridx++;
        add(affiliationInfo1Label,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;


        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(affiliationInfo2Label,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;

        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(affiliationInfo3Label,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;

        c.gridx = 1;
        c.gridy++;
        c.weightx = 0;
        add(affiliationInfo4Label,c);

        c.gridx++;
        c.weightx = 1;
        add(Box.createGlue(), c);
        c.weightx = 0;


    }
}
