package com.example.ProjectBinar.service;

import com.example.ProjectBinar.entity.Branch;
import com.example.ProjectBinar.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer untuk Branch dengan Redis caching.
 * 
 * @Cacheable: Cache hasil method, skip eksekusi jika data sudah ada di cache
 * @CacheEvict: Hapus entry dari cache saat data berubah
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {
    private final BranchRepository branchRepository;

    /**
     * Membuat branch baru.
     * Menghapus cache "branches" karena list berubah.
     */
    @CacheEvict(value = "branches", allEntries = true)
    public Branch createBranch(Branch branch) {
        log.info("Creating new branch: {}", branch.getName());
        return branchRepository.save(branch);
    }

    /**
     * Mendapatkan semua branch.
     * Hasil di-cache dengan key "branches::all"
     */
    @Cacheable(value = "branches", key = "'all'")
    public List<Branch> getAllBranch() {
        log.info("Fetching all branches from database (cache miss)");
        return branchRepository.findAll();
    }

    /**
     * Mencari branch berdasarkan name.
     * Hasil di-cache dengan key "branches::{name}"
     */
    @Cacheable(value = "branches", key = "#name")
    public Optional<Branch> findByName(String name) {
        log.info("Fetching branch by name from database: {} (cache miss)", name);
        return branchRepository.findByName(name);
    }

}
