package com.fitlink.service.fitness;

import com.fitlink.service.fitness.standards.FitnessGrade;
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
     * @param grade 1등급과 2등급 값 저장 객체
     * @return 계산된 점수 (0~100)
     */
    public Float scoreHigherIsBetter(Float value, FitnessGrade grade) {
        if (value == null) return 0f;

        float maxValue = grade.getGrade1(); // 1등급
        float minValue = grade.getGrade1() - 5 * (grade.getGrade1() - grade.getGrade2()); // 1~6등급 범위 가정
        float midValue = (minValue + maxValue) / 2;

        float score = value <= midValue
                ? linear(value, minValue, midValue, 0, 60)
                : linear(value, midValue, maxValue, 60, 100);

        return Math.max(0, score);
    }

    /**
     * 작은 값이 좋을 때 점수를 계산
     *
     * @param value 측정값
     * @param grade 1등급과 2등급 값 저장 객체
     * @return 계산된 점수 (0~100)
     */
    public Float scoreLowerIsBetter(Float value, FitnessGrade grade) {
        if (value == null) return 0f;

        float minValue = grade.getGrade1(); // 1등급
        float maxValue = grade.getGrade2() + 5 * (grade.getGrade2() - grade.getGrade1()); // 1~6등급 범위 가정
        float midValue = (minValue + maxValue) / 2;

        float score = value >= midValue
                ? linear(value, maxValue, midValue, 0, 60)
                : linear(value, midValue, minValue, 60, 100);

        return Math.max(0, score);
    }

    /**
     * 두 점을 이용한 선형 보간
     */
    private float linear(float x, float x0, float x1, float y0, float y1) {
        return y0 + (y1 - y0) * ((x - x0) / (x1 - x0));
    }
}
