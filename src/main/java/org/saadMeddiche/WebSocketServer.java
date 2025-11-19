package org.saadMeddiche;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class WebSocketServer {

    private final Logger logger = Logger.getLogger(WebSocketServer.class.getName());

    private final int serverPort;

    private boolean isOpen = true;

    public WebSocketServer(int port) {
        this.serverPort = port;
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            logger.log(Level.INFO, "Listening on port " + serverPort);

            while (isOpen) {

                Socket socket = serverSocket.accept();

                this.run(socket);

            }

        } catch (IOException ioException) {
            logger.log(Level.SEVERE, "Error opening server socket", ioException);
        }

    }

    public void close() {
        isOpen = false;
    }

    protected abstract void run(Socket socket);

}