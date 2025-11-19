package com.fitlink.web.mapper;

import com.fitlink.domain.FitnessResult;
import com.fitlink.domain.Users;
import com.fitlink.web.dto.FitnessResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FitnessResultMapper {

    // Entity -> DTO
    @Mapping(target = "strength", source = "strength")
    @Mapping(target = "muscular", source = "muscular")
    @Mapping(target = "flexibility", source = "flexibility")
    @Mapping(target = "cardiopulmonary", source = "cardiopulmonary")
    @Mapping(target = "agility", source = "agility")
    @Mapping(target = "quickness", source = "quickness")
    FitnessResponseDTO toResponseDTO(FitnessResult result);

    // DTO -> Entity
    @Mapping(target = "id", ignore = true) // PK 무시
    @Mapping(target = "user", source = "user") // 외부에서 전달
    @Mapping(target = "strength", source = "dto.strength")
    @Mapping(target = "muscular", source = "dto.muscular")
    @Mapping(target = "flexibility", source = "dto.flexibility")
    @Mapping(target = "cardiopulmonary", source = "dto.cardiopulmonary")
    @Mapping(target = "agility", source = "dto.agility")
    @Mapping(target = "quickness", source = "dto.quickness")
    FitnessResult toEntity(FitnessResponseDTO dto, Users user);

    FitnessResponseDTO toResponse(FitnessResult entity);

    // UPDATE: DTO -> Entity
    @Mapping(target = "user", ignore = true) // user는 변경하지 않음
    @Mapping(target = "id", ignore = true)   // PK는 변경하지 않음
    @Mapping(target = "strength", source = "dto.strength")
    @Mapping(target = "muscular", source = "dto.muscular")
    @Mapping(target = "flexibility", source = "dto.flexibility")
    @Mapping(target = "cardiopulmonary", source = "dto.cardiopulmonary")
    @Mapping(target = "agility", source = "dto.agility")
    @Mapping(target = "quickness", source = "dto.quickness")
    void updateEntityFromResponse(FitnessResponseDTO dto, @MappingTarget FitnessResult entity);
}
