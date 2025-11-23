package com.fitlink.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AIPrescriptionResponseDTO {
    private List<String> warmup;        // 준비운동
    private List<String> mainExercise;  // 본운동
    private List<String> cooldown;      // 마무리운동
}
