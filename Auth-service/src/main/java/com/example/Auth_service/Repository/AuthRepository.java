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

    @Query("""
    SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
    FROM User u
    WHERE u.email = :email
""")
    boolean existsByEmailCaseSensitive(@Param("email") String email);
}
