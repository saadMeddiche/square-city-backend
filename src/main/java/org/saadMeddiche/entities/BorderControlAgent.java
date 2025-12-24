package org.saadMeddiche.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.saadMeddiche.constants.PassportHeaders;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.*;

/**
 * This is the websocket server.
 * */
public class BorderControlAgent implements Runnable {

    private final static Logger LOG = LogManager.getLogger(BorderControlAgent.class);

    private final String name;
    private final int port;

    public BorderControlAgent() {
        this(UUID.randomUUID().toString(), 7070);
    }

    public BorderControlAgent(String name, int port) {
        this.name = name;
        this.port = port;
    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOG.info("Border control agent[{}] listening on port {}", name, serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOG.info("Visitor from {}", clientSocket);

                new Thread(() -> handlePassport(clientSocket)).start();
            }

        } catch (IOException ioException) {
            LOG.fatal("Border control agent[{}] failer to control port[{}]", name, port, ioException);
        }

    }

    /**
     * Handles and verifies 'Opening Handshake'.
     * */
    private void handlePassport(Socket socket) {

        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            PassportValidation passportValidation = validatePassport(in);

            if(!passportValidation.isValid) {
                LOG.info("[{}]'s passport validation failed", socket);
                socket.close();
                return;
            }

            Map<String,String> passportHeaders = passportValidation.passportHeaders;

            VisitorInfo visitorInfo = extractVisitorInfo(passportHeaders.get(PassportHeaders.SEC_WEBSOCKET_PROTOCOL));

            if(!visitorInfo.isExtractionSuccess) {
                LOG.info("[{}]'s visitor extraction failed", socket);
                socket.close();
                return;
            }

            String webSocketKey = passportHeaders.get(PassportHeaders.SEC_WEBSOCKET_KEY);

            Optional<String> optionalConfirmationCode = generateConfirmationCode(webSocketKey);

            if(optionalConfirmationCode.isEmpty()) {
                // If I am not mistaken, this block of code should never be executed
                LOG.info("[{}]'s passport confirmation failed []", socket);
                socket.close();
                return;
            }

            String acceptCode = optionalConfirmationCode.get();

            String confirmationResponse = generateConfirmationResponse(acceptCode);

            out.write(confirmationResponse);
            out.flush();

            LOG.info("[{}]'s passport is accepted response, his name is {}", socket, visitorInfo.fullName);

        } catch (Exception e) {
            LOG.fatal("Border control agent[{}] failer to verify passport for {}", name, socket, e);
        }

    }

    record PassportValidation(boolean isValid, Map<String,String> passportHeaders) {}

    /**
     * Validate the handshake based on RFC6455 rules, also custom ones related to the app.
     * */
    private PassportValidation validatePassport(BufferedReader in) {


        // =================== RFC6455's rules ===================

        if(!isGet(in)) {
            LOG.warn("Passport not valid, cause [NOT GET]");
            return new PassportValidation(false,null);
        }

        Map<String,String> passportHeaders = readHeader(in);

        if(passportHeaders.isEmpty()) {
            LOG.warn("Passport not valid, cause [NO HEADERS]");
            return new PassportValidation(false,null);
        }

        if(!"websocket".equalsIgnoreCase(passportHeaders.get(PassportHeaders.UPGRADE))) {
            LOG.warn("Passport not valid, cause [NOT WEBSOCKET UPGRADE]");
            return new PassportValidation(false,null);
        }

        if(passportHeaders.get(PassportHeaders.SEC_WEBSOCKET_KEY) == null) {
            LOG.warn("Passport not valid, cause [NO KEY PROVIDED]");
            return new PassportValidation(false,null);
        }

        // =================== Custom App's rules ===================

        // Because of browser security limitations, there is no way to send custom headers,
        // But there is a trick. The protocol header is modifiable.
        // So I will exploit that to send whatever I want.
        if(passportHeaders.get(PassportHeaders.SEC_WEBSOCKET_PROTOCOL) == null) {
            LOG.warn("Passport not valid, cause [NO PROTOCOL PROVIDED]");
            return new PassportValidation(false,null);
        }

        return new PassportValidation(true, passportHeaders);

    }

    private boolean isGet(BufferedReader in) {

        try {

            String firstLine = in.readLine();
            return firstLine.startsWith("GET /");

        } catch (Exception e) {
            LOG.warn("Failed to verify if HTTP request is get, returning false", e);
            return false;
        }

    }

    /**
     * this method returns an empty map when it fails to extract even a single header.
     * */
    private Map<String,String> readHeader(BufferedReader in) {

        Map<String , String> headers = new HashMap<>();

        String line;
        try {

            while((line = in.readLine()) != null) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    headers.put(parts[0].trim(), parts[1].trim());
                }
            }

        } catch (Exception e) {
            LOG.warn("Failed to read passport headers, returning empty map", e);
            return new HashMap<>();
        }

        return headers;

    }

    private final static String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    // TODO: understand why the server need to return 'Sec-WebSocket-Accept' header with this encoded and hashed value
    /**
     * RFC6455 require us to SHA-1 hashing and base64-encoding the concatenated
     */
    private Optional<String> generateConfirmationCode(String clientNonce) {
        // RFC6455 calls webSocketKey as client's nonce, Just if you wonder :)
        try {

            String concatenateInput = clientNonce + GUID;

            MessageDigest sha1 = MessageDigest.getInstance("SHA1");

            byte[] hash = sha1.digest(concatenateInput.getBytes());

            return Optional.of(Base64.getEncoder().encodeToString(hash));

        } catch (Exception e) {
            LOG.error("Failed to generate confirmation code", e);
            return Optional.empty();
        }

    }

    private final static String CONFIRMATION_RESPONSE_TEMPLATED = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: %s";

    private String generateConfirmationResponse(String acceptCode) {
        return String.format(CONFIRMATION_RESPONSE_TEMPLATED, acceptCode);
    }

    // TODO: understand why the browser do not allow custom header
    // TODO: write about the exploit of protocol header

    record VisitorInfo(boolean isExtractionSuccess, String fullName, String color) {}

    private VisitorInfo extractVisitorInfo(String websocketProtocolValue) {
        if(websocketProtocolValue.contains(",")) {
            String[] parts = websocketProtocolValue.split(",");
            return new VisitorInfo(true, parts[0], parts[1]);
        }
        return new VisitorInfo(false, null, null);
    }

}
