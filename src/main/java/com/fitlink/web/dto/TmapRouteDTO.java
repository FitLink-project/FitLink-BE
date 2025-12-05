package com.fitlink.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class TmapRouteDTO {

    private String type;
    private List<Feature> features;

    @Data
    public static class Feature {
        private String type;
        private Geometry geometry;
        private Properties properties;
    }

    @Data
    public static class Geometry {
        private String type;

        private List<Double> point;
        private List<List<Double>> line;

        @JsonAnySetter
        public void handle(String key, Object value) {
            if (!"coordinates".equals(key)) return;

            if (value instanceof List<?> list) {

                if (!list.isEmpty() && list.get(0) instanceof Number) {
                    this.point = (List<Double>) value;
                    return;
                }

                if (!list.isEmpty() && list.get(0) instanceof List<?> sub
                        && sub.size() == 2) {
                    this.line = (List<List<Double>>) value;
                }
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private Integer totalDistance;
        private Integer totalTime;
        private String description;
        private String turnType;
    }
}
