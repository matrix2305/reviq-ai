package com.reviq.identity.api;

import com.reviq.identity.api.dto.CreateUserRequest;
import com.reviq.identity.api.dto.UserDto;
import com.reviq.shared.search.SearchRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {

    UserDto createUser(CreateUserRequest request);

    Page<UserDto> search(SearchRequest request);

    UserDto findById(UUID id);

    UserDto deactivateUser(UUID id);
}
