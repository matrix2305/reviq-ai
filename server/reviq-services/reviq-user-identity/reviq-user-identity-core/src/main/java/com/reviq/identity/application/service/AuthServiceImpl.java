package com.reviq.identity.application.service;

import com.reviq.identity.api.AuthService;
import com.reviq.identity.api.dto.LoginRequest;
import com.reviq.identity.api.dto.LoginResponse;
import com.reviq.identity.api.dto.RefreshTokenRequest;
import com.reviq.identity.application.mapper.UserMapper;
import com.reviq.identity.domain.entity.AppUser;
import com.reviq.identity.domain.repository.AppUserRepository;
import com.reviq.security.jwt.JwtProperties;
import com.reviq.security.jwt.JwtTokenProvider;
import com.reviq.security.model.AuthenticatedUser;
import com.reviq.security.refresh.RefreshTokenService;
import com.reviq.security.service.LoginAttemptService;
import com.reviq.tenancy.grpc.GetTenantByIdRequest;
import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import com.reviq.tenancy.grpc.TenantInfo;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;

    @GrpcClient("tenancy-service")
    private TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub;

    public AuthServiceImpl(AppUserRepository userRepository,
                           JwtTokenProvider tokenProvider,
                           JwtProperties jwtProperties,
                           PasswordEncoder passwordEncoder,
                           RefreshTokenService refreshTokenService,
                           LoginAttemptService loginAttemptService,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.userMapper = userMapper;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for '{}'", request.getEmail());

        loginAttemptService.checkBlocked(request.getEmail());

        AppUser user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            loginAttemptService.loginFailed(request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            log.warn("AUDIT: Login failed — deactivated account '{}' (userId={})", request.getEmail(), user.getId());
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.loginFailed(request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        loginAttemptService.loginSucceeded(request.getEmail());

        String tenantCode = user.getTenantId() != null ? resolveTenantCode(user.getTenantId()) : null;

        AuthenticatedUser authUser = AuthenticatedUser.builder()
                .userId(user.getId())
                .tenantCode(tenantCode)
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .role(user.getRoleCode())
                .permissions(user.getPermissionCodes())
                .build();

        String token = tokenProvider.generateToken(authUser);
        String refreshToken = refreshTokenService.createRefreshToken(authUser);

        log.info("Login successful for '{}' on tenant '{}'", request.getEmail(), tenantCode);

        return userMapper.toLoginResponse(user, token, refreshToken,
                jwtProperties.getExpiration(), jwtProperties.getRefreshExpiration());
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        AuthenticatedUser authUser = refreshTokenService.validateAndConsume(request.getRefreshToken());

        String newToken = tokenProvider.generateToken(authUser);
        String newRefreshToken = refreshTokenService.createRefreshToken(authUser);

        log.info("Token refreshed for '{}' on tenant '{}'", authUser.getEmail(), authUser.getTenantCode());

        return LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtProperties.getExpiration())
                .refreshExpiresIn(jwtProperties.getRefreshExpiration())
                .build();
    }

    private String resolveTenantCode(UUID tenantId) {
        try {
            TenantInfo tenant = tenancyStub.getTenantById(
                    GetTenantByIdRequest.newBuilder().setTenantId(tenantId.toString()).build());
            return tenant.getCode();
        } catch (Exception e) {
            log.error("AUDIT: Failed to resolve tenant code for tenantId={}: {}", tenantId, e.getMessage());
            throw new BadCredentialsException("Unable to resolve tenant");
        }
    }
}
