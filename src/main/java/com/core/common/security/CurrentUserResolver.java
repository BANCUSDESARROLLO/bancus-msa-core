package com.core.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String COOKIE_AUTH = "BANCUP_AUTH";

    private final ObjectMapper objectMapper;

    public Optional<Long> resolveCurrentUserId(HttpServletRequest request) {
        Optional<Long> byHeader = parseLong(request.getHeader(HEADER_USER_ID));
        if (byHeader.isPresent()) {
            return byHeader;
        }

        Optional<Long> byBearer = resolveFromBearerToken(request.getHeader(HEADER_AUTHORIZATION));
        if (byBearer.isPresent()) {
            return byBearer;
        }

        Optional<String> cookieToken = resolveAuthCookie(request);
        if (cookieToken.isPresent()) {
            return resolveFromJwt(cookieToken.get());
        }

        return Optional.empty();
    }

    private Optional<Long> resolveFromBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return Optional.empty();
        }
        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            return Optional.empty();
        }
        return resolveFromJwt(authorizationHeader.substring(prefix.length()).trim());
    }

    private Optional<String> resolveAuthCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_AUTH.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<Long> resolveFromJwt(String jwt) {
        if (!StringUtils.hasText(jwt)) {
            return Optional.empty();
        }
        String[] parts = jwt.split("\\.");
        if (parts.length < 2 || !StringUtils.hasText(parts[1])) {
            return Optional.empty();
        }

        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedPayload, StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });

            Optional<Long> fromUserId = parseClaim(payload.get("userId"));
            if (fromUserId.isPresent()) {
                return fromUserId;
            }

            Optional<Long> fromIdUsuario = parseClaim(payload.get("idUsuario"));
            if (fromIdUsuario.isPresent()) {
                return fromIdUsuario;
            }

            return parseClaim(payload.get("user_id"));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<Long> parseClaim(Object value) {
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number number) {
            long parsed = number.longValue();
            return parsed > 0 ? Optional.of(parsed) : Optional.empty();
        }
        return parseLong(String.valueOf(value));
    }

    private Optional<Long> parseLong(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        try {
            long parsed = Long.parseLong(raw.trim());
            if (parsed <= 0) {
                return Optional.empty();
            }
            return Optional.of(parsed);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
