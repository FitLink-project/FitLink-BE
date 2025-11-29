package com.fitlink.web.mapper;

import com.fitlink.domain.UsersInfo;
import com.fitlink.web.dto.FitnessResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

    FitnessResponseDTO.UserInfo toDTO(UsersInfo entity);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "usersId", ignore = true)
    UsersInfo toEntity(FitnessResponseDTO.UserInfo dto);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "usersId", ignore = true)
    void updateEntityFromDto(FitnessResponseDTO.UserInfo dto, @MappingTarget UsersInfo entity);
}