package com.example.ProjectBinar.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity User - Merepresentasikan pengguna dalam sistem.
 * 
 * Anotasi JPA:
 * - @Entity: Menandai class ini sebagai JPA entity
 * - @Table: Menentukan nama tabel di database (users)
 * - @ManyToMany: Mendefinisikan relasi many-to-many dengan Role
 * - @JoinTable: Menentukan tabel penghubung (user_roles) untuk relasi
 * many-to-many
 * - @JoinColumn: Menentukan kolom foreign key
 * 
 * Relasi Many-to-Many:
 * - Satu user dapat memiliki banyak role
 * - Satu role dapat dimiliki oleh banyak user
 * - Tabel penghubung: user_roles (user_id, role_id)
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String email;

    @Column
    private String password;

    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Relasi Many-to-Many dengan Role.
     * 
     * @ManyToMany: Mendefinisikan hubungan many-to-many
     * @JoinTable: Membuat tabel penghubung "user_roles"
     *             - joinColumns: Foreign key ke tabel users (user_id)
     *             - inverseJoinColumns: Foreign key ke tabel roles (role_id)
     * @JsonIgnoreProperties: Menghindari infinite loop saat serialisasi JSON
     * 
     *                        FetchType.EAGER: Role akan langsung di-load bersama
     *                        User
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
