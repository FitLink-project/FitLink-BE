package com.fitlink.web.mapping;

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
    @Mapping(target = "name", source = "nickName")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "regDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "deleteDate", ignore = true)
    @Mapping(target = "profileUrl", ignore = true)
    Users toEntity(UserRequestDTO.JoinDTO joinDTO);

    // Users -> JoinResultDTO
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "createdAt", source = "regDate")
    UserResponseDTO.JoinResultDTO toJoinResultDTO(Users user);

    // Users -> LoginResultDTO (accessToken은 별도로 설정 필요)
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "accessToken", ignore = true)
    UserResponseDTO.LoginResultDTO toLoginResultDTO(Users user);
}

