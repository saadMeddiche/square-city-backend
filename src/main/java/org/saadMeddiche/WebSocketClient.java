package org.saadMeddiche;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketClient {

    private final Logger logger = Logger.getLogger(WebSocketClient.class.getName());

    private final String hostname;
    private final int port;

    public WebSocketClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void start() {

        try(Socket socket = new Socket(hostname, port)) {

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(true) {
                Scanner input = new Scanner(System.in);

                System.out.println("Send a message request: ");
                String userMessage = input.nextLine();

                out.println(userMessage);
                logger.log(Level.INFO, "User message: " + userMessage);

                String serverResponse = in.readLine();
                logger.log(Level.INFO, "Server response: " + serverResponse);

            }

        } catch (UnknownHostException unknownHostException) {
            logger.log(Level.SEVERE, "The IP address of the host could not be determined.", unknownHostException);
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, "An I/O error occurred when creating the socket.", ioException);
        }

    }

}
