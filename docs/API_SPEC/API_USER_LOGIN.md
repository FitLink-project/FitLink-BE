# 로그인 API 명세서

## 로그인

### 기본 정보
- **엔드포인트**: `POST /api/user/login`
- **Content-Type**: `application/json`
- **인증**: 불필요

---

## 요청 (Request)

### 요청 파라미터

#### Request Body (JSON)
| 파라미터명 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `email` | String | ✅ 필수 | 이메일 주소 (이메일 형식) |
| `password` | String | ✅ 필수 | 비밀번호 |

### 요청 예시

#### Postman 설정
1. **Method**: `POST` 선택
2. **URL**: `http://localhost:8080/api/user/login` (서버 주소에 맞게 변경)
3. **Headers 탭** 선택 → 다음 헤더 확인/추가:
   - `Content-Type`: `application/json` (raw JSON 선택 시 자동 설정되지만, 없으면 수동 추가)
4. **Body 탭** 선택
5. **raw** 선택 → **JSON** 선택 (이렇게 하면 Content-Type이 자동으로 `application/json`으로 설정됩니다)
6. **요청 본문**:
   ```json
   {
     "email": "test@example.com",
     "password": "password123"
   }
   ```
7. **Send** 클릭

**주의**: 만약 `415 Unsupported Media Type` 에러가 발생하면:
- **Headers 탭**에서 `Content-Type` 헤더가 `application/json`으로 설정되어 있는지 확인
- 또는 Body 탭에서 **raw**와 **JSON**을 선택했는지 다시 확인

#### cURL 예시
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123!"
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
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
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
| `result.accessToken` | String | JWT 액세스 토큰 (인증에 사용) |

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
- 필수 필드 누락 (`email`, `password`)
- 이메일 형식 오류

#### 401 Unauthorized - 로그인 실패
```json
{
  "isSuccess": false,
  "code": "USER4011",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "result": null
}
```

**발생 상황**:
- 존재하지 않는 이메일
- 비밀번호 불일치

**참고**: 보안상 어떤 정보가 틀렸는지 구체적으로 알려주지 않습니다.

#### 403 Forbidden - 비활성화된 사용자
```json
{
  "isSuccess": false,
  "code": "USER4032",
  "message": "비활성화된 사용자입니다.",
  "result": null
}
```

**발생 상황**:
- 계정이 비활성화(`isActive = false`)된 상태의 사용자

#### 404 Not Found - 사용자 없음
```json
{
  "isSuccess": false,
  "code": "USER4041",
  "message": "사용자를 찾을 수 없습니다.",
  "result": null
}
```

**발생 상황**:
- 등록되지 않은 이메일 주소

#### 500 Internal Server Error - 서버 에러
```json
{
  "isSuccess": false,
  "code": "COMMON500",
  "message": "서버 에러, 관리자에게 문의 바랍니다.",
  "result": null
}
```

---

## Postman 테스트 가이드

### 1. 기본 로그인

1. **새로운 Request 생성**
2. **Method**: `POST` 선택
3. **URL**: `http://localhost:8080/api/user/login`
4. **Body 탭** 선택
5. **raw** 선택 → **JSON** 선택
6. **요청 본문 입력**:
   ```json
   {
     "email": "test@example.com",
     "password": "password123!"
   }
   ```
7. **Send** 클릭

### 2. 에러 케이스 테스트

#### 존재하지 않는 이메일 테스트
- `email`: `wrong@example.com` → `401 Unauthorized` 또는 `404 Not Found`

#### 잘못된 비밀번호 테스트
- `email`: `test@example.com` (등록된 이메일)
- `password`: `wrongpassword` → `401 Unauthorized`

#### 비활성화된 사용자 테스트
- 계정이 비활성화된 이메일로 로그인 시도 → `403 Forbidden`

#### 필수 필드 누락 테스트
- `email` 또는 `password` 중 하나라도 누락 시 → `400 Bad Request`

---

## 유효성 검증 규칙

| 필드 | 규칙 | 에러 코드 |
|------|------|----------|
| `email` | 필수, 이메일 형식 | COMMON400 |
| `password` | 필수, 공백 불가 | COMMON400 |

---

## 인증 토큰 사용 방법

로그인 성공 시 받은 `accessToken`을 이후 API 요청 시 사용합니다.

### Authorization Header에 포함
```
Authorization: Bearer {accessToken}
```

### 예시
```bash
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 주의사항

1. **액세스 토큰**은 클라이언트에 안전하게 저장해야 합니다.
2. **비밀번호**는 평문으로 전송하지 않도록 HTTPS를 사용하는 것을 권장합니다.
3. 로그인 실패 시 어떤 정보가 틀렸는지 구체적으로 알려주지 않습니다 (보안상 이유).
4. **비활성화된 사용자**는 별도의 에러 코드로 구분됩니다.
5. 토큰은 유효 기간이 있으며, 만료 시 재로그인이 필요합니다.

---

## 보안 고려사항

- **HTTPS 사용 권장**: 비밀번호가 평문으로 전송되므로 프로덕션 환경에서는 반드시 HTTPS를 사용해야 합니다.
- **토큰 저장**: 액세스 토큰은 안전한 곳에 저장해야 합니다 (예: HttpOnly Cookie, Secure Storage).
- **Rate Limiting**: 무차별 대입 공격을 방지하기 위해 로그인 시도 횟수 제한을 고려해야 합니다.

