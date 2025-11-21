# 회원 수동 삭제 API 명세서

## 회원 수동 삭제 (즉시 물리적 삭제)

### 기본 정보
- **엔드포인트**: `DELETE /api/user/delete/hard`
- **Content-Type**: `application/json`
- **인증**: ✅ 필요 (Bearer Token)

**⚠️ 주의**: 이 API는 **즉시 물리적 삭제**를 수행합니다. 삭제된 데이터는 **복구할 수 없습니다**.

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
2. **URL**: `http://localhost:8080/api/user/delete/hard`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}`
4. **Body 탭**은 비워두거나 설정하지 않음
5. **Send** 클릭

#### cURL 예시

```bash
curl -X DELETE http://localhost:8080/api/user/delete/hard \
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
    "isActive": true,
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
| `result` | Object | 삭제된 사용자 정보 (삭제 전 정보) |
| `result.userId` | Long | 삭제된 사용자 ID |
| `result.email` | String | 삭제된 사용자 이메일 |
| `result.isActive` | Boolean | 삭제 전 활성화 상태 |
| `result.regDate` | String | 회원가입 일시 (ISO 8601 형식) |
| `result.provider` | String | 로그인 제공자 (GENERAL, KAKAO, GOOGLE) |
| `result.deleteDate` | String | 삭제 일시 (ISO 8601 형식) |

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
- 이미 삭제된 사용자

---

## Postman 테스트 가이드

### 1. 기본 수동 삭제

1. **새로운 Request 생성**
2. **Method**: `DELETE` 선택
3. **URL**: `http://localhost:8080/api/user/delete/hard`
4. **Authorization 탭** → `Bearer Token` 선택 → 토큰 입력
5. **Body 탭**은 설정하지 않음 (비워둠)
6. **Send** 클릭

### 2. 에러 케이스 테스트

#### 토큰 없이 요청
- Authorization 헤더를 제거하고 요청 → `401 Unauthorized`

#### 잘못된 토큰
- `Authorization: Bearer invalid_token` → `401 Unauthorized`

#### 이미 삭제된 사용자
- 이미 삭제된 사용자의 토큰으로 요청 → `404 Not Found`

---

## 일반 탈퇴 vs 수동 삭제 비교

| 구분 | 일반 탈퇴 (`/api/user/delete`) | 수동 삭제 (`/api/user/delete/hard`) |
|------|-------------------------------|-----------------------------------|
| **엔드포인트** | `DELETE /api/user/delete` | `DELETE /api/user/delete/hard` |
| **삭제 방식** | 소프트 삭제 (Soft Delete) | 물리적 삭제 (Hard Delete) |
| **데이터 보존** | 일정 기간 보존 (한 달 후 자동 삭제) | 즉시 완전 삭제 |
| **복구 가능** | 가능 (스케줄러 실행 전까지) | 불가능 |
| **처리 내용** | `deleteDate` 설정, `isActive = false` | DB에서 완전 삭제 |
| **관련 데이터** | 보존 | 모두 삭제 (AuthAccount, Agreement, 프로필 이미지) |
| **사용 시나리오** | 일반 사용자 탈퇴 | 관리자 강제 삭제, 즉시 삭제 필요 시 |

---

## 주의사항

1. **⚠️ 복구 불가능**: 이 API는 데이터를 **완전히 삭제**합니다. 삭제된 데이터는 복구할 수 없습니다.
2. **인증 토큰**: 로그인 API에서 받은 `accessToken`을 사용합니다.
3. **관련 데이터 삭제**: 다음 데이터가 함께 삭제됩니다:
   - Users 엔티티
   - AuthAccount (소셜 로그인 연동 정보)
   - Agreement (약관 동의 정보)
   - 프로필 이미지 파일
4. **즉시 삭제**: 일반 탈퇴와 달리 대기 기간 없이 즉시 삭제됩니다.
5. **권한 고려**: 프로덕션 환경에서는 관리자 권한이 필요할 수 있습니다.

---

## 보안 고려사항

- **HTTPS 사용 권장**: 프로덕션 환경에서는 반드시 HTTPS를 사용해야 합니다.
- **토큰 검증**: 삭제 요청 시 토큰의 유효성을 검증하여 본인만 삭제할 수 있도록 합니다.
- **권한 관리**: 프로덕션 환경에서는 관리자 권한 체크를 추가하는 것을 권장합니다.
- **로그 기록**: 중요한 작업이므로 삭제 이력을 로그로 남기는 것을 권장합니다.
- **사용 제한**: 일반 사용자에게는 이 API를 노출하지 않고, 관리자 전용으로 제한하는 것을 권장합니다.

---

## 사용 권장 사항

### 일반 탈퇴 사용 권장
- 일반 사용자가 직접 탈퇴하는 경우
- 데이터 복구 가능성을 유지하고 싶은 경우
- 법적 보존 기간을 고려해야 하는 경우

### 수동 삭제 사용 권장
- 관리자가 강제로 계정을 삭제해야 하는 경우
- 즉시 완전 삭제가 필요한 경우
- 테스트 데이터 정리
- 개인정보 보호법 등에 따라 즉시 삭제가 필요한 경우

---

## 관련 API

- [회원탈퇴 API](./API_USER_DELETE.md) - 일반 탈퇴 (소프트 삭제)
- [프로필 조회 API](./API_USER_PROFILE.md) - 사용자 정보 조회

