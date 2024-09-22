package com.example.gAZtos.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Services.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // NO FUNCIONA
    @GetMapping("/getProfileImg")
    public ResponseEntity<?> login(@RequestParam String username) {
        User user = userService.findByUserName(username);    

        if (user != null) {
            String profileImageUrl = user.getProfilePicture();  // O el método para obtener la imagen

            // Verificar si el usuario tiene una imagen de perfil
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                return ResponseEntity.ok(profileImageUrl);            
            } else {
                return ResponseEntity.ok("Invalid credentials");
            }
        } else {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
