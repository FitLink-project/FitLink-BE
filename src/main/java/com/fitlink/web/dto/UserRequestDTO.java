package com.fitlink.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UserRequestDTO {

    @Getter
    @Setter
    public static class JoinDTO {

        @Schema(example = "蹂꾨챸")
        @NotBlank
        String nickName;

        @Schema(example = "example@gmail.com")
        @NotBlank
        @Email
        String email;

        @Schema(example = "zaq123")
        @NotBlank
        String password;

        @Schema(example = "0")
        @NotNull
        Integer gender;

        @Schema(example = "0")
        @NotNull
        Integer job;

        @Schema(example = "[\"CAREER\", \"STUDY\"]")
        List<String> purposeList;

        @Schema(example = "[\"IT\", \"DESIGN\"]")
        List<String> interestList;
    }

    @Getter
    @Setter
    public static class LoginRequestDTO {
        @Schema(example = "example@gmail.com")
        @NotBlank(message = "?대찓?쇱? ?꾩닔?낅땲??")
        @Email(message = "?щ컮瑜??대찓???뺤떇?댁뼱???⑸땲??")
        private String email;

        @Schema(example = "zaq123")
        @NotBlank(message = "?⑥뒪?뚮뱶???꾩닔?낅땲??")
        private String password;

    }
}

