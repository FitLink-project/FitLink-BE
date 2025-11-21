# 프로필 조회 API 명세서

## 프로필 조회

### 기본 정보
- **엔드포인트**: `GET /api/user/profile`
- **Content-Type**: `application/json`
- **인증**: ✅ 필요 (Bearer Token)

---

## 요청 (Request)

### 요청 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `Authorization` | String | ✅ 필수 | Bearer Token (로그인 시 받은 accessToken) |

### 요청 예시

#### Postman 설정
1. **Method**: `GET` 선택
2. **URL**: `http://localhost:8080/api/user/profile`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}`
   - 예시: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
4. **Send** 클릭

#### cURL 예시
```bash
curl -X GET http://localhost:8080/api/user/profile \
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
    "name": "홍길동",
    "profileUrl": "http://localhost:8080/images/30cc1080-37d9-4cf9-9fb2-f7b3994da93b.png",
    "isActive": true,
    "regDate": "2023-09-01T13:26:22.123Z",
    "provider": "GENERAL",
    "deleteDate": null,
    "agreements": {
      "privacy": true,
      "service": true,
      "over14": true,
      "location": false
    }
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
| `result.email` | String | 이메일 주소 |
| `result.name` | String | 사용자 이름 |
| `result.profileUrl` | String | 프로필 이미지 URL (절대 URL) |
| `result.isActive` | Boolean | 계정 활성화 여부 |
| `result.regDate` | String | 회원가입 일시 (ISO 8601 형식) |
| `result.provider` | String | 로그인 제공자 (GENERAL, KAKAO, GOOGLE) |
| `result.deleteDate` | String | 계정 삭제 일시 (null이면 미삭제) |
| `result.agreements` | Object | 약관 동의 정보 |
| `result.agreements.privacy` | Boolean | 개인정보 처리방침 동의 |
| `result.agreements.service` | Boolean | 서비스 이용약관 동의 |
| `result.agreements.over14` | Boolean | 만 14세 이상 확인 |
| `result.agreements.location` | Boolean | 위치 정보 이용 동의 |

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

---

## Postman 테스트 가이드

### 1. 기본 프로필 조회

1. **새로운 Request 생성**
2. **Method**: `GET` 선택
3. **URL**: `http://localhost:8080/api/user/profile`
4. **Headers 탭** 선택
5. **Authorization 헤더 추가**:
   - Key: `Authorization`
   - Value: `Bearer {로그인 시 받은 accessToken}`
   - 예시: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ...`
6. **Send** 클릭

### 2. Authorization 헤더 자동 설정 (Postman 권장 방법)

1. **Authorization 탭** 선택
2. **Type**: `Bearer Token` 선택
3. **Token**: 로그인 API에서 받은 `accessToken` 값 입력
4. Postman이 자동으로 `Authorization: Bearer {token}` 헤더를 추가합니다

### 3. 에러 케이스 테스트

#### 토큰 없이 요청
- Authorization 헤더를 제거하고 요청 → `401 Unauthorized`

#### 잘못된 토큰
- `Authorization: Bearer invalid_token` → `401 Unauthorized`

---

## 주의사항

1. **인증 토큰**은 로그인 API에서 받은 `accessToken`을 사용합니다.
2. **프로필 이미지 URL**은 절대 URL로 반환됩니다 (로컬: `http://localhost:8080/images/...`, 프로덕션: `https://www.fitlink1207.store/images/...`).
3. **소셜 로그인 사용자**의 경우 `provider`가 `KAKAO` 또는 `GOOGLE`로 표시됩니다.
4. **약관 동의 정보**는 소셜 로그인 사용자의 경우 기본값(true)으로 설정됩니다.

