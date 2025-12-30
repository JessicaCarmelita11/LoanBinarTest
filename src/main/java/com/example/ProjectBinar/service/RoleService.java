package com.example.ProjectBinar.service;

import com.example.ProjectBinar.entity.Role;
import com.example.ProjectBinar.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer untuk Role dengan Redis caching.
 * 
 * @Service: Menandai class sebagai Spring service (business logic layer)
 * @RequiredArgsConstructor: Lombok annotation untuk dependency injection via
 *                           constructor
 * @Cacheable: Cache hasil method, skip eksekusi jika data sudah ada di cache
 * @CacheEvict: Hapus entry dari cache saat data berubah
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Membuat role baru.
     * Menghapus cache "roles" karena list berubah.
     */
    @CacheEvict(value = "roles", allEntries = true)
    public Role createRole(Role role) {
        log.info("Creating new role: {}", role.getName());
        return roleRepository.save(role);
    }

    /**
     * Mendapatkan semua role.
     * Hasil di-cache dengan key "roles::all"
     */
    @Cacheable(value = "roles", key = "'all'")
    public List<Role> getAllRoles() {
        log.info("Fetching all roles from database (cache miss)");
        return roleRepository.findAll();
    }

    /**
     * Mencari role berdasarkan nama.
     * Hasil di-cache dengan key "roles::{name}"
     */
    @Cacheable(value = "roles", key = "#name")
    public Optional<Role> findByName(String name) {
        log.info("Fetching role by name from database: {} (cache miss)", name);
        return roleRepository.findByName(name);
    }
}
