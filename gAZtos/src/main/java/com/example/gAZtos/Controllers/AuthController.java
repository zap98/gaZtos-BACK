package com.example.gAZtos.Controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import com.example.gAZtos.Services.EmailService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.gAZtos.Dto.LoginRequest;
import com.example.gAZtos.Dto.LoginResponse;
import com.example.gAZtos.Dto.UserDto;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Services.UserService;
import com.example.gAZtos.Utils.JwtUtil;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticateUser(loginRequest);        

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) throws JSONException {
        JSONObject jsonObject = new JSONObject(email);
        String stringEmail = jsonObject.getString("email");

        Optional<User> user = userService.findByEmail(stringEmail);

        if (user != null && user.isPresent()) {
            String token = jwtUtil.generateEmailToken(stringEmail);
            userService.saveTokenForUser(stringEmail, token);
            String resetLink = "http://localhost:8080/reset-password?token=" + token;

            try {
                emailService.sendPasswordResetEmail(user, resetLink);
                return ResponseEntity.ok("Se ha enviado un correo electrónico con instrucciones para restablecer la contraseña.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar el correo electrónico.");
            }
        }
        return null;
    }

    @PostMapping("/recoveryPassword")
    public ResponseEntity<?> recoveryPassword(@RequestBody String newPassword) throws JSONException {
        JSONObject jsonObject = new JSONObject(newPassword);
        String stringPassword = jsonObject.getString("password");

        return null;
    }

    @PostMapping("/prueba")
    public void prueba() {
       System.out.println("Prueba");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        try {
            // Verificar si el usuario ya existe
            User userAux = userService.findByUserName(username);
            if (userAux != null) {
                logger.warn("User {} is already registered", username);
                return ResponseEntity.badRequest().body("El usuario ya existe");
            }

            byte[] profilePictureBytes = null;
            if (profilePicture != null && !profilePicture.isEmpty()) {
                profilePictureBytes = profilePicture.getBytes();
                logger.info("Profile picture received, size: {} bytes", profilePictureBytes.length);
            }

            // Registrar el usuario
            userService.registerUser(username, email, password, firstName, lastName, profilePictureBytes);
            logger.info("User {} successfully registered", username);
            return ResponseEntity.ok("Usuario registrado exitosamente");

        } catch (Exception e) {
            logger.error("Error during user registration: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el registro: " + e.getMessage());
        }
    }
}
