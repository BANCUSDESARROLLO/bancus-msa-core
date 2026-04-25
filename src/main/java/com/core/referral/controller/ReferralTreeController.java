package com.core.referral.controller;

import com.core.common.dto.ApiResponse;
import com.core.common.security.CurrentUserResolver;
import com.core.referral.dto.response.ReferralTreeDiagramResponse;
import com.core.referral.dto.response.ReferralTreeTableResponse;
import com.core.referral.service.ReferralTreeQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/referral-tree")
@RequiredArgsConstructor
public class ReferralTreeController {

    private final ReferralTreeQueryService referralTreeQueryService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping({"/table", "/tabla"})
    public ResponseEntity<ApiResponse<ReferralTreeTableResponse>> getTreeTable(
            HttpServletRequest request) {
        Long currentUserId = currentUserResolver.resolveCurrentUserId(request).orElse(null);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No se pudo resolver el usuario actual", "NO_AUTORIZADO"));
        }

        ReferralTreeTableResponse response = referralTreeQueryService.getTreeAsTable(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Arbol de referidos en formato tabla", response));
    }

    @GetMapping({"/diagram", "/diagrama"})
    public ResponseEntity<ApiResponse<ReferralTreeDiagramResponse>> getTreeDiagram(
            HttpServletRequest request) {
        Long currentUserId = currentUserResolver.resolveCurrentUserId(request).orElse(null);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No se pudo resolver el usuario actual", "NO_AUTORIZADO"));
        }

        ReferralTreeDiagramResponse response = referralTreeQueryService.getTreeAsDiagram(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Arbol de referidos en formato jerarquico", response));
    }
}
