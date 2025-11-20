package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.config.security.jwt.CustomUserDetails;
import com.fitlink.domain.Users;
import com.fitlink.service.UserService;
import com.fitlink.web.dto.UserRequestDTO;
import com.fitlink.web.dto.UserResponseDTO;
import com.fitlink.web.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping(value = "/login", consumes = "application/json")
    public ApiResponse<UserResponseDTO.LoginResultDTO> login(
            @RequestBody @Valid UserRequestDTO.LoginRequestDTO request) {
        UserResponseDTO.LoginResultDTO loginResultDTO = userService.loginUser(request);
        return ApiResponse.onSuccess(loginResultDTO);
    }

    @PatchMapping("/email")
    public ApiResponse<UserResponseDTO.JoinResultDTO> updateEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserRequestDTO.UpdateEmailDTO request) {
        Users user = userService.updateEmail(userDetails.getUsers().getId(), request);
        return ApiResponse.onSuccess(userMapper.toJoinResultDTO(user));
    }

    @GetMapping(value="/user/profile")
    public ApiResponse<UserResponseDTO.UserProfileDTO> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        UserResponseDTO.UserProfileDTO userProfileDTO =userService.getProfile(userDetails.getUserId());
    }
}
