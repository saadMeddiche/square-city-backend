package org.saadMeddiche.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * This is the websocket server.
 * */
public class SquareCityAirport implements Runnable {

    private final static Logger LOG = LogManager.getLogger(SquareCityAirport.class);

    public final String name;
    public final int port;

    public SquareCityAirport() {
        this(UUID.randomUUID().toString(), 7070);
    }

    public SquareCityAirport(String name, int port) {
        this.name = name;
        this.port = port;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOG.info("Airport[{}] opened port[{}]", name, serverSocket.getLocalPort());

            while (true) {

                Socket visitorSocker = serverSocket.accept();
                LOG.info("Visitor[{}] entered airport[{}] from port[{}]", visitorSocker, name, port);

                new BorderControlAgent(this, visitorSocker).start();

            }

        } catch (IOException ioException) {
            LOG.fatal("Airport[{}] failer to control port[{}]", name, port, ioException);
        }

    }

}
