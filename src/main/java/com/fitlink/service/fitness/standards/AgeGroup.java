package com.fitlink.service.fitness.standards;

/**
 * 연령대 범위 저장하는 record. 간단하게 min~max만 관리함.
 * */
public record AgeGroup(int minAge, int maxAge) { }
