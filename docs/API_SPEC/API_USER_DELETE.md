# 회원탈퇴 API 명세서

## 회원탈퇴

### 기본 정보
- **엔드포인트**: `DELETE /api/user/delete`
- **Content-Type**: `application/json`
- **인증**: ✅ 필요 (Bearer Token)

---

## 요청 (Request)

### 요청 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `Authorization` | String | ✅ 필수 | Bearer Token (로그인 시 받은 accessToken) |

### 요청 파라미터

#### Request Body (JSON)
| 파라미터명 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `password` | String | ❌ 선택 | 비밀번호 (일반 로그인 사용자의 경우 필수, 소셜 로그인 사용자는 불필요) |

**참고**: 
- **일반 로그인 사용자** (`provider: GENERAL`): 비밀번호 확인 필요
- **소셜 로그인 사용자** (`provider: KAKAO`, `GOOGLE`): 비밀번호 필드 불필요 (비밀번호가 없음)

### 요청 예시

#### Postman 설정
1. **Method**: `DELETE` 선택
2. **URL**: `http://localhost:8080/api/user/delete`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}`
   - `Content-Type`: `application/json`
4. **Body 탭** 선택
5. **raw** 선택 → **JSON** 선택
6. **요청 본문** (일반 로그인 사용자의 경우):
   ```json
   {
     "password": "password123"
   }
   ```
   **소셜 로그인 사용자의 경우**:
   ```json
   {}
   ```
   또는 Body를 비워두고 요청
7. **Send** 클릭

#### cURL 예시

**일반 로그인 사용자**:
```bash
curl -X DELETE http://localhost:8080/api/user/delete \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "password": "password123"
  }'
```

**소셜 로그인 사용자**:
```bash
curl -X DELETE http://localhost:8080/api/user/delete \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{}'
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
    "message": "회원탈퇴가 완료되었습니다."
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
| `result.userId` | Long | 탈퇴된 사용자 ID |
| `result.message` | String | 탈퇴 완료 메시지 |

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
- 일반 로그인 사용자가 비밀번호를 제공하지 않은 경우

#### 401 Unauthorized - 인증 실패
```json
{
  "isSuccess": false,
  "code": "COMMON401",
  "message": "인증이 필요합니다.",
  "result": null
}
```

**발생 상황**:
- Authorization 헤더 누락
- 잘못된 토큰
- 만료된 토큰

#### 401 Unauthorized - 비밀번호 불일치
```json
{
  "isSuccess": false,
  "code": "USER4012",
  "message": "비밀번호가 올바르지 않습니다.",
  "result": null
}
```

**발생 상황**:
- 일반 로그인 사용자가 잘못된 비밀번호를 입력한 경우

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
- 토큰에 해당하는 사용자가 존재하지 않는 경우
- 이미 탈퇴한 사용자

#### 403 Forbidden - 이미 탈퇴한 사용자
```json
{
  "isSuccess": false,
  "code": "USER4033",
  "message": "이미 탈퇴한 사용자입니다.",
  "result": null
}
```

**발생 상황**:
- 이미 탈퇴 처리된 계정 (`isActive = false` 또는 `deleteDate`가 설정된 경우)

---

## Postman 테스트 가이드

### 1. 일반 로그인 사용자 탈퇴

1. **새로운 Request 생성**
2. **Method**: `DELETE` 선택
3. **URL**: `http://localhost:8080/api/user/delete`
4. **Authorization 탭** → `Bearer Token` 선택 → 토큰 입력
5. **Body 탭** → `raw` 선택 → `JSON` 선택
6. **요청 본문 입력**:
   ```json
   {
     "password": "password123"
   }
   ```
7. **Send** 클릭

### 2. 소셜 로그인 사용자 탈퇴

1. 위와 동일한 설정
2. **Body 탭** → `raw` 선택 → `JSON` 선택
3. **요청 본문 입력**:
   ```json
   {}
   ```
   또는 Body를 비워두기
4. **Send** 클릭

### 3. 에러 케이스 테스트

#### 토큰 없이 요청
- Authorization 헤더를 제거하고 요청 → `401 Unauthorized`

#### 잘못된 토큰
- `Authorization: Bearer invalid_token` → `401 Unauthorized`

#### 일반 로그인 사용자가 비밀번호 없이 요청
- Body를 비우고 요청 → `400 Bad Request`

#### 잘못된 비밀번호
- 일반 로그인 사용자가 잘못된 비밀번호 입력 → `401 Unauthorized`

---

## 유효성 검증 규칙

| 필드 | 규칙 | 에러 코드 |
|------|------|----------|
| `password` | 일반 로그인 사용자의 경우 필수, 소셜 로그인 사용자는 불필요 | COMMON400 / USER4012 |

---

## 주의사항

1. **인증 토큰**: 로그인 API에서 받은 `accessToken`을 사용합니다.
2. **비밀번호 확인**: 일반 로그인 사용자는 탈퇴 시 비밀번호 확인이 필요합니다. 소셜 로그인 사용자는 비밀번호가 없으므로 확인이 불필요합니다.
3. **탈퇴 처리**: 
   - 계정은 완전히 삭제되거나 (`isActive = false`, `deleteDate` 설정)
   - 관련 데이터(프로필 이미지, 약관 동의 정보 등)도 함께 처리됩니다.
4. **탈퇴 후 로그인**: 탈퇴 처리된 계정으로는 더 이상 로그인할 수 없습니다.
5. **소셜 계정 연동**: 소셜 로그인 사용자의 경우, 소셜 계정과의 연동 정보도 함께 처리됩니다.

---

## 보안 고려사항

- **HTTPS 사용 권장**: 프로덕션 환경에서는 반드시 HTTPS를 사용해야 합니다.
- **비밀번호 확인**: 일반 로그인 사용자의 경우 탈퇴 시 비밀번호 확인을 통해 무단 탈퇴를 방지합니다.
- **토큰 검증**: 탈퇴 요청 시 토큰의 유효성을 검증하여 본인만 탈퇴할 수 있도록 합니다.
- **데이터 보존 정책**: 법적 요구사항에 따라 일정 기간 데이터를 보존할 수 있습니다. 이 경우 완전 삭제 대신 `isActive = false` 및 `deleteDate` 설정으로 처리할 수 있습니다.

