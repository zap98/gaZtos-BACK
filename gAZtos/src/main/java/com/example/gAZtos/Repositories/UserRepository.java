package com.example.gAZtos.Repositories;

import com.example.gAZtos.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Método para encontrar un usuario por su nombre de usuario (username)
    User findByUsername(String username);
    Optional<User> findByEmail(String email);
}

