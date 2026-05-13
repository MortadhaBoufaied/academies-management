package com.footballacademy.services.scouting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Service
public
class ScoutingAiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String scoutingAiBaseUrl;
    public ScoutingAiService(
    @Value("${scouting.ai.base-url:http://localhost:8010}") String scoutingAiBaseUrl,
    @Value("${scouting.ai.timeout-ms:20000}") int timeoutMs, ObjectMapper objectMapper) {
        this.scoutingAiBaseUrl = normalizeBaseUrl(scoutingAiBaseUrl);
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(requestFactory);
    }
    public ResponseEntity<?> searchPlayers(Map<String, Object> queryParams) {
        return exchange(HttpMethod.GET, "/api/v1/scouter/players/search", queryParams, null);
    }
    public ResponseEntity<?> comparePlayers(Map<String, Object> payload) {
        return exchange(HttpMethod.POST, "/api/v1/scouter/players/compare", null, payload);
    }
    public ResponseEntity<?> generateShortlist(Map<String, Object> payload) {
        return exchange(HttpMethod.POST, "/api/v1/scouter/shortlists/generate", null, payload);
    }
    public ResponseEntity<?> potentialScore(Long playerExternalId) {
        return exchange(HttpMethod.GET, "/api/v1/ml/potential/" + playerExternalId, null, null);
    }
    public ResponseEntity<?> evolution(Long playerExternalId, Integer window) {
        Map<String, Object> query = window == null ? Map.of() : Map.of("window", window);
        return exchange(HttpMethod.GET, "/api/v1/ml/evolution/" + playerExternalId, query, null);
    }
    public ResponseEntity<?> churnRisk(Long playerExternalId) {
        return exchange(HttpMethod.GET, "/api/v1/ml/churn/" + playerExternalId, null, null);
    }
    public ResponseEntity<?> syncAcademy(Map<String, Object> payload) {
        ResponseEntity<?> response = exchange(HttpMethod.POST, "/api/v1/data/sync/academy", null, payload);
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return exchange(HttpMethod.POST, "/api/v1/data/sync/football-academy", null, payload);
        } return response;
    }
    private ResponseEntity<?> exchange(HttpMethod method, String path, Map<String, ?> queryParams, Object body) {
        String url = buildUrl(path);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (value != null && !String.valueOf(value) .isBlank()) {
                    builder.queryParam(key, value);
                }
            });
        } HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Object> response = restTemplate.exchange(builder.toUriString(), method, entity, Object.
            class);
            return ResponseEntity.status(response.getStatusCode()) .body(response.getBody());
        } catch (HttpStatusCodeException e) {
            Object parsedError = parseResponseBody(e.getResponseBodyAsString());
            if (parsedError == null) {
                parsedError = Map.of("error", "Scouting AI request failed");
            } return ResponseEntity.status(e.getStatusCode()) .body(parsedError);
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE) .body(Map.of("error", "Scouting AI service unavailable", "detail", e.getMessage() == null ? "Connection timeout" : e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Unexpected scouting proxy error", "detail", e.getMessage() == null ? "Unknown error" : e.getMessage()));
        }
    }
    private Object parseResponseBody(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return objectMapper.readValue(raw, Object.
            class);
        } catch (Exception ignored) {
            return Map.of("error", raw);
        }
    }
    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) return "http://localhost:8010";
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
    private String buildUrl(String path) {
        if (path == null || path.isBlank()) return scoutingAiBaseUrl;
        return path.startsWith("/") ? scoutingAiBaseUrl + path : scoutingAiBaseUrl + "/" + path;
    }
}
