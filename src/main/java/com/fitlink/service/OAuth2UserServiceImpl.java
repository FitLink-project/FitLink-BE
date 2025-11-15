package com.fitlink.service;

import com.fitlink.domain.AuthAccount;
import com.fitlink.domain.Users;
import com.fitlink.domain.enums.Provider;
import com.fitlink.domain.enums.Role;
import com.fitlink.repository.AuthAccountRepository;
import com.fitlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = getProvider(registrationId);
        String externalId = getExternalId(oAuth2User, registrationId);
        String email = getEmail(oAuth2User, registrationId);
        String name = getName(oAuth2User, registrationId);
        String profileImageUrl = getProfileImageUrl(oAuth2User, registrationId);
        
        // 카카오 이메일이 없는 경우 임시 이메일 생성
        // 주의: 임시 이메일은 실제 메일 주소가 아니므로 이메일 인증/재설정 등이 불가능함
        // 사용자가 나중에 실제 이메일로 업데이트해야 함
        boolean needsEmailUpdate = false;
        if (email == null || email.isBlank()) {
            if (provider == Provider.KAKAO) {
                email = generateTemporaryEmail(provider, externalId);
                needsEmailUpdate = true;
                log.warn("카카오 이메일이 없어 임시 이메일 생성: {}. 사용자가 나중에 실제 이메일로 업데이트해야 합니다.", email);
            } else {
                OAuth2Error oauth2Error = new OAuth2Error(
                        "email_required",
                        "이메일이 필요합니다.",
                        null
                );
                throw new OAuth2AuthenticationException(oauth2Error);
            }
        }
        
        // AuthAccount로 사용자 찾기
        Optional<AuthAccount> authAccountOpt = authAccountRepository.findByProviderAndExternalId(provider, externalId);
        
        Users user;
        AuthAccount authAccount;
        
        if (authAccountOpt.isPresent()) {
            // 기존 소셜 로그인 사용자
            authAccount = authAccountOpt.get();
            user = authAccount.getUser();
            
            // 사용자 정보 업데이트 (이름, 프로필 이미지 등)
            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
            }
            if (profileImageUrl != null && !profileImageUrl.equals(user.getProfileUrl())) {
                user.setProfileUrl(profileImageUrl);
            }
            
            // 소셜 토큰 업데이트
            authAccount.setSocialToken(userRequest.getAccessToken().getTokenValue());
            
        } else {
            // 신규 소셜 로그인 사용자
            // 이메일로 기존 사용자 확인 (같은 이메일로 일반 회원가입 했을 수 있음)
            Optional<Users> existingUserOpt = userRepository.findByEmail(email);
            
            if (existingUserOpt.isPresent()) {
                // 기존 사용자가 있으면 AuthAccount만 추가
                user = existingUserOpt.get();
            } else {
                // 완전히 새로운 사용자 생성
                try {
                    log.info("신규 소셜 로그인 사용자 생성 시작: email={}, provider={}", email, provider);
                    user = Users.builder()
                            .email(email)
                            .name(name != null ? name : "사용자")
                            .password(null)  // 소셜 로그인은 패스워드 없음
                            .role(Role.USER)
                            .isActive(true)
                            .profileUrl(profileImageUrl)
                            .build();
                    log.info("Users 엔티티 생성 완료: email={}", user.getEmail());
                    user = userRepository.save(user);
                    // 외래 키 제약 조건을 위해 즉시 DB에 플러시
                    entityManager.flush();
                    log.info("Users 저장 완료 (flushed): id={}, email={}", user.getId(), user.getEmail());
                } catch (Exception e) {
                    log.error("Users 저장 실패: email={}, provider={}, error={}", email, provider, e.getMessage(), e);
                    OAuth2Error oauth2Error = new OAuth2Error(
                            "user_creation_failed",
                            "사용자 생성 중 오류가 발생했습니다: " + e.getMessage(),
                            null
                    );
                    throw new OAuth2AuthenticationException(oauth2Error, e);
                }
            }
            
            // AuthAccount 생성
            try {
                String socialToken = userRequest.getAccessToken().getTokenValue();
                log.info("AuthAccount 생성 시작: user.id={}, provider={}, externalId={}, socialToken.length={}", 
                        user.getId(), provider, externalId, socialToken != null ? socialToken.length() : 0);
                authAccount = AuthAccount.builder()
                        .user(user)
                        .provider(provider)
                        .socialToken(socialToken)
                        .externalId(externalId)
                        .build();
                authAccount = authAccountRepository.save(authAccount);
                log.info("AuthAccount 저장 완료: id={}, user.id={}, provider={}", authAccount.getId(), user.getId(), provider);
            } catch (Exception e) {
                log.error("AuthAccount 저장 실패: user.id={}, provider={}, externalId={}, error={}", 
                        user.getId(), provider, externalId, e.getMessage(), e);
                log.error("전체 스택 트레이스:", e);
                OAuth2Error oauth2Error = new OAuth2Error(
                        "auth_account_creation_failed",
                        "인증 계정 생성 중 오류가 발생했습니다: " + e.getMessage(),
                        null
                );
                throw new OAuth2AuthenticationException(oauth2Error, e);
            }
        }
        
        // OAuth2User 반환 (JWT 토큰 생성에 사용됨)
        // needsEmailUpdate 플래그를 attributes에 추가하여 프론트엔드로 전달
        Map<String, Object> attributesWithFlag = new java.util.HashMap<>(oAuth2User.getAttributes());
        if (needsEmailUpdate) {
            attributesWithFlag.put("needsEmailUpdate", true);
            attributesWithFlag.put("temporaryEmail", email);
        }
        
        return new CustomOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())),
                attributesWithFlag,
                "email",
                user.getEmail()
        );
    }
    
    /**
     * 카카오 이메일이 없는 경우 임시 이메일 생성
     * 형식: kakao_{externalId}@kakao.fitlink
     */
    private String generateTemporaryEmail(Provider provider, String externalId) {
        return String.format("%s_%s@%s.fitlink", 
                provider.name().toLowerCase(), 
                externalId, 
                provider.name().toLowerCase());
    }

    private Provider getProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> Provider.GOOGLE;
            case "kakao" -> Provider.KAKAO;
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private String getExternalId(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "kakao" -> oAuth2User.getAttribute("id").toString();
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private String getEmail(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("email");
            case "kakao" -> {
                Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    yield (String) kakaoAccount.get("email");
                }
                yield null;
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private String getName(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name");
            case "kakao" -> {
                Map<String, Object> properties = oAuth2User.getAttribute("properties");
                if (properties != null) {
                    yield (String) properties.get("nickname");
                }
                yield null;
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private String getProfileImageUrl(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("picture");
            case "kakao" -> {
                Map<String, Object> properties = oAuth2User.getAttribute("properties");
                if (properties != null) {
                    yield (String) properties.get("profile_image");
                }
                yield null;
            }
            default -> null;
        };
    }

    // CustomOAuth2User 클래스 (OAuth2User 구현)
    private static class CustomOAuth2User implements OAuth2User {
        private final java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;
        private final Map<String, Object> attributes;
        private final String nameAttributeKey;
        private final String email;

        public CustomOAuth2User(
                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities,
                Map<String, Object> attributes,
                String nameAttributeKey,
                String email) {
            this.authorities = authorities;
            this.attributes = attributes;
            this.nameAttributeKey = nameAttributeKey;
            this.email = email;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getName() {
            return email;  // JWT 토큰 생성 시 email 사용
        }
    }
}

