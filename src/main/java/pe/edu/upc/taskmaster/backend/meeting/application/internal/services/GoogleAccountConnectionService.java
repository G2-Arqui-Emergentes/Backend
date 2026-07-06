package pe.edu.upc.taskmaster.backend.meeting.application.internal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.GoogleAccountConnection;
import pe.edu.upc.taskmaster.backend.meeting.infrastructure.persistence.jpa.repositories.GoogleAccountConnectionRepository;

import java.time.Instant;
import java.util.Map;

@Service
public class GoogleAccountConnectionService {

    private final GoogleAccountConnectionRepository repository;
    private final RestTemplate restTemplate;
    private final String googleClientId;
    private final String googleClientSecret;

    public GoogleAccountConnectionService(
            GoogleAccountConnectionRepository repository,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String googleClientSecret) {
        this.repository = repository;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.restTemplate = new RestTemplate();
    }

    public void upsertConnection(Long userId,
                                 String googleEmail,
                                 String accessToken,
                                 String refreshToken,
                                 Instant accessTokenExpiresAt) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
        var connection = repository.findByUserId(userId)
                .orElseGet(() -> new GoogleAccountConnection(userId, accessToken, refreshToken, accessTokenExpiresAt, googleEmail));

        if (connection.getId() == null) {
            repository.save(connection);
            return;
        }

        connection.setGoogleEmail(googleEmail);
        connection.updateTokens(accessToken, refreshToken, accessTokenExpiresAt);
        repository.save(connection);
    }

    public String getValidAccessTokenForUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
        var connection = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Google account is not connected"));

        if (isTokenValid(connection)) {
            return connection.getAccessToken();
        }

        if (connection.getRefreshToken() == null || connection.getRefreshToken().isBlank()) {
            throw new IllegalStateException("Google access token expired and refresh token is not available");
        }

        var refreshed = refreshAccessToken(connection.getRefreshToken());
        connection.updateTokens(
                refreshed.accessToken(),
                connection.getRefreshToken(),
                Instant.now().plusSeconds(refreshed.expiresIn())
        );
        repository.save(connection);
        return connection.getAccessToken();
    }

    public GoogleConnectionStatus getStatus(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
        return repository.findByUserId(userId)
                .map(connection -> new GoogleConnectionStatus(true, connection.getGoogleEmail()))
                .orElseGet(() -> new GoogleConnectionStatus(false, null));
    }

    private boolean isTokenValid(GoogleAccountConnection connection) {
        return connection.getAccessToken() != null
                && !connection.getAccessToken().isBlank()
                && connection.getAccessTokenExpiresAt() != null
                && connection.getAccessTokenExpiresAt().isAfter(Instant.now().plusSeconds(30));
    }

    private TokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", googleClientId);
        form.add("client_secret", googleClientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var request = new org.springframework.http.HttpEntity<>(form, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                "https://oauth2.googleapis.com/token",
                request,
                Map.class
        );

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Failed to refresh Google access token");
        }

        String accessToken = (String) response.get("access_token");
        Number expiresIn = (Number) response.getOrDefault("expires_in", 3600);
        return new TokenResponse(accessToken, expiresIn.longValue());
    }

    private record TokenResponse(String accessToken, long expiresIn) {}

    public record GoogleConnectionStatus(boolean connected, String googleEmail) {}
}
