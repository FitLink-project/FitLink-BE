# ARFF 클래스 한국어 → 영어 변환 매핑 (최종 완전 버전)

본 문서는 Weka에서 한국어 인코딩 문제를 해결하기 위해 ARFF 파일의 모든 한국어 클래스 이름을 영어로 변환한 매핑 테이블입니다.

## 변환 개요

- **원본 파일**: `model_test_weka_simplified.arff`
- **변환 파일**: `model_test_weka_simplified_english.arff`
- **변환 이유**: Weka에서 한국어가 깨져서 표시되는 문제 해결
- **변환 방법**: @attribute 섹션에서 추출한 정확한 클래스 목록 기반 변환

---

## Prep_Exercise (준비운동) 클래스 변환

| 한국어 | English |
|--------|---------|
| ? | `?` |
| 걷기 | `Walking` |
| 기타 | `Other` |
| 동적 스트레칭 루틴프로그램 | `Dynamic_Stretching_Routine` |
| 동적 스트레칭 루틴프로그램,요가 및 필라테스  루틴프로그램 | `Dynamic_Stretching_Routine,Yoga_Pilates_Routine` |
| 목 스트레칭,등/어깨 뒤쪽 스트레칭,가슴/어깨 앞쪽 스트레칭,아래 팔 스트레칭 | `Neck_Stretching,Back_Shoulder_Stretching,Chest_Shoulder_Stretching,Lower_Arm_Stretching` |
| 발목 돌리기 | `Ankle_Circle` |
| 상지 루틴 스트레칭,하지 루틴 스트레칭1 | `Upper_Body_Stretching,Lower_Body_Stretching_1` |
| 엎드려 한발 원 그리기 | `Prone_Leg_Circle` |
| 엎드려 한발 원 그리기,발목 돌리기 | `Prone_Leg_Circle,Ankle_Circle` |
| 엎드려 한발 원 그리기,발목 돌리기,발 끝 당기기 | `Prone_Leg_Circle,Ankle_Circle,Toe_Pull` |
| 요가 및 필라테스  루틴프로그램,정적 스트레칭  루틴프로그램 | `Yoga_Pilates_Routine,Static_Stretching_Routine` |
| 유산소 운동 전 동적 루틴 스트레칭 | `Pre_Cardio_Dynamic_Stretching` |
| 자가근막이완술 루틴 스트레칭 | `Foam_Rolling_Routine_Stretching` |
| 전신 루틴 스트레칭 | `Full_Body_Routine_Stretching` |
| 전신 루틴 스트레칭,유산소 운동 전 동적 루틴 스트레칭 | `Full_Body_Routine_Stretching,Pre_Cardio_Dynamic_Stretching` |
| 전신 루틴 스트레칭,유산소 운동 전 동적 루틴 스트레칭,자가근막이완술 루틴 스트레칭 | `Full_Body_Routine_Stretching,Pre_Cardio_Dynamic_Stretching,Foam_Rolling_Routine_Stretching` |
| 정적 스트레칭  루틴프로그램 | `Static_Stretching_Routine` |
| 진자운동 | `Pendulum_Movement` |
| 팔굽혀 펴기,자전거타기,몸통 비틀기,팔 벌려 뛰기 | `Pushup,Cycling,Torso_Twist,Jumping_Jack` |
| 팔굽혀펴기 | `Pushup` |
| 하지 루틴 스트레칭1,상지 루틴 스트레칭 | `Lower_Body_Stretching_1,Upper_Body_Stretching` |

---

## Main_Exercise (본운동) 클래스 변환

