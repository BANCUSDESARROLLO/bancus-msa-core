package com.bancus.core.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record DatabaseConnectionResponse(
        String service,
        String status,
        String database,
        String schema,
        Instant timestamp
) {
}
