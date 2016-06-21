package edu.lapidus.rec3d.networking;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Егор on 30.05.2016.
 */
public class Server implements Runnable {

    public static void main (String[] args) {
        Server s = new Server();
    }

    private ServerSocket serverSocket;
    private Thread serverThread;
    private CommunicationThread communicationThread;
    private final static int PORT = 4545;

    private final static Logger logger = Logger.getLogger(Server.class);

    public Server() {
        serverThread = new Thread(this);
        serverThread.start();
    }

    @Override
    public void run() {
        Socket socket;
        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("socket created");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("port locked");
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket = serverSocket.accept();
                logger.info("client connected");
                communicationThread = new CommunicationThread(socket);
                new Thread(communicationThread).start();
                logger.info("Communication started");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
