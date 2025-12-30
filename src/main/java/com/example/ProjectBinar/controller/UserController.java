package com.example.ProjectBinar.controller;

import com.example.ProjectBinar.dto.CreateUserRequest;
import com.example.ProjectBinar.entity.User;
import com.example.ProjectBinar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk User.
 * 
 * Menangani HTTP request untuk operasi CRUD User.
 * Response dalam format JSON dengan role ter-serialize.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /users - Mendapatkan semua user beserta role-nya.
     * 
     * Karena FetchType.EAGER pada relasi roles,
     * role akan otomatis di-load bersama user.
     * 
     * @return List of User dengan roles dalam format JSON
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        
        return ResponseEntity.ok(users);
    }

    /**
     * POST /users - Membuat user baru.
     * 
     * Contoh request body:
     * {
     * "username": "john",
     * "email": "john@example.com",
     * "password": "secret",
     * "isActive": true,
     * "roles": []
     * }
     * 
     * @return User yang baru dibuat
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .isActive(request.getIsActive())
                .build();
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
