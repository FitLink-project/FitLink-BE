package com.fitlink.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class RouteRequestDTO {
    private float originLat;
    private float originLng;
    private float destLat;
    private float destLng;
    private String type; // walk, car, transit

    // 경유지 (위도/경도 리스트)
    private List<Waypoint> waypoints;

    @Getter @Setter
    public static class Waypoint {
        private float lat;
        private float lng;
    }

}