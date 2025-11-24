package com.fitlink.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Weka 모델 예측 결과(영어)를 한국어로 변환하는 유틸리티 클래스
 * korean_to_english_mapping.md 파일을 읽어서 매핑 정보를 로드합니다.
 */
@Slf4j
@Component
public class ExerciseMappingUtil {

    private final Map<String, String> prepExerciseMap = new HashMap<>();
    private final Map<String, String> mainExerciseMap = new HashMap<>();
    private final Map<String, String> coolDownExerciseMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("ai/korean_to_english_mapping.md");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 섹션 감지
                if (line.contains("## Prep_Exercise")) {
                    currentSection = "PREP";
                    continue;
                } else if (line.contains("## Main_Exercise")) {
                    currentSection = "MAIN";
                    continue;
                } else if (line.contains("## CoolDown_Exercise")) {
                    currentSection = "COOLDOWN";
                    continue;
                }

                // 테이블 행 파싱 (| 한국어 | English | 형식)
                if (line.startsWith("|") && line.contains("|") && currentSection != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String korean = parts[1].trim();
                        String english = parts[2].trim().replace("`", "");

                        // 빈 값이나 헤더 행 제외
                        if (!korean.isEmpty() && !english.isEmpty() && 
                            !korean.equals("한국어") && !english.equals("English")) {
                            
                            switch (currentSection) {
                                case "PREP":
                                    prepExerciseMap.put(english, korean);
                                    break;
                                case "MAIN":
                                    mainExerciseMap.put(english, korean);
                                    break;
                                case "COOLDOWN":
                                    coolDownExerciseMap.put(english, korean);
                                    break;
                            }
                        }
                    }
                }
            }

            reader.close();
            log.info("Exercise mapping loaded - Prep: {}, Main: {}, CoolDown: {}", 
                    prepExerciseMap.size(), mainExerciseMap.size(), coolDownExerciseMap.size());

        } catch (Exception e) {
            log.error("Failed to load exercise mapping file", e);
            throw new RuntimeException("Failed to initialize ExerciseMappingUtil", e);
        }
    }

    /**
     * 영어 운동 이름을 한국어로 변환 (준비운동)
     */
    public String toKoreanPrep(String english) {
        return prepExerciseMap.getOrDefault(english, english);
    }

    /**
     * 영어 운동 이름을 한국어로 변환 (본운동)
     */
    public String toKoreanMain(String english) {
        return mainExerciseMap.getOrDefault(english, english);
    }

    /**
     * 영어 운동 이름을 한국어로 변환 (마무리운동)
     */
    public String toKoreanCoolDown(String english) {
        return coolDownExerciseMap.getOrDefault(english, english);
    }

    /**
     * 쉼표로 구분된 여러 운동 이름을 한국어로 변환 (준비운동)
     */
    public String convertPrepExercises(String englishExercises) {
        if (englishExercises == null || englishExercises.trim().isEmpty()) {
            return "";
        }
        
        String[] exercises = englishExercises.split(",");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < exercises.length; i++) {
            String exercise = exercises[i].trim();
            result.append(toKoreanPrep(exercise));
            if (i < exercises.length - 1) {
                result.append(",");
            }
        }
        
        return result.toString();
    }

    /**
     * 쉼표로 구분된 여러 운동 이름을 한국어로 변환 (본운동)
     */
    public String convertMainExercises(String englishExercises) {
        if (englishExercises == null || englishExercises.trim().isEmpty()) {
            return "";
        }
        
        String[] exercises = englishExercises.split(",");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < exercises.length; i++) {
            String exercise = exercises[i].trim();
            result.append(toKoreanMain(exercise));
            if (i < exercises.length - 1) {
                result.append(",");
            }
        }
        
        return result.toString();
    }

    /**
     * 쉼표로 구분된 여러 운동 이름을 한국어로 변환 (마무리운동)
     */
    public String convertCoolDownExercises(String englishExercises) {
        if (englishExercises == null || englishExercises.trim().isEmpty()) {
            return "";
        }
        
        String[] exercises = englishExercises.split(",");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < exercises.length; i++) {
            String exercise = exercises[i].trim();
            result.append(toKoreanCoolDown(exercise));
            if (i < exercises.length - 1) {
                result.append(",");
            }
        }
        
        return result.toString();
    }
}

