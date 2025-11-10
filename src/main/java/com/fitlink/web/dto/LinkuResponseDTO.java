package com.fitlink.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LinkuResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LinkuIsExistDTO {
        private Boolean isExist;
        private Long userId;
        private Long linkuId;
        private String memo;
        private Long emotionId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

