package server;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ClientHandler extends Thread {
    private Socket client;
    public File uploadDir;
    public File uploadFile;
    public long filesize;
    public String filename;
    public byte[] nameLenBuf = new byte[2];
    public byte[] fileSizeBuf = new byte[8];

    ClientHandler(Socket client, File uploadDir){
        this.client = client;
        this.uploadDir = uploadDir;
        start();
    }

    @Override
    public void run(){
        try {
            recInfo();
            createFile();
            recvFile();
        } catch (IOException | ReceiveException e) {
            try {

                client.close();
                uploadFile.delete();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void createFile() throws IOException {
        int dotPos = filename.indexOf('.');
        String tmpName = filename.substring(0, dotPos);
        String uplFilename = uploadDir + "\\" + filename;
        int countFiles = 0;
        while (new File(uplFilename).exists()){
            countFiles++;
            tmpName = tmpName.substring(0, dotPos);
            tmpName+= "(" + countFiles + ")";
            uplFilename = uploadDir + "\\" + tmpName;
        }
        uploadFile = new File(uplFilename + filename.substring(dotPos));
        uploadFile.createNewFile();
    }

    private void recvFile() throws IOException, ReceiveException {
        DataInputStream is = new DataInputStream(client.getInputStream());
        DataOutputStream ofs = new DataOutputStream(new FileOutputStream(uploadFile));
        int BUF_SIZE = 4096;
        byte []buf = new byte[BUF_SIZE];
        long uplDataSize = 0;
        Date startTime = new Date();
        Date curTime = new Date();
        while (is.read(buf) > 0) {
            if (filesize - uplDataSize < BUF_SIZE){
                ofs.write(buf, 0, (int) (filesize - uplDataSize));
                uplDataSize += filesize - uplDataSize;
                break;
            }
            ofs.write(buf);
            uplDataSize += buf.length;
            buf = new byte[BUF_SIZE];
        }
        client.getOutputStream().write(0);
        client.getOutputStream().flush();

        if (uplDataSize != filesize){
            System.out.println("Something goes wrong. \nDownloaded data size: " + uplDataSize +"\nMust be: " + filesize);
            client.getOutputStream().write(1);
            ofs.close();
            throw new ReceiveException();
        }
        ofs.close();
    }


    private void recInfo() throws IOException {
        DataInputStream inputStream = new DataInputStream(client.getInputStream());
        inputStream.readFully(nameLenBuf);
        inputStream.readFully(fileSizeBuf);
        byte[] filenameBuf = new byte[bytesToShort(nameLenBuf)];
        inputStream.readFully(filenameBuf);
        filesize = bytesToLong(fileSizeBuf);
        filename = new String(filenameBuf);
        /*System.out.println(filesize);
        System.out.println(filename);*/
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
