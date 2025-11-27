package com.fitlink.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RouteResponseDTO {

    private String type;
    private int duration;
    private int distance; // 도보/자동차만
    private List<List<Double>> path; // 도보/자동차만
    private List<RouteStep> routes;  // 대중교통만

    @Data
    @AllArgsConstructor
    public static class RouteStep {
        private String mode;
        private String instruction;
        private int duration;
    }
}
