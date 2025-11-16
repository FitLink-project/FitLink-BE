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
public class FitnessResponseDTO {

    @Schema(description = "근력(Strength) — 악력(gripStrength)로부터 계산됨")
    private Float strength;

    @Schema(description = "근지구력(Muscular Endurance) — 윗몸일으키기(sitUp)로부터 계산됨")
    private Float muscular;

    @Schema(description = "유연성(Flexibility) — 앉아 윗몸 앞으로 굽히기(sitAndReach)로부터 계산됨")
    private Float flexibility;

    @Schema(description = "심폐지구력(Cardiopulmonary) — 20m 왕복 오래달리기(shuttleRun) 또는 YMCA 스텝 테스트(ymcaStepTest) 결과로부터 계산됨")
    private Float cardiopulmonary;

    @Schema(description = "민첩성(Agility) — 10m 왕복달리기(sprint) 또는 슬라이더 점수(sliderAgility)로부터 계산됨")
    private Float agility;

    @Schema(description = "순발력(Quickness) — 제자리 멀리뛰기(standingLongJump) 또는 슬라이더 점수(sliderPower)로부터 계산됨")
    private Float quickness;
}
