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

package fr.epims.preferences;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * Access to Preferences. File is named EpimsPreferences.properties and is saved by default
 * in user home.
 *
 * @author JM235353
 *
 */
public class EpimsPreferences {

    private static FilePreferences m_preferences;

    public static java.util.prefs.Preferences root() {

        if (m_preferences == null) {
            initPreferences(null);
        }

        return m_preferences;
    }

    public static void initPreferences(String path) {

        if (path == null) {
            path = getUserHome()+ File.separator+"EpimsPreferences.properties";  // by default Preferences.properties is saved in the user home
        } else {
            path = path+File.separator+"EpimsPreferences.properties";
        }

        m_preferences = new FilePreferences(new File(path), null, "");
    }


    private static String getUserHome() {

        String userHome = null;

        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win"))
        {
            //it is simply the location of the "AppData" folder
            userHome = System.getenv("AppData");
        } else { // linux or Mac

            userHome = System.getProperty("user.home");

            //if we are on a Mac, we are not done, we look for "Application Support"
            if (OS.contains("mac")) {
                userHome += "/Library/Application Support";
            }
        }

        if (userHome != null) {
            userHome = userHome +File.separator+".epims"+File.separator+"dev"+File.separator+"config";
            Path path = Paths.get(userHome);
            try {
                Files.createDirectories(path);
            } catch (IOException ie) {
                //logger.warn("Impossible to create user directory: "+userHome);
            }

        }

        if (userHome == null) {
            // should not happen
            userHome = "./";
        }

        return userHome;
    }

}
