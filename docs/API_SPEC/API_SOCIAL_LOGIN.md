# 소셜 로그인 API 명세서

## 소셜 로그인 개요

구글(Google)과 카카오(Kakao) 소셜 로그인을 지원합니다.

---

## 기본 정보

- **인증 방식**: OAuth2 Authorization Code Flow
- **토큰 발급**: JWT Access Token
- **리다이렉트**: 로그인 성공 시 프론트엔드로 JWT 토큰 전달

---

## 소셜 로그인 시작

### 구글 로그인

#### 엔드포인트
- **URL**: `GET /oauth2/authorization/google`
- **인증**: 불필요

#### 사용 방법
브라우저에서 다음 URL로 접근:
```
http://localhost:8080/oauth2/authorization/google
```

또는 프론트엔드에서 링크/버튼으로 연결:
```html
<a href="http://localhost:8080/oauth2/authorization/google">
    구글로 로그인
</a>
```

---

### 카카오 로그인

#### 엔드포인트
- **URL**: `GET /oauth2/authorization/kakao`
- **인증**: 불필요

#### 사용 방법
브라우저에서 다음 URL로 접근:
```
http://localhost:8080/oauth2/authorization/kakao
```

또는 프론트엔드에서 링크/버튼으로 연결:
```html
<a href="http://localhost:8080/oauth2/authorization/kakao">
    카카오로 로그인
</a>
```

---

## 로그인 플로우

1. **사용자가 소셜 로그인 버튼 클릭**
   - 구글: `/oauth2/authorization/google`
   - 카카오: `/oauth2/authorization/kakao`

2. **소셜 로그인 페이지로 리다이렉트**
   - 구글/카카오 로그인 페이지로 이동

3. **사용자 인증 완료**
   - 구글/카카오 계정으로 로그인

4. **인증 코드 발급 및 토큰 교환**
   - Spring Security가 자동으로 처리

5. **사용자 정보 조회 및 DB 저장**
   - `OAuth2UserServiceImpl`에서 처리
   - 신규 사용자: `users`와 `auth_account` 테이블에 저장
   - 기존 사용자: `auth_account` 정보 업데이트

6. **JWT 토큰 생성 및 리다이렉트**
   - `OAuth2SuccessHandler`에서 JWT 토큰 생성
   - 프론트엔드로 리다이렉트: `{redirectUri}?token={jwtToken}`

---

## 성공 응답

### 리다이렉트 URL
로그인 성공 시 다음 형식으로 리다이렉트됩니다:
```
{oauth2.redirect.uri}?token={jwt_access_token}
```

#### 예시
```
http://localhost:3000/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MDI2NjU2MDAsImV4cCI6MTcwMjY2OTIwMH0....
```

#### 프론트엔드 처리 예시 (React)
```javascript
// URL에서 토큰 추출
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

if (token) {
    // 토큰을 localStorage 또는 상태 관리에 저장
    localStorage.setItem('accessToken', token);
    
    // 메인 페이지로 이동
    window.location.href = '/';
}
```

---

## 실패 응답

### 리다이렉트 URL
로그인 실패 시 다음 형식으로 리다이렉트됩니다:
```
{oauth2.redirect.uri}?error=oauth2_authentication_failed&message={error_message}
```

#### 예시
```
http://localhost:3000/oauth2/redirect?error=oauth2_authentication_failed&message=Access%20denied
```

#### 프론트엔드 처리 예시
```javascript
const urlParams = new URLSearchParams(window.location.search);
const error = urlParams.get('error');

if (error) {
    const errorMessage = urlParams.get('message');
    alert(`로그인 실패: ${errorMessage}`);
}
```

---

## 설정 필요 사항

### 1. OAuth2 클라이언트 등록

#### 구글 OAuth2 설정
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 및 OAuth2 클라이언트 ID 생성
3. 리다이렉트 URI 등록: `http://localhost:8080/login/oauth2/code/google`
4. `application.properties`에 클라이언트 정보 입력:
   ```properties
   spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
   ```

