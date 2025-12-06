package com.transactionsync.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactionsync.dto.integration.PrivvyLoginResponseDTO;
import com.transactionsync.dto.integration.PrivvyMerchantDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PrivvyApiClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String email;
    private final String password;
    private String cachedToken;
    private long tokenExpiryTime;

    public PrivvyApiClient(
            @Value("${transaction-sync.privvy.api-url:}") String apiUrl,
            @Value("${transaction-sync.privvy.email:}") String email,
            @Value("${transaction-sync.privvy.password:}") String password) {
        this.apiUrl = apiUrl;
        this.email = email;
        this.password = password;
        this.restTemplate = new RestTemplate();
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        restTemplate.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        restTemplate.getMessageConverters().add(converter);
    }

    private String getAuthToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedToken;
        }

        try {
            String loginUrl = apiUrl + "/login";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", email);
            loginRequest.put("password", password);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
            ResponseEntity<PrivvyLoginResponseDTO> response = restTemplate.exchange(
                    loginUrl, 
                    HttpMethod.POST, 
                    request, 
                    PrivvyLoginResponseDTO.class
            );

            if (response.getBody() != null) {
                PrivvyLoginResponseDTO loginResponse = response.getBody();
                String token = loginResponse.getToken() != null 
                        ? loginResponse.getToken() 
                        : loginResponse.getAccess_token();
                
                if (token != null && !token.isEmpty()) {
                    cachedToken = token;
                    tokenExpiryTime = System.currentTimeMillis() + (50 * 60 * 1000);
                    return cachedToken;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate with Privvy API: " + e.getMessage(), e);
        }

        throw new RuntimeException("Failed to authenticate with Privvy API: No token received");
    }

    public List<PrivvyMerchantDTO> fetchMerchants() {
        try {
            String token = getAuthToken();
            String merchantsUrl = apiUrl + "/mids";

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<List<PrivvyMerchantDTO>> response = restTemplate.exchange(
                    merchantsUrl, 
                    HttpMethod.GET, 
                    request,
                    new ParameterizedTypeReference<List<PrivvyMerchantDTO>>() {}
            );

            if (response.getBody() != null) {
                return response.getBody();
            }

            return List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch merchants from Privvy API: " + e.getMessage(), e);
        }
    }
}

