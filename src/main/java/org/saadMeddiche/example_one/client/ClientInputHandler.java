package org.saadMeddiche.example_one.client;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class ClientInputHandler extends Thread {

    private final String EXIT_COMMAND = "/exit";

    private final PrintWriter printWriter;
    private final InputStream inputStream;

    public ClientInputHandler(InputStream inputStream, PrintWriter printWriter) {
        this.printWriter = printWriter;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {

        Scanner scanner = new Scanner(inputStream);

        String userInput;
        while (true) {

            userInput = scanner.nextLine();

            if(EXIT_COMMAND.equals(userInput)) break;

            printWriter.println(userInput);
        }

    }

}
