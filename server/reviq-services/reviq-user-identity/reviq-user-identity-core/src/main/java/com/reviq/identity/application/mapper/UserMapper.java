package com.reviq.identity.application.mapper;

import com.reviq.identity.api.dto.LoginResponse;
import com.reviq.identity.api.dto.UserDto;
import com.reviq.identity.domain.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "roleCode", target = "role")
    @Mapping(target = "permissions", expression = "java(entity.getPermissionCodes())")
    UserDto toDto(AppUser entity);

    List<UserDto> toDtoList(List<AppUser> entities);

    @Mapping(source = "entity", target = "user")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "refreshToken", target = "refreshToken")
    @Mapping(source = "expiresIn", target = "expiresIn")
    @Mapping(source = "refreshExpiresIn", target = "refreshExpiresIn")
    LoginResponse toLoginResponse(AppUser entity, String token, String refreshToken,
                                   long expiresIn, long refreshExpiresIn);
}
