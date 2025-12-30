package com.example.ProjectBinar.controller;

import com.example.ProjectBinar.entity.Branch;
import com.example.ProjectBinar.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk Branch.
 * 
 * Menangani HTTP request untuk operasi CRUD Branch.
 * Response dalam format JSON.
 */
@RestController
@RequestMapping("/branch")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /**
     * GET /branch - Mendapatkan semua branch.
     * 
     * @return List of Branch dalam format JSON
     */
    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranch() {
        List<Branch> branch = branchService.getAllBranch();
        return ResponseEntity.ok(branch);
    }

    /**
     * POST /branch - Membuat branch baru.
     * 
     * Contoh request body:
     * {
     * "name": "Branch Name",
     * "address": "Jalan Example No. 1",
     * "city": "Jakarta"
     * }
     * 
     * @return Branch yang baru dibuat
     */
    @PostMapping
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        Branch createdBranch = branchService.createBranch(branch);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
    }
}
