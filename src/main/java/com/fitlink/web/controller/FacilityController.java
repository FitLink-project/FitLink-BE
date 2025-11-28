package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.service.FacilityRouteService;
import com.fitlink.service.FacilityService;
import com.fitlink.web.dto.FacilityDetailResponseDTO;
import com.fitlink.web.dto.FacilityProgramsResponseDTO;
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
    private final FacilityRouteService facilityRouteService;

    @GetMapping
    public ApiResponse<?> search(@RequestParam String keyword) {
        return ApiResponse.onSuccess(facilityService.search(keyword));
    }// 통합 검색

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


    @GetMapping("/{facilityId}/programs")
    public ApiResponse<?> getFacilityPrograms(@PathVariable Long facilityId) {

        FacilityProgramsResponseDTO dto = facilityService.getFacilityPrograms(facilityId);

        return ApiResponse.onSuccess(dto);
    }// 공공체육시설 프로그램 조회

    @GetMapping("/route")
    public ApiResponse<?> getRoute(
            @RequestParam float originLat, //출발지 위도
            @RequestParam float originLng, //출발지 경도
            @RequestParam float destLat, //목적지 위도
            @RequestParam float destLng, //목적지 경도
            @RequestParam String type
    ) {
        return ApiResponse.onSuccess(
                facilityRouteService.getRoute(originLat, originLng, destLat, destLng, type)
        );
    }


}