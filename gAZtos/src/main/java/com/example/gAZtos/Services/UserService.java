package com.example.gAZtos.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.gAZtos.Dto.LoginRequest;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Repositories.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Autenticación del usuario
    public User authenticateUser(LoginRequest loginRequest) {
       // Buscar usuario por nombre de usuario
       User user = userRepository.findByUsername(loginRequest.getUsername());

       // Verificar si el usuario existe
       if (user != null) {
           String hashedPasswordFromDb = user.getPassword(); // Obtener la contraseña hasheada

           if (passwordEncoder.matches(loginRequest.getPassword(), hashedPasswordFromDb)) {
                return user;
           }
       }
        return null;
    }

    // Comprueba si el username ya está registrado
    public User userExists(String username) {

        User result = userRepository.findByUsername(username);
        return result;     
    }

    public User registerUser(String username, String password, String firstName, String lastName, byte[] profilePictureBytes) throws IOException {
        User user = new User();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); 
        user.setFirstName(firstName);
        user.setLastName(lastName);

        if (profilePictureBytes != null && profilePictureBytes.length > 0) {
            InputStream profilePictureStream = new ByteArrayInputStream(profilePictureBytes);
            String imagePath = saveProfilePicture(profilePictureStream);

            // Asignar la ruta de la imagen al usuario
            user.setProfilePicturePath(imagePath);
        }

        return userRepository.save(user);
    }

    // Implementación del método para guardar la imagen
    private String saveProfilePicture(InputStream profilePictureStream) throws IOException {
        String directory = "uploads";
        // Genera un nombre de archivo único
        String fileName = "profile_picture_" + System.currentTimeMillis() + ".png";
        // Crea el archivo en el directorio
        java.nio.file.Path path = java.nio.file.Paths.get(directory, fileName);
        java.nio.file.Files.createDirectories(path.getParent());
        java.nio.file.Files.copy(profilePictureStream, path);

        return path.toString();
    }

    public User findByUserName(String username) {
        User result = userRepository.findByUsername(username);
        return result;
    }
}
