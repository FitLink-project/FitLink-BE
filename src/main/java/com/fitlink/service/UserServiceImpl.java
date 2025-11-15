package com.fitlink.service;

import com.fitlink.apiPayload.code.status.ErrorStatus;
import com.fitlink.apiPayload.exception.GeneralException;
import com.fitlink.config.security.jwt.JwtTokenProvider;
import com.fitlink.domain.AuthAccount;
import com.fitlink.domain.Users;
import com.fitlink.domain.enums.Provider;
import com.fitlink.repository.AuthAccountRepository;
import com.fitlink.repository.UserRepository;
import com.fitlink.storage.FileStorageService;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;
import com.fitlink.web.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Users joinUser(UserRequestDTO.JoinDTO joinDTO, MultipartFile Img){
        // 이메일 형식과 비밀번호 형식은 @ValidEmail, @ValidPassword 어노테이션으로 검증됨
        
        // 0. 이메일 중복 체크
        userRepository.findByEmail(joinDTO.getEmail())
                .ifPresent(user -> {
                    throw new GeneralException(ErrorStatus._DUPLICATE_EMAIL);
                });
        
        // 1. 프로필 이미지 저장 (optional)
        String profileUrl = Optional.ofNullable(Img)
                .filter(file -> !file.isEmpty())
                .map(fileStorageService::uploadFile)
                .orElse(null);
        
        // 2. DTO -> Entity 변환
        Users user = userMapper.toEntity(joinDTO);
        user.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        user.setIsActive(true);
        Optional.ofNullable(profileUrl)
                .ifPresent(user::setProfileUrl);
        
        // 3. Users 저장
        Users savedUser = userRepository.save(user);
        
        // 4. AuthAccount 생성 (일반 로그인용)
        AuthAccount authAccount = AuthAccount.builder()
                .user(savedUser)
                .provider(Provider.GENERAL)
                .socialToken(null)  // 일반 로그인은 소셜 토큰 없음
                .externalId(null)   // 일반 로그인은 external ID 없음
                .build();
        authAccountRepository.save(authAccount);
        
        return savedUser;
    }
    
    @Override
    public UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.@Valid LoginRequestDTO request){
        //1. email있는지 찾기
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
        //2. 사용자 활성화 여부 체크
        if(Boolean.FALSE.equals(user.getIsActive())){
            throw new GeneralException(ErrorStatus._USER_INACTIVE);
        }
        //3. 비밀먼호 매치 확인
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new GeneralException(ErrorStatus._LOGIN_FAILED);
        }
        //4. 권한 정보 부여 & 인증 객체 생성 (표준 방식)
        List<GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null, // 인증 후엔 패스워드는 null 처리
                authorities
        );
        // 5. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // 6. Mapper를 사용하여 결과 DTO 반환
        return userMapper.toLoginResultDTO(user, accessToken);
    }
}
