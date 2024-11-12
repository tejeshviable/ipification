package com.anios.ipification.requestDTO;

import lombok.Data;

@Data
public class Create2FARequestDTO {

    private String name;
    private boolean enabled;
    private Configuration configuration;

    @Data
    public static class Configuration {
        private boolean allowMultiplePinVerifications = true;
        private int pinAttempts = 10;
        private String pinTimeToLive = "15m";
        private String sendPinPerApplicationLimit = "10000/1d";
        private String sendPinPerPhoneNumberLimit = "3/1d";
        private String verifyPinLimit = "1/3s";
    }
}
