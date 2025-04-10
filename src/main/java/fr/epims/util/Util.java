package fr.epims.util;

import java.net.InetAddress;

public class Util {

    public static String getComputerName() {
        String hostName = null;
        try {
            final InetAddress addr = InetAddress.getLocalHost();
            hostName = new String(addr.getHostName());
        } catch(final Exception e) {
        }
        return hostName;
    }
}
