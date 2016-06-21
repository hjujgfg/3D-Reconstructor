package edu.lapidus.rec3d.networking;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Егор on 30.05.2016.
 */
public class CommunicationThread implements Runnable {
    private static final Logger logger = Logger.getLogger(CommunicationThread.class);

    private Socket clientSocket;
    private BufferedReader reader;
    CommunicationThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String input = reader.readLine();
                if (input != null) {
                    logger.info(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("We have encountered an exception");
            }
        }
    }
}
