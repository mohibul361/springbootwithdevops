package com.example.SpringBootWithSecurityAndDevops.repository;

import com.example.SpringBootWithSecurityAndDevops.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndEmail(Long id, String currentEmail);

    boolean existsByMobileNumber(String mobileNumber);
}
