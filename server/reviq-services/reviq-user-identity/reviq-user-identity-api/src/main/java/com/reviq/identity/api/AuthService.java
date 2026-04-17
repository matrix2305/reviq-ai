package com.reviq.identity.api;

import com.reviq.identity.api.dto.LoginRequest;
import com.reviq.identity.api.dto.LoginResponse;
import com.reviq.identity.api.dto.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);
}
