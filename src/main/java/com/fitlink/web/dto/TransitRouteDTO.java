package com.fitlink.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransitRouteDTO {

    private MetaData metaData;

    @Data
    public static class MetaData {
        private Plan plan;
    }

    @Data
    public static class Plan {
        private List<Itinerary> itineraries;
    }

    @Data
    public static class Itinerary {
        private int duration;
        private int transferCount;
        private List<Leg> legs;
    }

    @Data
    public static class Leg {
        private String mode;
        private int sectionTime;
        private double distance;
        private String route;
        private List<Step> steps;
    }

    @Data
    public static class Step {
        private String description;
        private int distance;
        private int duration;
    }
}
