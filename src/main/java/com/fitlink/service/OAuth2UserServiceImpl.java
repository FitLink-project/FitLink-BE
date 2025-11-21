package com.fitlink.service;

import com.fitlink.domain.Agreement;
import com.fitlink.domain.AuthAccount;
import com.fitlink.domain.Users;
import com.fitlink.domain.enums.Provider;
import com.fitlink.domain.enums.Role;
import com.fitlink.repository.AgreementRepository;
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
    private final AgreementRepository agreementRepository;
    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);
        
        Provider provider = getProvider(registrationId);
        String externalId = getExternalId(oAuth2User, registrationId);
        
        String email = getEmail(oAuth2User, registrationId);
        String name = getName(oAuth2User, registrationId);
        String profileImageUrl = getProfileImageUrl(oAuth2User, registrationId);
        
        boolean needsEmailUpdate = false;
        if (email == null || email.isBlank()) {
            if (provider == Provider.KAKAO) {
                email = generateTemporaryEmail(provider, externalId);
                needsEmailUpdate = true;
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
                
                // Agreement가 없으면 기본값(true)으로 생성
                if (agreementRepository.findByUser(user).isEmpty()) {
                    Agreement agreement = Agreement.builder()
                            .user(user)
                            .privacy(true)
                            .service(true)
                            .over14(true)
                            .location(true)
                            .build();
                    agreementRepository.save(agreement);
                }
            } else {
                // 완전히 새로운 사용자 생성
                try {
                    user = Users.builder()
                            .email(email)
                            .name(name != null ? name : "사용자")
                            .password(null)
                            .role(Role.USER)
                            .isActive(true)
                            .profileUrl(profileImageUrl)
                            .build();
                    user = userRepository.save(user);
                    entityManager.flush();
                    
                    // Agreement 기본값(true)으로 생성
                    Agreement agreement = Agreement.builder()
                            .user(user)
                            .privacy(true)
                            .service(true)
                            .over14(true)
                            .location(true)
                            .build();
                    agreementRepository.save(agreement);
                } catch (Exception e) {
                    log.error("Users 저장 실패: email={}, provider={}", email, provider, e);
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
                authAccount = AuthAccount.builder()
                        .user(user)
                        .provider(provider)
                        .socialToken(socialToken)
                        .externalId(externalId)
                        .build();
                authAccount = authAccountRepository.save(authAccount);
            } catch (Exception e) {
                log.error("AuthAccount 저장 실패: user.id={}, provider={}", user.getId(), provider, e);
                OAuth2Error oauth2Error = new OAuth2Error(
                        "auth_account_creation_failed",
                        "인증 계정 생성 중 오류가 발생했습니다: " + e.getMessage(),
                        null
                );
                throw new OAuth2AuthenticationException(oauth2Error, e);
            }
        }
        
        // OAuth2User 반환 (JWT 토큰 생성에 사용됨)
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

