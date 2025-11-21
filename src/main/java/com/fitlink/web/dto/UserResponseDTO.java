package com.fitlink.web.dto;

import com.fitlink.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class UserResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinResultDTO{
        Long userId;
        LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResultDTO{
        Long userId;
        String accessToken;
    }
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDTO{
        Long userId;
        String email;
        String name;
        String profileUrl;
        Boolean isActive;
        LocalDateTime regDate;
        String provider;
        LocalDateTime deleteDate;
        AgreementsDTO agreements;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgreementsDTO {
        Boolean privacy;
        Boolean service;
        Boolean over14;
        Boolean location;
    }
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDeletedDTO{
        Long userId;
        String email;
        Boolean isActive;
        LocalDateTime regDate;
        String provider;
        LocalDateTime deleteDate;
    }

}

