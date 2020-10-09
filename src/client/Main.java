package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * In this program you should give as arguments:
 *     1) Name of the file, which client want to send
 *     2) IP or DNS address of server
 *     3) Server's port
 *
 *     You should run the server first
 */

public class Main {

    public static void main(String[] args) {
        try {
            String filename = args[0];
            InetAddress addr = InetAddress.getByName(args[1]);
            int port = Integer.parseInt(args[2]);
            Client client = new Client(filename, addr, port);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}