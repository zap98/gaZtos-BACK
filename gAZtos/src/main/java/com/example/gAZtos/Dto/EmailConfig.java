package com.example.gAZtos.Dto;

import org.springframework.stereotype.Component;

@Component
public class EmailConfig {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String SENDER_EMAIL = "gaztos@outlook.com";
    private static final String SENDER_NAME = "GaZtos";

    public String getApiKey() {
        return API_KEY;
    }

    public String getApiUrl() {
        return API_URL;
    }

    public String getSenderEmail() {
        return SENDER_EMAIL;
    }

    public String getSenderName() {
        return SENDER_NAME;
    }
}

