package com.bancus.core.service;

import com.bancus.core.dto.DatabaseConnectionResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CoreConnectionService {

    private final JdbcTemplate jdbcTemplate;

    public CoreConnectionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DatabaseConnectionResponse checkDatabaseConnection() {
        String database = jdbcTemplate.queryForObject(
                "SELECT SYS_CONTEXT('USERENV', 'DB_NAME') FROM dual",
                String.class
        );
        String schema = jdbcTemplate.queryForObject(
                "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM dual",
                String.class
        );

        return DatabaseConnectionResponse.builder()
                .service("core")
                .status("UP")
                .database(database)
                .schema(schema)
                .timestamp(Instant.now())
                .build();
    }
}
