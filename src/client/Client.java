package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class Client extends Thread {

    private Socket socket;
    private File file;
    private long fileSize;
    private byte[] filename;
    private InetAddress address;
    private int port;

    Client(String filename, InetAddress addr, int port) throws IOException {
        this.socket = new Socket(addr, port);
        this.address = addr;
        this.port = port;
        //socket.setSoTimeout(3000);
        String absFilename = new File(filename).getAbsolutePath();
        this.file = new File(absFilename);
        if (file.exists()){
            fileSize = file.length(); //size in bytes
            this.filename = file.getName().getBytes();
        }
        else {
            throw new IOException();
        }
    }

    public void run(){
        try {
            System.out.println("Sending info about file...");
            sendInfo();
            System.out.println("Sending file...");
            sendFile();
            socket.close();
        } catch (IOException e) {
            try {
                System.out.println("Failed! :^(");
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void sendFile() throws IOException {
        int BUF_SIZE = 4096;
        byte[] buf = new byte[BUF_SIZE];
        byte answ;
        int sentBytes = 0;
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        while(sentBytes < fileSize){
            int sendSize = bis.read(buf, 0, BUF_SIZE);
            socket.getOutputStream().write(buf, 0, sendSize);
            sentBytes += sendSize;
        }
        socket.getInputStream().read(buf, 0, 1);
        if (buf[0] == 0) System.out.println("Successful! :^)");
        bis.close();
    }

    private void sendInfo() throws IOException {
        byte[] fileInfo = new byte[10 + filename.length];
        numToBytes(fileInfo, (short)filename.length, 0);
        numToBytes(fileInfo, fileSize, 2);
        socket.getOutputStream().write(fileInfo, 0, 10);
        socket.getOutputStream().write(filename);
    }

    private void numToBytes(byte[] buf, short n, int offset) {
        for(int i = 0; i < 2; i++) {
            buf[offset + i] = (byte)(n % 256);
            n /= 256;
        }
    }

    private void numToBytes(byte[] buf, long n, int offset) {
        for(int i = 0; i < 8; i++) {
            buf[offset + i] = (byte) (n % 256);
            n /= 256;
        }
    }

}
