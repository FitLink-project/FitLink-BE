package com.fitlink.web.mapper;

import com.fitlink.domain.Users;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    // JoinDTO -> Users (회원가입 시)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "deleteDate", ignore = true)
    @Mapping(target = "profileUrl", ignore = true)
    Users toEntity(UserRequestDTO.JoinDTO joinDTO);

    // Users -> JoinResultDTO
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    UserResponseDTO.JoinResultDTO toJoinResultDTO(Users user);

    // Users -> LoginResultDTO
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "accessToken", source = "accessToken")
    UserResponseDTO.LoginResultDTO toLoginResultDTO(Users user, String accessToken);
}

