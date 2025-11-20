package com.fitlink.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FacilityDetailResponseDTO {

    private Long facilityId;
    private String facilityName;
    private String address;
    private Double latitude;
    private Double longitude;

    private List<String> programNames; // 2개만
    private String homepageUrl;
}
