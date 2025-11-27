package com.fitlink.web.dto;

import lombok.Data;
import java.util.List;

@Data
public class TmapPoiResultDTO {

    private SearchPoiInfo searchPoiInfo;

    @Data
    public static class SearchPoiInfo {
        private String totalCount;
        private String count;
        private String page;
        private Pois pois;
    }

    @Data
    public static class Pois {
        private List<Poi> poi;
    }

    @Data
    public static class Poi {

        // 기본 정보
        private String name;
        private String upperAddrName;
        private String middleAddrName;
        private String lowerAddrName;
        private String detailAddrName;

        // 좌표 정보 (null이어도 필드 있어야 매핑됨)
        private String lat;
        private String lon;
        private String frontLat;
        private String frontLon;
        private String noorLat;
        private String noorLon;
        private String pnsLat;
        private String pnsLon;
    }
}
