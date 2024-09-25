package com.example.gAZtos.Controllers;

import com.example.gAZtos.Dto.UserPasswordDto;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Services.UserService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

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
            return ResponseEntity.internalServerError().body("Error retrieving profile image");
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody UserPasswordDto userPasswordDto) throws JSONException {
        String userName = userPasswordDto.getUsername();
        String newPassword = userPasswordDto.getPassword();

        String result = userService.changePassword(userName, newPassword);
        return ResponseEntity.ok(result);
    }
}
