package org.saadMeddiche.example_one.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private final Logger logger = Logger.getLogger(Client.class.getName());

    private final String serverAddress;
    private final int serverPort;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() {

        try (Socket socket = new Socket(this.serverAddress, this.serverPort)) {

            System.out.println("Connected to the chat server!");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ChatServerHandler chatServerHandler = new ChatServerHandler(in, System.out);
            chatServerHandler.start();

            ClientInputHandler clientInputHandler = new ClientInputHandler(System.in, out);
            clientInputHandler.start();

            clientInputHandler.join();

        } catch (IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Error while connecting to the chat server!", e);
        }

    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 8080);
        client.start();
    }

}
