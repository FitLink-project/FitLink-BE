package com.fitlink.web.mapper;

import com.fitlink.domain.TestGeneral;
import com.fitlink.domain.TestKookmin;
import com.fitlink.web.dto.FitnessResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FitnessMapper {

    /**
     * TestKookmin Entity -> TestKookminDTO 변환
     */
    public FitnessResponseDTO.TestKookminDTO toKookminDTO(TestKookmin entity) {
        if (entity == null) {
            return null;
        }

        return FitnessResponseDTO.TestKookminDTO.builder()
                .gripStrength(entity.getGripStrength())
                .sitUp(entity.getSitUp())
                .sitAndReach(entity.getSitAndReach())
                .shuttleRun(entity.getShuttleRun())
                .sprint(entity.getSprint())
                .standingLongJump(entity.getStandingLongJump())
                .build();
    }

    /**
     * TestGeneral Entity -> TestGeneralDTO 변환
     */
    public FitnessResponseDTO.TestGeneralDTO toGeneralDTO(TestGeneral entity) {
        if (entity == null) {
            return null;
        }

        return FitnessResponseDTO.TestGeneralDTO.builder()
                .sliderStrength(entity.getSliderStrength())
                .sitUp(entity.getSitUp())
                .sitAndReach(entity.getSitAndReach())
                .ymcaStepTest(entity.getYmcaStepTest())
                .sliderAgility(entity.getSliderAgility())
                .sliderPower(entity.getSliderPower())
                .build();
    }
}