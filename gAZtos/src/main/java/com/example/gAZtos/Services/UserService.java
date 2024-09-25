package com.example.gAZtos.Services;

import com.example.gAZtos.Entities.PasswordResetToken;
import com.example.gAZtos.Repositories.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.gAZtos.Dto.LoginRequest;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Repositories.UserRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Autenticación del usuario
    public User authenticateUser(LoginRequest loginRequest) {
       User user = findByUserName(loginRequest.getUsername());
       // Verificar si el usuario existe
       if (user != null) {
           logger.info("User {} exists in the database", user.getUsername());
           String hashedPasswordFromDb = user.getPassword(); // Obtener la contraseña hasheada

           if (passwordEncoder.matches(loginRequest.getPassword(), hashedPasswordFromDb)) {
                return user;
           }
       }
        return null;
    }

    public void saveTokenForUser(String email, String token) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        try {
            // Buscar si ya existe un token para este usuario
            PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user)
                    .orElse(null);

            if (existingToken.getToken() != null) {
                // Actualizar el token existente
                existingToken.setToken(token);
                existingToken.setExpiryDate(calculateExpiryDate(10));
                passwordResetTokenRepository.save(existingToken);
            } else {
                // Crear un nuevo token si no existe
                PasswordResetToken passwordResetToken = new PasswordResetToken();
                passwordResetToken.setToken(token);
                passwordResetToken.setUser(user);
                passwordResetToken.setExpiryDate(calculateExpiryDate(10));
                passwordResetTokenRepository.save(passwordResetToken);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving password reset token", e);
        }
    }

    public String determineContentType(byte[] imageData) {
        // Esta es una implementación muy básica. En la práctica, deberías usar una biblioteca
        // como Apache Tika para determinar con precisión el tipo de contenido.
        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
            return "image/jpeg";
        } else if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50) {
            return "image/png";
        } else if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49) {
            return "image/gif";
        }
        // Por defecto, asumimos PNG
        return "image/png";
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return cal.getTime();
    }

    public void registerUser(String username, String email, String password, String firstName, String lastName, byte[] profilePictureBytes) {
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Guardar la imagen como un arreglo de bytes en la base de datos
        if (profilePictureBytes != null && profilePictureBytes.length > 0) {
            user.setProfilePicture(profilePictureBytes); // Almacenar directamente los bytes en el objeto User
        }
        userRepository.save(user);
    }

    public String changePassword(String userName, String newPassword) {
        try {
            User user = userRepository.findByUsername(userName);
            if (user != null) {
                if (!passwordEncoder.matches(newPassword, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return "OK";
                } else {
                    return "Equal passwords";
                }
            } else {
                return "Usuario no encontrado";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cambiar la contraseña", e);
        }
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