#### 카카오 OAuth2 설정
1. [Kakao Developers](https://developers.kakao.com/) 접속
2. 애플리케이션 생성 및 REST API 키 확인
3. 리다이렉트 URI 등록: `http://localhost:8080/login/oauth2/code/kakao`
4. 카카오 로그인 활성화 및 동의 항목 설정 (이메일, 닉네임 등)
5. `application.properties`에 클라이언트 정보 입력:
   ```properties
   spring.security.oauth2.client.registration.kakao.client-id=YOUR_KAKAO_CLIENT_ID
   spring.security.oauth2.client.registration.kakao.client-secret=YOUR_KAKAO_CLIENT_SECRET
   ```

### 2. 리다이렉트 URI 설정

`application.properties`에서 프론트엔드 리다이렉트 URI 설정:
```properties
oauth2.redirect.uri=http://localhost:3000/oauth2/redirect
```

프로덕션 환경에서는 실제 프론트엔드 도메인으로 변경:
```properties
oauth2.redirect.uri=https://your-frontend-domain.com/oauth2/redirect
```

---

## 데이터베이스 구조

### 신규 소셜 로그인 사용자
1. **users** 테이블에 사용자 정보 저장
   - `email`: 소셜 계정 이메일
   - `name`: 소셜 계정 이름/닉네임
   - `password`: `null` (소셜 로그인은 패스워드 없음)
   - `role`: `USER`
   - `isActive`: `true`
   - `profileUrl`: 소셜 계정 프로필 이미지 URL

2. **auth_account** 테이블에 소셜 계정 정보 저장
   - `user`: `users` 테이블 참조
   - `provider`: `GOOGLE` 또는 `KAKAO`
   - `externalId`: 소셜 계정 고유 ID
   - `socialToken`: 소셜 OAuth2 Access Token

### 기존 사용자
- 같은 `externalId`로 로그인하면 기존 `auth_account` 정보 업데이트
- 같은 이메일로 일반 회원가입했던 사용자면 기존 `users`에 `auth_account`만 추가

---

## 주요 엔드포인트

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `/oauth2/authorization/google` | GET | 구글 로그인 시작 |
| `/oauth2/authorization/kakao` | GET | 카카오 로그인 시작 |
| `/login/oauth2/code/google` | GET | 구글 리다이렉트 URI (자동 처리) |
| `/login/oauth2/code/kakao` | GET | 카카오 리다이렉트 URI (자동 처리) |

---

## 주의사항

1. **프로덕션 환경 설정**
   - OAuth2 클라이언트 설정 시 실제 도메인으로 리다이렉트 URI 등록 필요
   - HTTPS 사용 권장

2. **토큰 보안**
   - JWT 토큰은 URL 파라미터로 전달되므로 HTTPS 필수
   - 프론트엔드에서 안전하게 저장 (localStorage, sessionStorage 등)

3. **중복 이메일 처리**
   - 같은 이메일로 일반 회원가입과 소셜 로그인 모두 가능
   - `auth_account` 테이블에서 여러 Provider를 연결할 수 있음

4. **프로필 이미지**
   - 소셜 계정의 프로필 이미지를 `users.profileUrl`에 자동 저장
   - 구글: `picture` 속성
   - 카카오: `properties.profile_image` 속성

---

## 테스트 방법

### 브라우저에서 직접 테스트
1. 브라우저에서 `http://localhost:8080/oauth2/authorization/google` 접근
2. 구글 로그인 진행
3. 성공 시 `{redirectUri}?token={jwtToken}`로 리다이렉트 확인

### Postman 테스트
소셜 로그인은 브라우저 기반 OAuth2 플로우이므로 Postman으로 직접 테스트하기 어렵습니다.
대신:
1. 브라우저에서 로그인하여 JWT 토큰 획득
2. Postman에서 `Authorization: Bearer {token}` 헤더로 인증 테스트

---

## 트러블슈팅

### 1. "redirect_uri_mismatch" 에러
- **원인**: OAuth2 클라이언트에 등록된 리다이렉트 URI와 불일치
- **해결**: 구글/카카오 개발자 콘솔에서 정확한 리다이렉트 URI 등록

### 2. "invalid_client" 에러
- **원인**: 클라이언트 ID 또는 Secret이 잘못됨
- **해결**: `application.properties`의 클라이언트 정보 확인

### 3. 이메일 정보가 null
- **원인**: 소셜 계정 동의 항목에서 이메일 제공 미동의
- **해결**: 카카오 개발자 콘솔에서 동의 항목 설정 확인

### 4. 리다이렉트가 안 됨
- **원인**: `oauth2.redirect.uri` 설정이 없거나 잘못됨
- **해결**: `application.properties`에서 `oauth2.redirect.uri` 설정 확인

