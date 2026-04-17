package com.reviq.identity.web;

import com.reviq.identity.api.AuthService;
import com.reviq.identity.api.UserService;
import com.reviq.identity.api.dto.LoginRequest;
import com.reviq.identity.api.dto.LoginResponse;
import com.reviq.identity.api.dto.RefreshTokenRequest;
import com.reviq.identity.api.dto.UserDto;
import com.reviq.security.model.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/api/v1/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/api/v1/auth/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/api/v1/users/me")
    public UserDto getAuthUser(@AuthenticationPrincipal AuthenticatedUser user) {
        return userService.findById(user.getUserId());
    }
}
