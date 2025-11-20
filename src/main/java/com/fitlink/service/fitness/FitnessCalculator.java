package com.fitlink.service.fitness;

import com.fitlink.service.fitness.standards.FitnessGrade;
import org.springframework.stereotype.Component;

/**
 * 체력 측정 점수 계산 전용 클래스
 */
@Component
public class FitnessCalculator {

    /**
     * value를 float으로 형변환 하고 큰 값이 좋을 때 점수를 계산
     *
     * @param value 측정값
     * @param grade 1등급과 2등급 값 저장 객체
     * @return 계산된 점수 (0~100)
     */
    public <T extends Number> Float scoreHigherIsBetter(T value, FitnessGrade grade) {
        if (value == null) return null;

        float val = value.floatValue();

        float maxValue = grade.getGrade1();
        float minValue = grade.getGrade1() - 5 * (grade.getGrade1() - grade.getGrade2());
        float midValue = (minValue + maxValue) / 2;

        float score = val <= midValue
                ? linear(val, minValue, midValue, 0, 60)
                : linear(val, midValue, maxValue, 60, 100);

        return Math.round(Math.max(0, score) * 10) / 10f;
    }

    /**
     * value를 float으로 형변환 하고 작은 값이 좋을 때 점수를 계산
     *
     * @param value 측정값
     * @param grade 1등급과 2등급 값 저장 객체
     * @return 계산된 점수 (0~100)
     */
    public <T extends Number> Float scoreLowerIsBetter(T value, FitnessGrade grade) {
        if (value == null) return null;

        float val = value.floatValue();

        float minValue = grade.getGrade1();
        float maxValue = grade.getGrade2() + 5 * (grade.getGrade2() - grade.getGrade1());
        float midValue = (minValue + maxValue) / 2;

        float score = val >= midValue
                ? linear(val, maxValue, midValue, 0, 60)
                : linear(val, midValue, minValue, 60, 100);

        return Math.round(Math.max(0, score) * 10) / 10f;
    }

    /**
     * 두 점을 이용한 선형 보간
     */
    private float linear(float x, float x0, float x1, float y0, float y1) {
        return y0 + (y1 - y0) * ((x - x0) / (x1 - x0));
    }
}
