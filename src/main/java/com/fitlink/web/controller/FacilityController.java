package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.service.FacilityService;
import com.fitlink.web.dto.FacilityDetailResponseDTO;
import com.fitlink.web.dto.NearByRequestDTO;
import com.fitlink.web.dto.NearbyFacilityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping("/nearby")
    public ApiResponse<?> getNearby(@RequestBody NearByRequestDTO req) {
        List<NearbyFacilityResponseDTO> response = facilityService.getNearbyFacilities(req);
        return ApiResponse.onSuccess(response);
    }// 사용자 반경 내에 있는 체육시설 조회


    @GetMapping("/{facilityId}")
    public ApiResponse<?> getFacilityDetail(@PathVariable Long facilityId) {
        FacilityDetailResponseDTO dto = facilityService.getFacilityDetail(facilityId);
        return ApiResponse.onSuccess(dto);
    }// 공공체육시설 상세 조회

}