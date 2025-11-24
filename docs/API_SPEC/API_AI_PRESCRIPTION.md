# AI 운동 처방 API 명세서

## AI 기반 운동 처방 생성

### 기본 정보
- **엔드포인트**: `POST /api/ai/prescription`
- **Content-Type**: `application/json`
- **인증**: ✅ 필요 (Bearer Token)

---

## 요청 (Request)

### 요청 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `Authorization` | String | ✅ 필수 | Bearer Token (로그인 시 받은 accessToken) |
| `Content-Type` | String | ✅ 필수 | `application/json` |

### 요청 파라미터

#### Request Body (JSON)

| 파라미터명 | 타입 | 필수 | 설명 | 예시 |
|----------|------|------|------|------|
| `age` | Integer | ✅ 필수 | 나이 (양수) | `25` |
| `gender` | Integer | ✅ 필수 | 성별 (0: 여자, 1: 남자) | `1` |
| `height` | Integer | ✅ 필수 | 키 (cm, 양수) | `175` |
| `weight` | Integer | ✅ 필수 | 몸무게 (kg, 양수) | `70` |

### 입력값 제약 조건

| 필드 | 제약 조건 | 에러 메시지 |
|------|----------|------------|
| `age` | 양수 (1 이상) | "Age must be a positive number" |
| `gender` | 0 또는 1 | "Gender must be 0 (female) or 1 (male)" |
| `height` | 양수 (1 이상) | "Height must be a positive number" |
| `weight` | 양수 (1 이상) | "Weight must be a positive number" |

### 요청 예시

#### Postman 설정
1. **Method**: `POST` 선택
2. **URL**: `http://localhost:8080/api/ai/prescription`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}` (로그인 시 받은 토큰)
   - `Content-Type`: `application/json` (자동 설정됨)
4. **Body 탭** 선택
5. **raw** 선택 → **JSON** 선택
6. **요청 본문 입력**:
   ```json
   {
     "age": 25,
     "gender": 1,
     "height": 175,
     "weight": 70
   }
   ```
7. **Send** 클릭

#### cURL 예시
```bash
curl -X POST http://localhost:8080/api/ai/prescription \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "age": 25,
    "gender": 1,
    "height": 175,
    "weight": 70
  }'
```

---

## 응답 (Response)

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "warmup": [
      "전신 루틴 스트레칭",
      "유산소 운동 전 동적 루틴 스트레칭"
    ],
    "mainExercise": [
      "조깅",
      "줄넘기 운동",
      "수영"
    ],
    "cooldown": [
      "정적 스트레칭 루틴프로그램"
    ]
  }
}
```

#### 응답 필드

| 필드명 | 타입 | 설명 |
|--------|------|------|
| `isSuccess` | Boolean | 요청 성공 여부 |
| `code` | String | 응답 코드 |
| `message` | String | 응답 메시지 |
| `result` | Object | 결과 데이터 |
| `result.warmup` | List<String> | 준비운동 목록 (한국어) |
| `result.mainExercise` | List<String> | 본운동 목록 (한국어) |
| `result.cooldown` | List<String> | 마무리운동 목록 (한국어) |

