# OAuth2 소셜 계정 연결 시스템

## 개요

본 문서는 FitLink 백엔드에서 다중 소셜 계정(네이버, 카카오, 구글 등)을 하나의 사용자 계정에 연결하는 방식과 현재 구현 상태를 설명합니다.

## 현재 시스템 아키텍처

### 데이터베이스 구조

#### 1. Users 테이블
- **Primary Key**: `users_id` (Long)
- **Unique Key**: `email` (String)
- 사용자의 기본 정보를 저장하는 메인 테이블
- 한 사용자는 하나의 `Users` 레코드를 가짐

#### 2. AuthAccount 테이블
- **Primary Key**: `social_account_id` (Long)
- **Foreign Key**: `users_id` → `Users.users_id`
- **필드**:
  - `provider`: 소셜 로그인 제공자 (GOOGLE, KAKAO, NAVER 등)
  - `external_id`: 소셜 로그인 제공자에서 발급한 고유 ID
  - `social_token`: OAuth2 Access Token
- 한 사용자는 여러 개의 `AuthAccount`를 가질 수 있음 (1:N 관계)

### 데이터베이스 관계

```
Users (1) ──< (N) AuthAccount
```

- 하나의 `Users`는 여러 개의 `AuthAccount`를 가질 수 있음
- 각 `AuthAccount`는 반드시 하나의 `Users`에 속함
- 예시:
  ```
  Users: {id: 1, email: "user@example.com", name: "홍길동"}
  ├── AuthAccount: {provider: NAVER, external_id: "12345", user_id: 1}
  ├── AuthAccount: {provider: KAKAO, external_id: "67890", user_id: 1}
  └── AuthAccount: {provider: GOOGLE, external_id: "abcde", user_id: 1}
  ```

## 코드 흐름

### 전체 OAuth2 로그인 플로우

```
[프론트엔드]
1. 사용자가 "카카오 로그인" 버튼 클릭
   → GET /oauth2/authorization/kakao

[백엔드 - Spring Security Filter Chain]
2. OAuth2AuthorizationRequestRedirectFilter
   → OAuth2 인가 요청 생성 (authorization URL, state, scope 등)
   → 소셜 로그인 제공자(카카오)로 리다이렉트

[카카오/구글]
3. 사용자 인증 및 동의
   → 사용자가 카카오에서 로그인 및 권한 동의
   → GET /login/oauth2/code/kakao?code={authorization_code}&state={state}

[백엔드 - Spring Security Filter Chain]
4. OAuth2LoginAuthenticationFilter
   → Authorization Code로 Access Token 교환
   → OAuth2TokenExchangeLogger.getTokenResponse() 호출
     - 카카오: 직접 토큰 요청 수행 (client_secret 포함)
     - 구글: 기본 구현 사용

5. OAuth2UserService.loadUser() 호출
   → OAuth2UserServiceImpl.loadUser() 실행
     - Provider + ExternalId로 AuthAccount 조회
     - 이메일로 Users 조회 또는 생성
     - AuthAccount 생성 또는 업데이트
   → CustomOAuth2User 반환

6. OAuth2SuccessHandler.onAuthenticationSuccess()
   → JWT 토큰 생성 (JwtTokenProvider.generateToken())
   → 프론트엔드로 리다이렉트: {frontendUrl}/oauth2/redirect?token={jwt}

[프론트엔드]
7. 토큰 저장 및 인증 완료
   → localStorage/sessionStorage에 JWT 토큰 저장
   → 인증된 상태로 앱 사용
```

### 핵심 메서드 상세 흐름

#### 1. `OAuth2UserServiceImpl.loadUser()`

**코드 위치**: `src/main/java/com/fitlink/service/OAuth2UserServiceImpl.java:41-156`

