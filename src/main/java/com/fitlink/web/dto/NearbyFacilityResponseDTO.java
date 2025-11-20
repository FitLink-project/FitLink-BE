package com.fitlink.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NearbyFacilityResponseDTO {

    private Long facilityId;
    private String facilityName;
    private String address;
    private Double latitude;
    private Double longitude;
    private double distance; // meter 단위로 하기
}
