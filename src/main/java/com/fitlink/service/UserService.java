package com.fitlink.service;

import com.fitlink.domain.Users;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;

public interface UserService {

    Users joinUser(UserRequestDTO.JoinDTO request);
    UserResponseDTO.LoginResultDTO loginUser(UserRequestDTO.LoginRequestDTO request);

    public void validateNickNameNotDuplicate(String nickname);
}