```
입력: OAuth2UserRequest (OAuth2 Access Token 포함)

1. Provider 정보 추출
   → registrationId (google/kakao)로 Provider enum 변환
   
2. OAuth2 사용자 정보 조회
   → DefaultOAuth2UserService.loadUser() 호출
   → email, name, profileImageUrl, externalId 추출
   
3. 이메일 처리
   IF email == null OR email.isBlank():
      IF provider == KAKAO:
         → 임시 이메일 생성: kakao_{externalId}@kakao.fitlink
         → needsEmailUpdate = true
      ELSE:
         → OAuth2AuthenticationException throw (이메일 필수)
   
4. 기존 AuthAccount 조회
   → authAccountRepository.findByProviderAndExternalId(provider, externalId)
   
   IF AuthAccount 존재:
      → 기존 Users 조회 (authAccount.getUser())
      → 사용자 정보 업데이트 (이름, 프로필 이미지)
      → AuthAccount의 social_token 업데이트
      → CustomOAuth2User 반환
   
   ELSE:
      5-1. 이메일로 기존 Users 조회
           → userRepository.findByEmail(email)
           
           IF Users 존재:
              → 기존 Users에 새 AuthAccount 연결
              → AuthAccount 생성 및 저장
              → CustomOAuth2User 반환
              
           ELSE:
              5-2. 새로운 Users 생성
                   → Users.builder()
                       .email(email)
                       .name(name != null ? name : "사용자")
                       .password(null)
                       .role(USER)
                       .isActive(true)
                       .profileUrl(profileImageUrl)
                   → userRepository.save(user)
                   → entityManager.flush()
               
               5-3. AuthAccount 생성
                   → AuthAccount.builder()
                       .user(user)
                       .provider(provider)
                       .externalId(externalId)
                       .socialToken(accessToken)
                   → authAccountRepository.save(authAccount)
               
               → CustomOAuth2User 반환 (needsEmailUpdate 플래그 포함)

출력: CustomOAuth2User (OAuth2User 구현체)
```

#### 2. `UserServiceImpl.updateEmail()`

**코드 위치**: `src/main/java/com/fitlink/service/UserServiceImpl.java:108-169`

```
입력: userId (Long), UpdateEmailDTO (email: String)

1. 현재 Users 조회
   → userRepository.findById(userId)
   → 없으면: USER_NOT_FOUND 예외

2. 이메일 변경 확인
   IF 현재 이메일 == 새 이메일:
      → 현재 Users 반환 (변경 없음)

3. 기존 Users 확인
   → userRepository.findByEmail(request.getEmail())
   
   IF 기존 Users 존재:
      3-1. 임시 카카오 이메일 확인
           → isTemporaryKakaoEmail(currentUser.getEmail())
           → 패턴: kakao_\\d+@kakao\\.fitlink
           
           IF 임시 이메일:
              3-2. 카카오 AuthAccount 찾기
                   → authAccountRepository.findByUserAndProvider(currentUser, KAKAO)
                   
                   IF 카카오 AuthAccount 존재:
                      3-3. AuthAccount 소유권 확인
                           → 이미 다른 사용자에 연결되어 있으면 예외
                      
                      3-4. 현재 Users의 모든 AuthAccount 확인
                           → authAccountRepository.findByUser(currentUser)
                      
                      3-5. 카카오 AuthAccount를 기존 Users에 연결
                           → kakaoAuthAccount.setUser(existingUser)
                           → authAccountRepository.save(kakaoAuthAccount)
                      
                      3-6. 임시 이메일 Users 삭제
                           IF AuthAccount가 1개만 있으면:
                              → userRepository.delete(currentUser)
                           ELSE:
                              → 현재 Users 유지 (다른 AuthAccount 있음)
                      
                      → 기존 Users 반환
                  
                  ELSE:
                      → DUPLICATE_EMAIL 예외
           
           ELSE:
              → DUPLICATE_EMAIL 예외 (일반 사용자)
   
   ELSE:
      4. 새 이메일로 업데이트
          → currentUser.setEmail(request.getEmail())
          → 현재 Users 반환

출력: Users (이메일이 업데이트되었거나 기존 Users에 연결된 Users)
```

#### 3. `OAuth2SuccessHandler.onAuthenticationSuccess()`

**코드 위치**: `src/main/java/com/fitlink/config/security/handler/OAuth2SuccessHandler.java:34-79`

