package com.fitlink.service;

import com.fitlink.domain.Users;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    Users joinUser(UserRequestDTO.JoinDTO joinDTO, MultipartFile Img);

    UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request);

    Users updateEmail(Long userId, UserRequestDTO.UpdateEmailDTO request);

    UserResponseDTO.UserProfileDTO getProfile(Long userId);

    UserResponseDTO.UserProfileDTO editProfile(Long userId, UserRequestDTO.@Valid EditProfileDTO request, MultipartFile img);

    UserResponseDTO.UserDeletedDTO deleteUser(Long userId);

    UserResponseDTO.UserDeletedDTO hardDeleteUser(Long userId);
}
