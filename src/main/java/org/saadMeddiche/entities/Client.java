package org.saadMeddiche.entities;

import java.net.SocketAddress;
import java.util.UUID;

public class Client {

    private final UUID identifier;

    public String name;
    private SocketAddress address;
    private Chat chat;

    public Client(String name, SocketAddress address) {
        this(name, address, null);
    }

    public Client(String name, SocketAddress address, Chat chat) {

        if (name == null || address == null)
            throw new RuntimeException("name and address cannot be null");

        this.identifier = UUID.randomUUID();
        this.name = name;
        this.address = address;
        this.chat = chat;

    }

    public void connect(Chat chat) {
        this.chat = chat;
        this.chat.clientJoined(this);
    }

    public void disconnect() {
        this.chat.clientLeft(this);
        this.chat = null;
    }

    public boolean isConnected() {
        return chat != null;
    }

    public void sendMessage(String message) {
        if(isConnected()) {
            this.chat.sendMessage(this, message);
        }
    }

    @Override
    public boolean equals(Object object) {

        if(getClass() != object.getClass()) return false;

        Client client = (Client) object;

        return this.identifier.equals(client.identifier);

    }

}
