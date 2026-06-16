package com.inventory.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FirebaseTokenVerifier {

    private static final String CERTS_URL =
            "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";

    private final String projectId;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private long cacheExpiryEpoch = 0;

    public FirebaseTokenVerifier(@Value("${app.firebase.project-id:}") String projectId) {
        this.projectId = projectId;
    }

    @PostConstruct
    public void init() {
        if (projectId != null && !projectId.isBlank()) {
            log.info("FirebaseTokenVerifier initialized for project: {}", projectId);
        } else {
            log.warn("Firebase project ID not configured. Token verification will fail.");
        }
    }

    public Claims verify(String idToken) {
        if (projectId == null || projectId.isBlank()) {
            throw new SecurityException("Firebase is not configured. Contact the administrator.");
        }

        refreshKeysIfExpired();

        String kid = extractKid(idToken);
        if (kid == null) {
            throw new SecurityException("Token header missing 'kid'");
        }

        PublicKey publicKey = keyCache.get(kid);
        if (publicKey == null) {
            refreshKeys();
            publicKey = keyCache.get(kid);
        }
        if (publicKey == null) {
            throw new SecurityException("No matching public key found for kid: " + kid);
        }

        return Jwts.parser()
                .verifyWith(publicKey)
                .requireAudience(projectId)
                .requireIssuer("https://securetoken.google.com/" + projectId)
                .build()
                .parseSignedClaims(idToken)
                .getPayload();
    }

    private String extractKid(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new SecurityException("Invalid JWT format");
            }
            byte[] headerBytes = Base64.getUrlDecoder().decode(parts[0]);
            Map<?, ?> header = objectMapper.readValue(headerBytes, Map.class);
            return (String) header.get("kid");
        } catch (Exception e) {
            throw new SecurityException("Failed to parse token header: " + e.getMessage());
        }
    }

    private void refreshKeysIfExpired() {
        if (System.currentTimeMillis() > cacheExpiryEpoch) {
            refreshKeys();
        }
    }

    private void refreshKeys() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CERTS_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String cacheControl = response.headers().firstValue("Cache-Control").orElse("max-age=3600");
            int maxAge = 3600;
            var matcher = java.util.regex.Pattern.compile("(?i)max-age\\s*=\\s*(\\d+)")
                    .matcher(cacheControl);
            if (matcher.find()) {
                maxAge = Integer.parseInt(matcher.group(1));
            }
            cacheExpiryEpoch = System.currentTimeMillis() + maxAge * 1000L;

            @SuppressWarnings("unchecked")
            Map<String, String> certs = objectMapper.readValue(response.body(), Map.class);

            keyCache.clear();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for (var entry : certs.entrySet()) {
                String pem = entry.getValue()
                        .replace("-----BEGIN CERTIFICATE-----", "")
                        .replace("-----END CERTIFICATE-----", "")
                        .replaceAll("\\s", "");
                byte[] der = Base64.getDecoder().decode(pem);
                X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
                keyCache.put(entry.getKey(), cert.getPublicKey());
            }

            log.debug("Fetched {} Firebase public keys", keyCache.size());
        } catch (Exception e) {
            log.warn("Failed to fetch Firebase public keys: {}", e.getMessage());
            if (keyCache.isEmpty()) {
                cacheExpiryEpoch = System.currentTimeMillis() + 30_000L;
            }
        }
    }
}