#### 응답 예시 (빈 리스트 포함)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "warmup": [],
    "mainExercise": [
      "맨몸운동 루틴프로그램"
    ],
    "cooldown": []
  }
}
```

---

## 에러 응답

### 400 Bad Request - 잘못된 요청

#### 필수 필드 누락
```json
{
  "isSuccess": false,
  "code": "COMMON400",
  "message": "잘못된 요청입니다.",
  "result": null
}
```

**발생 상황**:
- `age`, `gender`, `height`, `weight` 중 하나라도 누락
- JSON 형식 오류

#### 유효성 검증 실패
```json
{
  "isSuccess": false,
  "code": "COMMON400",
  "message": "잘못된 요청입니다.",
  "result": {
    "age": "Age must be a positive number"
  }
}
```

**발생 상황**:
- `age`가 0 이하
- `gender`가 0 또는 1이 아님
- `height`가 0 이하
- `weight`가 0 이하

### 500 Internal Server Error - 서버 에러

```json
{
  "isSuccess": false,
  "code": "COMMON500",
  "message": "서버 에러, 관리자에게 문의 바랍니다.",
  "result": null
}
```

**발생 상황**:
- Weka 모델 로드 실패
- 예측 과정에서 오류 발생
- 한국어 변환 과정에서 오류 발생

---

## Postman 테스트 가이드

### 1. 기본 운동 처방 요청

1. **새로운 Request 생성**
2. **Method**: `POST` 선택
3. **URL**: `http://localhost:8080/api/ai/prescription`
4. **Headers 탭** 선택 → 다음 헤더 추가:
   - Key: `Authorization`
   - Value: `Bearer {로그인 시 받은 accessToken}`
   - 예시: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ...`
5. **Body 탭** 선택
6. **raw** 선택
7. **JSON** 선택 (오른쪽 드롭다운)
8. **요청 본문 입력**:
   ```json
   {
     "age": 25,
     "gender": 1,
     "height": 175,
     "weight": 70
   }
   ```
9. **Send** 클릭

### 1-1. Authorization 헤더 자동 설정 (Postman 권장 방법)

1. **Authorization 탭** 선택
2. **Type**: `Bearer Token` 선택
3. **Token**: 로그인 API에서 받은 `accessToken` 값 입력
4. Postman이 자동으로 `Authorization: Bearer {token}` 헤더를 추가합니다
5. 나머지 설정은 위와 동일

### 2. 여성 사용자 예시

```json
{
  "age": 28,
  "gender": 0,
  "height": 165,
  "weight": 55
}
```

### 3. 다양한 연령대 테스트

#### 청소년 (15세)
```json
{
  "age": 15,
  "gender": 1,
  "height": 170,
  "weight": 60
}
```

#### 중년 (45세)
```json
{
  "age": 45,
  "gender": 1,
  "height": 175,
  "weight": 80
}
```

#### 고령 (65세)
```json
{
  "age": 65,
  "gender": 0,
  "height": 160,
  "weight": 58
}
```

### 4. 에러 케이스 테스트

#### 인증 실패 테스트
- Authorization 헤더를 제거하고 요청 → `401 Unauthorized`
- 잘못된 토큰 사용: `Authorization: Bearer invalid_token` → `401 Unauthorized`
- 만료된 토큰 사용 → `401 Unauthorized`

#### 필수 필드 누락
- `age` 필드 제거 → `400 Bad Request`
- `gender` 필드 제거 → `400 Bad Request`
- `height` 필드 제거 → `400 Bad Request`
- `weight` 필드 제거 → `400 Bad Request`

#### 잘못된 값 입력
```json
{
  "age": 0,
  "gender": 1,
  "height": 175,
  "weight": 70
}
```
→ `age`가 0 이하 → `400 Bad Request`

```json
{
  "age": 25,
  "gender": 2,
  "height": 175,
  "weight": 70
}
```
→ `gender`가 0 또는 1이 아님 → `400 Bad Request`

```json
{
  "age": 25,
  "gender": 1,
  "height": -175,
  "weight": 70
}
```
→ `height`가 음수 → `400 Bad Request`

```json
{
  "age": 25,
  "gender": 1,
  "height": 175,
  "weight": 0
}
```
→ `weight`가 0 이하 → `400 Bad Request`

#### JSON 형식 오류
```json
{
  "age": 25,
  "gender": 1,
  "height": 175,
  "weight": 70
```
→ 닫는 중괄호 누락 → `400 Bad Request`

---

## 응답 데이터 설명

### 운동 목록 형식

- 각 운동은 **한국어**로 반환됩니다.
- 운동 목록은 **배열(List)** 형식입니다.
- 여러 운동이 예측된 경우 쉼표로 구분되어 배열의 각 요소로 분리됩니다.
- 예측 결과가 없는 경우 **빈 배열(`[]`)**로 반환됩니다.

### 예측 모델 정보

- **준비운동 (warmup)**: Prep-LMT 모델 사용 (정확도: 약 70%)
- **본운동 (mainExercise)**: Main-LMT 모델 사용 (정확도: 약 91%)
- **마무리운동 (cooldown)**: CoolDown-LMT 모델 사용 (정확도: 약 64%)

### 운동 이름 변환

- Weka 모델은 영어로 예측하지만, API 응답은 **한국어**로 변환되어 반환됩니다.
- 변환은 `korean_to_english_mapping.md` 파일의 매핑 테이블을 사용합니다.
- 매핑되지 않은 운동 이름은 그대로 반환됩니다.

---

## 주의사항

1. **인증 토큰 필수**: 로그인 API에서 받은 `accessToken`을 Authorization 헤더에 포함해야 합니다.
2. **토큰 형식**: `Bearer {accessToken}` 형식으로 전송해야 합니다.
3. **응답 시간**: AI 모델 예측으로 인해 일반 API보다 응답 시간이 다소 걸릴 수 있습니다.
3. **예측 정확도**: 모델의 정확도는 각각 다르며, 특히 마무리운동의 정확도가 상대적으로 낮습니다.
4. **빈 배열**: 예측 결과가 없거나 매핑되지 않은 경우 빈 배열이 반환될 수 있습니다.
5. **서버 리소스**: 모델 로드는 서버 시작 시 한 번만 수행되며, 이후 요청은 메모리에서 처리됩니다.

---

## 예상 응답 예시

### 예시 1: 일반적인 운동 처방

**요청**:
```json
{
  "age": 30,
  "gender": 1,
  "height": 180,
  "weight": 75
}
```

**응답**:
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "warmup": [
      "전신 루틴 스트레칭",
      "유산소 운동 전 동적 루틴 스트레칭"
    ],
    "mainExercise": [
      "조깅",
      "줄넘기 운동",
      "수영"
    ],
    "cooldown": [
      "정적 스트레칭 루틴프로그램"
    ]
  }
}
```

### 예시 2: 복합 운동 처방

**요청**:
```json
{
  "age": 25,
  "gender": 0,
  "height": 165,
  "weight": 55
}
```

**응답**:
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "warmup": [
      "동적 스트레칭 루틴프로그램",
      "요가 및 필라테스 루틴프로그램"
    ],
    "mainExercise": [
      "걷기",
      "조깅",
      "자전거타기",
      "줄넘기 운동",
      "수영"
    ],
    "cooldown": [
      "하지 루틴 스트레칭1",
      "하지 루틴 스트레칭2",
      "상지 루틴 스트레칭",
      "전신 루틴 스트레칭"
    ]
  }
}
```

---

## 관련 문서

- [AI 모델 학습 가이드](../ai/README.md)
- [한국어-영어 매핑 테이블](../ai/korean_to_english_mapping.md)

