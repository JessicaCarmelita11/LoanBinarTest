package com.example.ProjectBinar.service;

import com.example.ProjectBinar.dto.CreatePlafondRequest;
import com.example.ProjectBinar.dto.PlafondResponse;
import com.example.ProjectBinar.dto.UpdatePlafondRequest;
import com.example.ProjectBinar.entity.Plafond;
import com.example.ProjectBinar.repository.PlafondRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer untuk Plafond dengan Redis caching.
 * 
 * Menyediakan operasi CRUD lengkap dengan fitur sorting dan filtering.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlafondService {

    private final PlafondRepository plafondRepository;

    /**
     * Membuat plafond baru.
     * Menghapus cache karena data berubah.
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public PlafondResponse createPlafond(CreatePlafondRequest request) {
        log.info("Creating new plafond: {}", request.getName());

        // Validasi nama unik (hanya cek yang belum dihapus)
        if (plafondRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Plafond dengan nama '" + request.getName() + "' sudah ada");
        }

        Plafond plafond = Plafond.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxAmount(request.getMaxAmount())
                .interestRate(request.getInterestRate())
                .tenorMonth(request.getTenorMonth())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Plafond saved = plafondRepository.save(plafond);
        log.info("Plafond created with ID: {}", saved.getId());

        return PlafondResponse.fromEntity(saved);
    }

    /**
     * Mendapatkan semua plafond dengan pagination, sorting, dan filtering.
     * 
     * @param page       Nomor halaman (0-indexed)
     * @param size       Jumlah item per halaman
     * @param sortBy     Field untuk sorting (default: id)
     * @param sortDir    Arah sorting: asc atau desc (default: asc)
     * @param name       Filter berdasarkan nama (contains, case-insensitive)
     * @param isActive   Filter berdasarkan status aktif
     * @param minAmount  Filter minimum maxAmount
     * @param maxAmount  Filter maximum maxAmount
     * @param tenorMonth Filter berdasarkan tenor
     */
    // Note: Page objects are not cached due to Redis serialization complexity
    @Transactional(readOnly = true)
    public Page<PlafondResponse> getAllPlafonds(
            int page, int size, String sortBy, String sortDir,
            String name, Boolean isActive, BigDecimal minAmount,
            BigDecimal maxAmount, Integer tenorMonth) {

        log.info("Fetching plafonds with filters - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        // Setup sorting
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Build dynamic specification for filtering
        Specification<Plafond> spec = buildSpecification(name, isActive, minAmount, maxAmount, tenorMonth);

        Page<Plafond> plafondPage = plafondRepository.findAll(spec, pageable);

        return plafondPage.map(PlafondResponse::fromEntity);
    }

    /**
     * Mendapatkan plafond berdasarkan ID.
     */
    @Cacheable(value = "plafonds", key = "'id:' + #id")
    @Transactional(readOnly = true)
    public Optional<PlafondResponse> getPlafondById(Long id) {
        log.info("Fetching plafond by ID: {} (cache miss)", id);
        return plafondRepository.findByIdAndIsDeletedFalse(id)
                .map(PlafondResponse::fromEntity);
    }

    /**
     * Update plafond berdasarkan ID.
     * Hanya field yang tidak null akan diupdate.
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public PlafondResponse updatePlafond(Long id, UpdatePlafondRequest request) {
        log.info("Updating plafond ID: {}", id);

        Plafond plafond = plafondRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Plafond dengan ID " + id + " tidak ditemukan"));

        // Validasi nama unik jika diubah (hanya cek yang belum dihapus)
        if (request.getName() != null && !request.getName().equals(plafond.getName())) {
            if (plafondRepository.existsByNameAndIdNotAndIsDeletedFalse(request.getName(), id)) {
                throw new IllegalArgumentException("Plafond dengan nama '" + request.getName() + "' sudah ada");
            }
            plafond.setName(request.getName());
        }

        // Update fields jika tidak null
        if (request.getDescription() != null) {
            plafond.setDescription(request.getDescription());
        }
        if (request.getMaxAmount() != null) {
            plafond.setMaxAmount(request.getMaxAmount());
        }
        if (request.getInterestRate() != null) {
            plafond.setInterestRate(request.getInterestRate());
        }
        if (request.getTenorMonth() != null) {
            plafond.setTenorMonth(request.getTenorMonth());
        }
        if (request.getIsActive() != null) {
            plafond.setIsActive(request.getIsActive());
        }

        Plafond updated = plafondRepository.save(plafond);
        log.info("Plafond updated successfully: {}", updated.getId());

        return PlafondResponse.fromEntity(updated);
    }

    /**
     * Soft delete plafond berdasarkan ID.
     * Mengubah isDeleted menjadi true dan menyimpan deletedAt timestamp.
     */
    @CacheEvict(value = "plafonds", allEntries = true)
    public void deletePlafond(Long id) {
        log.info("Soft deleting plafond ID: {}", id);

        Plafond plafond = plafondRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Plafond dengan ID " + id + " tidak ditemukan"));

        plafond.setIsDeleted(true);
        plafond.setDeletedAt(LocalDateTime.now());
        plafondRepository.save(plafond);

        log.info("Plafond soft deleted successfully: {}", id);
    }

    /**
     * Build Specification untuk dynamic filtering.
     */
    private Specification<Plafond> buildSpecification(
            String name, Boolean isActive, BigDecimal minAmount,
            BigDecimal maxAmount, Integer tenorMonth) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Selalu filter yang belum dihapus
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxAmount"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxAmount"), maxAmount));
            }

            if (tenorMonth != null) {
                predicates.add(criteriaBuilder.equal(root.get("tenorMonth"), tenorMonth));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Mendapatkan semua plafond aktif (tanpa pagination).
     */
    @Cacheable(value = "plafonds", key = "'active'")
    @Transactional(readOnly = true)
    public List<PlafondResponse> getActivePlafonds() {
        log.info("Fetching active plafonds (cache miss)");
        return plafondRepository.findByIsActiveAndIsDeletedFalse(true).stream()
                .map(PlafondResponse::fromEntity)
                .toList();
    }
}
