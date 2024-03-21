package example;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SystemIDGenerator {

    public static String getSystemID() {
        StringBuilder sb = new StringBuilder();

        // Get operating system information
        sb.append(System.getProperty("os.name"));
        sb.append(System.getProperty("os.version"));
        sb.append(System.getProperty("os.arch"));

        // Get network interface information
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress != null) {
                    for (byte b : hardwareAddress) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Get host name
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            sb.append(inetAddress.getHostName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a hash of the concatenated information
        int hashCode = sb.toString().hashCode();

        return Integer.toHexString(hashCode);
    }

    public static void main(String[] args) {
        String systemID = getSystemID();
        System.out.println("System ID: " + systemID);
    }
}
