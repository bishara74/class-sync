package com.hodali.classsync.controller;

import com.hodali.classsync.dto.LoginRequest;
import com.hodali.classsync.dto.LoginResponse;
import com.hodali.classsync.model.User;
import com.hodali.classsync.model.enums.Role;
import com.hodali.classsync.repository.UserRepository;
import com.hodali.classsync.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password.");
        }

        if (user.getRole() == Role.STUDENT) {
            if (request.neptunCode() == null || !request.neptunCode().equalsIgnoreCase(user.getNeptunCode())) {
                return ResponseEntity.badRequest().body("Invalid Neptun Code.");
            }
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, user));
    }
}