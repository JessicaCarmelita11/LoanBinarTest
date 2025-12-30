package com.example.ProjectBinar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity Role - Merepresentasikan role/peran pengguna dalam sistem.
 * 
 * Anotasi JPA:
 * - @Entity: Menandai class ini sebagai JPA entity yang akan di-mapping ke
 * tabel database
 * - @Table: Menentukan nama tabel di database (roles)
 * - @Id: Menandai field sebagai primary key
 * - @GeneratedValue: Mengatur strategi auto-increment (IDENTITY untuk SQL
 * Server)
 * - @Column: Mengatur properti kolom (unique, nullable, dll)
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
