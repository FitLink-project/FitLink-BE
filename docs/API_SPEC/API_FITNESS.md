# 체력 측정 API 명세서

## 개요

사용자의 체력을 측정하고 결과를 저장/조회하는 API입니다.
- **국민체력100**: 공식 체력 측정 프로그램 기반 (악력, 윗몸일으키기, 유연성 등)
- **간단체력**: 슬라이더 기반의 간편 체력 측정

---

## 1. 국민체력100 측정 결과 저장

### `POST /api/fitness/kookmin`

**인증**: ✅ Bearer Token 필요  
**Content-Type**: `application/json`

### Request Body

```json
{
  "sex": "M",
  "birthDate": "19900101",
  "height": 175.0,
  "weight": 70.5,
  "gripStrength": 45.5,
  "sitUp": 30,
  "crossSitUp": 25,
  "sitAndReach": 15.2,
  "shuttleRun": 50,
  "sprint": 12.5,
  "standingLongJump": 220.0
}
```

### 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `sex` | String | ✅ | 성별 - "M"(남성) 또는 "F"(여성) |
| `birthDate` | String | ✅ | 생년월일 - YYYYMMDD 형식 |
| `height` | Float | ✅ | 신장 (cm) |
| `weight` | Float | ✅ | 체중 (kg) |
| `gripStrength` | Float | ✅ | 악력 (kg) - 근력 측정 |
| `sitUp` | Integer | ✅ | 윗몸말아올리기 (회) - 근지구력 측정 |
| `crossSitUp` | Integer | ❌ | 교차윗몸일으키기 (회) |
| `sitAndReach` | Float | ✅ | 앉아 윗몸 앞으로 굽히기 (cm) - 유연성 측정 |
| `shuttleRun` | Integer | ✅ | 20m 왕복 오래달리기 (회) - 심폐지구력 측정 |
| `sprint` | Float | ✅ | 10m 왕복 달리기 (초) - 민첩성 측정 |
| `standingLongJump` | Float | ✅ | 제자리 멀리뛰기 (cm) - 순발력 측정 |

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "strength": 85.0,
    "muscular": 78.0,
    "flexibility": 72.0,
    "cardiopulmonary": 80.0,
    "agility": 88.0,
    "quickness": 82.0,
    "standard": {
      "grade1": {
        "gripStrength": 48.0,
        "sitUp": 35.0,
        "sitAndReach": 18.0,
        "shuttleRun": 60.0,
        "sprint": 11.0,
        "standingLongJump": 240.0
      },
      "grade2": {
        "gripStrength": 42.0,
        "sitUp": 28.0,
        "sitAndReach": 12.0,
        "shuttleRun": 45.0,
        "sprint": 13.5,
        "standingLongJump": 200.0
      }
    },
    "userInfo": {
      "sex": "M",
      "birthDate": "19900101",
      "height": 175.0,
      "weight": 70.5
    },
    "testKookmin": {
      "gripStrength": 45.5,
      "sitUp": 30,
      "sitAndReach": 15.2,
      "shuttleRun": 50,
      "sprint": 12.5,
      "standingLongJump": 220.0
    }
  }
}
```

### 응답 필드 설명

#### 체력 점수 (0-100)
| 필드 | 설명 |
|------|------|
| `strength` | 근력 점수 |
| `muscular` | 근지구력 점수 |
| `flexibility` | 유연성 점수 |
| `cardiopulmonary` | 심폐지구력 점수 |
| `agility` | 민첩성 점수 |
| `quickness` | 순발력 점수 |

#### standard (대한민국 평균 기준값)
- `grade1`: 1등급 기준값 (상위권)
- `grade2`: 2등급 기준값 (평균)

---

## 2. 국민체력100 측정 결과 수정

### `PATCH /api/fitness/kookmin`

**인증**: ✅ Bearer Token 필요  
**Content-Type**: `application/json`

> 기존에 저장된 최신 측정 결과를 업데이트합니다.

### Request Body

`POST /api/fitness/kookmin`과 동일

### 에러 응답

#### 측정 결과가 없는 경우
```json
{
  "isSuccess": false,
  "code": "NOT_FOUND",
  "message": "저장된 측정 결과가 없습니다.",
  "result": null
}
```

---

## 3. 간단 체력 측정 결과 저장

### `POST /api/fitness/general`

**인증**: ✅ Bearer Token 필요  
**Content-Type**: `application/json`

### Request Body

```json
{
  "sex": "M",
  "birthDate": "19900101",
  "height": 175.0,
  "weight": 70.5,
  "sliderStrength": 80,
  "sitUp": 25,
  "sitAndReach": 12.5,
  "ymcaStepTest": 42.0,
  "sliderAgility": 75,
  "sliderPower": 85
}
```

### 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `sex` | String | ✅ | 성별 - "M"(남성) 또는 "F"(여성) |
| `birthDate` | String | ✅ | 생년월일 - YYYYMMDD 형식 |
| `height` | Float | ✅ | 신장 (cm) |
| `weight` | Float | ✅ | 체중 (kg) |
| `sliderStrength` | Integer | ✅ | 근력 슬라이더 값 (0-100) |
| `sitUp` | Integer | ✅ | 윗몸일으키기 횟수 (회) |
| `sitAndReach` | Float | ✅ | 앉아 윗몸 앞으로 굽히기 (cm) |
| `ymcaStepTest` | Float | ✅ | YMCA 스텝 테스트 결과 |
| `sliderAgility` | Integer | ✅ | 민첩성 슬라이더 값 (0-100) |
| `sliderPower` | Integer | ✅ | 순발력 슬라이더 값 (0-100) |

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "strength": 80.0,
    "muscular": 70.0,
    "flexibility": 65.0,
    "cardiopulmonary": 75.0,
    "agility": 75.0,
    "quickness": 85.0,
    "standard": { ... },
    "userInfo": {
      "sex": "M",
      "birthDate": "19900101",
      "height": 175.0,
      "weight": 70.5
    },
    "testGeneral": {
      "sliderStrength": 80,
      "sitUp": 25,
      "sitAndReach": 12.5,
      "ymcaStepTest": 42.0,
      "sliderAgility": 75,
      "sliderPower": 85
    }
  }
}
```

