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

**Request Body 없음** - 이 API는 요청 본문이 필요하지 않습니다.

### 요청 예시

#### Postman 설정
1. **Method**: `DELETE` 선택
2. **URL**: `http://localhost:8080/api/user/delete`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}`
4. **Body 탭**은 비워두거나 설정하지 않음
5. **Send** 클릭

#### cURL 예시

```bash
curl -X DELETE http://localhost:8080/api/user/delete \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
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
    "email": "test@example.com",
    "isActive": false,
    "regDate": "2023-09-01T13:26:22.123Z",
    "provider": "GENERAL",
    "deleteDate": "2024-01-15T10:30:00.000Z"
  }
}
```

#### 응답 필드
| 필드명 | 타입 | 설명 |
|--------|------|------|
| `isSuccess` | Boolean | 요청 성공 여부 |
| `code` | String | 응답 코드 |
| `message` | String | 응답 메시지 |
| `result` | Object | 탈퇴 처리된 사용자 정보 |
| `result.userId` | Long | 탈퇴된 사용자 ID |
| `result.email` | String | 탈퇴된 사용자 이메일 |
| `result.isActive` | Boolean | 활성화 상태 (false로 설정됨) |
| `result.regDate` | String | 회원가입 일시 (ISO 8601 형식) |
| `result.provider` | String | 로그인 제공자 (GENERAL, KAKAO, GOOGLE) |
| `result.deleteDate` | String | 탈퇴 일시 (ISO 8601 형식) |

---

### 에러 응답

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

### 1. 기본 탈퇴

1. **새로운 Request 생성**
2. **Method**: `DELETE` 선택
3. **URL**: `http://localhost:8080/api/user/delete`
4. **Authorization 탭** → `Bearer Token` 선택 → 토큰 입력
5. **Body 탭**은 설정하지 않음 (비워둠)
6. **Send** 클릭

### 2. 에러 케이스 테스트

#### 토큰 없이 요청
- Authorization 헤더를 제거하고 요청 → `401 Unauthorized`

#### 잘못된 토큰
- `Authorization: Bearer invalid_token` → `401 Unauthorized`

#### 이미 탈퇴한 사용자
- 이미 탈퇴 처리된 계정의 토큰으로 요청 → `403 Forbidden`

---

## 주의사항

1. **인증 토큰**: 로그인 API에서 받은 `accessToken`을 사용합니다.
2. **탈퇴 처리**: 
   - 계정은 소프트 삭제 처리됩니다 (`isActive = false`, `deleteDate` 설정)
   - 데이터는 일정 기간 보존되며, 스케줄러에 의해 한 달 후 자동으로 물리적 삭제됩니다.
3. **탈퇴 후 로그인**: 탈퇴 처리된 계정으로는 더 이상 로그인할 수 없습니다.
4. **복구 가능**: 스케줄러가 실행되기 전까지는 데이터가 보존되어 복구가 가능합니다.
5. **즉시 삭제 필요 시**: 즉시 완전 삭제가 필요한 경우 [수동 삭제 API](./API_USER_DELETE_HARD.md)를 사용하세요.

---

## 보안 고려사항

- **HTTPS 사용 권장**: 프로덕션 환경에서는 반드시 HTTPS를 사용해야 합니다.
- **토큰 검증**: 탈퇴 요청 시 토큰의 유효성을 검증하여 본인만 탈퇴할 수 있도록 합니다.
- **데이터 보존 정책**: 법적 요구사항에 따라 일정 기간 데이터를 보존합니다. 스케줄러에 의해 한 달 후 자동으로 물리적 삭제됩니다.

---

## 관련 API

- [회원 수동 삭제 API](./API_USER_DELETE_HARD.md) - 즉시 물리적 삭제
- [프로필 조회 API](./API_USER_PROFILE.md) - 사용자 정보 조회

