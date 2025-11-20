package com.fitlink.service.fitness.standards;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * 모든 성별 및 연령대 체력 기준표를 저장한 컴포넌트.
 * 프로그램 시작 시 로딩됨.
 * */
@Component
public class FitnessStandards {

    /** 성별 -> 연령대 -> 기준표 형태로 저장됨.
     *  실제 테이블 같은 형식으로 생각하면 됨.
     * */
    private final Map<Sex, Map<AgeGroup, FitnessStandardSet>> table = new HashMap<>();

    /** 생성자, 내부에서 기준값 로딩함 */
    public FitnessStandards() {
        loadMaleStandards();
        loadFemaleStandards();
    }

    /** 남성 기준표 초기화 */
    private void loadMaleStandards() {
        Map<AgeGroup, FitnessStandardSet> male = new HashMap<>();

        male.put(new AgeGroup(19, 24), FitnessStandardSet.createSet(60f,55f,55f,45f,20f,15f,60f,50f,10.5f,12f,250f,220f));
        male.put(new AgeGroup(25, 29), FitnessStandardSet.createSet(62.4f,54.4f,51f,42f,14.9f,12.7f,54f,41f,10.3f,10.9f,223f,208f));
        male.put(new AgeGroup(30, 34), FitnessStandardSet.createSet(62.6f,52.1f,47f,41f,14.2f,11.5f,49f,36f,10.6f,11.2f,219f,202f));
        male.put(new AgeGroup(35, 39), FitnessStandardSet.createSet(62.1f,50.8f,45f,40f,14f,11f,45f,33f,10.9f,11.5f,214f,196f));
        male.put(new AgeGroup(40, 44), FitnessStandardSet.createSet(62.8f,49.5f,44f,39f,14.2f,10.8f,42f,30f,11f,11.6f,209f,189f));
        male.put(new AgeGroup(45, 49), FitnessStandardSet.createSet(62f,48.7f,41f,37f,13.6f,10.1f,40f,28f,11.3f,11.9f,202f,181f));
        male.put(new AgeGroup(50, 54), FitnessStandardSet.createSet(60.5f,46.9f,38f,34f,13.9f,10.4f,35f,24f,11.8f,12.4f,192f,170f));
        male.put(new AgeGroup(55, 59), FitnessStandardSet.createSet(59.4f,45.4f,35f,31f,13.3f,9.7f,31f,20f,12.3f,12.9f,183f,160f));
        male.put(new AgeGroup(60, 64), FitnessStandardSet.createSet(56.8f,42.7f,31f,27f,11.8f,8.2f,26f,15f,13.1f,13.7f,170f,148f));

        table.put(Sex.M, male);
    }

    /** 여성 기준표 초기화 */
    private void loadFemaleStandards() {
        Map<AgeGroup, FitnessStandardSet> female = new HashMap<>();

        female.put(new AgeGroup(19, 24), FitnessStandardSet.createSet(46.8f,40.2f,36f,29f,19.7f,16.5f,30f,22f,12.3f,13.0f,162f,148f));
        female.put(new AgeGroup(25, 29), FitnessStandardSet.createSet(47f,39.4f,33f,27f,18.5f,15.3f,28f,20f,12.7f,13.4f,156f,142f));
        female.put(new AgeGroup(30, 34), FitnessStandardSet.createSet(49.6f,41.5f,31f,26f,18.2f,15f,26f,19f,12.9f,13.6f,154f,139f));
        female.put(new AgeGroup(35, 39), FitnessStandardSet.createSet(47.5f,39.9f,31f,26f,18.9f,15.6f,25f,18f,13f,13.7f,155f,140f));
        female.put(new AgeGroup(40, 44), FitnessStandardSet.createSet(47.1f,39.5f,30f,25f,18.8f,15.5f,24f,17f,13.1f,13.8f,153f,138f));
        female.put(new AgeGroup(45, 49), FitnessStandardSet.createSet(46.2f,38.6f,28f,23f,18.9f,15.6f,23f,16f,13.4f,14.1f,148f,133f));
        female.put(new AgeGroup(50, 54), FitnessStandardSet.createSet(44.7f,37.5f,24f,19f,19.5f,16.2f,21f,14f,13.8f,14.5f,138f,122f));
        female.put(new AgeGroup(55, 59), FitnessStandardSet.createSet(43.2f,36.4f,20f,16f,19.5f,16.2f,18f,11f,14.5f,15.2f,129f,113f));
        female.put(new AgeGroup(60, 64), FitnessStandardSet.createSet(41.9f,35.2f,17f,13f,19.6f,16.3f,15f,9f,15.3f,16f,120f,106f));

        table.put(Sex.F, female);
    }

    /**
     * 성별+나이 넣으면 해당 연령대 기준표를 반환함.
     * @param sex M/F
     * @param age 나이
     * @return 매칭되는 기준표
     * */
    public FitnessStandardSet getStandard(Sex sex, int age) {
        // 성별로 먼저 분류
        Map<AgeGroup, FitnessStandardSet> map = table.get(sex);

        if (map == null)
            throw new IllegalArgumentException("해당 성별 기준표 없음");

        return map.entrySet().stream()
                // 연령대로 분류한 뒤 기준표 가져오기
                .filter(e -> age >= e.getKey().minAge() && age <= e.getKey().maxAge())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 나이 기준표 없음"))
                .getValue();
    }
}
