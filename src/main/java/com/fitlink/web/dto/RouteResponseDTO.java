package com.fitlink.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteResponseDTO {

    private String type;       // walk / car / transit
    private int duration;      // minutes
    private int distance;      // meters (walk/car only)
    private List<List<Double>> path;

    private List<Waypoint> waypoints;   // ★ 자동 추출된 안내 포인트

    @Data
    @AllArgsConstructor
    public static class Waypoint {
        private double lat;
        private double lng;
        private String description; // 예: "좌회전", "횡단보도 건너기"
    }
}
