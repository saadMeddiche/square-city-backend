package org.saadMeddiche.example_one.entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    private final String name;
    private final int serverPort;
    public final CopyOnWriteArrayList<ClientHandler> clients;

    public ChatServer(String name, int serverPort) {
       this.name = name;
       this.serverPort = serverPort;
       this.clients = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(serverPort)) {

            System.out.printf("Chat Server[%s] is running on port[%s] and waiting for connections...", this.name, this.serverPort);
            System.out.println();

            // Thread to handle server admin input
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String serverMessage = scanner.nextLine();
                    broadcast("[Server]: " + serverMessage, null);
                }
            }).start();

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : this.clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

}