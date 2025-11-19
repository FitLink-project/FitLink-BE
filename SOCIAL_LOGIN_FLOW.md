# 소셜 로그인 전체 로직 플로우

## 📋 목차
1. [전체 플로우 개요](#전체-플로우-개요)
2. [단계별 상세 설명](#단계별-상세-설명)
3. [데이터베이스 처리 로직](#데이터베이스-처리-로직)
4. [주요 컴포넌트 역할](#주요-컴포넌트-역할)
5. [시퀀스 다이어그램](#시퀀스-다이어그램)

---

## 전체 플로우 개요

```
사용자가 프론트엔드(React 등)에서 "구글/카카오로 로그인"을 클릭합니다.

프론트엔드가 /oauth2/authorization/{provider}로 요청을 보냅니다.

백엔드(Spring Security)가 OAuth2 인증 플로우를 자동 처리하여 사용자를 소셜 로그인 제공자(구글/카카오 등)로 리다이렉트합니다.

인증이 성공하면 provider가 authorization code를 백엔드로 넘깁니다.

백엔드는 access token을 교환한 뒤, 사용자 소셜 프로필을 받아옵니다.

DB에 사용자 정보를 저장(신규 가입/정보 갱신/연동), JWT 토큰을 발급해 프론트로 전달합니다.
```

### 전체 6단계 프로세스

1. **사용자 소셜 로그인 시작** (프론트엔드)
2. **OAuth2 Authorization Code 요청** (Spring Security 자동)
3. **사용자 인증 및 Authorization Code 발급** (구글/카카오)
4. **Access Token 교환 및 사용자 정보 조회** (OAuth2UserServiceImpl)
5. **DB 저장/업데이트 및 JWT 토큰 생성** (OAuth2SuccessHandler)
6. **프론트엔드로 리다이렉트** (토큰 전달)

---

## 단계별 상세 설명

### 1단계: 사용자 소셜 로그인 시작

**프론트엔드에서 버튼 클릭**
```html
<!-- 구글 로그인 -->
<a href="http://localhost:8080/oauth2/authorization/google">
    구글로 로그인
</a>

<!-- 카카오 로그인 -->
<a href="http://localhost:8080/oauth2/authorization/kakao">
    카카오로 로그인
</a>
```

**요청 경로:**
- 구글: `GET /oauth2/authorization/google`
- 카카오: `GET /oauth2/authorization/kakao`

**SecurityConfig 설정:**
```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
    // ...
)
```

---

### 2단계: Spring Security OAuth2 자동 처리

**Spring Security가 자동으로 처리:**
1. `/oauth2/authorization/{provider}` 요청 감지
2. `application.properties`에서 OAuth2 클라이언트 설정 로드
3. OAuth2 제공자(구글/카카오)로 Authorization Code 요청 URL 생성
4. 사용자를 구글/카카오 로그인 페이지로 리다이렉트

**리다이렉트 URL 예시:**
```
https://accounts.google.com/o/oauth2/v2/auth?
  client_id=YOUR_CLIENT_ID
  &redirect_uri=http://localhost:8080/login/oauth2/code/google
  &response_type=code
  &scope=profile email
  &state=...
```

---

### 3단계: 사용자 인증 및 Authorization Code 발급

**구글/카카오에서 처리:**
1. 사용자가 구글/카카오 계정으로 로그인
2. 애플리케이션 권한 동의
3. Authorization Code 발급
4. 백엔드 리다이렉트 URI로 리다이렉트
   ```
   http://localhost:8080/login/oauth2/code/google?code=AUTHORIZATION_CODE&state=...
   ```

---

### 4단계: Access Token 교환 및 사용자 정보 조회

**SecurityConfig에서 OAuth2 설정:**
```java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .userService(oAuth2UserService)  // 👈 여기서 사용자 정보 처리
    )
    .successHandler(oAuth2SuccessHandler)  // 👈 성공 시 처리
    .failureHandler(oAuth2FailureHandler)  // 👈 실패 시 처리
)
```

**OAuth2UserServiceImpl.loadUser() 실행:**
```java
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    // 1. DefaultOAuth2UserService로 Access Token으로 사용자 정보 조회
    OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);
    
    // 2. Provider 확인 (GOOGLE 또는 KAKAO)
    Provider provider = getProvider(registrationId);
    
    // 3. 소셜 계정 정보 추출
    String externalId = getExternalId(oAuth2User, registrationId);  // 구글: sub, 카카오: id
    String email = getEmail(oAuth2User, registrationId);
    String name = getName(oAuth2User, registrationId);
    String profileImageUrl = getProfileImageUrl(oAuth2User, registrationId);
    
    // 4. DB에서 사용자 찾기/생성 (아래 상세 설명)
    // ...
    
    // 5. CustomOAuth2User 반환 (JWT 토큰 생성에 사용)
    return new CustomOAuth2User(authorities, attributes, "email", email);
}
```

**소셜 계정 정보 추출 로직:**

#### 구글 (Google)
```java
externalId = oAuth2User.getAttribute("sub");           // 고유 ID
email = oAuth2User.getAttribute("email");              // 이메일
name = oAuth2User.getAttribute("name");                // 이름
profileImageUrl = oAuth2User.getAttribute("picture");  // 프로필 이미지
```

#### 카카오 (Kakao)
```java
externalId = oAuth2User.getAttribute("id").toString(); // 고유 ID
kakaoAccount = oAuth2User.getAttribute("kakao_account");
email = kakaoAccount.get("email");                     // 이메일
properties = oAuth2User.getAttribute("properties");
name = properties.get("nickname");                     // 닉네임
profileImageUrl = properties.get("profile_image");     // 프로필 이미지
```

---

### 5단계: 데이터베이스 저장/업데이트 로직

**OAuth2UserServiceImpl에서 처리:**

#### 케이스 1: 기존 소셜 로그인 사용자 (같은 Provider, 같은 External ID)
```java
Optional<AuthAccount> authAccountOpt = 
    authAccountRepository.findByProviderAndExternalId(provider, externalId);

if (authAccountOpt.isPresent()) {
    // ✅ 기존 사용자
    AuthAccount authAccount = authAccountOpt.get();
    Users user = authAccount.getUser();
    
    // 사용자 정보 업데이트
    if (name != null && !name.equals(user.getName())) {
        user.setName(name);  // 이름 변경 시 업데이트
    }
    if (profileImageUrl != null && !profileImageUrl.equals(user.getProfileUrl())) {
        user.setProfileUrl(profileImageUrl);  // 프로필 이미지 변경 시 업데이트
    }
    
    // 소셜 토큰 업데이트 (최신 Access Token 저장)
    authAccount.setSocialToken(userRequest.getAccessToken().getTokenValue());
}
```

#### 케이스 2: 신규 소셜 로그인 사용자
```java
else {
    // 2-1. 같은 이메일로 일반 회원가입 했는지 확인
    Optional<Users> existingUserOpt = userRepository.findByEmail(email);
    
    if (existingUserOpt.isPresent()) {
        // ✅ 기존 일반 회원가입 사용자 → AuthAccount만 추가
        Users user = existingUserOpt.get();
        
        AuthAccount authAccount = AuthAccount.builder()
            .user(user)
            .provider(provider)  // GOOGLE 또는 KAKAO
            .socialToken(userRequest.getAccessToken().getTokenValue())
            .externalId(externalId)
            .build();
        authAccountRepository.save(authAccount);
        
    } else {
        // ✅ 완전히 새로운 사용자 → Users와 AuthAccount 모두 생성
        Users user = Users.builder()
            .email(email)
            .name(name != null ? name : "사용자")
            .password(null)  // 소셜 로그인은 패스워드 없음
            .role(Role.USER)
            .isActive(true)
            .profileUrl(profileImageUrl)
            .build();
        user = userRepository.save(user);
        
        AuthAccount authAccount = AuthAccount.builder()
            .user(user)
            .provider(provider)
            .socialToken(userRequest.getAccessToken().getTokenValue())
            .externalId(externalId)
            .build();
        authAccountRepository.save(authAccount);
    }
}
```

**데이터베이스 구조:**

| 테이블 | 컬럼 | 설명 |
|--------|------|------|
| **users** | `users_id` | 사용자 ID (PK) |
| | `email` | 이메일 (UNIQUE) |
| | `name` | 이름/닉네임 |
| | `password` | 비밀번호 (소셜 로그인은 NULL) |
| | `role` | 역할 (USER/ADMIN) |
| | `is_active` | 활성화 여부 |
| | `profile_url` | 프로필 이미지 URL |
| **auth_account** | `social_account_id` | 소셜 계정 ID (PK) |
| | `users_id` | 사용자 ID (FK → users) |
| | `provider` | 제공자 (GENERAL/GOOGLE/KAKAO) |
| | `external_id` | 소셜 계정 고유 ID |
| | `social_token` | 소셜 OAuth2 Access Token |

---

### 6단계: JWT 토큰 생성 및 프론트엔드 리다이렉트

**OAuth2SuccessHandler.onAuthenticationSuccess() 실행:**

```java
@Override
public void onAuthenticationSuccess(...) {
    // 1. OAuth2User에서 이메일 추출
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getName();  // CustomOAuth2User에서 email 반환
    
    // 2. 권한 정보 가져오기
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    
    // 3. Authentication 객체 생성
    UsernamePasswordAuthenticationToken authToken = 
        new UsernamePasswordAuthenticationToken(email, null, authorities);
    
    // 4. JWT 토큰 생성
    String accessToken = jwtTokenProvider.generateToken(authToken);
    // 토큰 내용: {email, role, iat, exp}
    
    // 5. 프론트엔드로 리다이렉트 (토큰 포함)
    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("token", accessToken)
        .build()
        .encode(StandardCharsets.UTF_8)
        .toUriString();
    
    // 리다이렉트 실행
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
}
```

**JWT 토큰 생성 (JwtTokenProvider.generateToken()):**
```java
public String generateToken(Authentication authentication) {
    String email = authentication.getName();
    
    return Jwts.builder()
        .setSubject(email)                                    // 이메일
        .claim("role", authentication.getAuthorities()        // 역할
            .iterator().next().getAuthority())
        .setIssuedAt(new Date())                              // 발급 시간
        .setExpiration(new Date(                              // 만료 시간
            System.currentTimeMillis() + jwtProperties.getExpiration().getAccess()))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 서명
        .compact();
}
```

**프론트엔드 리다이렉트 URL:**
```
http://localhost:3000/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MDI2NjU2MDAsImV4cCI6MTcwMjY2OTIwMH0....
```

**프론트엔드 처리 예시 (React):**
```javascript
// URL에서 토큰 추출
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

if (token) {
    // 토큰 저장
    localStorage.setItem('accessToken', token);
    
    // 메인 페이지로 이동
    window.location.href = '/';
}
```

---

## 데이터베이스 처리 로직

### 시나리오별 처리

#### 시나리오 1: 완전히 새로운 소셜 로그인 사용자
```
1. users 테이블에 새 레코드 생성
   - email: "user@gmail.com"
   - name: "사용자 이름"
   - password: NULL
   - role: "USER"
   - is_active: true
   - profile_url: "https://..."

2. auth_account 테이블에 새 레코드 생성
   - users_id: 1 (위에서 생성된 사용자 ID)
   - provider: "GOOGLE"
   - external_id: "1234567890"
   - social_token: "ya29.a0..."
```

#### 시나리오 2: 기존 소셜 로그인 사용자 (같은 Provider)
```
1. auth_account 테이블에서 provider와 external_id로 찾기
   ✅ 찾음 → 기존 사용자

2. users 테이블의 정보 업데이트 (필요시)
   - name, profile_url 업데이트 가능

3. auth_account 테이블의 social_token 업데이트
   - 최신 Access Token으로 갱신
```

#### 시나리오 3: 일반 회원가입 했던 사용자가 소셜 로그인
```
1. users 테이블에서 email로 찾기
   ✅ 찾음 → "user@example.com"으로 이미 가입된 사용자

2. auth_account 테이블에 새 레코드 추가
   - users_id: 기존 사용자 ID
   - provider: "GOOGLE" (또는 "KAKAO")
   - external_id: "1234567890"
   - social_token: "ya29.a0..."

결과: 하나의 users 레코드에 여러 auth_account 연결 가능
```

#### 시나리오 4: 여러 Provider 사용 (구글 + 카카오)
```
users 테이블:
  - users_id: 1
  - email: "user@example.com"
  - name: "사용자"
  ...

auth_account 테이블:
  - 레코드 1: provider=GOOGLE, external_id="google123"
  - 레코드 2: provider=KAKAO, external_id="kakao456"
  
✅ 한 사용자가 여러 소셜 계정 연결 가능
```

---

## 주요 컴포넌트 역할

### 1. SecurityConfig
- **역할**: Spring Security 전체 설정
- **주요 기능**:
  - OAuth2 로그인 설정
  - 허용 경로 설정 (`/oauth2/**`, `/login/oauth2/**`)
  - OAuth2UserService, SuccessHandler, FailureHandler 연결

### 2. OAuth2UserServiceImpl
- **역할**: 소셜 로그인 사용자 정보 처리
- **주요 기능**:
  - 소셜 계정 정보 추출 (이메일, 이름, 프로필 이미지 등)
  - DB에서 사용자 찾기/생성/업데이트
  - CustomOAuth2User 반환 (JWT 토큰 생성에 사용)

### 3. OAuth2SuccessHandler
- **역할**: 소셜 로그인 성공 시 처리
- **주요 기능**:
  - JWT 토큰 생성
  - 프론트엔드로 리다이렉트 (토큰 포함)

### 4. OAuth2FailureHandler
- **역할**: 소셜 로그인 실패 시 처리
- **주요 기능**:
  - 에러 메시지 생성
  - 프론트엔드로 리다이렉트 (에러 포함)

### 5. JwtTokenProvider
- **역할**: JWT 토큰 생성 및 검증
- **주요 기능**:
  - Access Token 생성 (이메일, 역할 포함)
  - 토큰 검증
  - 토큰에서 Authentication 객체 생성

### 6. AuthAccountRepository
- **역할**: auth_account 테이블 데이터 접근
- **주요 메서드**:
  - `findByProviderAndExternalId()`: Provider와 External ID로 찾기
  - `findByUserAndProvider()`: 사용자와 Provider로 찾기
  - `findByUser()`: 사용자의 모든 소셜 계정 찾기

---

## 시퀀스 다이어그램

```
┌─────────┐      ┌──────────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
│ 사용자  │      │ 프론트엔드    │      │ 백엔드    │      │ 구글/카카오│     │ 데이터베이스│
└────┬────┘      └──────┬───────┘      └────┬─────┘      └────┬─────┘      └────┬─────┘
     │                  │                    │                  │                  │
     │ 1. 소셜 로그인 클릭│                    │                  │                  │
     ├──────────────────>│                    │                  │                  │
     │                  │                    │                  │                  │
     │                  │ 2. GET /oauth2/authorization/google │                  │                  │
     │                  ├───────────────────────────────────>│                  │                  │
     │                  │                    │                  │                  │
     │                  │                    │ 3. Authorization 요청│                  │                  │
     │                  │                    ├───────────────────────────────────>│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │                  │ 4. 로그인 페이지 표시│                  │
     │                  │                    │                  │<───────────────────│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │                  │ 5. 사용자 로그인 및 동의│                  │
     │                  │                    │                  │<───────────────────│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │                  │ 6. Authorization Code 발급│                  │
     │                  │                    │                  │───────────────────>│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │ 7. Authorization Code 수신│                  │                  │
     │                  │                    │<───────────────────────────────────│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │ 8. Access Token 교환│                  │                  │
     │                  │                    │───────────────────────────────────>│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │ 9. Access Token 발급│                  │                  │
     │                  │                    │<───────────────────────────────────│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │ 10. 사용자 정보 조회│                  │                  │
     │                  │                    │───────────────────────────────────>│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │ 11. 사용자 정보 반환│                  │                  │
     │                  │                    │<───────────────────────────────────│                  │
     │                  │                    │                  │                  │                  │
     │                  │                    │ 12. OAuth2UserServiceImpl.loadUser()│                  │
     │                  │                    │───────────────────────────────────────────────────>│
     │                  │                    │                  │                  │ 13. 사용자 찾기/생성│
     │                  │                    │                  │                  │<───────────────────│
     │                  │                    │                  │                  │                  │
     │                  │                    │                  │                  │ 14. DB 저장/업데이트│
     │                  │                    │                  │                  │───────────────────>│
     │                  │                    │                  │                  │                  │
     │                  │                    │                  │                  │ 15. 저장 완료│
     │                  │                    │                  │                  │<───────────────────│
     │                  │                    │                  │                  │                  │
     │                  │                    │ 16. CustomOAuth2User 반환│                  │                  │
     │                  │                    │<───────────────────────────────────────────────────│
     │                  │                    │                  │                  │                  │
     │                  │                    │ 17. OAuth2SuccessHandler 실행│                  │                  │
     │                  │                    │───────────────────────────────────────────────────>│
     │                  │                    │                  │                  │                  │
     │                  │                    │ 18. JWT 토큰 생성│                  │                  │
     │                  │                    │<───────────────────────────────────────────────────│
     │                  │                    │                  │                  │                  │
     │                  │                    │ 19. 리다이렉트 (토큰 포함)│                  │                  │
     │                  │<───────────────────────────────────────────────────────────────────────│
     │                  │                    │                  │                  │                  │
     │ 20. 토큰 받음│                  │                    │                  │                  │
     │<─────────────────────────────────────────────────────────────────────────────────────────│
     │                  │                    │                  │                  │                  │
```

---

## 주요 특징

### ✅ 자동 회원가입
- 소셜 로그인 시 자동으로 회원가입 처리
- 별도의 회원가입 페이지 불필요

### ✅ 기존 사용자 연동
- 같은 이메일로 일반 회원가입한 경우 기존 계정에 소셜 계정 연결

### ✅ 다중 Provider 지원
- 한 사용자가 구글과 카카오 모두 사용 가능
- `auth_account` 테이블에서 여러 레코드로 관리

### ✅ 통합 JWT 토큰
- 일반 로그인과 소셜 로그인 모두 동일한 JWT 토큰 형식
- 이후 API 요청 시 동일한 방식으로 인증

### ✅ 최신 정보 유지
- 소셜 로그인 시 이름, 프로필 이미지 자동 업데이트
- 소셜 Access Token 최신화

---

## 요약

1. **사용자가 소셜 로그인 버튼 클릭** → `/oauth2/authorization/{provider}`
2. **Spring Security가 OAuth2 플로우 자동 처리** → 구글/카카오로 리다이렉트
3. **사용자가 구글/카카오에서 로그인 및 동의** → Authorization Code 발급
4. **OAuth2UserServiceImpl이 사용자 정보 조회 및 DB 처리**
   - 기존 사용자 확인
   - 신규 사용자 생성 또는 기존 사용자에 연결
5. **OAuth2SuccessHandler가 JWT 토큰 생성 및 리다이렉트**
   - 토큰을 URL 파라미터로 프론트엔드에 전달
6. **프론트엔드에서 토큰 저장 및 로그인 완료**

전체 과정이 Spring Security OAuth2 Client에 의해 자동으로 처리되며, 우리가 구현한 부분은 **사용자 정보 처리**와 **JWT 토큰 생성**입니다.

