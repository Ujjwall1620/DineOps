package com.example.Auth_service.Repository;

import com.example.Auth_service.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<User, Integer> {
    public User findByEmail(String email);
}
