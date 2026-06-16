package com.inventory.api.controller;

import com.inventory.api.dto.AuthResponse;
import com.inventory.api.dto.FirebaseSyncRequest;
import com.inventory.api.service.FirebaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Firebase Authentication endpoints")
public class AuthController {

    private final FirebaseService firebaseService;

    @PostMapping("/firebase")
    @Operation(summary = "Authenticate with Firebase ID token")
    public ResponseEntity<AuthResponse> syncFirebaseUser(@Valid @RequestBody FirebaseSyncRequest request) {
        return ResponseEntity.ok(firebaseService.syncUser(request.token()));
    }
}
