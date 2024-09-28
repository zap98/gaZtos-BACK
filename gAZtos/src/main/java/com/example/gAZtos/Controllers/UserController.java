package com.example.gAZtos.Controllers;

import com.example.gAZtos.Dto.UserPasswordDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getProfileImg")
    public ResponseEntity<?> getProfileImg(@RequestParam String username) {
        try {
            User user = userService.findByUserName(username);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] profileImage = user.getProfilePicture();

            if (profileImage == null || profileImage.length == 0) {
                return ResponseEntity.noContent().build();
            }

            String contentType = userService.determineContentType(profileImage);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());
            headers.setContentType(MediaType.parseMediaType(contentType));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(profileImage);

        } catch (Exception e) {
            logger.error("getProfileImg: Error retrieving profile image: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error retrieving profile image");
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody UserPasswordDto userPasswordDto) {
        String userName = userPasswordDto.getUsername();
        String newPassword = userPasswordDto.getPassword();

        String result = userService.changePassword(userName, newPassword);
        Map<String, String> response = new HashMap<>();

        if (result.equals("ok")) {
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else if (result.equals("Equal passwords")) {
            response.put("status", "warning");
            return ResponseEntity.ok(response);
        }

        response.put("status", "error");
        return ResponseEntity.ok(response);
    }
}
