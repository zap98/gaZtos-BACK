package com.example.gAZtos.Services;
import com.example.gAZtos.Dto.EmailConfig;
import com.example.gAZtos.Entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Service
public class EmailService {

    private final EmailConfig emailConfig;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    public void sendPasswordResetEmail(Optional<User> user, String resetLink) {
        User foundUser = user.orElseThrow(() -> new IllegalArgumentException("User not found"));

        String recipient = foundUser.getEmail();
        String recipientName = foundUser.getFirstName() + " " + foundUser.getLastName();
        String SENDER_EMAIL = emailConfig.getSenderEmail();
        String SENDER_NAME = emailConfig.getSenderName();
        String API_KEY = emailConfig.getApiKey();
        String API_URL = emailConfig.getApiUrl();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String json = "{"
                    + "\"sender\": {\"name\": \"" + SENDER_NAME + "\", \"email\": \"" + SENDER_EMAIL + "\"},"
                    + "\"to\": [{\"email\": \"" + recipient + "\", \"name\": \"" + recipientName + "\"}],"
                    + "\"subject\": \"Recupera tu contraseña\","
                    + "\"htmlContent\": \"<html><body><h1>Recupera tu contraseña</h1>"
                    + "<p>Hola " + recipientName + ",</p>"
                    + "<p>Has solicitado restablecer tu contraseña para tu cuenta en Gaztos. "
                    + "Por favor, haz clic en el siguiente enlace para restablecerla:</p>"
                    + "<p><a href='" + resetLink + "'>Restablecer contraseña</a></p>"
                    + "<p>Si no solicitaste este cambio, puedes ignorar este correo. "
                    + "Si tienes alguna pregunta, no dudes en contactarnos.</p>"
                    + "<p>Saludos,<br>El equipo de soporte de Gaztos</p></body></html>\""
                    + "}";

            // Construir y enviar la solicitud
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("accept", "application/json")
                    .header("api-key", API_KEY)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Response code: {}", response.statusCode());
            logger.info("Response body: {}", response.body());
        } catch (IOException e) {
            logger.error("Error sending email: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Request interrupted: {}", e.getMessage());
        }
    }
}

