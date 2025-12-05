package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.service.TmapGeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class TmapGeoController {

    private final TmapGeoService geoService;

    @GetMapping("/reverse")
    public ApiResponse<?> getAddress(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        return ApiResponse.onSuccess(
                geoService.reverseGeocode(lat, lon)
        );
    }
}
