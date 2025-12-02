package com.fitlink.service.fitness;

import com.fitlink.service.fitness.standards.FitnessStandardSet;
import com.fitlink.service.fitness.standards.FitnessStandards;
import com.fitlink.domain.enums.Sex;
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

        // 해당되는 구간의 1, 2등급 기준값 추가 (기존 getAverage -> getStandards 변경)
        res.setStandard(getStandards(standardSet));
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

        // 해당되는 구간의 1, 2등급 기준값 추가 (기존 getAverage -> getStandards 변경)
        res.setStandard(getStandards(standardSet));
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

    /** 해당 standardSet에서 1등급과 2등급 값을 모두 가져와 ResponseDTO에 추가함.
     *
     * @param standardSet 가져올 평균 세트
     * @return FitnessStandardResponse 1등급과 2등급 데이터가 모두 포함된 응답 객체
     */
    public FitnessResponseDTO.FitnessStandardResponse getStandards(FitnessStandardSet standardSet) {

        // 1등급(Grade 1) 데이터 추출 및 빌드
        FitnessResponseDTO.FitnessAverage grade1Average = FitnessResponseDTO.FitnessAverage.builder()
                .gripStrength(standardSet.getGripStrength().getGrade1())
                .sitUp(standardSet.getSitUp().getGrade1())
                .sitAndReach(standardSet.getSitAndReach().getGrade1())
                .shuttleRun(standardSet.getShuttleRun().getGrade1())
                .sprint(standardSet.getSprint().getGrade1())
                .standingLongJump(standardSet.getStandingLongJump().getGrade1())
                .build();

        // 2등급(Grade 2) 데이터 추출 및 빌드
        FitnessResponseDTO.FitnessAverage grade2Average = FitnessResponseDTO.FitnessAverage.builder()
                .gripStrength(standardSet.getGripStrength().getGrade2())
                .sitUp(standardSet.getSitUp().getGrade2())
                .sitAndReach(standardSet.getSitAndReach().getGrade2())
                .shuttleRun(standardSet.getShuttleRun().getGrade2())
                .sprint(standardSet.getSprint().getGrade2())
                .standingLongJump(standardSet.getStandingLongJump().getGrade2())
                .build();

        // 두 등급 데이터를 하나의 DTO로 묶어서 반환
        return FitnessResponseDTO.FitnessStandardResponse.builder()
                .grade1(grade1Average)
                .grade2(grade2Average)
                .build();
    }
}

