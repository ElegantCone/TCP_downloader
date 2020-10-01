package server;

public class ReceiveException extends Exception {

    @Override
    public String getMessage() {
        return "Can't receive file";
    }
}
