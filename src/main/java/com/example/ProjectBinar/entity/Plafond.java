package com.example.ProjectBinar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity Plafond merepresentasikan jenis/tipe pinjaman kredit.
 * Menyimpan informasi limit pinjaman, suku bunga, dan tenor.
 */
@Entity
@Table(name = "plafond")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Plafond implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Maximum loan amount yang dapat diajukan untuk plafond ini.
     * Menggunakan BigDecimal untuk presisi mata uang.
     */
    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;

    /**
     * Suku bunga tahunan dalam persen (e.g., 12.5 = 12.5% per tahun).
     */
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    /**
     * Tenor pinjaman dalam bulan.
     */
    @Column(name = "tenor_month", nullable = false)
    private Integer tenorMonth;

    /**
     * Flag untuk menandakan apakah plafond ini masih aktif/tersedia.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Timestamp pembuatan record, auto-set saat insert.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Flag untuk menandakan apakah record telah di-soft-delete.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Timestamp saat record di-soft-delete.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}
