package org.saadMeddiche.entities;

import java.util.Map;

public record PassportScanResult(
        boolean isPassportValid,
        Visitor visitorInfo,
        Map<String,String> passportHeaders
) {}