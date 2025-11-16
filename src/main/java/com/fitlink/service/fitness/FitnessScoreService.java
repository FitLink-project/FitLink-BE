package com.fitlink.service.fitness;

import com.fitlink.web.dto.FitnessKf100RequestDTO;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * KF100 및 일반 체력 점수 계산 서비스
 */
@Service
@RequiredArgsConstructor
public class FitnessScoreService {

    private final FitnessCalculator calculator;

    // ===== grade 배열 예시 (float) =====
    private static final float[] GRIP = {60f, 55f};
    private static final float[] SITUP = {55f, 45f};
    private static final float[] REACH = {20f, 15f};
    private static final float[] SHUTTLE = {60f, 50f};
    private static final float[] SPRINT = {10.5f, 12f};
    private static final float[] JUMP = {250f, 220f};
    
    /**
     * KF100 체력 측정 DTO를 기반으로 점수를 계산
     *
     * @param dto KF100 체력 측정 DTO
     * @return 계산된 FitnessResponseDTO
     */
    public FitnessResponseDTO calculateKf100(FitnessKf100RequestDTO dto) {
        FitnessResponseDTO res = new FitnessResponseDTO();

        res.setStrength(calculator.scoreHigherIsBetter(dto.getGripStrength(), GRIP));
        res.setMuscular(calculator.scoreHigherIsBetter(dto.getSitUp().floatValue(), SITUP));
        res.setFlexibility(calculator.scoreHigherIsBetter(dto.getSitAndReach(), REACH));
        res.setCardiopulmonary(calculator.scoreHigherIsBetter(dto.getShuttleRun().floatValue(), SHUTTLE));
        res.setAgility(calculator.scoreLowerIsBetter(dto.getSprint(), SPRINT));
        res.setQuickness(calculator.scoreHigherIsBetter(dto.getStandingLongJump(), JUMP));

        return res;
    }

    /**
     * 일반 체력 측정 DTO를 기반으로 점수를 계산
     *
     * @param dto 일반 체력 측정 DTO
     * @return 계산된 FitnessResponseDTO
     */
    public FitnessResponseDTO calculateGeneral(FitnessGeneralRequestDTO dto) {
        FitnessResponseDTO res = new FitnessResponseDTO();

        res.setStrength(calculator.scoreHigherIsBetter(dto.getSliderStrength().floatValue(), GRIP));
        res.setMuscular(calculator.scoreHigherIsBetter(dto.getSitUp().floatValue(), SITUP));
        res.setFlexibility(calculator.scoreHigherIsBetter(dto.getSitAndReach(), REACH));
        res.setCardiopulmonary(calculator.scoreHigherIsBetter(dto.getYmcaStepTest(), SHUTTLE));
        res.setAgility(calculator.scoreLowerIsBetter(dto.getSliderAgility().floatValue(), SPRINT));
        res.setQuickness(calculator.scoreHigherIsBetter(dto.getSliderPower().floatValue(), JUMP));

        return res;
    }
}
