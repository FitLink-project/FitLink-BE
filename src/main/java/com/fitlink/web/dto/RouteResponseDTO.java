package com.fitlink.web.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponseDTO {

    private String type;
    private int distance;
    private int duration;
    private List<List<Double>> path;
    private List<Waypoint> waypoints;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Waypoint {
        private double lat;
        private double lng;
        private String description;
    }
}
