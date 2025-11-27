package com.fitlink.service;

import com.fitlink.web.dto.RouteResponseDTO;

public interface FacilityRouteService {

    RouteResponseDTO getRoute(
            float originLat, float originLng,
            float destLat, float destLng,
            String type
    );
}