```
입력: HttpServletRequest, HttpServletResponse, Authentication

1. OAuth2User 정보 추출
   → authentication.getPrincipal() → OAuth2User
   → email = oAuth2User.getName()
   → needsEmailUpdate = oAuth2User.getAttribute("needsEmailUpdate")

2. JWT 토큰 생성
   → UsernamePasswordAuthenticationToken 생성
     - principal: email
     - authorities: OAuth2User의 authorities
   → jwtTokenProvider.generateToken(authToken)

3. 프론트엔드 리다이렉트 URL 생성
   → UriComponentsBuilder.fromUriString(redirectUri)
     .queryParam("token", accessToken)
   
   IF needsEmailUpdate == true:
      → .queryParam("needsEmailUpdate", true)

4. 리다이렉트
   → response.sendRedirect(targetUrl)
   → 프론트엔드: {frontendUrl}/oauth2/redirect?token={jwt}&needsEmailUpdate=true

예외 처리:
   → 예외 발생 시 에러 URL로 리다이렉트
   → {frontendUrl}/oauth2/redirect?error=oauth2_processing_error&message={error}
```

#### 4. `OAuth2TokenExchangeLogger.getTokenResponse()`

**코드 위치**: `src/main/java/com/fitlink/config/security/OAuth2TokenExchangeLogger.java:159-206`

```
입력: OAuth2AuthorizationCodeGrantRequest

1. Provider 확인
   → registrationId 추출 (google/kakao)
   
   IF kakao:
      2-1. Code 재사용 체크
           → checkCodeReuse(code)
           → ConcurrentHashMap에서 이전 사용 기록 확인
           → 재사용 감지 시 에러 로깅
      
      2-2. 카카오 토큰 요청 수행
           → performKakaoTokenRequest(request)
             - RequestEntity 생성 (client_secret 포함)
             - RestTemplate.exchange() 호출
             - 응답을 OAuth2AccessTokenResponse로 변환
      
      2-3. Code 사용 표시
           → markCodeAsUsed(code)
           → ConcurrentHashMap에 저장
      
      → OAuth2AccessTokenResponse 반환
   
   ELSE (google 등):
      2-4. 기본 구현 사용
           → delegateForGoogle.getTokenResponse(request)
           → Spring Security 기본 구현으로 처리

출력: OAuth2AccessTokenResponse (Access Token, Refresh Token 등)
```

## 현재 연결 로직

### OAuth2 로그인 프로세스

`OAuth2UserServiceImpl.loadUser()` 메서드에서 다음 순서로 처리됩니다:

```java
1. Provider + ExternalId로 기존 AuthAccount 조회
   ↓ (존재함)
   기존 사용자 인식 → 사용자 정보 업데이트 → 토큰 갱신

   ↓ (존재하지 않음)

2. 이메일로 기존 Users 조회
   ↓ (존재함)
   기존 Users에 새 AuthAccount 연결

   ↓ (존재하지 않음)

3. 새로운 Users 생성 → 새 AuthAccount 생성
```

### 상세 처리 흐름

#### 시나리오 1: 기존 소셜 계정으로 로그인

```
1. 사용자가 카카오로 로그인 시도
2. OAuth2UserServiceImpl.loadUser() 호출
3. authAccountRepository.findByProviderAndExternalId(KAKAO, "12345") 조회
4. 기존 AuthAccount 발견
   → 기존 Users 객체 반환
   → AuthAccount의 social_token 업데이트
   → 사용자 정보(이름, 프로필 이미지) 업데이트
```

**코드 위치**: `OAuth2UserServiceImpl.java:69-90`

#### 시나리오 2: 새로운 소셜 계정으로 로그인 (이메일 매칭)

```
1. 사용자가 네이버로 가입 (user@example.com)
   → Users 생성: {id: 1, email: "user@example.com"}
   → AuthAccount 생성: {provider: NAVER, user_id: 1}

2. 같은 이메일로 카카오 로그인 시도 (user@example.com)
   → authAccountRepository.findByProviderAndExternalId(KAKAO, "67890") → 없음
   → userRepository.findByEmail("user@example.com") → Users 발견
   → 기존 Users(id: 1)에 새 AuthAccount 연결
   → AuthAccount 생성: {provider: KAKAO, user_id: 1}
```

**코드 위치**: `OAuth2UserServiceImpl.java:91-141`

#### 시나리오 3: 완전히 새로운 사용자

