package com.fitlink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "체력 평가 결과 응답 DTO")
public class FitnessResponseDTO {

    // ==========================================
    // 1. 체력 측정 6대 요소 결과 (점수)
    // ==========================================
    @Schema(description = "근력(Strength)")
    private Float strength;

    @Schema(description = "근지구력(Muscular Endurance)")
    private Float muscular;

    @Schema(description = "유연성(Flexibility)")
    private Float flexibility;

    @Schema(description = "심폐지구력(Cardiopulmonary)")
    private Float cardiopulmonary;

    @Schema(description = "민첩성(Agility)")
    private Float agility;

    @Schema(description = "순발력(Quickness)")
    private Float quickness;

    // ==========================================
    // 2. 포함된 객체 정보 (평균, 유저정보, 상세기록)
    // ==========================================
    @Schema(description = "운동 종목별 대한민국 평균 데이터")
    private FitnessAverage average;

    @Schema(description = "사용자 신체 정보")
    private UserInfo userInfo;

    @Schema(description = "국민체력 100 상세 측정 수치 (해당 시 포함)")
    private TestKookminDTO testKookmin;

    @Schema(description = "간단 체력 상세 측정 수치 (해당 시 포함)")
    private TestGeneralDTO testGeneral;


    // ==========================================
    // 3. 내부 클래스 (DTO) 정의
    // ==========================================

    /** 대한민국 평균값 DTO */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class FitnessAverage {
        private Float gripStrength;
        private Float sitUp;
        private Float sitAndReach;
        private Float shuttleRun;
        private Float sprint;
        private Float standingLongJump;
    }

    /** 사용자 정보 DTO */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class UserInfo {
        private String sex;
        private String birthDate;
        private Float height;
        private Float weight;
    }

    /** 국민체력 100 상세 기록 DTO */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class TestKookminDTO {
        @Schema(description = "악력 (kg)")
        private BigDecimal gripStrength;

        @Schema(description = "윗몸일으키기 (회)")
        private Integer sitUp;

        @Schema(description = "앉아 윗몸 앞으로 굽히기 (cm)")
        private BigDecimal sitAndReach;

        @Schema(description = "20m 왕복 오래달리기 (회)")
        private Integer shuttleRun;

        @Schema(description = "10m 왕복달리기 (초)")
        private BigDecimal sprint;

        @Schema(description = "제자리 멀리뛰기 (cm)")
        private BigDecimal standingLongJump;
    }

    /** 간단 체력 상세 기록 DTO */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class TestGeneralDTO {
        @Schema(description = "근력 슬라이더 점수")
        private Integer sliderStrength;

        @Schema(description = "윗몸일으키기 (회)")
        private Integer sitUp;

        @Schema(description = "앉아 윗몸 앞으로 굽히기 (cm)")
        private BigDecimal sitAndReach;

        @Schema(description = "YMCA 스텝 테스트")
        private BigDecimal ymcaStepTest;

        @Schema(description = "민첩성 슬라이더 점수")
        private Integer sliderAgility;

        @Schema(description = "순발력 슬라이더 점수")
        private Integer sliderPower;
    }
}