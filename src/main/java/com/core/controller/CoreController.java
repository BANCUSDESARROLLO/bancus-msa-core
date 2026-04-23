package com.core.controller;

import com.core.common.dto.ApiResponse;
import com.core.dto.DatabaseConnectionResponse;
import com.core.service.CoreConnectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/core")
public class CoreController {

    private final CoreConnectionService coreConnectionService;

    public CoreController(CoreConnectionService coreConnectionService) {
        this.coreConnectionService = coreConnectionService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "core",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/test-db")
    public ApiResponse<DatabaseConnectionResponse> testDatabaseConnection() {
        DatabaseConnectionResponse response = coreConnectionService.checkDatabaseConnection();
        return ApiResponse.success("Conexion a base de datos OK", response);
    }
}
