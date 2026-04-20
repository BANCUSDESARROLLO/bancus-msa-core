package com.bancup.integration.auth;

import com.bancup.exception.AuthIntegrationException;
import com.bancup.exception.ErrorCode;
import com.bancup.exception.ResourceNotFoundException;
import com.bancup.referral.dto.ReferralCodeOwnerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestClient authRestClient;

    public ReferralCodeOwnerDto getOwnerByReferralCode(String code) {
        try {
            return authRestClient.get()
                    .uri("/api/auth/referrals/code/{code}", code)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        if (response.getStatusCode().value() == 404) {
                            throw new ResourceNotFoundException(
                                    ErrorCode.RECURSO_NO_ENCONTRADO,
                                    "Codigo de referido no encontrado: " + code
                            );
                        }
                        throw new AuthIntegrationException(
                                "AUTH rechazo la solicitud con estado " + response.getStatusCode().value()
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new AuthIntegrationException("AUTH respondio con error interno");
                    })
                    .body(ReferralCodeOwnerDto.class);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new AuthIntegrationException("No fue posible comunicarse con AUTH", ex);
        }
    }
}
