package com.fitlink.service.fitness.standards;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 하나의 성별 및 연령대에 대해 각 종목별로 1, 2등급 기준값을 저장하는 기준표.
 * ex: 여성 19~24세의 체력 기준표
 * */
@AllArgsConstructor
@Getter
public class FitnessStandardSet {

    private final FitnessGrade gripStrength;
    private final FitnessGrade sitUp;
    private final FitnessGrade sitAndReach;
    private final FitnessGrade shuttleRun;
    private final FitnessGrade sprint;
    private final FitnessGrade standingLongJump;

    /*
    * 반복 코드 줄이기용 헬퍼 함수
    * */
    public static FitnessStandardSet createSet(
            float grip1, float grip2,
            float sitUp1, float sitUp2,
            float reach1, float reach2,
            float shuttle1, float shuttle2,
            float sprint1, float sprint2,
            float jump1, float jump2
    ) {
        return new FitnessStandardSet(
                new FitnessGrade(grip1, grip2),
                new FitnessGrade(sitUp1, sitUp2),
                new FitnessGrade(reach1, reach2),
                new FitnessGrade(shuttle1, shuttle2),
                new FitnessGrade(sprint1, sprint2),
                new FitnessGrade(jump1, jump2)
        );
    }

}