| 한국어 | English |
|--------|---------|
| ? | `?` |
| 걷기,조깅,자전거타기,수영 | `Walking,Jogging,Cycling,Swimming` |
| 걷기,조깅,자전거타기,줄넘기 운동 | `Walking,Jogging,Cycling,Jump_Rope_Exercise` |
| 걷기,조깅,자전거타기,줄넘기 운동,수영 | `Walking,Jogging,Cycling,Jump_Rope_Exercise,Swimming` |
| 계단 뛰어 오르기,엎드려 버티기,윗몸올리기 ,앉았다 일어서기,누워서 엉덩이 들어올리기,팔굽혀펴기,턱걸이,앉았다 일어서기,한발 앞으로 내밀고 앉았다 일어서기,서서 상체 일으키기,앉아서 다리 밀기,손목 펴기/굽히기,뒤꿈치 들기 | `Stairs_Jump_Up,Plank,Situp,Sit_Stand,Hip_Lift,Pushup,Chin_Up,Sit_Stand,Lunge,Standing_Situp,Seated_Leg_Press,Wrist_Flex_Ext,Heel_Raise` |
| 계단 올라갔다 내려오기,전신 루틴 스트레칭,맨몸운동  루틴프로그램,바운딩 운동  루틴프로그램 | `Stairs_Up_Down,Full_Body_Routine_Stretching,Bodyweight_Exercise_Routine,Bounding_Exercise_Routine` |
| 기타 | `Other` |
| 달리기,실내 자전거타기,줄넘기,수영,맨몸운동  루틴프로그램,웨이트 트레이닝  루틴프로그램,저항밴드 운동  루틴프로그램,계단 두 칸씩 뛰기 | `Running,Indoor_Cycling,Jump_Rope,Swimming,Bodyweight_Exercise_Routine,Weight_Training_Routine,Resistance_Band_Routine,Stairs_Two_Steps` |
| 달리기,줄넘기,수영,앉았다 일어서기,팔굽혀 펴기,윗몸올리기 ,엎드려 버티기,누워서 엉덩이 들어올리기,바운딩 운동  루틴프로그램 | `Running,Jump_Rope,Swimming,Sit_Stand,Pushup,Situp,Plank,Hip_Lift,Bounding_Exercise_Routine` |
| 맨몸운동  루틴프로그램 | `Bodyweight_Exercise_Routine` |
| 맨몸운동  루틴프로그램,버피 테스트,줄넘기 운동,순간반응 콘 찍기 | `Bodyweight_Exercise_Routine,Burpee_Test,Jump_Rope_Exercise,Reaction_Cone_Tap` |
| 맨몸운동  루틴프로그램,웨이트 트레이닝  루틴프로그램 | `Bodyweight_Exercise_Routine,Weight_Training_Routine` |
| 맨몸운동  루틴프로그램,저항밴드 운동  루틴프로그램,전완대고 버티기,앉았다 일어서기,앞굽이 앉았다 일어서기 | `Bodyweight_Exercise_Routine,Resistance_Band_Routine,Forearm_Plank,Sit_Stand,Squat` |
| 맨몸운동  루틴프로그램,전완대고 버티기,버피 테스트 | `Bodyweight_Exercise_Routine,Forearm_Plank,Burpee_Test` |
| 맨몸운동  루틴프로그램,조깅,줄넘기 운동 | `Bodyweight_Exercise_Routine,Jogging,Jump_Rope_Exercise` |
| 발 바꿔 뛰기,엎드려 팔다리 교차올리기,윗몸 일으키기,전완대고 버티기,팔다리 교차 버티기,누워 다리 들어올리기,몸통 옆으로 굽히기,박스 운동  루틴프로그램,사다리 운동  루틴프로그램,조깅,자전거타기,줄넘기 운동,수영,정적 스트레칭  루틴프로그램 | `Hop,Cross_Body_Lift,Situp,Forearm_Plank,Cross_Plank,Lying_Leg_Raise,Side_Bend,Box_Exercise_Routine,Ladder_Exercise_Routine,Jogging,Cycling,Jump_Rope_Exercise,Swimming,Static_Stretching_Routine` |
| 앉아서 다리 밀기,앉아서 당겨 내리기,누워서 밀기,한발 앞으로 내밀고 앉았다 일어서기,엎드려 버티기 | `Seated_Leg_Press,Seated_Pull_Down,Lying_Press,Lunge,Plank` |
| 앉았다 일어서기,한발 앞으로 내밀고 앉았다 일어서기,앉아서 위로 밀기,물병 옆으로 들어올리기,팔굽혀펴기,윗몸올리기 ,누워서 엉덩이 들어올리기,엎드려 버티기 | `Sit_Stand,Lunge,Seated_Overhead_Press,Bottle_Side_Raise,Pushup,Situp,Hip_Lift,Plank` |
| 웨이트 트레이닝  루틴프로그램,저항밴드 운동  루틴프로그램 | `Weight_Training_Routine,Resistance_Band_Routine` |
| 윗몸올리기 ,누워서 엉덩이 들어올리기,엎드려 버티기,서서 상체 일으키기,앉아서 밀기,앉아서 당겨 내리기,앉아서 다리 밀기,앉아서 다리 펴기,실내 자전거타기,트레드밀에서 걷기 | `Situp,Hip_Lift,Plank,Standing_Situp,Seated_Press,Seated_Pull_Down,Seated_Leg_Press,Seated_Leg_Extension,Indoor_Cycling,Treadmill_Walking` |
| 조깅,자전거타기,줄넘기 운동,수영 | `Jogging,Cycling,Jump_Rope_Exercise,Swimming` |
| 조깅,줄넘기 운동 | `Jogging,Jump_Rope_Exercise` |
| 조깅,줄넘기 운동,상체 감아올리기,런지하며 상체 비틀기 | `Jogging,Jump_Rope_Exercise,Curl_Up,Lunge_Twist` |
| 조깅,줄넘기 운동,수영 | `Jogging,Jump_Rope_Exercise,Swimming` |
| 줄넘기 운동 | `Jump_Rope_Exercise` |
| 줄넘기 운동,저항밴드 운동  루틴프로그램 | `Jump_Rope_Exercise,Resistance_Band_Routine` |
| 팔굽혀 펴기,누워 다리 들어올리기,맨몸운동  루틴프로그램,윗몸 일으키기,앉았다 일어서기,앞굽이 앉았다 일어서기,발 바꿔 뛰기,조깅,자전거타기,줄넘기 운동,수영 | `Pushup,Lying_Leg_Raise,Bodyweight_Exercise_Routine,Situp,Sit_Stand,Squat,Hop,Jogging,Cycling,Jump_Rope_Exercise,Swimming` |
| 팔굽혀 펴기,뒤로 팔굽혀펴기,다리 뻗어 올리기,앉았다 일어서기 | `Pushup,Reverse_Pushup,Leg_Extension,Sit_Stand` |
| 팔굽혀 펴기,버피 테스트,걷기,조깅,자전거타기 | `Pushup,Burpee_Test,Walking,Jogging,Cycling` |
| 팔굽혀펴기,슈퍼맨자세,윗몸말아올리기,앉았다 일어서기 | `Pushup,Superman_Pose,Crunch,Sit_Stand` |
| 팔로 써클링 누르기,옆으로 누워 다리 들어올리기,엎드려 다리 들어올리기,1단 줄넘기,박스 옆으로 번갈아 뛰기,한 발 무릎위 올려차기,잔발치기,점프해서 발바닥 치기,2단 줄넘기,십(十)자 달리기,발바닥 마주대고 끌어당기기,다리 굽혀(W자) 상체 뒤로 눕기,벽패스,십(十)자 드리블 | `Arm_Circle_Press,Side_Lying_Leg_Raise,Prone_Leg_Raise,Single_Jump_Rope,Box_Side_Hop,Knee_High_Kick,Quick_Feet,Jump_Foot_Tap,Double_Jump_Rope,Cross_Running,Foot_Pull,W_Sit_Back_Lie,Wall_Pass,Cross_Dribble` |
| 한발 앞으로 내밀고 앉았다 일어서기,바벨 끌어당기기 | `Lunge,Barbell_Pull` |

