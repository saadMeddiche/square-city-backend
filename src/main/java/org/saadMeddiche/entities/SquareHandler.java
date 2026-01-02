package org.saadMeddiche.entities;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SquareHandler {

    private final Socket socket;

    public SquareHandler(Socket socket) {
        this.socket = socket;
    }

    public void listen() throws IOException {

        InputStream in = socket.getInputStream();

        while (!socket.isClosed()) {

            byte[] frames = readFrames(in);

            String message = new String(frames);

            System.out.println(message);

        }

    }

    private byte[] readFrames(InputStream in) throws IOException {

        byte firstByte = (byte) in.read();

        boolean fin = (firstByte & 0b1000_0000) != 0;

        // TODO: understand why fail when RSVs are not 0, read comment bellow for more context
        // I'll skip the RSVs, I don't think that I need them. And also I will not provide validation.
        // In RFC6455 they said 'If a nonzero value is received and none of the negotiated extensions defines the meaning of such a nonzero value, the receiving endpoint MUST _Fail the WebSocket Connection_.'
        // But why bother :)

        int opcode = (firstByte & 0b0000_1111);

        byte secondByte = (byte) in.read();

        boolean mask = (secondByte & 0b1000_0000) != 0;

        int payloadLength = calculatePayloadLength(secondByte, in);

        byte[] maskingKey = extractMaskingKey(mask, in);

        byte[] payload = new byte[payloadLength];
        in.read(payload);

        if(mask && maskingKey != null) {
            for(int i = 0; i < payloadLength; i++) {
                payload[i] ^= maskingKey[i % 4];
            }
        }

        return payload;
    }

    /**
     * Maskin, Unmasking example
     * */
    public static void main(String[] args) {
        // decoded payload
        // byte[] message = {'T','E','S','T'};
        // coded payload
        byte[] message = {'Q','D','Q','W'};
        byte[] key = {0x5, 0x1, 0x2, 0x3};

        for(int i = 0; i < message.length; i++) {
            message[i] = (byte) (message[i] ^ key[i % 4]);
        }
        System.out.println(new String(message));
    }

    private int calculatePayloadLength(byte secondByte, InputStream in) throws IOException {

        int payloadLength = (secondByte & 0b0111_1111);

        if(payloadLength == 126) {
            byte[] buffer = new byte[2];
            int countRead = in.read(buffer);
            payloadLength = 0;
            for(int i = 0; i < countRead; i++) {
                payloadLength += buffer[i];
            }
            return payloadLength;
        }

        if(payloadLength == 127) {
            byte[] buffer = new byte[8];
            int countRead = in.read(buffer);
            payloadLength = 0;
            for(int i = 0; i < countRead; i++) {
                payloadLength += buffer[i];
            }
            return payloadLength;
        }

        return payloadLength;

    }

    private byte[] extractMaskingKey(boolean mask, InputStream in) throws IOException {

        if(!mask) return null;

        byte[] maskingKey = new byte[4];

        in.read(maskingKey);

        return maskingKey;
    }

}
