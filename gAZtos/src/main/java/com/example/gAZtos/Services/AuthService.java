package com.example.gAZtos.Services;

import com.example.gAZtos.Dto.LoginRequest;
import com.example.gAZtos.Entities.PasswordResetToken;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Repositories.PasswordResetTokenRepository;
import com.example.gAZtos.Repositories.UserRepository;
import com.example.gAZtos.Utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    public User authenticateUser(LoginRequest loginRequest) {
        User user = findByUserName(loginRequest.getUsername());
        // Verificar si el usuario existe
        if (user != null) {
            logger.info("authenticateUser: User {} exists in the database", user.getUsername());
            String hashedPasswordFromDb = user.getPassword();

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

            if (existingToken != null) {
                // Actualizar el token existente
                existingToken.setToken(token);
                existingToken.setExpiryDate(calculateExpiryDate());
                passwordResetTokenRepository.save(existingToken);
            } else {
                // Crear un nuevo token si no existe
                PasswordResetToken passwordResetToken = new PasswordResetToken();
                passwordResetToken.setToken(token);
                passwordResetToken.setUser(user);
                passwordResetToken.setExpiryDate(calculateExpiryDate());
                passwordResetTokenRepository.save(passwordResetToken);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving password reset token", e);
        }
    }

    /* Método para modificar la contraseña una vez hemos accedido al enlace del correo */
    public String recoveryPassword(String username, String newPassword, String token) {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user)
                    .orElse(null);

            if (existingToken != null) {
                if (existingToken.getToken().equals(token)) {
                    if (!jwtUtil.isTokenExpired(token)) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        userRepository.save(user);
                        logger.info("recoveryPassword: Password reset successfully to user {}", user.getUsername());
                        return "ok";
                    } else {
                        logger.warn("recoveryPassword: Token is expired to user {}", user.getUsername());
                        return "Token expirado";
                    }
                } else {
                    logger.error("recoveryPassword: Invalid token");
                    return "Token invalido";
                }
            }
        } else {
            logger.error("recoveryPassword: User {} not found", username);
            return "Usuario no encontrado";
        }
        return null;
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

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 10);
        return cal.getTime();
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
