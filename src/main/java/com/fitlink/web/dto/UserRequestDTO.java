package com.fitlink.web.dto;

import com.fitlink.validation.annotation.ValidEmail;
import com.fitlink.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
public class UserRequestDTO {

    @Getter
    @Setter
    public static class JoinDTO {
        @NotBlank
        String name;

        @NotBlank
        @Email
        @ValidEmail
        String email;

        @NotBlank
        @ValidPassword
        String password;
    }

    @Getter
    @Setter
    public static class LoginRequestDTO {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

    }

    @Getter
    @Setter
    public static class UpdateEmailDTO {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @ValidEmail
        private String email;
    }

    @Getter
    @Setter
    public static class EditProfileDTO {
        String name;

        @ValidEmail
        String email;

        @ValidPassword
        String password;
    }
}

