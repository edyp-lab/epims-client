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

package fr.epims.dataaccess;

import java.util.HashMap;

/**
 *
 * Authentification Token received from the server when the user logs.
 * This token is used each time an authentification request is done
 *
 * @author JM235353
 *
 */
public class TokenManager {

    public static String TOKEN_EPIMS_SERVER = "TOKEN_EPIMS_SERVER";
    public static String TOKEN_ANALYSES_SERVER = "TOKEN_ANALYSES_SERVER";

    private static HashMap<String, String> m_tokenMap = new HashMap<>();

    public static void setToken(String serverTokenKey, String token) {
        m_tokenMap.put(serverTokenKey, token);
    }

    public static String getToken(String serverTokenKey) {
        return m_tokenMap.get(serverTokenKey);
    }
}
