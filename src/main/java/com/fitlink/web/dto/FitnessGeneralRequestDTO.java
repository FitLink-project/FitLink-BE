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
public class FitnessGeneralRequestDTO {

    @Schema(description = "성별(Sex) - 남성: 'M', 여성: 'F'로 구분", example = "M")
    private String sex;

    @Schema(description = "생년월일(Date of Birth) - YYYYMMDD 형식", example = "19900101")
    private String birthDate;

    @Schema(description = "신장(Height) - 단위: cm", example = "175.0")
    private Float height;

    @Schema(description = "체중(Weight) - 단위: kg", example = "70.5")
    private Float weight;

    @Schema(description = "근력(Strength) 슬라이더 값", example = "80")
    private Integer sliderStrength;

    @Schema(description = "윗몸일으키기 횟수 - 근지구력(Muscular Endurance) 계산에 사용", example = "45")
    private Integer sitUp;

    @Schema(description = "앉아 윗몸 앞으로 굽히기(cm) - 유연성(Flexibility) 계산에 사용", example = "15.1")
    private Float sitAndReach;

    @Schema(description = "YMCA 스텝 테스트 결과(심박수 또는 점수) - 심폐지구력(Cardiopulmonary) 계산에 사용", example = "42.5")
    private Float ymcaStepTest;

    @Schema(description = "민첩성(Agility) 슬라이더 값", example = "90")
    private Integer sliderAgility;

    @Schema(description = "순발력(Power) 슬라이더 값", example = "85")
    private Integer sliderPower;
}
