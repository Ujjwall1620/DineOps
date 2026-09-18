package com.example.Auth_service.Repository;

import com.example.Auth_service.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRepository extends JpaRepository<User, Integer> {
    @Query(
            value = "SELECT * FROM users WHERE BINARY email = :email",
            nativeQuery = true
    )
    public User findByEmailCaseSensitive(@Param("email") String email);
}
