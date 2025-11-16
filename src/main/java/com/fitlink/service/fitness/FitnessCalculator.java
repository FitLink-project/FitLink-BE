package com.fitlink.service.fitness;

import org.springframework.stereotype.Component;

/**
 * 체력 측정 점수 계산 전용 클래스
 */
@Component
public class FitnessCalculator {

    /**
     * 큰 값이 좋을 때 점수를 계산
     *
     * @param value 측정값
     * @param grade 1등급과 2등급 값 배열 [grade1, grade2]
     * @return 계산된 점수 (0~100)
     */
    public Float scoreHigherIsBetter(Float value, float[] grade) {
        if (value == null) return 0f;

        float maxValue = grade[0]; // 1등급
        float minValue = grade[0] - 5 * (grade[0] - grade[1]); // 1등급과 6등급 정도의 차이라고 두었음
        float midValue = (minValue + maxValue) / 2; // 중앙값

        float score = value <= midValue
                ? linear(value, minValue, midValue, 0, 60) // 입력값이 중앙값보다 낮은 경우
                : linear(value, midValue, maxValue, 60, 100); // 입력값이 중앙값보다 높은 경우

        return Math.max(0, score);
    }

    /**
     * 작은 값이 좋을 때 점수를 계산
     *
     * @param value 측정값
     * @param grade 1등급과 2등급 값 배열 [grade1, grade2]
     * @return 계산된 점수 (0~100)
     */
    public Float scoreLowerIsBetter(Float value, float[] grade) {
        if (value == null) return 0f;

        float minValue = grade[0]; // 1등급
        float maxValue = grade[1] + 5 * (grade[1] - grade[0]); // 1등급과 6등급 정도의 차이
        float midValue = (minValue + maxValue) / 2; // 중앙값

        float score = value >= midValue
                ? linear(value, maxValue, midValue, 0, 60) // 입력값이 중앙값보다 높은 경우
                : linear(value, midValue, minValue, 60, 100); // 입력값이 중앙값보다 낮은 경우

        return Math.max(0, score);
    }

    /**
     * 두 점을 이용한 선형 보간
     */
    private float linear(float x, float x0, float x1, float y0, float y1) {
        return y0 + (y1 - y0) * ((x - x0) / (x1 - x0));
    }
}
