package server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread {
    private ServerSocket server;
    private ArrayList <ClientHandler> clients;
    private File uploadDir;
    private final static String dir = "/uploads";

    Server(int port){
        try {
            this.server = new ServerSocket(port, 0, InetAddress.getLocalHost());
            System.out.println("Server created! Address: " + server.getInetAddress());
            clients = new ArrayList<>();
            uploadDir = new File(new File(".").getCanonicalPath() + dir);
            uploadDir.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try{
            while (!server.isClosed()){
                Socket client = server.accept();
                clients.add(new ClientHandler(client, uploadDir));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
