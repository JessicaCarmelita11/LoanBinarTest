package com.example.ProjectBinar.repository;

import com.example.ProjectBinar.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk Plafond entity.
 * 
 * Extends JpaRepository untuk CRUD dasar dan JpaSpecificationExecutor
 * untuk dynamic query/filtering.
 */
@Repository
public interface PlafondRepository extends JpaRepository<Plafond, Long>, JpaSpecificationExecutor<Plafond> {

    /**
     * Cari plafond berdasarkan nama (exact match).
     */
    Optional<Plafond> findByName(String name);

    /**
     * Cari plafond berdasarkan nama (case-insensitive, contains).
     */
    List<Plafond> findByNameContainingIgnoreCase(String name);

    /**
     * Cari semua plafond yang aktif.
     */
    List<Plafond> findByIsActive(Boolean isActive);

    /**
     * Cari plafond dengan maxAmount dalam range tertentu.
     */
    List<Plafond> findByMaxAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Cari plafond berdasarkan tenor.
     */
    List<Plafond> findByTenorMonth(Integer tenorMonth);

    /**
     * Cari plafond aktif dengan maxAmount >= nilai tertentu.
     */
    List<Plafond> findByIsActiveAndMaxAmountGreaterThanEqual(Boolean isActive, BigDecimal minAmount);

    /**
     * Cek apakah nama plafond sudah ada.
     */
    boolean existsByName(String name);

    /**
     * Cek apakah nama plafond sudah ada (kecuali ID tertentu).
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Cari plafond berdasarkan ID yang belum dihapus.
     */
    Optional<Plafond> findByIdAndIsDeletedFalse(Long id);

    /**
     * Cari semua plafond yang aktif dan belum dihapus.
     */
    List<Plafond> findByIsActiveAndIsDeletedFalse(Boolean isActive);

    /**
     * Cek apakah nama plafond sudah ada dan belum dihapus.
     */
    boolean existsByNameAndIsDeletedFalse(String name);

    /**
     * Cek apakah nama plafond sudah ada (kecuali ID tertentu) dan belum dihapus.
     */
    boolean existsByNameAndIdNotAndIsDeletedFalse(String name, Long id);

    /**
     * Cek apakah ID ada dan belum dihapus.
     */
    boolean existsByIdAndIsDeletedFalse(Long id);
}
