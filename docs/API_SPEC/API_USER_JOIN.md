# 회원가입 API 명세서

## 회원가입

### 기본 정보
- **엔드포인트**: `POST /api/user/join`
- **Content-Type**: `multipart/form-data`
- **인증**: 불필요

---

## 요청 (Request)

### 요청 파라미터

#### Form Data
| 파라미터명 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `name` | String | ✅ 필수 | 사용자 이름 |
| `email` | String | ✅ 필수 | 이메일 주소 (이메일 형식, 중복 불가) |
| `password` | String | ✅ 필수 | 비밀번호 (최소 8자, 영문+숫자 포함) |
| `agreements` | JSON String | ✅ 필수 | 약관 동의 정보 (JSON 문자열) |
| `agreements.privacy` | Boolean | ✅ 필수 | 개인정보 처리방침 동의 |
| `agreements.service` | Boolean | ✅ 필수 | 서비스 이용약관 동의 |
| `agreements.over14` | Boolean | ✅ 필수 | 만 14세 이상 확인 |
| `agreements.location` | Boolean | ❌ 선택 | 위치 정보 이용 동의 (기본값: false) |
| `Img` | File | ❌ 선택 | 프로필 이미지 파일 |

### 비밀번호 형식 규칙
- 최소 8자 이상
- 영문(대소문자)과 숫자 포함 필수
- 허용 특수문자: `@$!%*#?&`

### 요청 예시

#### Postman 설정
1. **Method**: `POST`
2. **URL**: `http://localhost:8080/api/user/join` (서버 주소에 맞게 변경)
3. **Body**: `form-data` 선택
4. **파라미터 추가**:
   - `name` (Text): `홍길동`
   - `email` (Text): `test@example.com`
   - `password` (Text): `password123`
   - `agreements` (Text): `{"privacy":true,"service":true,"over14":true,"location":false}`
   - `Img` (File, 선택): 프로필 이미지 파일 선택

**중요**: `agreements`는 JSON 문자열로 입력해야 합니다. Postman에서:
   - Key: `agreements`
   - Value: `{"privacy":true,"service":true,"over14":true,"location":false}`
   - Type: `Text` (기본값)

#### cURL 예시
```bash
curl -X POST http://localhost:8080/api/user/join \
  -F "name=홍길동" \
  -F "email=test@example.com" \
  -F "password=password123" \
  -F 'agreements={"privacy":true,"service":true,"over14":true,"location":false}' \
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
    "createdAt": "2025-01-15T14:30:00"
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
| `result.userId` | Long | 생성된 사용자 ID |
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
- 필수 필드 누락 (`name`, `email`, `password`)
- 이메일 형식 오류

#### 409 Conflict - 이메일 형식 오류
```json
{
  "isSuccess": false,
  "code": "USER4001",
  "message": "올바른 이메일 형식이 아닙니다.",
  "result": null
}
```

#### 409 Conflict - 비밀번호 형식 오류
```json
{
  "isSuccess": false,
  "code": "USER4002",
  "message": "올바른 비밀번호 형식이 아닙니다.",
  "result": null
}
```

**발생 상황**:
- 비밀번호가 8자 미만
- 비밀번호에 영문 또는 숫자가 포함되지 않음

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
- 이미 가입된 이메일 주소로 회원가입 시도

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

### 1. 기본 회원가입 (프로필 이미지 없이)

1. **새로운 Request 생성**
2. **Method**: `POST` 선택
3. **URL**: `http://localhost:8080/api/user/join`
4. **Body 탭** 선택
5. **form-data** 선택
6. **파라미터 추가**:
   ```
   name: 홍길동
   email: test@example.com
   password: password123
   agreements: {"privacy":true,"service":true,"over14":true,"location":false}
   ```
   - `agreements`는 JSON 문자열로 입력 (따옴표 포함)
7. **Send** 클릭

### 2. 프로필 이미지 포함 회원가입

1. 위와 동일한 설정
2. **파라미터 추가**:
   ```
   name: 홍길동
   email: test@example.com
   password: password123
   agreements: {"privacy":true,"service":true,"over14":true,"location":false}
   Img: [File] (파일 선택)
   ```
   - `Img` 필드의 타입을 `Text` → `File`로 변경
   - 프로필 이미지 파일 선택
3. **Send** 클릭

### 3. 에러 케이스 테스트

#### 이메일 중복 테스트
- 동일한 이메일로 2번 요청 시 `409 Conflict` 응답

#### 비밀번호 형식 오류 테스트
- `password`: `123456` (숫자만) → `409 Conflict`
- `password`: `abcdefgh` (영문만) → `409 Conflict`
- `password`: `12345` (8자 미만) → `409 Conflict`

#### 필수 필드 누락 테스트
- `name`, `email`, `password` 중 하나라도 누락 시 `400 Bad Request`

---

## 유효성 검증 규칙

| 필드 | 규칙 | 에러 코드 |
|------|------|----------|
| `name` | 필수, 공백 불가 | COMMON400 |
| `email` | 필수, 이메일 형식, 중복 불가 | USER4001 / USER4031 |
| `password` | 필수, 최소 8자, 영문+숫자 포함 | USER4002 |

---

## 주의사항

1. **프로필 이미지**는 선택 사항입니다. 업로드하지 않아도 회원가입이 가능합니다.
2. **비밀번호**는 BCrypt로 암호화되어 저장됩니다.
3. **생성일시** (`createdAt`)는 서버에서 자동으로 설정됩니다.
4. 회원가입 시 기본적으로 **활성화 상태** (`isActive = true`)로 설정됩니다.

