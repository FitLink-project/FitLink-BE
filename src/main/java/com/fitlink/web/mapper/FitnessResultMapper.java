package com.fitlink.web.mapper;

import com.fitlink.domain.FitnessResult;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FitnessResultMapper {

    // FitnessResult -> FitnessResponseDTO
    @Mapping(target = "strength", source = "strength")
    @Mapping(target = "muscular", source = "muscular")
    @Mapping(target = "flexibility", source = "flexibility")
    @Mapping(target = "cardiopulmonary", source = "cardiopulmonary")
    @Mapping(target = "agility", source = "agility")
    @Mapping(target = "quickness", source = "quickness")
    FitnessResponseDTO toResponseDTO(FitnessResult result);
}
