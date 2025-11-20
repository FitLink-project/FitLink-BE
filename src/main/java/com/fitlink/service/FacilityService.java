package com.fitlink.service;

import com.fitlink.web.dto.FacilityDetailResponseDTO;
import com.fitlink.web.dto.NearByRequestDTO;
import com.fitlink.web.dto.NearbyFacilityResponseDTO;

import java.util.List;

public interface FacilityService {

    List<NearbyFacilityResponseDTO> getNearbyFacilities(NearByRequestDTO req); // 사용자 반경

    FacilityDetailResponseDTO getFacilityDetail(Long facilityId); // 상세 조회

}
