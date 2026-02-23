package com.hodali.classsync.controller;

import com.hodali.classsync.dto.LoginRequest;
import com.hodali.classsync.model.User;
import com.hodali.classsync.model.enums.Role;
import com.hodali.classsync.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(request.password())) {
            return ResponseEntity.badRequest().body("Invalid password.");
        }

        if (user.getRole() == Role.STUDENT) {
            if (request.neptunCode() == null || !request.neptunCode().equalsIgnoreCase(user.getNeptunCode())) {
                return ResponseEntity.badRequest().body("Invalid Neptun Code.");
            }
        }

        return ResponseEntity.ok(user);
    }
}