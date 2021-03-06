package server;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ClientHandler extends Thread {
    private Socket client;
    private File uploadDir;
    private File uploadFile;
    private long filesize;
    private String filename;
    private byte[] nameLenBuf = new byte[2];
    private byte[] fileSizeBuf = new byte[8];
    private DataInputStream inputStream;
    private FileOutputStream ofs;

    ClientHandler(Socket client, File uploadDir){
        this.client = client;
        this.uploadDir = uploadDir;
        start();
    }

    @Override
    public void run(){
        try {
            recvInfo();
            createFile();
            recvFile();
        } catch (IOException | ReceiveException e) {
            uploadFile.delete();
            e.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
                ofs.close();
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createFile() throws IOException {
        int dotPos = filename.lastIndexOf('.');
        int slashPos = filename.lastIndexOf('\\');
        if (slashPos < 0) slashPos = 0;
        String tmpName = filename.substring(slashPos, dotPos);
        String uplFilename = uploadDir + "\\" + filename;
        int countFiles = 0;
        while (new File(uplFilename).exists()){
            countFiles++;
            tmpName = tmpName.substring(0, dotPos);
            tmpName += "(" + countFiles + ")";
            uplFilename = uploadDir + "\\" + tmpName + filename.substring(dotPos);
        }
        uploadFile = new File(uplFilename);
        uploadFile.createNewFile();
    }

    private void recvFile() throws IOException, ReceiveException {
        ofs = new FileOutputStream(uploadFile);
        int BUF_SIZE = 4096;
        long lastRecv = 0;
        byte[] buf = new byte[BUF_SIZE];
        long uplDataSize = 0;
        Date startTime = new Date();
        Date curTime = new Date();
        while (client.getInputStream().read(buf) > 0) {
            if (filesize - uplDataSize < BUF_SIZE) {
                ofs.write(buf, 0, (int) (filesize - uplDataSize));
                uplDataSize += filesize - uplDataSize;
                break;
            }
            ofs.write(buf);
            uplDataSize += buf.length;
            //buf = new byte[BUF_SIZE];
            if (new Date().getTime() - curTime.getTime() > 3000) {
                System.out.println(
                        "Client " + client.getInetAddress() +
                                ":\nAverage speed: " + uplDataSize / ((new Date().getTime() - startTime.getTime()) * 1024 / 1000) +
                                " Kb/sec\nCurrent speed: " + (uplDataSize - lastRecv) / ((new Date().getTime() - curTime.getTime()) * 1024 / 1000) +
                                " Kb/sec");
                curTime = new Date();
                lastRecv = uplDataSize;
            }
        }
        client.getOutputStream().write(0);
        client.getOutputStream().flush();
        System.out.println("File received");
        if (uplDataSize != filesize) {
            System.out.println("Something goes wrong. \nDownloaded data size: " + uplDataSize + "\nMust be: " + filesize);
            client.getOutputStream().write(1);
            throw new ReceiveException();
        }
}

    private void recvInfo() throws IOException {
        inputStream = new DataInputStream(client.getInputStream());
        inputStream.readFully(nameLenBuf);
        inputStream.readFully(fileSizeBuf);
        byte[] filenameBuf = new byte[bytesToShort(nameLenBuf)];
        inputStream.readFully(filenameBuf);
        filesize = bytesToLong(fileSizeBuf);
        filename = new String(filenameBuf);
    }

    private short bytesToShort(byte[] buf){
        short num = 0;
        for (int i = 2; i > 0; i--){
            num *= 256;
            num += (short) Byte.toUnsignedInt(buf[i-1]);
        }
        return num;
    }

    private long bytesToLong(byte[] buf){
        long num = 0;
        for (int i = 8; i > 0; i--){
            num *= 256;
            num += Byte.toUnsignedInt(buf[i-1]);
        }
        return num;
    }

}
