package com.example.ProjectBinar.service;

import com.example.ProjectBinar.entity.User;
import com.example.ProjectBinar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer untuk User dengan Redis caching.
 * 
 * Service layer berisi business logic dan menjadi penghubung
 * antara Controller dan Repository.
 * 
 * @Cacheable: Cache hasil method, skip eksekusi jika data sudah ada di cache
 * @CacheEvict: Hapus entry dari cache saat data berubah
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Membuat user baru.
     * Menghapus cache "users" karena list berubah.
     */
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        log.info("Creating new user: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Mendapatkan semua user beserta role-nya.
     * Hasil di-cache dengan key "users::all"
     */
    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        System.out.println("Fetching all users from database (cache miss)");
        return userRepository.findAll();
        // return null;
    }

    /**
     * Mencari user berdasarkan username.
     * Hasil di-cache dengan key "users::{username}"
     */
    @Cacheable(value = "users", key = "#username")
    public Optional<User> findByUsername(String username) {
        log.info("Fetching user by username from database: {} (cache miss)", username);
        return userRepository.findByUsername(username);
    }
}
