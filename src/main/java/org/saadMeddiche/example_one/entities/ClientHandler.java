package org.saadMeddiche.example_one.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private ChatServer chatServer;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.clientSocket = socket;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            // Get the username from the client
            out.println("Enter your username:");
            username = in.readLine();
            System.out.println("User " + username + " connected.");
            out.println("Welcome to the chat, " + username + "!");
            out.println("Type Your Message");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[" + username + "]: " + inputLine);
                chatServer.broadcast("[" + username + "]: " + inputLine, this);
            }

            // Remove the client handler from the list
            chatServer.clients.remove(this);
            System.out.println("User " + username + " disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

}
