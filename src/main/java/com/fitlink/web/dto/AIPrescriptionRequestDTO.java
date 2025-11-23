package com.fitlink.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIPrescriptionRequestDTO {

    private Integer age;        // MESURE_AGE_CO → 나이
    private Integer gender;     // SEXDSTN_FLAG_CD → 성별 (0: 여자, 1: 남자)
    private Integer height;     // MESURE_IEM_001_VALUE → 키(cm)
    private Integer weight;     // MESURE_IEM_002_VALUE → 몸무게(kg)
}
