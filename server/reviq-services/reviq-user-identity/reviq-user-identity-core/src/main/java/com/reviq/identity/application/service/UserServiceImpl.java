package com.reviq.identity.application.service;

import com.reviq.identity.api.UserService;
import com.reviq.identity.api.dto.CreateUserRequest;
import com.reviq.identity.api.dto.UserDto;
import com.reviq.identity.application.mapper.UserMapper;
import com.reviq.identity.domain.entity.AppUser;
import com.reviq.identity.domain.entity.Role;
import com.reviq.identity.domain.repository.AppUserRepository;
import com.reviq.identity.domain.repository.RoleRepository;
import com.reviq.tenancy.grpc.GetConnectionRequest;
import com.reviq.tenancy.grpc.TenancyServiceGrpc;
import com.reviq.shared.exception.BadRequestException;
import com.reviq.shared.exception.NotFoundException;
import com.reviq.shared.search.SearchRequest;
import com.reviq.shared.search.SearchSpecification;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @GrpcClient("tenancy-service")
    private TenancyServiceGrpc.TenancyServiceBlockingStub tenancyStub;

    public UserServiceImpl(AppUserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        UUID tenantId = request.getTenantId() != null
                ? request.getTenantId()
                : resolveTenantId(request.getTenantCode());

        if (userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new BadRequestException("USER_EXISTS", "User already exists: " + request.getEmail());
        }

        Role role = resolveRole(request);

        AppUser user = AppUser.builder()
                .tenantId(tenantId)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .build();

        user = userRepository.save(user);
        log.info("Created user '{}' for tenant '{}'", request.getEmail(), request.getTenantCode());

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> search(SearchRequest request) {
        SearchSpecification<AppUser> spec = new SearchSpecification<>(AppUser.class, request);
        Pageable pageable = SearchSpecification.getPageable(request.getPage(), request.getSize());
        return userRepository.findAll(spec, pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDto deactivateUser(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user: {}", user.getEmail());
        return userMapper.toDto(user);
    }

    private Role resolveRole(CreateUserRequest request) {
        if (request.getRoleId() != null) {
            return roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + request.getRoleId()));
        }
        if (request.getRoleCode() != null) {
            return roleRepository.findByCode(request.getRoleCode())
                    .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + request.getRoleCode()));
        }
        return null;
    }

    private UUID resolveTenantId(String tenantCode) {
        var response = tenancyStub.getConnectionInfo(
                GetConnectionRequest.newBuilder().setTenantCode(tenantCode).build());
        return UUID.fromString(response.getTenantId());
    }
}
