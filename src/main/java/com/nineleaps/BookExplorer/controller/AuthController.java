package com.nineleaps.BookExplorer.controller;

import com.nineleaps.BookExplorer.dto.AuthResponse;
import com.nineleaps.BookExplorer.dto.RegisterInput;
import com.nineleaps.BookExplorer.entity.User;
import com.nineleaps.BookExplorer.repository.UserRepository;
import com.nineleaps.BookExplorer.service.JwtService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @MutationMapping
    public User register(@Argument RegisterInput input) {
        if (userRepository.existsByEmail(input.email())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(input.name());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));

        return userRepository.save(user);
    }

    @MutationMapping
    public AuthResponse login(@Argument String email, @Argument String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Manual password verification
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate the token using the simple email payload
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user);
    }
}