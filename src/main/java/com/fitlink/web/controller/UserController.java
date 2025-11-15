package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.domain.Users;
import com.fitlink.service.UserService;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;
import com.fitlink.web.mapping.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping(value = "/join", consumes = "multipart/form-data")
    public ApiResponse<UserResponseDTO.JoinResultDTO> join(
            @ModelAttribute @Valid UserRequestDTO.JoinDTO request, MultipartFile Img) {
        
        Users user = userService.joinUser(request,Img);
        return ApiResponse.onSuccess(userMapper.toJoinResultDTO(user));
    }
}
