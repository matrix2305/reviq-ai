package com.reviq.identity.grpc.server;

import com.reviq.identity.api.AuthService;
import com.reviq.identity.api.UserService;
import com.reviq.identity.api.dto.CreateUserRequest;
import com.reviq.identity.api.dto.LoginRequest;
import com.reviq.identity.api.dto.UserDto;
import com.reviq.identity.grpc.AuthenticateRequest;
import com.reviq.identity.grpc.AuthenticateResponse;
import com.reviq.identity.grpc.CreateUserGrpcRequest;
import com.reviq.identity.grpc.GetUserByTokenRequest;
import com.reviq.identity.grpc.UserIdentityServiceGrpc;
import com.reviq.identity.grpc.UserInfoResponse;
import com.reviq.identity.grpc.ValidateTokenRequest;
import com.reviq.identity.grpc.ValidateTokenResponse;
import com.reviq.security.jwt.JwtTokenProvider;
import com.reviq.security.model.AuthenticatedUser;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserIdentityGrpcService extends UserIdentityServiceGrpc.UserIdentityServiceImplBase {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @Override
    public void authenticate(AuthenticateRequest request,
                              StreamObserver<AuthenticateResponse> responseObserver) {
        try {
            var loginResponse = authService.login(new LoginRequest(
                    request.getEmail(), request.getPassword()));

            var user = loginResponse.getUser();
            var responseBuilder = AuthenticateResponse.newBuilder()
                    .setToken(loginResponse.getToken())
                    .setExpiresIn(loginResponse.getExpiresIn())
                    .setUser(toUserInfo(user));

            if (loginResponse.getRefreshToken() != null) {
                responseBuilder.setRefreshToken(loginResponse.getRefreshToken());
                responseBuilder.setRefreshExpiresIn(loginResponse.getRefreshExpiresIn());
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void validateToken(ValidateTokenRequest request,
                               StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            if (!tokenProvider.validateToken(request.getToken())) {
                responseObserver.onNext(ValidateTokenResponse.newBuilder()
                        .setValid(false)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            AuthenticatedUser authUser = tokenProvider.parseToken(request.getToken());
            responseObserver.onNext(ValidateTokenResponse.newBuilder()
                    .setValid(true)
                    .setTenantCode(authUser.getTenantCode())
                    .setUserId(authUser.getUserId().toString())
                    .setRole(authUser.getRole() != null ? authUser.getRole() : "")
                    .addAllPermissions(authUser.getPermissions())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ValidateTokenResponse.newBuilder()
                    .setValid(false)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserByToken(GetUserByTokenRequest request,
                                StreamObserver<UserInfoResponse> responseObserver) {
        try {
            if (!tokenProvider.validateToken(request.getToken())) {
                responseObserver.onError(Status.UNAUTHENTICATED
                        .withDescription("Invalid token")
                        .asRuntimeException());
                return;
            }

            AuthenticatedUser authUser = tokenProvider.parseToken(request.getToken());
            UserDto user = userService.findById(authUser.getUserId());

            responseObserver.onNext(toUserInfo(user));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void createUser(CreateUserGrpcRequest request,
                            StreamObserver<UserInfoResponse> responseObserver) {
        try {
            log.info("gRPC CreateUser for tenant '{}': {}", request.getTenantCode(), request.getEmail());

            java.util.UUID tenantId = request.getTenantId().isEmpty()
                    ? null : java.util.UUID.fromString(request.getTenantId());

            java.util.UUID roleId = request.getRoleId().isEmpty()
                    ? null : java.util.UUID.fromString(request.getRoleId());

            var createRequest = CreateUserRequest.builder()
                    .tenantCode(request.getTenantCode())
                    .tenantId(tenantId)
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .roleId(roleId)
                    .roleCode(request.getRoleCode().isEmpty() ? null : request.getRoleCode())
                    .build();

            UserDto user = userService.createUser(createRequest);

            responseObserver.onNext(toUserInfo(user));
            responseObserver.onCompleted();

            log.info("gRPC CreateUser success: {} for tenant '{}'", request.getEmail(), request.getTenantCode());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    private UserInfoResponse toUserInfo(UserDto user) {
        var builder = UserInfoResponse.newBuilder()
                .setId(user.getId().toString())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());

        if (user.getRole() != null) builder.setRole(user.getRole());
        if (user.getPermissions() != null) builder.addAllPermissions(user.getPermissions());

        return builder.build();
    }
}
