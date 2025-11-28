package com.fitlink.service.fitness;

import com.fitlink.service.fitness.standards.FitnessStandardSet;
import com.fitlink.service.fitness.standards.FitnessStandards;
import com.fitlink.service.fitness.standards.Sex;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * KF100 및 일반 체력 점수 계산 서비스.
 * */
@Service
@RequiredArgsConstructor
public class FitnessScoreService {

    private final FitnessCalculator calculator;
    private final FitnessStandards fitnessStandards;

    /**
     * 국민체력100 체력 측정 DTO를 기반으로 점수를 계산함.
     */
    public FitnessResponseDTO calculateKookmin(FitnessKookminRequestDTO dto) {
        FitnessResponseDTO res = new FitnessResponseDTO();

        // 사용자의 성별과 나이 기준으로 FitnessStandardSet 조회
        Sex sex = "M".equals(dto.getSex()) ? Sex.M : Sex.F;
        int age = calculateAge(dto.getBirthDate());
        FitnessStandardSet standardSet = fitnessStandards.getStandard(sex, age);

        res.setStrength(calculator.scoreHigherIsBetter(dto.getGripStrength(), standardSet.getGripStrength()));
        res.setMuscular(calculator.scoreHigherIsBetter(dto.getSitUp() == null ? dto.getCrossSitUp() : dto.getSitUp(), standardSet.getSitUp()));
        res.setFlexibility(calculator.scoreHigherIsBetter(dto.getSitAndReach(), standardSet.getSitAndReach()));
        res.setCardiopulmonary(calculator.scoreHigherIsBetter(dto.getShuttleRun(), standardSet.getShuttleRun()));
        res.setAgility(calculator.scoreLowerIsBetter(dto.getSprint(), standardSet.getSprint()));
        res.setQuickness(calculator.scoreHigherIsBetter(dto.getStandingLongJump(), standardSet.getStandingLongJump()));

        return res;
    }

    /**
     * 일반 체력 측정 DTO를 기반으로 점수를 계산함.
     */
    public FitnessResponseDTO calculateGeneral(FitnessGeneralRequestDTO dto) {
        FitnessResponseDTO res = new FitnessResponseDTO();

        // 사용자의 성별과 나이 기준으로 FitnessStandardSet 조회
        Sex sex = "M".equals(dto.getSex()) ? Sex.M : Sex.F;
        int age = calculateAge(dto.getBirthDate());
        FitnessStandardSet standardSet = fitnessStandards.getStandard(sex, age);

        res.setStrength(dto.getSliderStrength() == null ? null : dto.getSliderStrength().floatValue());
        res.setMuscular(calculator.scoreHigherIsBetter(dto.getSitUp(), standardSet.getSitUp()));
        res.setFlexibility(calculator.scoreHigherIsBetter(dto.getSitAndReach(), standardSet.getSitAndReach()));
        res.setCardiopulmonary(calculator.scoreHigherIsBetter(dto.getYmcaStepTest(), standardSet.getShuttleRun()));
        res.setAgility(dto.getSliderAgility() == null ? null : dto.getSliderAgility().floatValue());
        res.setQuickness(dto.getSliderPower() == null ? null : dto.getSliderPower().floatValue());

        return res;
    }

    /**
     * 생년월일(YYYYMMDD) 기반으로 만 나이 계산
     * @param birthDate "19900101" 형식 문자열
     * @return 만 나이
     */
    public static int calculateAge(String birthDate) {
        if (birthDate == null || birthDate.length() != 8) {
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate birth = LocalDate.parse(birthDate, formatter);
        LocalDate today = LocalDate.now();

        int age = (int) ChronoUnit.YEARS.between(birth, today);

        // 생일이 올해 아직 안 지났으면 -1
        if (today.getMonthValue() < birth.getMonthValue() ||
                (today.getMonthValue() == birth.getMonthValue() && today.getDayOfMonth() < birth.getDayOfMonth())) {
            age--;
        }

        return age;
    }
}