---

## CoolDown_Exercise (마무리운동) 클래스 변환

| 한국어 | English |
|--------|---------|
| ? | `?` |
| 걷기 | `Walking` |
| 기타 | `Other` |
| 동적 스트레칭 루틴프로그램 | `Dynamic_Stretching_Routine` |
| 엉덩이 스트레칭,넙다리 뒤쪽 스트레칭,넙다리 앞쪽 스트레칭,넙다리 안쪽 스트레칭 | `Hip_Stretching,Thigh_Back_Stretching,Thigh_Front_Stretching,Thigh_Inner_Stretching` |
| 요가 및 필라테스  루틴프로그램 | `Yoga_Pilates_Routine` |
| 요가 및 필라테스  루틴프로그램,정적 스트레칭  루틴프로그램 | `Yoga_Pilates_Routine,Static_Stretching_Routine` |
| 자가근막이완술 루틴 스트레칭 | `Foam_Rolling_Routine_Stretching` |
| 전신 루틴 스트레칭 | `Full_Body_Routine_Stretching` |
| 전신 루틴 스트레칭,자가근막이완술 루틴 스트레칭 | `Full_Body_Routine_Stretching,Foam_Rolling_Routine_Stretching` |
| 정적 스트레칭  루틴프로그램 | `Static_Stretching_Routine` |
| 정적 스트레칭  루틴프로그램,짝 스트레칭  루틴프로그램 | `Static_Stretching_Routine,Partner_Stretching_Routine` |
| 좌식생활자를 위한 동적 루틴 스트레칭 | `Sedentary_Dynamic_Stretching` |
| 하지 루틴 스트레칭1,상지 루틴 스트레칭 | `Lower_Body_Stretching_1,Upper_Body_Stretching` |
| 하지 루틴 스트레칭1,하지 루틴 스트레칭2 | `Lower_Body_Stretching_1,Lower_Body_Stretching_2` |
| 하지 루틴 스트레칭1,하지 루틴 스트레칭2,상지 루틴 스트레칭 | `Lower_Body_Stretching_1,Lower_Body_Stretching_2,Upper_Body_Stretching` |
| 하지 루틴 스트레칭1,하지 루틴 스트레칭2,상지 루틴 스트레칭,전신 루틴 스트레칭 | `Lower_Body_Stretching_1,Lower_Body_Stretching_2,Upper_Body_Stretching,Full_Body_Routine_Stretching` |
| 하지 루틴 스트레칭2 | `Lower_Body_Stretching_2` |
| 하지 루틴 스트레칭2,상지 루틴 스트레칭 | `Lower_Body_Stretching_2,Upper_Body_Stretching` |
| 하지 루틴 스트레칭2,상지 루틴 스트레칭,전신 루틴 스트레칭 | `Lower_Body_Stretching_2,Upper_Body_Stretching,Full_Body_Routine_Stretching` |
| 하지 루틴 스트레칭2,전신 루틴 스트레칭 | `Lower_Body_Stretching_2,Full_Body_Routine_Stretching` |
| 허리 스트레칭,배스트레칭,넙다리 뒤쪽 스트레칭,넙다리 안쪽 스트레칭 | `Waist_Stretching,Stomach_Stretching,Thigh_Back_Stretching,Thigh_Inner_Stretching` |

---

## 사용 방법

1. Weka에서 `model_test_weka_simplified_english.arff` 파일 열기
2. 한국어 대신 영어 클래스 이름이 표시됨
3. 학습 및 예측 결과도 영어로 표시됨

## 주의사항

- 원본 데이터의 의미는 동일하게 유지됨
- 클래스 이름만 영어로 변환되었을 뿐, 데이터 값은 변경되지 않음
- 변환 후에도 모델 성능은 동일함
- 모든 한국어 클래스가 영어로 변환되었으므로 인코딩 문제가 해결됨

---

*변환 일자: 2025.11.24*