```
1. 새로운 이메일로 구글 로그인 시도 (newuser@gmail.com)
   → authAccountRepository.findByProviderAndExternalId(GOOGLE, "xyz") → 없음
   → userRepository.findByEmail("newuser@gmail.com") → 없음
   → 새로운 Users 생성: {id: 2, email: "newuser@gmail.com"}
   → AuthAccount 생성: {provider: GOOGLE, user_id: 2}
```

**코드 위치**: `OAuth2UserServiceImpl.java:99-121`

## 이메일 기반 매칭의 한계

### 문제점 1: 이메일이 없는 경우 (카카오)

카카오는 사용자가 이메일 제공을 거부할 수 있습니다. 이 경우 시스템은 임시 이메일을 생성합니다:

```java
// OAuth2UserServiceImpl.java:163-168
private String generateTemporaryEmail(Provider provider, String externalId) {
    return String.format("%s_%s@%s.fitlink", 
            provider.name().toLowerCase(), 
            externalId, 
            provider.name().toLowerCase());
}
```

**예시**:
- 네이버 가입: `user@naver.com` → Users 생성
- 카카오 로그인 (이메일 없음): `kakao_67890@kakao.fitlink` → **새로운 Users 생성** ❌

### 문제점 2: 설정에서 소셜 계정 추가 불가

현재 시스템은 **OAuth2 로그인 시점에만** 자동으로 연결됩니다. 이미 로그인한 상태에서 "설정 > 소셜 계정 연결" 메뉴를 통해 추가하는 기능이 없습니다.

**시나리오**:
```
1. 사용자가 네이버로 가입 및 로그인 완료
2. 설정 페이지에서 "카카오 계정 연결" 버튼 클릭
3. 카카오 OAuth2 인증 진행
4. 현재 시스템: 새로운 Users 생성 또는 연결 실패 ❌
5. 기대 동작: 현재 로그인한 Users에 카카오 AuthAccount 추가 ✅
```

### 문제점 3: 이메일이 다른 경우 연결 불가

사용자가 네이버에는 `user@naver.com`, 카카오에는 `user2@gmail.com`을 사용하는 경우, 자동으로 연결되지 않습니다.

## 개선 방안

### 방안 1: 명시적 연결 API 추가 (권장)

이미 로그인한 사용자가 설정에서 소셜 계정을 추가할 수 있는 API를 제공합니다.

#### API 스펙

**엔드포인트**: `POST /api/users/social/connect`

**요청**:
```json
{
  "provider": "KAKAO",
  "authorizationCode": "카카오에서 받은 인증 코드",
  "redirectUri": "콜백 URI"
}
```

**처리 로직**:
1. 현재 로그인한 사용자(`@AuthenticationPrincipal`) 확인
2. OAuth2 토큰 교환 (authorization code → access token)
3. Provider + ExternalId로 이미 다른 사용자에 연결되어 있는지 확인
4. 현재 사용자에 `AuthAccount` 추가

**예상 코드 구조**:
```java
@PostMapping("/api/users/social/connect")
public ResponseEntity<?> connectSocialAccount(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestBody ConnectSocialAccountRequest request
) {
    Users currentUser = userDetails.getUser();
    
    // 1. OAuth2 토큰 교환 및 사용자 정보 조회
    OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
    String externalId = getExternalId(oAuth2User, request.getProvider());
    
    // 2. 이미 연결된 계정인지 확인
    Optional<AuthAccount> existing = authAccountRepository
        .findByProviderAndExternalId(request.getProvider(), externalId);
    
    if (existing.isPresent()) {
        if (existing.get().getUser().equals(currentUser)) {
            return ResponseEntity.ok("이미 연결된 계정입니다.");
        } else {
            throw new GeneralException(ErrorStatus._SOCIAL_ACCOUNT_ALREADY_LINKED);
        }
    }
    
    // 3. 현재 사용자에 연결
    AuthAccount authAccount = AuthAccount.builder()
        .user(currentUser)
        .provider(request.getProvider())
        .externalId(externalId)
        .socialToken(accessToken)
        .build();
    
    authAccountRepository.save(authAccount);
    
    return ResponseEntity.ok("소셜 계정 연결 완료");
}
```

