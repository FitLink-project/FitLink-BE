package com.fitlink.web.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class FacilityProgramsResponseDTO {

    private Long facilityId;
    private String facilityName;
    private String address;
    private String homepage;
    private List<ProgramInfoDTO> programs;

    @Getter
    @Builder
    public static class ProgramInfoDTO {
        private Long programId;
        private String name;
        private String target;
        private String days;     // "월, 화 | 수, 목"
        private String time;
        private Integer capacity;
        private Integer price;
    }
}
