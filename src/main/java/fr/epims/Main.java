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

package fr.epims;


import fr.epims.preferences.EpimsPreferences;

import javax.swing.*;
import java.util.Locale;

/**
 *
 * Main class : Load Preferences and start main window
 *
 * @author JM235353
 *
 */
public class Main {

    public static void main(String[ ] args) {


        // Set Local for whole application
        Locale locale = new Locale("en", "US", "WIN");
        Locale.setDefault(locale);

        // init Preferences
        EpimsPreferences.initPreferences(null);



        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }
        MainFrame mainFrame = MainFrame.getMainWindow();
        // schedule this for the event dispatch thread (edt)
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                mainFrame.setVisible(true);

                mainFrame.connection();


            }
        });
    }

}
