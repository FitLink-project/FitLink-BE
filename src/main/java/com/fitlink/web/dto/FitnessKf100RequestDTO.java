package com.fitlink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FitnessKf100RequestDTO {

    @Schema(description = "성별(Sex) — 남성: 'M', 여성: 'F'로 구분", example = "M")
    private String sex;

    @Schema(description = "생년월일(Date of Birth) — YYYYMMDD 형식", example = "19900101")
    private String birthDate;

    @Schema(description = "신장(Height) — 단위: cm", example = "175.0")
    private Float height;

    @Schema(description = "체중(Weight) — 단위: kg", example = "70.5")
    private Float weight;

    @Schema(description = "악력(kg) — 근력(Strength) 계산에 사용", example = "65.5")
    private Float gripStrength;

    @Schema(description = "윗몸일으키기(회) — 근지구력(Muscular Endurance) 계산에 사용", example = "50")
    private Integer sitUp;

    @Schema(description = "앉아 윗몸 앞으로 굽히기(cm) — 유연성(Flexibility) 계산에 사용", example = "18.2")
    private Float sitAndReach;

    @Schema(description = "20m 왕복 오래달리기(회) — 심폐지구력(Cardiopulmonary) 계산에 사용", example = "55")
    private Integer shuttleRun;

    @Schema(description = "10m 왕복 달리기(초) — 민첩성(Agility) 계산에 사용", example = "12.3")
    private Float sprint;

    @Schema(description = "제자리 멀리뛰기(cm) — 순발력(Quickness) 계산에 사용", example = "245.0")
    private Float standingLongJump;
}
