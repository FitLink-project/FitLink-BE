package com.fitlink.service;

import com.fitlink.web.dto.RouteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacilityRouteServiceImpl implements FacilityRouteService {

    private final TmapRouteService tmapRouteService;

    @Override
    public RouteResponseDTO getRoute(
            float originLat, float originLng,
            float destLat, float destLng,
            String type
    ) {

        return switch (type) {
            case "walk" -> tmapRouteService.getPedestrianRoute(originLat, originLng, destLat, destLng);
            case "car" -> tmapRouteService.getCarRoute(originLat, originLng, destLat, destLng);
            case "transit" -> tmapRouteService.getTransitRoute(originLat, originLng, destLat, destLng);
            default -> throw new IllegalArgumentException("type은 walk/car/transit 중 하나여야 합니다.");
        };
    }
}
