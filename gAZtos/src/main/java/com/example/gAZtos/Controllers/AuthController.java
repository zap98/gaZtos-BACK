package com.example.gAZtos.Controllers;

import java.io.IOException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.gAZtos.Dto.LoginRequest;
import com.example.gAZtos.Dto.LoginResponse;
import com.example.gAZtos.Dto.UserDto;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Services.UserService;
import com.example.gAZtos.Utils.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

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

    @PostMapping("/prueba")
    public void prueba() {
       System.out.println("Prueba");
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) throws IOException {
        byte[] profilePictureBytes = null;
        User userAux = userService.findByUserName(user.getUsername());

        try {
            if (userAux != null) {
                logger.warn("User {} is already currently registered", userAux.getUsername());
                return "El usuario ya existe";
            }
        
            if (user.getProfilePicture() != null) {
                // Decodifica la imagen base64 a bytes
                profilePictureBytes = Base64.getDecoder().decode(user.getProfilePicture());
            }
        
            userService.registerUser(user.getUsername(),user.getPassword(), user.getFirstName(), user.getLastName(), profilePictureBytes);
            logger.info("User {} successfully registered", user.getUsername());
            return "Usuario registrado exitosamente";

        } catch (Exception e) {
            logger.error(e.getMessage());
            return e.toString();
        }
    }
}
