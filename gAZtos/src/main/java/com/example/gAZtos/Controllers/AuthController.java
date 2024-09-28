package com.example.gAZtos.Controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.example.gAZtos.Dto.*;
import com.example.gAZtos.Services.AuthService;
import com.example.gAZtos.Services.EmailService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Utils.JwtUtil;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(JwtUtil jwtUtil, EmailService emailService, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = authService.authenticateUser(loginRequest);

        if (user != null) {
            String token = jwtUtil.generateToken(loginRequest.getUsername());

            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());

            LoginResponse response = new LoginResponse(token, userDto);
            logger.info("User {} successfully logged in", user.getUsername());

            return ResponseEntity.ok(response);
        } else {
            logger.warn("Invalid credentials");
            return ResponseEntity.ok("Invalid credentials");
        }
    }

    @PostMapping("/prueba")
    public ResponseEntity<?> prueba() {
        return null;
    }

    /* Método que envía el correo de recuperación */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) throws JSONException {
        JSONObject jsonObject = new JSONObject(email);
        String stringEmail = jsonObject.getString("email");

        RecoveryResponse recoveryResponse = new RecoveryResponse(null, null);
        Optional<User> user = authService.findByEmail(stringEmail);

        if (user != null && user.isPresent()) {
            String token = jwtUtil.generateEmailToken(stringEmail);
            authService.saveTokenForUser(stringEmail, token);
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            try {
                recoveryResponse.setUsername(user.get().getUsername());
                recoveryResponse.setToken(token);
                emailService.sendPasswordResetEmail(user, resetLink);
                logger.info("forgotPassword: Send email to {} to reset link", stringEmail);
                return ResponseEntity.ok(recoveryResponse);
            } catch (Exception e) {
                logger.error("forgotPassword: Error sending recovery email to {}", stringEmail);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar el correo electrónico.");
            }
        }
        return ResponseEntity.ok(recoveryResponse);
    }

    /* Endpoint para modificar la contraseña una vez hemos accedido al enlace del correo */
    @PostMapping("/recoveryPassword")
    public ResponseEntity<?> recoveryPassword(@RequestBody RecoveryPasswordDto recoveryPasswordDto) {
        String username = recoveryPasswordDto.getUsername();
        String newPassword = recoveryPasswordDto.getPassword();
        String token = recoveryPasswordDto.getToken();

        String result = authService.recoveryPassword(username, newPassword, token);
        Map<String, String> response = new HashMap<>();

        if (result.equals("ok")) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else if (result.equals("Token expirado") || result.equals("Token invalido")) {
            response.put("status", "error");
            return ResponseEntity.ok(result);
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        try {
            // Verificar si el usuario ya existe
            User userAux = authService.findByUserName(username);
            Map<String, String> response = new HashMap<>();

            if (userAux != null) {
                logger.warn("register: User {} is already registered", username);
                response.put("status", "error");
                return ResponseEntity.ok(response);
            }

            byte[] profilePictureBytes = null;
            if (profilePicture != null && !profilePicture.isEmpty()) {
                profilePictureBytes = profilePicture.getBytes();
                logger.info("register: Profile picture received, size: {} bytes", profilePictureBytes.length);
            }

            // Registrar el usuario
            authService.registerUser(username, email, password, firstName, lastName, profilePictureBytes);
            logger.info("register: User {} successfully registered", username);
            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during user registration: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el registro: " + e.getMessage());
        }
    }
}
