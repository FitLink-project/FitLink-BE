# 프로필 수정 API 명세서

## 프로필 수정

### 기본 정보
- **엔드포인트**: `PATCH /api/user/edit`
- **Content-Type**: `multipart/form-data`
- **인증**: ✅ 필요 (Bearer Token)

---

## 요청 (Request)

### 요청 헤더

| 헤더명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `Authorization` | String | ✅ 필수 | Bearer Token (로그인 시 받은 accessToken) |

### 요청 파라미터

#### Form Data
| 파라미터명 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `name` | String | ❌ 선택 | 사용자 이름 |
| `email` | String | ❌ 선택 | 이메일 주소 (이메일 형식, 중복 불가) |
| `password` | String | ❌ 선택 | 비밀번호 (최소 8자, 영문+숫자 포함) |
| `agreements` | JSON String | ❌ 선택 | 약관 동의 정보 (JSON 문자열) |
| `agreements.privacy` | Boolean | ❌ 선택 | 개인정보 처리방침 동의 |
| `agreements.service` | Boolean | ❌ 선택 | 서비스 이용약관 동의 |
| `agreements.over14` | Boolean | ❌ 선택 | 만 14세 이상 확인 |
| `agreements.location` | Boolean | ❌ 선택 | 위치 정보 이용 동의 |
| `Img` | File | ❌ 선택 | 프로필 이미지 파일 |

**참고**: 모든 필드는 선택사항입니다. 수정하고 싶은 필드만 전송하면 됩니다.

### 요청 예시

#### Postman 설정
1. **Method**: `PATCH` 선택
2. **URL**: `http://localhost:8080/api/user/edit`
3. **Headers 탭** 선택 → 다음 헤더 추가:
   - `Authorization`: `Bearer {accessToken}`
4. **Body 탭** 선택
5. **form-data** 선택
6. **파라미터 추가** (수정하고 싶은 필드만):
   - `name` (Text): `홍길동` (이름 수정)
   - `email` (Text): `newemail@example.com` (이메일 수정)
   - `password` (Text): `newpassword123` (비밀번호 수정)
   - `agreements` (Text): `{"privacy":true,"service":true,"over14":true,"location":true}` (약관 동의 수정)
   - `Img` (File): 프로필 이미지 파일 선택
7. **Send** 클릭

#### cURL 예시
```bash
curl -X PATCH http://localhost:8080/api/user/edit \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "name=홍길동" \
  -F "email=newemail@example.com" \
  -F 'agreements={"privacy":true,"service":true,"over14":true,"location":true}' \
  -F "Img=@/path/to/profile.jpg"
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
    "email": "newemail@example.com",
    "name": "홍길동",
    "profileUrl": "http://localhost:8080/images/new-image-uuid.png",
    "isActive": true,
    "regDate": "2023-09-01T13:26:22.123Z",
    "provider": "GENERAL",
    "deleteDate": null,
    "agreements": {
      "privacy": true,
      "service": true,
      "over14": true,
      "location": true
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
| `result` | Object | 수정된 사용자 정보 (프로필 조회와 동일한 구조) |

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
- 이메일 형식 오류
- 비밀번호 형식 오류

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
- 이미 사용 중인 이메일로 변경 시도

---

## Postman 테스트 가이드

### 1. 이름만 수정

1. **새로운 Request 생성**
2. **Method**: `PATCH` 선택
3. **URL**: `http://localhost:8080/api/user/edit`
4. **Authorization 탭** → `Bearer Token` 선택 → 토큰 입력
5. **Body 탭** → `form-data` 선택
6. **파라미터 추가**:
   ```
   name: 새로운 이름
   ```
7. **Send** 클릭

### 2. 비밀번호만 수정

1. 위와 동일한 설정
2. **파라미터 추가**:
   ```
   password: newpassword123
   ```
3. **Send** 클릭

### 3. 약관 동의만 수정

1. 위와 동일한 설정
2. **파라미터 추가**:
   ```
   agreements: {"privacy":true,"service":true,"over14":true,"location":true}
   ```
   - `agreements`는 JSON 문자열로 입력
3. **Send** 클릭

### 4. 프로필 이미지만 수정

1. 위와 동일한 설정
2. **파라미터 추가**:
   ```
   Img: [File] (파일 선택)
   ```
   - `Img` 필드의 타입을 `Text` → `File`로 변경
   - 프로필 이미지 파일 선택
3. **Send** 클릭

### 5. 여러 필드 동시 수정

1. 위와 동일한 설정
2. **파라미터 추가**:
   ```
   name: 홍길동
   email: newemail@example.com
   agreements: {"privacy":true,"service":true,"over14":true,"location":true}
   Img: [File] (파일 선택)
   ```
3. **Send** 클릭

---

## 유효성 검증 규칙

| 필드 | 규칙 | 에러 코드 |
|------|------|----------|
| `name` | 공백 불가 (제공된 경우) | COMMON400 |
| `email` | 이메일 형식, 중복 불가 (제공된 경우) | USER4001 / USER4031 |
| `password` | 최소 8자, 영문+숫자 포함 (제공된 경우) | USER4002 |
| `agreements` | JSON 형식 (제공된 경우) | COMMON400 |

---

## 주의사항

1. **부분 업데이트**: 수정하고 싶은 필드만 전송하면 됩니다. 전송하지 않은 필드는 기존 값이 유지됩니다.
2. **이메일 변경**: 이메일을 변경하면 중복 체크가 수행됩니다.
3. **비밀번호 변경**: 비밀번호는 BCrypt로 암호화되어 저장됩니다.
4. **약관 동의**: `agreements` 객체의 각 필드는 개별적으로 업데이트됩니다. 전송하지 않은 필드는 기존 값이 유지됩니다.
5. **프로필 이미지**: 새 이미지를 업로드하면 기존 이미지는 교체됩니다. 이미지 URL은 절대 URL로 반환됩니다.

