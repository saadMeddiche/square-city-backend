package org.saadMeddiche.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.saadMeddiche.constants.PassportHeaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class validates visitor passport (aka handshake),
 * and extracts visitor info if it is valid.
 * */
public class PassportScanner {

    private final static Logger LOG = LogManager.getLogger(PassportScanner.class);

    private final Pattern VISITOR_INFO_PATTERN = Pattern.compile("\\?visitor\\.name=(\\w+)&visitor\\.color=(\\w+)");

    /**
     * Validate the handshake based on RFC6455 rules, also custom ones related to the app.
     * */
    public PassportScanResult scan(BufferedReader in) {

        Optional<String> optionalPassportTitle = extractPassportTitle(in);

        if(optionalPassportTitle.isEmpty()) {
            LOG.warn("Passport not valid, cause [PASSPORT TITLE MISSING]");
            return new PassportScanResult(false,null, null);
        }

        String passportTitle = optionalPassportTitle.get();

        // =================== RFC6455's rules ===================

        if(!isGet(passportTitle)) {
            LOG.warn("Passport not valid, cause [NOT GET]");
            return new PassportScanResult(false,null, null);
        }

        Map<String,String> passportHeaders = readHeader(in);

        if(passportHeaders.isEmpty()) {
            LOG.warn("Passport not valid, cause [NO HEADERS]");
            return new PassportScanResult(false,null, null);
        }

        if(!"websocket".equalsIgnoreCase(passportHeaders.get(PassportHeaders.UPGRADE))) {
            LOG.warn("Passport not valid, cause [NOT WEBSOCKET UPGRADE]");
            return new PassportScanResult(false,null, null);
        }

        if(passportHeaders.get(PassportHeaders.SEC_WEBSOCKET_KEY) == null) {
            LOG.warn("Passport not valid, cause [NO KEY PROVIDED]");
            return new PassportScanResult(false,null, null);
        }

        // =================== Custom App's rules ===================

        Matcher matcher = VISITOR_INFO_PATTERN.matcher(passportTitle);

        if(!matcher.find()) {
            LOG.warn("Passport not valid, cause [VISITOR INFO MISSING]");
            return new PassportScanResult(false,null, null);
        }

        String visitorName = matcher.group(1);

        String visitorColor = matcher.group(2);

        if(visitorName == null || visitorName.isEmpty()) {
            LOG.warn("Passport not valid, cause [VISITOR NAME MISSING]");
            return new PassportScanResult(false,null, null);
        }

        if(visitorColor == null || visitorColor.isEmpty()) {
            LOG.warn("Passport not valid, cause [VISITOR COLOR MISSING]");
            return new PassportScanResult(false,null, null);
        }

        return new PassportScanResult(true, new Visitor(visitorName,visitorColor), passportHeaders);

    }

    private Optional<String> extractPassportTitle(BufferedReader in) {

        try {

            String passportTitle = in.readLine();
            return Optional.of(passportTitle);

        }
        catch (IOException e) {
            LOG.warn("Failed to extract passport title, returning empty", e);
            return Optional.empty();
        }

    }

    private boolean isGet(String passportTitle) {

        try {

            return passportTitle.startsWith("GET /");

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

            while((line = in.readLine()) != null && !line.isEmpty()) {
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

}
