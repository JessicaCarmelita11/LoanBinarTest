package com.example.ProjectBinar.controller;

import com.example.ProjectBinar.base.ApiResponse;
import com.example.ProjectBinar.dto.CreatePlafondRequest;
import com.example.ProjectBinar.dto.PlafondResponse;
import com.example.ProjectBinar.dto.UpdatePlafondRequest;
import com.example.ProjectBinar.service.PlafondService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * REST Controller untuk Plafond.
 * 
 * Menyediakan endpoint untuk operasi CRUD dengan sorting dan filtering.
 * Base URL: /plafonds
 */
@RestController
@RequestMapping("/plafonds")
@RequiredArgsConstructor
public class PlafondController {

    private final PlafondService plafondService;

    /**
     * POST /plafonds - Membuat plafond baru.
     * 
     * Request body:
     * {
     * "name": "Gold",
     * "description": "Pinjaman level Gold",
     * "maxAmount": 50000000,
     * "interestRate": 12.5,
     * "tenorMonth": 24,
     * "isActive": true
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlafondResponse>> createPlafond(@RequestBody CreatePlafondRequest request) {
        PlafondResponse created = plafondService.createPlafond(request);

        ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message("Plafond berhasil dibuat")
                .data(created)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /plafonds - Mendapatkan semua plafond dengan pagination, sorting, dan
     * filtering.
     * 
     * Query Parameters:
     * - page: Nomor halaman (default: 0)
     * - size: Jumlah item per halaman (default: 10)
     * - sortBy: Field untuk sorting (default: id)
     * - sortDir: Arah sorting - asc/desc (default: asc)
     * - name: Filter berdasarkan nama (contains)
     * - isActive: Filter berdasarkan status aktif (true/false)
     * - minAmount: Filter minimum maxAmount
     * - maxAmount: Filter maximum maxAmount
     * - tenorMonth: Filter berdasarkan tenor
     * 
     * Contoh: GET
     * /plafonds?page=0&size=10&sortBy=maxAmount&sortDir=desc&isActive=true
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlafondResponse>>> getAllPlafonds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Integer tenorMonth) {

        Page<PlafondResponse> plafonds = plafondService.getAllPlafonds(
                page, size, sortBy, sortDir, name, isActive, minAmount, maxAmount, tenorMonth);

        ApiResponse<Page<PlafondResponse>> response = ApiResponse.<Page<PlafondResponse>>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Data plafond berhasil diambil")
                .data(plafonds)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /plafonds/active - Mendapatkan semua plafond aktif (tanpa pagination).
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PlafondResponse>>> getActivePlafonds() {
        List<PlafondResponse> plafonds = plafondService.getActivePlafonds();

        ApiResponse<List<PlafondResponse>> response = ApiResponse.<List<PlafondResponse>>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Data plafond aktif berhasil diambil")
                .data(plafonds)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /plafonds/{id} - Mendapatkan plafond berdasarkan ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlafondResponse>> getPlafondById(@PathVariable Long id) {
        return plafondService.getPlafondById(id)
                .map(plafond -> {
                    ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                            .success(true)
                            .code(HttpStatus.OK.value())
                            .message("Plafond ditemukan")
                            .data(plafond)
                            .timestamp(Instant.now())
                            .build();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                            .success(false)
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("Plafond dengan ID " + id + " tidak ditemukan")
                            .data(null)
                            .timestamp(Instant.now())
                            .build();
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    /**
     * PUT /plafonds/{id} - Update plafond berdasarkan ID.
     * 
     * Request body (semua field optional):
     * {
     * "name": "Gold Premium",
     * "maxAmount": 75000000
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlafondResponse>> updatePlafond(
            @PathVariable Long id,
            @RequestBody UpdatePlafondRequest request) {
        try {
            PlafondResponse updated = plafondService.updatePlafond(id, request);

            ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                    .success(true)
                    .code(HttpStatus.OK.value())
                    .message("Plafond berhasil diupdate")
                    .data(updated)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<PlafondResponse> response = ApiResponse.<PlafondResponse>builder()
                    .success(false)
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * DELETE /plafonds/{id} - Hapus plafond berdasarkan ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlafond(@PathVariable Long id) {
        try {
            plafondService.deletePlafond(id);

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .code(HttpStatus.OK.value())
                    .message("Plafond berhasil dihapus")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
