package com.fitlink.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapCarRouteDTO {

    private String type;
    private List<Feature> features;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String type;
        private Geometry geometry;
        private Properties properties;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {

        private String type;

        // Point
        private List<Double> point;

        // LineString
        private List<List<Double>> line;

        // MultiLineString
        private List<List<List<Double>>> multi;

        @JsonAnySetter
        public void handle(String key, Object value) {
            if (!"coordinates".equals(key)) return;

            if (value instanceof List<?> list) {

                // POINT: [lng, lat]
                if (!list.isEmpty() && list.get(0) instanceof Number) {
                    this.point = (List<Double>) value;
                    return;
                }

                // LINESTRING: [[lng, lat], ...]
                if (!list.isEmpty() && list.get(0) instanceof List<?> sub
                        && sub.size() == 2 && sub.get(0) instanceof Number) {
                    this.line = (List<List<Double>>) value;
                    return;
                }

                // MULTILINESTRING: [[[lng,lat], ...], ...]
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

        private Object time;
        private Object distance;
        private String description;
        private Integer index;

        public int getTimeInt() {
            if (time == null) return 0;
            if (time instanceof Number n) return n.intValue();
            if (time instanceof String s) return Integer.parseInt(s);
            return 0;
        }

        public int getDistanceInt() {
            if (distance == null) return 0;
            if (distance instanceof Number n) return n.intValue();
            if (distance instanceof String s) return Integer.parseInt(s);
            return 0;
        }
    }
}