---

## 4. 간단 체력 측정 결과 수정

### `PATCH /api/fitness/general`

**인증**: ✅ Bearer Token 필요  
**Content-Type**: `application/json`

> 기존에 저장된 최신 측정 결과를 업데이트합니다.

### Request Body

`POST /api/fitness/general`과 동일

---

## 5. 체력 측정 결과 조회

### `GET /api/fitness/result`

**인증**: ✅ Bearer Token 필요

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "strength": 85.0,
    "muscular": 78.0,
    "flexibility": 72.0,
    "cardiopulmonary": 80.0,
    "agility": 88.0,
    "quickness": 82.0,
    "standard": {
      "grade1": { ... },
      "grade2": { ... }
    },
    "userInfo": {
      "sex": "M",
      "birthDate": "19900101",
      "height": 175.0,
      "weight": 70.5
    },
    "testKookmin": { ... }
  }
}
```

### 에러 응답

#### 조회 가능한 결과가 없는 경우
```json
{
  "isSuccess": false,
  "code": "NOT_FOUND",
  "message": "조회 가능한 결과가 없습니다.",
  "result": null
}
```

---

## 체력 요소 점수 계산 기준

체력 점수는 사용자의 나이, 성별을 기반으로 대한민국 평균 데이터와 비교하여 산출됩니다.

| 체력 요소 | 측정 항목 (국민체력100) | 측정 항목 (간단체력) |
|-----------|------------------------|---------------------|
| 근력 | 악력 (kg) | 슬라이더 값 |
| 근지구력 | 윗몸말아올리기 (회) | 윗몸일으키기 (회) |
| 유연성 | 앉아 윗몸 앞으로 굽히기 (cm) | 앉아 윗몸 앞으로 굽히기 (cm) |
| 심폐지구력 | 20m 왕복 오래달리기 (회) | YMCA 스텝 테스트 |
| 민첩성 | 10m 왕복 달리기 (초) | 슬라이더 값 |
| 순발력 | 제자리 멀리뛰기 (cm) | 슬라이더 값 |

---

## cURL 예시

### 국민체력100 저장
```bash
curl -X POST http://localhost:8080/api/fitness/kookmin \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "sex": "M",
    "birthDate": "19900101",
    "height": 175.0,
    "weight": 70.5,
    "gripStrength": 45.5,
    "sitUp": 30,
    "sitAndReach": 15.2,
    "shuttleRun": 50,
    "sprint": 12.5,
    "standingLongJump": 220.0
  }'
```

### 결과 조회
```bash
curl -X GET http://localhost:8080/api/fitness/result \
  -H "Authorization: Bearer {access_token}"
```

