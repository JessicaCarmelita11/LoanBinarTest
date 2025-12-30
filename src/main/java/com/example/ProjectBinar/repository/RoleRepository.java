package com.example.ProjectBinar.repository;

import com.example.ProjectBinar.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository untuk entity Role.
 * 
 * JpaRepository menyediakan method CRUD standar:
 * - save(), findById(), findAll(), deleteById(), dll.
 * 
 * Spring Data JPA akan otomatis mengimplementasikan interface ini
 * berdasarkan nama method (query method).
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Mencari role berdasarkan nama.
     * Spring Data JPA akan otomatis membuat query:
     * SELECT * FROM roles WHERE name = ?
     */
    Optional<Role> findByName(String name);
}
