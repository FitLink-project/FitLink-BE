# 이메일 수정 API 명세서

## 이메일 수정

### 기본 정보
- **엔드포인트**: `PATCH /api/user/email`
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
| 파라미터명 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `email` | String | ✅ 필수 | 새로운 이메일 주소 (이메일 형식, 중복 불가) |

### 요청 예시

#### Postman 설정
1. **Method**: `PATCH` 선택
2. **URL**: `http://localhost:8080/api/user/email`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}`
   - `Content-Type`: `application/json` (raw JSON 선택 시 자동 설정)
4. **Body 탭** 선택
5. **raw** 선택 → **JSON** 선택
6. **요청 본문**:
   ```json
   {
     "email": "newemail@example.com"
   }
   ```
7. **Send** 클릭

#### cURL 예시
```bash
curl -X PATCH http://localhost:8080/api/user/email \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com"
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
    "userId": 1,
    "createdAt": "2023-09-01T13:26:22.123Z"
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
| `result.userId` | Long | 사용자 ID |
| `result.createdAt` | String | 회원가입 일시 (ISO 8601 형식) |

---

### 에러 응답

#### 400 Bad Request - 잘못된 요청
```json
{
  "isSuccess": false,
  "code": "COMMON400",
  "message": "잘못된 요청입니다.",
  "result": null
}
```

**발생 상황**:
- 필수 필드 누락 (`email`)
- 이메일 형식 오류

#### 401 Unauthorized - 인증 실패
```json
{
  "isSuccess": false,
  "code": "COMMON401",
  "message": "인증이 필요합니다.",
  "result": null
}
```

#### 409 Conflict - 중복된 이메일
```json
{
  "isSuccess": false,
  "code": "USER4031",
  "message": "중복된 이메일입니다.",
  "result": null
}
```

**발생 상황**:
- 이미 사용 중인 이메일 주소로 변경 시도

---

## Postman 테스트 가이드

### 1. 기본 이메일 수정

1. **새로운 Request 생성**
2. **Method**: `PATCH` 선택
3. **URL**: `http://localhost:8080/api/user/email`
4. **Authorization 탭** → `Bearer Token` 선택 → 토큰 입력
5. **Body 탭** 선택
6. **raw** 선택 → **JSON** 선택
7. **요청 본문 입력**:
   ```json
   {
     "email": "newemail@example.com"
   }
   ```
8. **Send** 클릭

### 2. 에러 케이스 테스트

#### 중복된 이메일 테스트
- 이미 사용 중인 이메일로 변경 시도 → `409 Conflict`

#### 이메일 형식 오류 테스트
- `email`: `invalid-email` → `400 Bad Request`

#### 필수 필드 누락 테스트
- `email` 필드 누락 → `400 Bad Request`

---

## 유효성 검증 규칙

| 필드 | 규칙 | 에러 코드 |
|------|------|----------|
| `email` | 필수, 이메일 형식, 중복 불가 | USER4001 / USER4031 |

---

## 주의사항

1. **이메일 중복 체크**: 새로운 이메일이 이미 사용 중인지 확인합니다.
2. **소셜 로그인 사용자**: 카카오 로그인 사용자가 임시 이메일을 사용 중인 경우, 이메일 수정 시 기존 사용자와 계정이 연결될 수 있습니다.
3. **동일한 이메일**: 현재 이메일과 동일한 이메일로 변경하면 변경 없이 성공 응답을 반환합니다.

