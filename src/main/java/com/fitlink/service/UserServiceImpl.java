package com.fitlink.service;

import com.fitlink.apiPayload.code.status.ErrorStatus;
import com.fitlink.apiPayload.exception.handler.UserHandler;
import com.fitlink.config.security.jwt.JwtTokenProvider;
import com.fitlink.converter.UserConverter;
import com.fitlink.domain.Interests;
import com.fitlink.domain.Purposes;
import com.fitlink.domain.Users;
import com.fitlink.domain.enums.Interest;
import com.fitlink.domain.enums.Purpose;
import com.fitlink.repository.UserRepository;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;
//import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public Users joinUser(UserRequestDTO.JoinDTO request){
        if (userRepository.findByNickName(request.getNickName()).isPresent()) {
            throw new UserHandler(ErrorStatus._DUPLICATE_NICKNAME);
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new UserHandler(ErrorStatus._DUPLICATE_JOIN_REQUEST);
        }

        Users newUser = UserConverter.toUser(request);
        newUser.encodePassword(passwordEncoder.encode(request.getPassword()));

        List<String> purposeNames = request.getPurposeList(); // ?꾨줎?몄뿉??諛쏆? enum ?대쫫 由ъ뒪??

        List<Purposes> purposeList = purposeNames.stream()
                .map(name -> {
                    Purpose enumPurpose = Purpose.valueOf(name); // 臾몄옄????enum
                    return new Purposes(enumPurpose, newUser);
                })
                .toList();

        List<String> interestNames = request.getInterestList(); // ?꾨줎?몄뿉??諛쏆? enum ?대쫫 由ъ뒪??

        List<Interests> interestList = interestNames.stream()
                .map(name -> {
                    Interest enumInterest = Interest.valueOf(name); // 臾몄옄????enum
                    return new Interests(enumInterest, newUser);
                })
                .toList();

        return userRepository.save(newUser);
    }

    @Override
    public UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserHandler(ErrorStatus._INVALID_PASSWORD);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                Collections.singleton(() -> user.getRole().name())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);

        return UserConverter.toLoginResultDTO(
                user.getId(),
                accessToken
        );
    }

    @Override
    public void validateNickNameNotDuplicate(String nickname) {
        if (userRepository.findByNickName(nickname).isPresent()) {
            throw new UserHandler(ErrorStatus._DUPLICATE_NICKNAME);
        }
    }

}


