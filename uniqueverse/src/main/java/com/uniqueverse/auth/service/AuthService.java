package com.uniqueverse.auth.service;

import com.uniqueverse.auth.dto.RegisterRequest;
import com.uniqueverse.auth.dto.UserResponse;
import com.uniqueverse.auth.entity.User;
import com.uniqueverse.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Every public registration creates a normal USER.
        // ADMIN creation will be handled separately.
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}