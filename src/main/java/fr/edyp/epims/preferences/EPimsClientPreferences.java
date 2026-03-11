/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

/*
 * ClientConfig utility to load optional client-side overrides from config/epims-client.properties
 */
package fr.edyp.epims.preferences;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public final class EPimsClientPreferences {
    private static final String CONFIG_FILE_NAME = "epims-client.properties";
    private static final String CONFIG_FOLDER = "config";

    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private EPimsClientPreferences() {}

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        try {
            File external = new File(new File("."), CONFIG_FOLDER + File.separator + CONFIG_FILE_NAME);
            if (external.exists() && external.isFile()) {
                try (FileInputStream fis = new FileInputStream(external)) {
                    PROPS.load(fis);
                    loaded = true;
                    return;
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger("Epims.Client").warn("Unable to load external config overrides", e);
        }
//        // Fallback to classpath resource at config/epims-client.properties
//        try (InputStream is = ClientConfig.class.getClassLoader().getResourceAsStream(CONFIG_FOLDER + "/" + CONFIG_FILE_NAME)) {
//            if (is != null) {
//                PROPS.load(is);
//            }
//        } catch (Exception e) {
//            LoggerFactory.getLogger("Epims.Client").warn("Unable to load classpath config overrides", e);
//        }
        loaded = true;
    }

    public static String get(String key) {
        ensureLoaded();
        return PROPS.getProperty(key);
    }

    public static String getFtpHost() {
        return get("ftp.host");
    }

    public static Integer getFtpPort() {
        String v = get("ftp.port");
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException nfe) {
            LoggerFactory.getLogger("Epims.Client").warn("Invalid ftp.port value in epims-client.properties: {}", v);
            return null;
        }
    }

//    public static String getFtpUser() {
//        return get("ftp.user");
//    }

    public static String getFtpKeyPath() {
        String path = get("ftp.keyPath");
        if (path != null && !path.trim().isEmpty()) return path.trim();
        // Default to ./config/epims-id_rsa in distribution folder
        return CONFIG_FOLDER + File.separator + "epims-id_rsa";
    }
}
