package com.inventory.api.service;

import com.inventory.api.dto.AuthResponse;
import com.inventory.api.models.User;
import com.inventory.api.exception.UnauthorizedException;
import com.inventory.api.repository.UserRepository;
import com.inventory.api.security.FirebaseTokenVerifier;
import com.inventory.api.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @Transactional
    public AuthResponse syncUser(String idToken) {
        Claims claims;
        try {
            claims = firebaseTokenVerifier.verify(idToken);
        } catch (Exception e) {
            log.error("Firebase token verification failed: {}", e.getMessage());
            throw new UnauthorizedException("Invalid Firebase ID token");
        }

        String firebaseUid = claims.getSubject();
        String email = claims.get("email", String.class);

        if (email == null || email.trim().isEmpty()) {
            throw new UnauthorizedException("Firebase token does not contain an email address");
        }

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseGet(() -> User.builder()
                                .email(email)
                                .firebaseUid(firebaseUid)
                                .build()));

        boolean changed = false;

        if (user.getFirebaseUid() == null) {
            user.setFirebaseUid(firebaseUid);
            changed = true;
        }

        if (!user.getEmail().equals(email)) {
            user.setEmail(email);
            changed = true;
        }

        if (changed || user.getId() == null) {
            user = userRepository.save(user);
            log.info("Firebase user synchronized: uid={}, email={}", firebaseUid, email);
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
