package com.fitlink.service;

import com.fitlink.apiPayload.code.status.ErrorStatus;
import com.fitlink.apiPayload.exception.GeneralException;
import com.fitlink.domain.Users;
import com.fitlink.repository.UserRepository;
import com.fitlink.storage.FileStorageService;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.mapping.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");
    private final PasswordEncoder passwordEncoder;

    @Override
    public Users joinUser(UserRequestDTO.JoinDTO joinDTO, MultipartFile Img){
        //0. 이메일 형식 체크
        if(!EMAIL_PATTERN.matcher(joinDTO.getEmail()).matches()){
            throw new GeneralException(ErrorStatus._BAD_EMAIL_FORMAT);
        }
        //0. 비민번호 형식 체크
        if(!PASSWORD_PATTERN.matcher(joinDTO.getPassword()).matches()){
            throw new GeneralException(ErrorStatus._BAD_PASSWORD_FORMAT);
        }
        //0. 이메일 중복 체크
        if(userRepository.findByEmail(joinDTO.getEmail()).isPresent()){//isPresent()는 Optional 객체가 값을 가지고 있으면 true, 없으면 false를 반환합니다.
            throw new GeneralException(ErrorStatus._DUPLICATE_EMAIL);
        }
        //1. img를 디스크에 저장-> 값 반환
        String profileUrl = null;
        if (Img != null && !Img.isEmpty()) {
            profileUrl = fileStorageService.uploadFile(Img);
        }
        //2. 저장한 이미지, dto를 mapper를 이용해 변환
        Users user = userMapper.toEntity(joinDTO);
        user.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        user.setIsActive(true);
        if (profileUrl != null) {
            user.setProfileUrl(profileUrl);
        }
        //3. repository에 저장
        return userRepository.save(user);
    }
}
