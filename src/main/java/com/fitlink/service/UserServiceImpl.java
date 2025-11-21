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
import com.fitlink.util.EmailUtil;
import com.fitlink.util.UserUtil;
import com.fitlink.validation.validator.UserValidator;
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
    private final UserUtil userUtil;
    private final EmailUtil emailUtil;
    private final UserValidator userValidator;

    @Override
    public Users joinUser(UserRequestDTO.JoinDTO joinDTO, MultipartFile Img){
        // 이메일 형식과 비밀번호 형식은 @ValidEmail, @ValidPassword 어노테이션으로 검증됨
        
        // 0. 이메일 중복 체크
        userValidator.validateEmailNotDuplicate(joinDTO.getEmail());
        
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
        Users user = userUtil.findByEmailOrThrow(request.getEmail());
        //2. 사용자 활성화 여부 체크
        userValidator.validateUserActive(user);
        //3. 비밀번호 매치 확인
        userValidator.validatePasswordMatch(request.getPassword(), user.getPassword(), passwordEncoder);
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

    @Override
    public Users updateEmail(Long userId, UserRequestDTO.UpdateEmailDTO request) {
        // 1. 사용자 확인
        Users currentUser = userUtil.findByIdOrThrow(userId);

        // 2. 새 이메일이 현재 이메일과 같은 경우 그대로 반환
        if (currentUser.getEmail().equals(request.getEmail())) {
            return currentUser;
        }

        // 3. 입력한 이메일이 이미 존재하는 Users인지 확인
        Optional<Users> existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isPresent()) {
            // 3-1. 기존 Users가 존재하는 경우: 카카오 계정 연결 및 임시 이메일 Users 삭제
            Users existingUser = existingUserOpt.get();

            // 3-2. 현재 Users가 임시 이메일로 생성된 카카오 사용자인지 확인
            if (emailUtil.isTemporaryKakaoEmail(currentUser.getEmail())) {
                // 3-3. 현재 Users의 카카오 AuthAccount 찾기
                Optional<AuthAccount> kakaoAuthAccountOpt = authAccountRepository
                        .findByUserAndProvider(currentUser, Provider.KAKAO);

                if (kakaoAuthAccountOpt.isPresent()) {
                    AuthAccount kakaoAuthAccount = kakaoAuthAccountOpt.get();

                    // 3-4. 카카오 AuthAccount가 이미 다른 사용자에 연결되어 있는지 확인
                    if (!kakaoAuthAccount.getUser().equals(currentUser)) {
                        throw new GeneralException(ErrorStatus._DUPLICATE_EMAIL);
                    }

                    // 3-5. 현재 Users의 모든 AuthAccount 확인 (카카오 외에 다른 AuthAccount가 있는지)
                    List<AuthAccount> currentUserAuthAccounts = authAccountRepository.findByUser(currentUser);
                    
                    // 3-6. 카카오 AuthAccount를 기존 Users에 연결
                    kakaoAuthAccount.setUser(existingUser);
                    authAccountRepository.save(kakaoAuthAccount);

                    // 3-7. 카카오 AuthAccount 외에 다른 AuthAccount가 있으면 삭제하지 않음
                    // (일반 로그인 계정 등이 있을 수 있음)
                    if (currentUserAuthAccounts.size() == 1) {
                        // 카카오 AuthAccount만 있는 경우 현재 Users 삭제
                        userRepository.delete(currentUser);
                    }
                    // 다른 AuthAccount가 있는 경우 현재 Users를 유지 (카카오 계정만 연결)

                    // 3-8. 기존 Users 반환
                    return existingUser;
                } else {
                    // 카카오 AuthAccount가 없으면 일반 사용자이므로 기존 로직대로 처리
                    throw new GeneralException(ErrorStatus._DUPLICATE_EMAIL);
                }
            } else {
                // 임시 이메일이 아니면 일반 사용자이므로 중복 에러
                throw new GeneralException(ErrorStatus._DUPLICATE_EMAIL);
            }
        }

        // 4. 입력한 이메일이 존재하지 않는 경우: 기존 로직대로 이메일 업데이트
        currentUser.setEmail(request.getEmail());
        return currentUser;
    }

    @Override
    public UserResponseDTO.UserProfileDTO getProfile(Long userId){
        //1. 사용자 확인
        Users currentUser = userUtil.findByIdOrThrow(userId);
        //2. mapper로 사용자 정보 반환하기
        UserResponseDTO.UserProfileDTO profileDTO = userMapper.toUserProfileDTO(currentUser);
        //3. profileUrl을 절대 URL로 변환 (기존 상대 경로도 절대 URL로 변환)
        if (profileDTO.getProfileUrl() != null) {
            String absoluteUrl = fileStorageService.convertToAbsoluteUrl(profileDTO.getProfileUrl());
            profileDTO.setProfileUrl(absoluteUrl);
        }
        return profileDTO;
    }
    @Override
    public UserResponseDTO.UserProfileDTO editProfile(Long userId, UserRequestDTO.@Valid EditProfileDTO request, MultipartFile img){
        //1. 사용자 확인
        Users currentUser = userUtil.findByIdOrThrow(userId);
        
        //2. 이름 업데이트 (제공된 경우)
        Optional.ofNullable(request.getName())
                .filter(name -> !name.isBlank())
                .ifPresent(currentUser::setName);
        
        //3. 이메일 업데이트 (제공된 경우)
        Optional.ofNullable(request.getEmail())
                .filter(email -> !email.isBlank())
                .ifPresent(email -> {
                    // 이메일이 변경되는 경우에만 중복 체크
                    if (!currentUser.getEmail().equals(email)) {
                        userValidator.validateEmailNotDuplicate(email);
                        currentUser.setEmail(email);
                    }
                });
        
        //4. 비밀번호 업데이트 (제공된 경우)
        Optional.ofNullable(request.getPassword())
                .filter(password -> !password.isBlank())
                .ifPresent(password -> currentUser.setPassword(passwordEncoder.encode(password)));
        
        //5. 프로필 이미지 업로드 (제공된 경우)
        Optional.ofNullable(img)
                .filter(file -> !file.isEmpty())
                .map(fileStorageService::uploadFile)
                .ifPresent(currentUser::setProfileUrl);
        
        //6. 저장 후 DTO로 변환하여 반환
        Users savedUser = userRepository.save(currentUser);
        return userMapper.toUserProfileDTO(savedUser);
    }

}
