package org.saadMeddiche;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    private static final Logger logger = Logger.getLogger("MAIN");

    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {

        try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            logger.log(Level.INFO, "Starting server on port " + serverSocket.getLocalPort());

            Socket socket = serverSocket.accept();
            logger.log(Level.INFO, "Accepted connection from " + socket.getRemoteSocketAddress());

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            Scanner scanner = new Scanner(inputStream , StandardCharsets.UTF_8);

            String data = scanner.useDelimiter("\r\n\r\n").next();

            logger.info("Handshaking " + data);

            if(data.startsWith("GET /")) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);

                if(match.find()) {

                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                            + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);

                    outputStream.write(response);
                }

            }


        } catch(IOException | NoSuchAlgorithmException ioException) {
            logger.log(Level.SEVERE, "Error opening server socket", ioException);
        }

    }
}