# OAuth2 소셜 로그인 테스트 가이드

## 📋 목차
1. [테스트 환경 준비](#테스트-환경-준비)
2. [백엔드 서버 실행](#백엔드-서버-실행)
3. [구글 로그인 테스트](#구글-로그인-테스트)
4. [카카오 로그인 테스트](#카카오-로그인-테스트)
5. [시나리오별 테스트](#시나리오별-테스트)
6. [문제 해결](#문제-해결)

---

## 테스트 환경 준비

### 1. 백엔드 설정 확인

#### `application.properties` 확인
- ✅ 구글 OAuth2 클라이언트 ID 및 Secret 설정 확인
- ✅ 카카오 OAuth2 REST API 키 및 Client Secret 설정 확인
- ✅ OAuth2 리다이렉트 URI 확인: `https://fit-link-fe.vercel.app/oauth2/redirect`

### 2. 데이터베이스 확인
- ✅ MySQL 서버 실행 확인
- ✅ 데이터베이스 연결 확인
- ✅ 테이블 자동 생성 확인 (`ddl-auto=update`)

### 3. OAuth2 콘솔 설정 확인

#### 구글 OAuth2 콘솔
- ✅ 승인된 리디렉션 URI 확인:
  - `https://www.fitlink1207.store/login/oauth2/code/google`
  - (로컬 테스트용) `http://localhost:8080/login/oauth2/code/google`

#### 카카오 OAuth2 콘솔
- ✅ Redirect URI 확인:
  - `https://www.fitlink1207.store/login/oauth2/code/kakao`
  - (로컬 테스트용) `http://localhost:8080/login/oauth2/code/kakao`

---

## 백엔드 서버 실행

### 1. 로컬 환경에서 실행

```bash
# 프로젝트 루트 디렉토리에서
./gradlew bootRun

# 또는 IDE에서 FitLinkApplication 실행
```

### 2. 서버 실행 확인

```bash
# Health Check API 호출
curl http://localhost:8080/api/health

# 또는 브라우저에서
http://localhost:8080/api/health
```

**예상 응답:**
```json
{
  "status": "OK",
  "message": "Server is running"
}
```

---

## 구글 로그인 테스트

### 방법 1: 브라우저에서 직접 테스트

#### 1단계: OAuth2 인증 URL 접속

**프로덕션 서버:**
```
https://www.fitlink1207.store/oauth2/authorization/google
```

**로컬 서버:**
```
http://localhost:8080/oauth2/authorization/google
```

#### 2단계: 구글 로그인 진행
1. 브라우저에서 위 URL 접속
2. 구글 로그인 페이지로 리다이렉트됨
3. 구글 계정 선택 및 로그인
4. 애플리케이션 권한 동의
5. 백엔드로 리다이렉트 → 프론트엔드로 최종 리다이렉트

#### 3단계: 리다이렉트 URL 확인

**예상 리다이렉트 URL:**
```
https://fit-link-fe.vercel.app/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9...
```

### 방법 2: Postman에서 테스트

#### 1단계: GET 요청 보내기

**Request:**
```
GET http://localhost:8080/oauth2/authorization/google
```

**Settings:**
- Method: `GET`
- URL: `http://localhost:8080/oauth2/authorization/google`
- **중요:** "Follow redirects" 옵션 활성화

#### 2단계: 리다이렉트 확인
- Postman이 자동으로 리다이렉트를 따라가며 최종 URL 확인
- URL 파라미터에서 `token` 확인

### 방법 3: curl로 테스트

```bash
# OAuth2 인증 시작
curl -v http://localhost:8080/oauth2/authorization/google

# 리다이렉트 URL 확인
# Location 헤더에서 확인 가능
```

---

## 카카오 로그인 테스트

### 방법 1: 브라우저에서 직접 테스트

#### 1단계: OAuth2 인증 URL 접속

**프로덕션 서버:**
```
https://www.fitlink1207.store/oauth2/authorization/kakao
```

**로컬 서버:**
```
http://localhost:8080/oauth2/authorization/kakao
```

#### 2단계: 카카오 로그인 진행
1. 브라우저에서 위 URL 접속
2. 카카오 로그인 페이지로 리다이렉트됨
3. 카카오 계정 로그인
4. 애플리케이션 권한 동의 (이메일, 닉네임 등)
5. 백엔드로 리다이렉트 → 프론트엔드로 최종 리다이렉트

#### 3단계: 리다이렉트 URL 확인

**이메일이 있는 경우:**
```
https://fit-link-fe.vercel.app/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9...
```

**이메일이 없는 경우 (임시 이메일 생성됨):**
```
https://fit-link-fe.vercel.app/oauth2/redirect?token=eyJhbGciOiJIUzI1NiJ9...&needsEmailUpdate=true
```

### 방법 2: Postman에서 테스트

**Request:**
```
GET http://localhost:8080/oauth2/authorization/kakao
```

**Settings:**
- Method: `GET`
- URL: `http://localhost:8080/oauth2/authorization/kakao`
- "Follow redirects" 옵션 활성화

---

## 시나리오별 테스트

### 시나리오 1: 신규 구글 로그인 사용자

**테스트 단계:**
1. 구글 로그인 URL 접속
2. 구글 계정으로 로그인
3. 권한 동의

**확인 사항:**
- ✅ `users` 테이블에 새 사용자 생성 확인
- ✅ `auth_account` 테이블에 `provider=GOOGLE` 레코드 생성 확인
- ✅ JWT 토큰 발급 확인
- ✅ 프론트엔드로 리다이렉트 확인

**SQL 확인:**
```sql
-- users 테이블 확인
SELECT * FROM users WHERE email = 'google_user@gmail.com';

-- auth_account 테이블 확인
SELECT * FROM auth_account WHERE provider = 'GOOGLE';
```

### 시나리오 2: 신규 카카오 로그인 사용자 (이메일 있음)

**테스트 단계:**
1. 카카오 로그인 URL 접속
2. 카카오 계정으로 로그인 (이메일 동의)
3. 권한 동의

**확인 사항:**
- ✅ `users` 테이블에 새 사용자 생성 확인
- ✅ `auth_account` 테이블에 `provider=KAKAO` 레코드 생성 확인
- ✅ 실제 카카오 이메일로 저장 확인
- ✅ JWT 토큰 발급 확인
- ✅ `needsEmailUpdate` 파라미터 없는지 확인

### 시나리오 3: 신규 카카오 로그인 사용자 (이메일 없음)

**테스트 단계:**
1. 카카오 로그인 URL 접속
2. 카카오 계정으로 로그인 (이메일 동의 안 함)
3. 권한 동의

**확인 사항:**
- ✅ `users` 테이블에 새 사용자 생성 확인
- ✅ 임시 이메일 형식: `kakao_{externalId}@kakao.fitlink` 확인
- ✅ `auth_account` 테이블에 `provider=KAKAO` 레코드 생성 확인
- ✅ JWT 토큰 발급 확인
- ✅ **프론트엔드 리다이렉트 URL에 `needsEmailUpdate=true` 포함 확인**

**예상 리다이렉트 URL:**
```
https://fit-link-fe.vercel.app/oauth2/redirect?token=...&needsEmailUpdate=true
```

### 시나리오 4: 기존 소셜 로그인 사용자 재로그인

**테스트 단계:**
1. 이미 로그인한 적이 있는 소셜 계정으로 다시 로그인

**확인 사항:**
- ✅ 새 사용자 생성 안 됨 (기존 사용자 재사용)
- ✅ `users` 테이블의 정보 업데이트 (이름, 프로필 이미지)
- ✅ `auth_account` 테이블의 `social_token` 업데이트
- ✅ JWT 토큰 발급 확인

### 시나리오 5: 일반 회원가입 후 소셜 로그인

**테스트 단계:**
1. 일반 회원가입 (`/api/user/join`)
2. 같은 이메일로 소셜 로그인

**확인 사항:**
- ✅ 기존 `users` 레코드 재사용
- ✅ `auth_account` 테이블에 새 레코드 추가 (GENERAL + 소셜)
- ✅ 한 사용자에 여러 `auth_account` 연결 확인

**SQL 확인:**
```sql
-- 한 사용자의 모든 auth_account 확인
SELECT u.email, aa.provider, aa.external_id 
FROM users u
JOIN auth_account aa ON u.users_id = aa.users_id
WHERE u.email = 'test@example.com';
```

### 시나리오 6: 카카오 이메일 업데이트

**테스트 단계:**
1. 카카오 로그인 (이메일 없음) → 임시 이메일로 로그인
2. 프론트엔드에서 `needsEmailUpdate=true` 확인
3. 이메일 입력 페이지로 이동
4. 실제 이메일 입력
5. `PATCH /api/user/email` API 호출

**API 요청 예시:**
```bash
# PATCH /api/user/email
curl -X PATCH http://localhost:8080/api/user/email \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "email": "user@example.com"
  }'
```

**확인 사항:**
- ✅ 이메일 중복 체크 (이미 존재하는 이메일인지)
- ✅ `users` 테이블의 이메일 업데이트 확인
- ✅ 다음 로그인 시 새로운 이메일로 JWT 토큰 생성

---

## 프론트엔드 연동 테스트

### 1. 프론트엔드 OAuth2 리다이렉트 페이지 구현

```jsx
// pages/oauth2/redirect.js 또는 해당 경로
import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');
    const needsEmailUpdate = searchParams.get('needsEmailUpdate') === 'true';

    if (error) {
      // 에러 처리
      console.error('OAuth2 로그인 실패:', error);
      navigate('/login?error=oauth2_failed');
      return;
    }

    if (token) {
      // 토큰 저장
      localStorage.setItem('accessToken', token);
      
      if (needsEmailUpdate) {
        // 이메일 입력 페이지로 이동
        navigate('/auth/email-required?token=' + token);
      } else {
        // 정상 로그인 완료
        navigate('/');
      }
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

### 2. 이메일 입력 페이지 구현

```jsx
// pages/auth/email-required.js
import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

function EmailRequired() {
  const [searchParams] = useSearchParams();
  const [email, setEmail] = useState('');
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const response = await fetch('https://www.fitlink1207.store/api/user/email', {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ email })
      });

      if (response.ok) {
        alert('이메일이 성공적으로 업데이트되었습니다.');
        navigate('/');
      } else {
        const error = await response.json();
        alert(error.message || '이메일 업데이트 실패');
      }
    } catch (error) {
      console.error('이메일 업데이트 오류:', error);
      alert('이메일 업데이트 중 오류가 발생했습니다.');
    }
  };

  return (
    <div>
      <h2>이메일 입력이 필요합니다</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="이메일을 입력하세요"
          required
        />
        <button type="submit">이메일 저장</button>
      </form>
    </div>
  );
}

export default EmailRequired;
```

### 3. 소셜 로그인 버튼 구현

```jsx
// components/SocialLoginButtons.js
function SocialLoginButtons() {
  const handleGoogleLogin = () => {
    window.location.href = 'https://www.fitlink1207.store/oauth2/authorization/google';
  };

  const handleKakaoLogin = () => {
    window.location.href = 'https://www.fitlink1207.store/oauth2/authorization/kakao';
  };

  return (
    <div>
      <button onClick={handleGoogleLogin}>구글로 로그인</button>
      <button onClick={handleKakaoLogin}>카카오로 로그인</button>
    </div>
  );
}

export default SocialLoginButtons;
```

---

## 데이터베이스 확인

### 1. 사용자 확인

```sql
-- 모든 사용자 조회
SELECT * FROM users ORDER BY created_at DESC;

-- 소셜 로그인 사용자 확인
SELECT u.*, aa.provider, aa.external_id, aa.social_token
FROM users u
LEFT JOIN auth_account aa ON u.users_id = aa.users_id
WHERE aa.provider IN ('GOOGLE', 'KAKAO')
ORDER BY u.created_at DESC;
```

### 2. 임시 이메일 확인

```sql
-- 임시 이메일로 저장된 사용자 확인
SELECT * FROM users 
WHERE email LIKE 'kakao_%@kakao.fitlink';

-- 또는
SELECT * FROM users 
WHERE email LIKE 'google_%@google.fitlink';
```

### 3. AuthAccount 확인

```sql
-- Provider별 사용자 수 확인
SELECT provider, COUNT(*) as user_count
FROM auth_account
GROUP BY provider;

-- 한 사용자의 모든 인증 계정 확인
SELECT u.email, aa.provider, aa.external_id, aa.created_at
FROM users u
JOIN auth_account aa ON u.users_id = aa.users_id
WHERE u.email = 'test@example.com';
```

---

## 문제 해결

### 문제 1: "redirect_uri_mismatch" 에러

**원인:** OAuth2 콘솔에 등록된 리다이렉트 URI와 실제 URI가 일치하지 않음

**해결:**
1. 구글/카카오 OAuth2 콘솔에서 정확한 URI 확인
2. 백엔드 서버 주소 확인
3. 리다이렉트 URI 정확히 일치하는지 확인

### 문제 2: "invalid_client" 에러

**원인:** client-id 또는 client-secret이 잘못되었음

**해결:**
1. `application.properties`에서 client-id와 secret 확인
2. 구글/카카오 콘솔에서 키 재확인
3. 환경 변수 설정 확인 (하드코딩 금지)

### 문제 3: CORS 에러

**원인:** 프론트엔드에서 백엔드로 요청 시 CORS 정책 위반

**해결:**
1. `SecurityConfig.java`의 `corsConfigurationSource()` 확인
2. 프론트엔드 도메인 추가 확인

### 문제 4: 카카오 이메일이 null인 경우

**확인 사항:**
1. 카카오 개발자 콘솔에서 "동의항목" 설정 확인
2. 이메일이 "필수" 또는 "선택"으로 설정되어 있는지 확인
3. 사용자가 이메일 동의를 했는지 확인
4. 로그에서 `needsEmailUpdate=true` 확인

**로그 확인:**
```bash
# 백엔드 로그에서 확인
카카오 이메일이 없어 임시 이메일 생성: kakao_123456789@kakao.fitlink
OAuth2 로그인 성공 (임시 이메일): kakao_123456789@kakao.fitlink
```

### 문제 5: 이메일 업데이트 API 실패

**확인 사항:**
1. JWT 토큰이 유효한지 확인
2. 이메일 중복 체크 (이미 존재하는 이메일인지)
3. 이메일 형식 검증 통과 여부

**API 테스트:**
```bash
# 이메일 업데이트 API 테스트
curl -X PATCH http://localhost:8080/api/user/email \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "email": "newemail@example.com"
  }'
```

---

## 테스트 체크리스트

- [ ] 백엔드 서버 정상 실행 확인
- [ ] 데이터베이스 연결 확인
- [ ] 구글 OAuth2 콘솔 설정 확인
- [ ] 카카오 OAuth2 콘솔 설정 확인
- [ ] 구글 로그인 테스트 (신규 사용자)
- [ ] 카카오 로그인 테스트 (이메일 있음)
- [ ] 카카오 로그인 테스트 (이메일 없음)
- [ ] 기존 사용자 재로그인 테스트
- [ ] 일반 회원가입 후 소셜 로그인 테스트
- [ ] 카카오 이메일 업데이트 테스트
- [ ] 프론트엔드 리다이렉트 확인
- [ ] JWT 토큰 발급 확인
- [ ] 데이터베이스 저장 확인

---

## 추가 참고사항

### 로컬 테스트 시 주의사항

1. **리다이렉트 URI 설정:**
   - 로컬 개발 시 `http://localhost:8080/login/oauth2/code/{provider}`도 OAuth2 콘솔에 추가
   - 프론트엔드 리다이렉트 URI는 `http://localhost:3000/oauth2/redirect`로 설정

2. **프로덕션 테스트:**
   - 실제 배포 서버에서 테스트할 때는 프로덕션 URL 사용
   - `https://www.fitlink1207.store`
   - `https://fit-link-fe.vercel.app`

### 디버깅 팁

1. **백엔드 로그 확인:**
   ```bash
   # 로그에서 OAuth2 관련 메시지 확인
   grep "OAuth2" logs/application.log
   ```

2. **네트워크 요청 확인:**
   - 브라우저 개발자 도구 → Network 탭
   - 리다이렉트 체인 확인
   - 최종 리다이렉트 URL 확인

3. **데이터베이스 직접 확인:**
   ```sql
   -- 최근 로그인 사용자 확인
   SELECT * FROM users ORDER BY updated_at DESC LIMIT 10;
   ```

---

## 다음 단계

1. ✅ 백엔드 설정 완료
2. ✅ 프론트엔드 구현 (OAuth2 리다이렉트 페이지, 이메일 입력 페이지)
3. ✅ 전체 플로우 테스트
4. ✅ 프로덕션 배포 및 테스트
5. ✅ 사용자 가이드 작성

