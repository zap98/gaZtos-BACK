package com.example.gAZtos.Repositories;

import com.example.gAZtos.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Método para encontrar un usuario por su nombre de usuario (username)
    User findByUsername(String username);
}

