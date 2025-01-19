package tech.investbuddy.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tech.investbuddy.userservice.dto.UserLoginRequest;
import tech.investbuddy.userservice.dto.UserRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private final RestTemplate restTemplate;

    private final String authServerUrl = "http://localhost:8181/realms/investbuddy-microservices-security-realm/protocol/openid-connect";
    private final String adminUrl = "http://localhost:8181/admin/realms/investbuddy-microservices-security-realm";
    private final String clientId = "investbuddy-client-credentials-id";
    private final String clientSecret = "KlVOnpUO0ukeOkJj8LeTB74usBv4c0Bu";

    public String createUser(UserRequest userRequest) {
        // Obtenir le token admin
        String token = getAdminToken();

        System.out.println("Admin token: " + token);

        // Créer un utilisateur Keycloak
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", userRequest.getFirstName());
        userPayload.put("firstName", userRequest.getFirstName());
        userPayload.put("lastName", userRequest.getLastName());
        userPayload.put("email", userRequest.getEmail());
        userPayload.put("enabled", true);
        userPayload.put("credentials", new Object[]{
                Map.of("type", "password", "value", userRequest.getPassword(), "temporary", false)
        });

        System.out.println("User payload: " + userPayload);
        System.out.println("Credentials: " + Arrays.toString((Object[]) userPayload.get("credentials")));


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userPayload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(adminUrl + "/users", requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return "User created successfully!";
        }
        throw new RuntimeException("Failed to create user in Keycloak.");
    }

    public String authenticate(UserLoginRequest userLoginRequest) {
        // Authentifier l'utilisateur et obtenir le token
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("client_id", clientId);
        payload.add("client_secret", clientSecret);
        payload.add("username", userLoginRequest.getEmail());
        payload.add("password", userLoginRequest.getPassword());
        payload.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(authServerUrl + "/token", requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("access_token");
        }
        throw new RuntimeException("Authentication failed.");
    }

    private String getAdminToken() {
        // Obtenir un token avec les identifiants admin
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("client_id", clientId);
        payload.add("client_secret", clientSecret);
        payload.add("grant_type", "client_credentials");

        System.out.println("Get admin token payload: " + payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(authServerUrl + "/token", requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("access_token");
        }
        throw new RuntimeException("Failed to get admin token.");
    }
}
