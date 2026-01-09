package org.saadMeddiche.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.saadMeddiche.constants.PassportHeaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class BorderControlAgent extends Thread {

    private final static Logger LOG = LogManager.getLogger(BorderControlAgent.class);

    private final SquareCityAirport airport;

    private final Socket visitorSocket;

    private final PassportScanner passportScanner = new PassportScanner();

    public BorderControlAgent(SquareCityAirport airport, Socket visitorSocket) {
        this.airport = airport;
        this.visitorSocket = visitorSocket;
    }

    @Override
    public void run() {
        handlePassport(visitorSocket);
    }

    /**
     * Handles and verifies 'Opening Handshake'.
     * */
    private void handlePassport(Socket socket) {

        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            PassportScanResult scanResult = passportScanner.scan(in);

            if(!scanResult.isPassportValid()) {
                LOG.info("[{}]'s passport validation failed", socket);
                socket.close();
                return;
            }

            Map<String,String> passportHeaders = scanResult.passportHeaders();

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

            SquareHandler squareHandler = new SquareHandler(socket);
            squareHandler.listen();

            LOG.info("[{}]'s passport is accepted response, his name is {}", socket, scanResult.visitorInfo().squareName);

        } catch (Exception e) {
            LOG.fatal("Border control agent[{}] failer to verify passport for {}", airport.name, socket, e);
        }

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

    private final static String CONFIRMATION_RESPONSE_TEMPLATED = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: %s\r\n\r\n";

    private String generateConfirmationResponse(String acceptCode) {
        return String.format(CONFIRMATION_RESPONSE_TEMPLATED, acceptCode);
    }

    // TODO: understand why the browser do not allow custom header
    // TODO: write about the exploit of protocol header

}
