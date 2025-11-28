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

        // Point -> [lng, lat]
        private List<Double> point;

        // LineString -> [[lng, lat], [lng, lat] ...]
        private List<List<Double>> line;

        // MultiLineString -> [[[lng, lat], ...], [...]]
        private List<List<List<Double>>> multi;

        @JsonAnySetter
        public void handle(String key, Object value) {
            if (!"coordinates".equals(key)) return;

            if (value instanceof List<?> list) {

                // 1) Point : [lng, lat]
                if (!list.isEmpty() && list.get(0) instanceof Number) {
                    this.point = (List<Double>) value;
                    return;
                }

                // 2) LineString : [[lng,lat], [lng,lat]]
                if (!list.isEmpty() && list.get(0) instanceof List<?> sub
                        && sub.size() == 2 && sub.get(0) instanceof Number) {
                    this.line = (List<List<Double>>) value;
                    return;
                }

                // 3) MultiLineString : [[[lng,lat],...], ...]
                if (!list.isEmpty() && list.get(0) instanceof List<?> sub2
                        && sub2.get(0) instanceof List<?>) {
                    this.multi = (List<List<List<Double>>>) value;
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