### 방안 2: 로그인 시 확인 절차 추가

새로운 소셜 계정으로 로그인할 때, 이미 다른 이메일로 가입된 사용자가 있다면 연결 확인 절차를 거칩니다.

#### 처리 흐름

```
1. 카카오로 로그인 시도 (이메일 없음)
2. authAccountRepository.findByProviderAndExternalId() → 없음
3. userRepository.findByEmail() → 없음 (임시 이메일)
4. 세션/쿠키에서 기존 로그인 정보 확인
   → 기존 로그인 세션 발견
   → "이 카카오 계정을 기존 계정에 연결하시겠습니까?" 확인
   → 사용자 확인 시 기존 Users에 연결
```

### 방안 3: 이메일 매칭 개선

이메일이 다르더라도 추가 확인 절차를 통해 연결할 수 있도록 합니다.

#### 처리 흐름

```
1. 네이버로 가입 (user@naver.com)
2. 카카오로 로그인 시도 (user2@gmail.com)
3. 이메일이 다르므로 자동 연결하지 않음
4. 추가 정보 확인 (전화번호, 생년월일 등) 또는 사용자 확인
5. 사용자 확인 시 기존 Users에 연결
```

## 현재 구현 상태

### 구현된 기능

✅ **OAuth2 로그인 시 자동 계정 연결**
- 같은 이메일을 사용하는 소셜 계정은 자동으로 하나의 `Users`에 연결됨
- 코드 위치: `OAuth2UserServiceImpl.java:69-141`

✅ **다중 소셜 계정 관리**
- 한 사용자가 여러 개의 `AuthAccount`를 가질 수 있음
- 각 소셜 계정의 토큰을 개별적으로 관리

✅ **기존 소셜 계정 인식**
- Provider + ExternalId로 기존 계정을 식별하고 업데이트

### 미구현 기능

❌ **설정에서 소셜 계정 추가**
- 이미 로그인한 상태에서 새로운 소셜 계정을 추가하는 API 없음

❌ **이메일 없는 경우 연결**
- 카카오처럼 이메일이 없는 경우, 임시 이메일로 인해 다른 `Users`로 생성됨. 추가로 이메일을 받았을 때 해당 이메일의 유저 계정으로 자동 통합되도록 처리.

❌ **이메일이 다른 경우 연결**
- 다른 이메일을 사용하는 소셜 계정은 자동 연결되지 않음

❌ **소셜 계정 연결 해제**
- 연결된 소셜 계정을 해제하는 API 없음

## Repository 메서드

### AuthAccountRepository

```java
// Provider와 ExternalId로 조회 (소셜 로그인용)
Optional<AuthAccount> findByProviderAndExternalId(Provider provider, String externalId);

// User와 Provider로 조회 (사용자의 특정 Provider 계정 조회)
Optional<AuthAccount> findByUserAndProvider(Users user, Provider provider);

// User로 모든 AuthAccount 조회
List<AuthAccount> findByUser(Users user);
```

### UserRepository

```java
// 이메일로 Users 조회
Optional<Users> findByEmail(String email);
```

## 참고 파일

- `src/main/java/com/fitlink/service/OAuth2UserServiceImpl.java`: 소셜 로그인 처리 로직
- `src/main/java/com/fitlink/domain/Users.java`: Users 엔티티
- `src/main/java/com/fitlink/domain/AuthAccount.java`: AuthAccount 엔티티
- `src/main/java/com/fitlink/repository/AuthAccountRepository.java`: AuthAccount Repository
- `src/main/java/com/fitlink/repository/UserRepository.java`: User Repository

## 결론

현재 시스템은 **이메일 기반 자동 연결**을 통해 기본적인 다중 소셜 계정 관리를 지원합니다. 하지만 설정에서 소셜 계정을 추가하는 명시적 API가 없어, 사용자가 추가 소셜 계정을 연결하려면 해당 소셜 계정으로 **새로 로그인**해야 합니다.

이메일이 없거나 다른 경우에는 자동 연결이 되지 않으므로, 향후 **명시적 연결 API**를 추가하여 이러한 제약을 해결하는 것을 권장합니다.

