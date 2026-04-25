package com.core.controller;

import com.core.common.dto.ApiResponse;
import com.core.dto.DatabaseConnectionResponse;
import com.core.service.CoreConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/core")
public class CoreController {

    private final CoreConnectionService coreConnectionService;

    public CoreController(CoreConnectionService coreConnectionService) {
        this.coreConnectionService = coreConnectionService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Instant now = Instant.now();
        try {
            DatabaseConnectionResponse db = coreConnectionService.checkDatabaseConnection();

            Map<String, Object> database = new LinkedHashMap<>();
            database.put("status", "UP");
            database.put("name", db.database());
            database.put("schema", db.schema());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("service", "core");
            payload.put("status", "UP");
            payload.put("timestamp", now.toString());
            payload.put("database", database);

            return ResponseEntity.ok(payload);
        } catch (Exception ex) {
            Map<String, Object> database = new LinkedHashMap<>();
            database.put("status", "DOWN");
            database.put("error", ex.getMessage());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("service", "core");
            payload.put("status", "DOWN");
            payload.put("timestamp", now.toString());
            payload.put("database", database);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(payload);
        }
    }

    @GetMapping("/test-db")
    public ApiResponse<DatabaseConnectionResponse> testDatabaseConnection() {
        DatabaseConnectionResponse response = coreConnectionService.checkDatabaseConnection();
        return ApiResponse.success("Conexion a base de datos OK", response);
    }
}
