package com.example.gAZtos.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String determineContentType(byte[] imageData) {
        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
            return "image/jpeg";
        } else if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50) {
            return "image/png";
        } else if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49) {
            return "image/gif";
        }
        return "image/png";
    }

    public String changePassword(String userName, String newPassword) {
        try {
            User user = userRepository.findByUsername(userName);
            if (user != null) {
                if (!passwordEncoder.matches(newPassword, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    logger.info("changePassword: {} Password changed successfully", userName);
                    return "ok";
                } else {
                    return "Equal passwords";
                }
            } else {
                return "Usuario no encontrado";
            }
        } catch (Exception e) {
            logger.error("changePassword: {} Password modification error {}", e.getMessage(), userName);
            throw new RuntimeException("Error al cambiar la contraseña", e);
        }
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }
}
