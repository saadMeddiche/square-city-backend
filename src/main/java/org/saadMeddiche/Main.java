package org.saadMeddiche;

import org.saadMeddiche.example_one.entities.ChatServer;

import java.util.logging.Logger;


public class Main {

    private static final Logger logger = Logger.getLogger("MAIN");

    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer("Chat 1", SERVER_PORT);
        chatServer.start();
    }

}