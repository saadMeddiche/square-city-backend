package org.saadMeddiche.example_one.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerHandler extends Thread {

    private final BufferedReader bufferedReader;
    private final PrintStream printStream;
    private final Logger logger;

    public ChatServerHandler(BufferedReader bufferedReader, PrintStream printStream) {
        this.bufferedReader = bufferedReader;
        this.printStream = printStream;
        this.logger = Logger.getLogger(ChatServerHandler.class.getName());
    }

    @Override
    public void run() {

        try {
            String serverMessage;
            while ((serverMessage = bufferedReader.readLine()) != null) {
                printStream.println(serverMessage);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while reading from the socket!", e);
        }

    }

}
