package com.example.ProjectBinar.dto;

import com.example.ProjectBinar.entity.Plafond;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO untuk response data Plafond.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private Integer tenorMonth;
    private Boolean isActive;
    private LocalDateTime createdAt;

    /**
     * Factory method untuk konversi dari Entity ke Response DTO.
     */
    public static PlafondResponse fromEntity(Plafond plafond) {
        return PlafondResponse.builder()
                .id(plafond.getId())
                .name(plafond.getName())
                .description(plafond.getDescription())
                .maxAmount(plafond.getMaxAmount())
                .interestRate(plafond.getInterestRate())
                .tenorMonth(plafond.getTenorMonth())
                .isActive(plafond.getIsActive())
                .createdAt(plafond.getCreatedAt())
                .build();
    }
}
