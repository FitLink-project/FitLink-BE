package com.fitlink.web.controller;

import com.fitlink.service.FacilityService;
import com.fitlink.web.dto.NearByRequestDTO;
import com.fitlink.web.dto.NearbyFacilityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping("/nearby")
    public ResponseEntity<?> getNearby(@RequestBody NearByRequestDTO req) {
        return ResponseEntity.ok(facilityService.getNearbyFacilities(req));
    }

}