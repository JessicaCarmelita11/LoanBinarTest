package com.example.ProjectBinar.controller;

import com.example.ProjectBinar.entity.Role;
import com.example.ProjectBinar.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk Role.
 * 
 * @RestController: Kombinasi @Controller + @ResponseBody
 *                  - Semua response otomatis di-serialize ke JSON
 * @RequestMapping: Base URL untuk semua endpoint di controller ini
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * GET /roles - Mendapatkan semua role.
     * 
     * @return List of Role dalam format JSON
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * POST /roles - Membuat role baru.
     * 
     * @RequestBody: Mengambil data dari request body (JSON)
     * @return Role yang baru dibuat
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }
}
