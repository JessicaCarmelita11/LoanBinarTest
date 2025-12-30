package com.example.ProjectBinar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO untuk request update Plafond.
 * Semua field optional - hanya yang disertakan akan diupdate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlafondRequest {

    private String name;
    private String description;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private Integer tenorMonth;
    private Boolean isActive;
}
