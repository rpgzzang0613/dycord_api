package kr.co.soymilk.dycord_api.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public final class ServerUtil {

    public static String getHostName() {
        String hostName = "";

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to get host name");
        }

        return hostName;
    }

    public static OSType getOsType() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("mac")) {
            return OSType.MAC;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OSType.LINUX;
        } else if (osName.contains("sunos")) {
            return OSType.SOLARIS;
        } else {
            return OSType.UNKNOWN;
        }
    }

}
