package com.fitlink.service.fitness.standards;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1등급, 2등급 값을 저장함.
 * */
@AllArgsConstructor
@Getter
public class FitnessGrade {

    private final float grade1;
    private final float grade2;
}
