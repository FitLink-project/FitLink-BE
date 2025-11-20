package com.fitlink.service;

import com.fitlink.web.dto.NearByRequestDTO;
import com.fitlink.web.dto.NearbyFacilityResponseDTO;

import java.util.List;

public interface FacilityService {
    List<NearbyFacilityResponseDTO> getNearbyFacilities(NearByRequestDTO req);
}
