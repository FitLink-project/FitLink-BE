package com.fitlink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FitnessResponseDTO {

    @Schema(description = "근력(Strength) - 악력(gripStrength) 또는 슬라이더 점수로부터 계산됨")
    private Float strength;

    @Schema(description = "근지구력(Muscular Endurance) - 윗몸일으키기(sitUp)로부터 계산됨")
    private Float muscular;

    @Schema(description = "유연성(Flexibility) - 앉아 윗몸 앞으로 굽히기(sitAndReach)로부터 계산됨")
    private Float flexibility;

    @Schema(description = "심폐지구력(Cardiopulmonary) - 20m 왕복 오래달리기(shuttleRun) 또는 YMCA 스텝 테스트(ymcaStepTest) 결과로부터 계산됨")
    private Float cardiopulmonary;

    @Schema(description = "민첩성(Agility) - 10m 왕복달리기(sprint) 또는 슬라이더 점수(sliderAgility)로부터 계산됨")
    private Float agility;

    @Schema(description = "순발력(Quickness) - 제자리 멀리뛰기(standingLongJump) 또는 슬라이더 점수로부터 계산됨")
    private Float quickness;

    @Schema(description = "운동 종목별 대한민국 평균(기준) 데이터 객체")
    private FitnessAverage average;

    @Schema(description = "사용자 신체 정보")
    private UserInfo userInfo;

    /**
     * 평균값 데이터를 담는 내부 클래스
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class FitnessAverage {

        @Schema(description = "근력(Strength) 평균 - 악력 측정값 기준")
        private Float gripStrength;

        @Schema(description = "근지구력(Muscular Endurance) 평균 - 윗몸일으키기 측정값 기준")
        private Float sitUp;

        @Schema(description = "유연성(Flexibility) 평균 - 앉아 윗몸 앞으로 굽히기 측정값 기준")
        private Float sitAndReach;

        @Schema(description = "심폐지구력(Cardiopulmonary Endurance) 평균 - 20m 왕복 오래달리기 측정값 기준")
        private Float shuttleRun;

        @Schema(description = "민첩성(Agility) 평균 - 10m 왕복달리기 측정값 기준")
        private Float sprint;

        @Schema(description = "순발력(Quickness) 평균 - 제자리 멀리뛰기 측정값 기준")
        private Float standingLongJump;
    }

    /**
     * 사용자의 정보를 담은 클래스
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class UserInfo {
        @Schema(description = "성별(Sex) - 남성: 'M', 여성: 'F'로 구분", example = "M")
        private String sex;

        @Schema(description = "생년월일(Date of Birth) - YYYYMMDD 형식", example = "19900101")
        private String birthDate;

        @Schema(description = "신장(Height) - 단위: cm", example = "175.0")
        private Float height;

        @Schema(description = "체중(Weight) - 단위: kg", example = "70.5")
        private Float weight;
    }
}
