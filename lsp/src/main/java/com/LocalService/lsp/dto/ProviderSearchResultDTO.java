package com.LocalService.lsp.dto;

import com.LocalService.lsp.model.Provider;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Data Transfer Object for Aggregated Search Results.
 * Includes counts calculated on the database side.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderSearchResultDTO extends Provider {
    private long reviewCount;
    private long completedOrders;
    private Double averageRating;
}