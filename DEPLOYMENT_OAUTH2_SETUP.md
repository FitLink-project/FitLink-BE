# OAuth2 소셜 로그인 배포 설정 가이드

## 📋 목차
1. [배포 정보](#배포-정보)
2. [필수 설정 단계](#필수-설정-단계)
3. [구글 OAuth2 설정](#구글-oauth2-설정)
4. [카카오 OAuth2 설정](#카카오-oauth2-설정)
5. [환경 변수 설정](#환경-변수-설정)
6. [테스트 방법](#테스트-방법)
7. [문제 해결](#문제-해결)

---

## 배포 정보

- **백엔드 서버**: `{{BACKEND_URL}}`
- **프론트엔드 서버**: `{{FRONTEND_URL}}`
- **OAuth2 리다이렉트 URI**: `{{FRONTEND_URL}}/oauth2/redirect`
- **백엔드 OAuth2 콜백 URI**: `{{BACKEND_URL}}/login/oauth2/code/{provider}`

---

## 필수 설정 단계

### ✅ 1단계: 구글 OAuth2 클라이언트 설정

### ✅ 2단계: 카카오 OAuth2 클라이언트 설정

### ✅ 3단계: 백엔드 환경 변수 설정

### ✅ 4단계: 프론트엔드 OAuth2 버튼 설정

### ✅ 5단계: 테스트

---

## 구글 OAuth2 설정

### 1. Google Cloud Console 접속
- [Google Cloud Console](https://console.cloud.google.com/) 접속
- 프로젝트 선택 또는 새 프로젝트 생성

### 2. OAuth2 클라이언트 ID 생성

#### 2-1. API 및 서비스 > 사용자 인증 정보
1. 좌측 메뉴에서 **"API 및 서비스" > "사용자 인증 정보"** 선택
2. **"+ 사용자 인증 정보 만들기" > "OAuth 클라이언트 ID"** 클릭

#### 2-2. 동의 화면 설정 (처음인 경우)
- 애플리케이션 이름: `FitLink`
- 사용자 지원 이메일: (본인 이메일)
- 개발자 연락처 정보: (본인 이메일)
- **저장 후 계속**

#### 2-3. OAuth 클라이언트 ID 만들기
- **애플리케이션 유형**: `웹 애플리케이션`
- **이름**: `FitLink Backend`
- **승인된 리디렉션 URI**에 다음 추가:
  ```
  {{BACKEND_URL}}/login/oauth2/code/google
  ```
  
  > 💡 **로컬 개발용도 추가하려면:**
  > ```
  > http://localhost:8080/login/oauth2/code/google
  > ```

- **만들기** 클릭

#### 2-4. 클라이언트 ID 및 비밀번호 복사
- **클라이언트 ID**: `{{GOOGLE_CLIENT_ID}}`
- **클라이언트 보안 비밀번호**: `{{GOOGLE_CLIENT_SECRET}}`

### 3. 백엔드 설정에 적용

`application.properties` 파일 업데이트:
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google
```

> ⚠️ **중요**: `{baseUrl}`은 Spring Security가 자동으로 현재 서버 주소로 대체합니다.
> 배포 서버에서는 `{{BACKEND_URL}}`로 자동 설정됩니다.

---

## 카카오 OAuth2 설정

### 1. Kakao Developers 접속
- [Kakao Developers](https://developers.kakao.com/) 접속
- 로그인 후 **"내 애플리케이션"** 선택

### 2. 애플리케이션 생성 (없는 경우)
- **"애플리케이션 추가하기"** 클릭
- 앱 이름: `FitLink`
- 사업자명: (본인 또는 회사명)
- **저장**

### 3. 플랫폼 설정

#### 3-1. 플랫폼 > Web 플랫폼 등록
1. **"앱 설정" > "플랫폼"** 메뉴 선택
2. **"Web 플랫폼 등록"** 클릭
3. **사이트 도메인** 입력:
   ```
   {{BACKEND_URL}}
   ```
4. **저장**

### 4. 카카오 로그인 활성화

#### 4-1. 제품 설정 > 카카오 로그인
1. **"제품 설정" > "카카오 로그인"** 메뉴 선택
2. **"활성화 설정"** ON
3. **Redirect URI** 추가:
   ```
   {{BACKEND_URL}}/login/oauth2/code/kakao
   ```
   
   > 💡 **로컬 개발용도 추가하려면:**
   > ```
   > http://localhost:8080/login/oauth2/code/kakao
   > ```

4. **저장**

### 5. 동의항목 설정
1. **"제품 설정" > "카카오 로그인" > "동의항목"** 메뉴 선택
2. 필수 동의 항목 설정:
   - **닉네임** (필수)
   - **이메일** (필수 또는 선택)
3. **저장**

### 6. REST API 키 및 보안 비밀번호 확인

#### 6-1. 앱 키 확인
1. **"앱 설정" > "앱 키"** 메뉴 선택
2. **REST API 키** 복사: `{{KAKAO_CLIENT_ID}}`
   - 이것이 `client-id`입니다.

#### 6-2. 보안 비밀번호 생성 (Client Secret)
1. **"제품 설정" > "카카오 로그인" > "보안"** 메뉴 선택
2. **"Client Secret 코드 생성"** 클릭
3. **Client Secret** 복사: `{{KAKAO_CLIENT_SECRET}}`
   - ⚠️ 한 번만 표시되므로 안전하게 보관하세요!

### 7. 백엔드 설정에 적용

`application.properties` 파일 업데이트:
```properties
# 카카오 OAuth2
spring.security.oauth2.client.registration.kakao.client-id=YOUR_KAKAO_REST_API_KEY
spring.security.oauth2.client.registration.kakao.client-secret=YOUR_KAKAO_CLIENT_SECRET
spring.security.oauth2.client.registration.kakao.scope=profile_nickname,account_email
spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/kakao
spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.kakao.client-name=Kakao
```

---

## 환경 변수 설정

### ⚠️ 중요: 민감 정보 보호

절대 `application.properties`에 실제 `client-id`와 `client-secret`을 하드코딩하지 마세요!

### 방법 1: 환경 변수 사용 (권장)

#### 백엔드 서버에서 환경 변수 설정
```bash
# 예: Docker 또는 서버 환경 변수
export GOOGLE_CLIENT_ID="{{GOOGLE_CLIENT_ID}}"
export GOOGLE_CLIENT_SECRET="{{GOOGLE_CLIENT_SECRET}}"
export KAKAO_CLIENT_ID="{{KAKAO_CLIENT_ID}}"
export KAKAO_CLIENT_SECRET="{{KAKAO_CLIENT_SECRET}}"
```

#### `application.properties` 수정
```properties
# 구글 OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

# 카카오 OAuth2
spring.security.oauth2.client.registration.kakao.client-id=${KAKAO_CLIENT_ID}
spring.security.oauth2.client.registration.kakao.client-secret=${KAKAO_CLIENT_SECRET}
```

### 방법 2: Spring Profile 사용

#### `application-prod.properties` 생성
```properties
# 프로덕션 환경 설정
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.kakao.client-id=${KAKAO_CLIENT_ID}
spring.security.oauth2.client.registration.kakao.client-secret=${KAKAO_CLIENT_SECRET}
```

#### 배포 시 Profile 지정
```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## 프론트엔드 설정

### OAuth2 로그인 버튼 URL

프론트엔드에서 소셜 로그인 버튼을 다음과 같이 설정:

```html
<!-- 구글 로그인 -->
<a href="{{BACKEND_URL}}/oauth2/authorization/google">
    구글로 로그인
</a>

<!-- 카카오 로그인 -->
<a href="{{BACKEND_URL}}/oauth2/authorization/kakao">
    카카오로 로그인
</a>
```

### React 예시
```jsx
const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || '{{BACKEND_URL}}';

const handleGoogleLogin = () => {
  window.location.href = `${BACKEND_URL}/oauth2/authorization/google`;
};

const handleKakaoLogin = () => {
  window.location.href = `${BACKEND_URL}/oauth2/authorization/kakao`;
};

// JSX
<button onClick={handleGoogleLogin}>구글로 로그인</button>
<button onClick={handleKakaoLogin}>카카오로 로그인</button>
```

### OAuth2 리다이렉트 처리 페이지

프론트엔드에 `/oauth2/redirect` 경로의 페이지 생성:

```jsx
// pages/oauth2/redirect.js 또는 components/OAuth2Redirect.js
import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');
    const message = searchParams.get('message');

    if (error) {
      // 에러 처리
      console.error('OAuth2 로그인 실패:', error, message);
      // 에러 페이지로 이동
      navigate('/login?error=oauth2_failed');
      return;
    }

    if (token) {
      // 토큰 저장
      localStorage.setItem('accessToken', token);
      
      // 메인 페이지로 이동
      navigate('/');
    }
  }, [searchParams, navigate]);

  return (
    <div>
      <p>로그인 처리 중...</p>
    </div>
  );
}

export default OAuth2Redirect;
```

---

## 테스트 방법

### 1. 로컬 테스트 (선택사항)

#### 로컬 개발용 설정
1. `application.properties`에서 프론트엔드 리다이렉트 URI를 로컬로 변경:
   ```properties
   oauth2.redirect.uri=http://localhost:3000/oauth2/redirect
   ```

2. 구글/카카오 OAuth2 콘솔에 로컬 URI 추가:
   - 구글: `http://localhost:8080/login/oauth2/code/google`
   - 카카오: `http://localhost:8080/login/oauth2/code/kakao`

3. 프론트엔드에서 로컬 백엔드로 연결:
   ```jsx
   const BACKEND_URL = process.env.NODE_ENV === 'production' 
     ? '{{BACKEND_URL}}' 
     : 'http://localhost:8080';
   ```

### 2. 프로덕션 테스트

1. 프론트엔드 배포 확인: `{{FRONTEND_URL}}`
2. 백엔드 배포 확인: `{{BACKEND_URL}}/api/health`
3. 소셜 로그인 버튼 클릭
4. 구글/카카오 로그인 진행
5. 리다이렉트 확인: `{{FRONTEND_URL}}/oauth2/redirect?token=...`

---

## 문제 해결

### 문제 1: "redirect_uri_mismatch" 에러

**원인**: OAuth2 콘솔에 등록된 리다이렉트 URI와 실제 요청 URI가 일치하지 않음

**해결 방법**:
1. 구글/카카오 OAuth2 콘솔에서 정확한 URI 확인
2. 백엔드 서버 주소 확인: `{{BACKEND_URL}}`
3. 리다이렉트 URI 정확히 일치하는지 확인:
   - 구글: `{{BACKEND_URL}}/login/oauth2/code/google`
   - 카카오: `{{BACKEND_URL}}/login/oauth2/code/kakao`

### 문제 2: "invalid_client" 에러

**원인**: client-id 또는 client-secret이 잘못되었음

**해결 방법**:
1. `application.properties`에서 client-id와 client-secret 확인
2. 구글/카카오 콘솔에서 키 재생성
3. 환경 변수 설정 확인

### 문제 3: CORS 에러

**원인**: 프론트엔드에서 백엔드로 요청 시 CORS 정책 위반

**해결 방법**:
1. `SecurityConfig.java`의 `corsConfigurationSource()` 확인
2. 프론트엔드 도메인 추가:
   ```java
   configuration.setAllowedOriginPatterns(List.of(
       "*",
       "{{FRONTEND_URL}}"
   ));
   ```

### 문제 4: "Access Denied" 또는 403 에러

**원인**: Spring Security에서 OAuth2 경로가 차단됨

**해결 방법**:
1. `SecurityConfig.java`에서 OAuth2 경로 허용 확인:
   ```java
   .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
   ```

### 문제 5: 프론트엔드 리다이렉트 실패

**원인**: `oauth2.redirect.uri` 설정이 잘못되었음

**해결 방법**:
1. `application.properties`에서 `oauth2.redirect.uri` 확인:
   ```properties
   oauth2.redirect.uri={{FRONTEND_URL}}/oauth2/redirect
   ```
2. 프론트엔드에 `/oauth2/redirect` 경로 페이지 존재 확인

---

## 체크리스트

배포 전 확인사항:

- [ ] 구글 OAuth2 클라이언트 ID 및 Secret 생성 완료
- [ ] 카카오 OAuth2 REST API 키 및 Client Secret 생성 완료
- [ ] 구글/카카오 콘솔에 프로덕션 리다이렉트 URI 등록 완료
- [ ] 백엔드 `application.properties`에 환경 변수 설정
- [ ] 실제 client-id와 secret은 환경 변수로 관리 (하드코딩 금지)
- [ ] 프론트엔드에 소셜 로그인 버튼 구현 완료
- [ ] 프론트엔드에 `/oauth2/redirect` 페이지 구현 완료
- [ ] 로컬 테스트 완료 (선택사항)
- [ ] 프로덕션 배포 및 테스트 완료

---

## 다음 단계

1. **구글 OAuth2 클라이언트 설정** (위 가이드 참고)
2. **카카오 OAuth2 클라이언트 설정** (위 가이드 참고)
3. **백엔드 환경 변수 설정** (실제 서버에 적용)
4. **프론트엔드 OAuth2 버튼 구현**
5. **프론트엔드 리다이렉트 페이지 구현**
6. **전체 플로우 테스트**

---

## 참고 자료

- [Spring Security OAuth2 Client 공식 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth2 설정 가이드](https://developers.google.com/identity/protocols/oauth2)
- [Kakao OAuth2 설정 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)

