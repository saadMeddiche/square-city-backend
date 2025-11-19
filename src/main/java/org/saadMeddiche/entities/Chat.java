package org.saadMeddiche.entities;

import org.saadMeddiche.WebSocketServer;

import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Chat extends WebSocketServer {

    public final String name;

    public final Set<Client> clients;

    public final List<Message> messages;

    public Chat(String name, int port) {
        super(port);
        this.name = name;
        this.clients = new HashSet<>();
        this.messages = new LinkedList<>();
    }

    public void clientJoined(Client client) {
        this.clients.add(client);
    }

    public void sendMessage(Client client, String messageContent) {
        Message message = new Message(client, this, messageContent);
        this.messages.add(message);
    }

    public void clientLeft(Client client) {
        this.clients.remove(client);
    }

    @Override
    protected void run(Socket socket) {

    }

}
