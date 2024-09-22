package com.example.gAZtos.Controllers;

import java.io.IOException;
import java.util.Base64;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticateUser(loginRequest);        

        if (user != null) {
            String token = jwtUtil.generateToken(loginRequest.getUsername());

            UserDto userDto = new UserDto();
            userDto.setUsername(user.getUsername());

            LoginResponse response = new LoginResponse(token, userDto);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) throws IOException {
        byte[] profilePictureBytes = null;

        User userAux = userService.userExists(user.getUsername());

        try {
            if (userAux != null) {
                return "El usuario ya existe";
            }
        
            if (user.getProfilePicture() != null) {
                // Decodifica la imagen base64 a bytes
                profilePictureBytes = Base64.getDecoder().decode(user.getProfilePicture());
            }
        
            userService.registerUser(user.getUsername(),user.getPassword(), user.getFirstName(), user.getLastName(), profilePictureBytes);
            return "Usuario registrado exitosamente";

        } catch (Exception e) {
            return e.toString();
        }
    }
}
