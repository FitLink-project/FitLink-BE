# 운동 처방 예측 모델 학습 및 서비스 적용 가이드

## 📋 프로젝트 개요

본 프로젝트는 사용자의 신체 정보(나이, 성별, 키, 몸무게)를 기반으로 맞춤형 운동 처방을 예측하는 머신러닝 모델을 개발하고 서비스에 적용하는 과정을 다룹니다.

## 📁 파일 구조

```
fitlink-md/
├── README.md                         # 본 문서
├── model_test_analysis.ipynb         # 데이터 전처리 및 분석 노트북
└── models                            # 실제 학습한 모델 
```

---

## 🎓 모델 학습 방법

### 0. 데이터 전처리
📚 참고 자료
- [데이터 분석 노트북](./model_test_analysis.ipynb)
- [사용한 데이터](https://www.bigdata-culture.kr/bigdata/user/data_market/detail.do?id=599b29a1-bb8d-41a5-8de5-400d2c8d2ba5)


### 1. ARFF 형식 변환

Weka에서 학습하기 위해 Python 파일을 이용하여 ARFF 형식으로 변환합니다.

**변환 이유:**
- Weka는 ARFF 형식의 파일을 표준으로 사용
- 한국어 인코딩 문제를 해결하기 위해 영어로 변환된 ARFF 파일 사용 권장

### 2. Weka에서 모델 학습

**시도한 알고리즘:**
- RandomForest
- LMT (Logistic Model Tree)
- J48 (의사결정나무)
- DecisionStump
- HoeffdingTree
- RandomTree
- REPTree

**최종 선택: LMT (Logistic Model Tree)**

각 타깃 변수별 학습 결과:

- **Prep Exercise**: **70.2605%** (10-fold 교차 검증)
- **Main Exercise**: **91.3851%** (10-fold 교차 검증)
- **CoolDown Exercise**: **63.6028%** (10-fold 교차 검증)

모든 타깃 변수에서 LMT가 다른 알고리즘들보다 가장 높은 성능을 보였습니다.

**학습 설정:**
- Test options: Cross-validation
- Folds: 10
- 각 타깃 변수(Prep_Exercise, Main_Exercise, CoolDown_Exercise)별로 별도의 모델 학습

### 3. 모델 저장

학습 완료 후 `.model` 형식으로 저장하여 Spring Boot 서버에 적용할 예정입니다.

### 4. 한국어 변환 처리

**문제점:**
- Weka가 버전에 따라 한국어를 인식하지 못하여 모델 예측 결과가 영어로 출력됨

**해결 방법:**
- 매핑 테이블(`korean_to_english_mapping.md`)을 이용하여 Spring 서버에서 영어 예측 결과를 한국어로 변환
- Weka 모델이 영어 클래스 이름으로 예측한 결과를 받아서, 매핑 테이블을 참조하여 한국어로 변환 후 클라이언트에 반환

**참고:**
- [한국어-영어 매핑 테이블](./korean_to_english_mapping.md)

---

## 📈 실험 결과 및 분석

### 성능 평가

각 타깃 변수별 Cross-validation (10-fold) 정확도:

| 타깃 변수 | 정확도 | 평가 |
|---------|--------|------|
| **Prep Exercise** | **70.2605%** | 보통 |
| **Main Exercise** | **91.3851%** | **매우 우수** |
| **CoolDown Exercise** | **63.6028%** | 보통 |

### 결과 해석

#### Prep Exercise (준비운동)
LMT가 10-fold 교차 검증에서 **70.2605%** 정확도를 보여, 베이스라인보다는 분명 향상된 성능이지만 목표로 했던 수준(예: 80% 이상)에는 다소 못 미치는 결과였습니다. 다만 입력 특성이 4개뿐이고 클래스 수가 많은 멀티클래스 문제라는 점을 고려하면, 추가 피처 확장·하이퍼파라미터 튜닝을 통해 개선 여지가 있는 합리적인 출발점으로 볼 수 있습니다.

#### Main Exercise (본운동)
LMT가 10-fold 교차 검증에서 **91.3851%**의 높은 정확도를 기록해, 세 목표 변수 중 가장 안정적이고 신뢰도 높은 예측 성능을 보였습니다. 특히 클래스 수를 크게 축소한 상태에서도 90%를 넘는 정확도를 달성했다는 점에서, **실서비스에서 메인 운동 추천의 핵심 모델로 활용하기에 충분히 유의미한 결과**입니다.

#### CoolDown Exercise (마무리운동)
LMT가 10-fold 교차 검증에서 **63.6028%**로 가장 성능이 좋았습니다. 80% 이하의 결과이기 때문에 실험 결과 자체로만 보면 매우 유의미하다고 볼 수 없으나, 실제 Spring에서 적용할 API에서 Top-3까지 보여줄 것이기 때문에 완전히 무의미한 결과를 도출한다고 볼 수는 없습니다. 클래스 수(22~32개)와 현재 Top-1 정확도(약 63.6%)를 감안하면 Top-3 정확도는 Top-1보다 꽤 크게 올라갈 가능성이 높고, 따라서 실서비스에서 **"Top-3 추천" 방식은 충분히 실용성이 있을 수 있습니다**.



## 📚 참고 자료

- [데이터 분석 노트북](./model_test_analysis.ipynb)
- [사용한 데이터](https://www.bigdata-culture.kr/bigdata/user/data_market/detail.do?id=599b29a1-bb8d-41a5-8de5-400d2c8d2ba5)
- [전처리한 데이터](https://drive.google.com/drive/folders/11AZHyVAa7nRHbZuFUGooYsEgrQP_bW7K)

---

*작성일: 2025.11.24*  
*데이터: model_test.csv (10,000개 인스턴스)*

