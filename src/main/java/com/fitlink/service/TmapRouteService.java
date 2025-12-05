package com.fitlink.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlink.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TmapRouteService {

    private final RestTemplate restTemplate;

    @Value("${tmap.app-key}")
    private String appKey;

    private static final String BASE_URL = "https://apis.openapi.sk.com";


    /* =================================================
     *   WALK
     * ================================================= */
    public RouteResponseDTO getPedestrianRoute(float oLat, float oLng, float dLat, float dLng) {

        String url = BASE_URL + "/tmap/routes/pedestrian?version=1";

        String body = String.format("""
        {
            "startX": %f,
            "startY": %f,
            "endX": %f,
            "endY": %f,
            "startName": "출발지",
            "endName": "도착지",
            "reqCoordType": "WGS84GEO",
            "resCoordType": "WGS84GEO"
        }
        """, oLng, oLat, dLng, dLat);

        TmapRouteDTO dto = post(url, body, TmapRouteDTO.class);
        return convertWalk(dto);
    }


    /* =================================================
     *   CAR
     * ================================================= */
    public RouteResponseDTO getCarRoute(float oLat, float oLng, float dLat, float dLng) {

        String url = BASE_URL + "/tmap/routes?version=1";

        String body = String.format("""
        {
            "startX": %f,
            "startY": %f,
            "endX": %f,
            "endY": %f,
            "startName": "출발지",
            "endName": "도착지",
            "reqCoordType": "WGS84GEO",
            "resCoordType": "WGS84GEO"
        }
        """, oLng, oLat, dLng, dLat);

        TmapCarRouteDTO dto = post(url, body, TmapCarRouteDTO.class);
        return convertCar(dto);
    }


    /* =================================================
     *   TRANSIT
     * ================================================= */
    public RouteResponseDTO getTransitRoute(float oLat, float oLng, float dLat, float dLng) {

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/transit/routes")
                .queryParam("version", 1)
                .queryParam("startX", oLng)
                .queryParam("startY", oLat)
                .queryParam("endX", dLng)
                .queryParam("endY", dLat)
                .queryParam("reqCoordType", "WGS84GEO")
                .queryParam("resCoordType", "WGS84GEO")
                .queryParam("format", "json")
                .toUriString();

        ResponseEntity<TmapRouteDTO> res =
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), TmapRouteDTO.class);

        return RouteResponseDTO.builder()
                .type("transit")
                .duration(0)
                .path(new ArrayList<>())
                .waypoints(new ArrayList<>())
                .build();
    }


    /* =================================================
     *   POST 공용
     * ================================================= */
    private <T> T post(String url, String body, Class<T> clazz) {

        HttpEntity<String> entity = new HttpEntity<>(body, buildHeaders());

        ResponseEntity<String> raw =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        System.out.println("[RAW] = " + raw.getBody());

        try {
            return new ObjectMapper().readValue(raw.getBody(), clazz);
        } catch (Exception e) {
            System.err.println(clazz.getSimpleName() + " 변환 오류: " + e.getMessage());
            return null;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("appKey", appKey);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }


    /* =================================================
     *   WALK 변환
     * ================================================= */
    private RouteResponseDTO convertWalk(TmapRouteDTO dto) {

        if (dto == null || dto.getFeatures() == null) return empty("walk");

        int dist = dto.getFeatures().get(0).getProperties().getTotalDistance();
        int time = dto.getFeatures().get(0).getProperties().getTotalTime() / 60;

        List<List<Double>> path = new ArrayList<>();
        List<RouteResponseDTO.Waypoint> waypoints = new ArrayList<>();

        for (var f : dto.getFeatures()) {
            var geo = f.getGeometry();
            var prop = f.getProperties();
            if (geo == null) continue;

            if (geo.getPoint() != null) {
                double lat = geo.getPoint().get(1);
                double lng = geo.getPoint().get(0);
                path.add(List.of(lat, lng));
            }

            if (geo.getLine() != null) {
                geo.getLine().forEach(c -> path.add(List.of(c.get(1), c.get(0))));
            }

            if (prop != null && prop.getDescription() != null && prop.getTurnType() != null) {
                waypoints.add(new RouteResponseDTO.Waypoint(
                        path.get(path.size()-1).get(0),
                        path.get(path.size()-1).get(1),
                        prop.getDescription()
                ));
            }
        }

        return RouteResponseDTO.builder()
                .type("walk")
                .distance(dist)
                .duration(time)
                .path(path)
                .waypoints(waypoints)
                .build();
    }


    /* =================================================
     *   CAR 변환
     * ================================================= */
    private RouteResponseDTO convertCar(TmapCarRouteDTO dto) {

        if (dto == null || dto.getFeatures() == null) {
            return empty("car");
        }

        int[] totalDistance = {0};
        int[] totalTime = {0};

        List<List<Double>> path = new ArrayList<>();
        List<RouteResponseDTO.Waypoint> waypoints = new ArrayList<>();

        dto.getFeatures().forEach(f -> {

            var geo = f.getGeometry();
            var prop = f.getProperties();

            // 거리/시간 누적
            if (prop != null) {
                totalDistance[0] += prop.getDistanceInt();
                totalTime[0] += prop.getTimeInt();
            }

            if (geo == null) return;

            /* ------------------------
             * MULTILINESTRING
             * ------------------------ */
            if ("MultiLineString".equals(geo.getType()) && geo.getMulti() != null) {
                geo.getMulti().forEach(line ->
                        line.forEach(coord ->
                                path.add(List.of(coord.get(1), coord.get(0))))
                );
            }

            /* ------------------------
             * LINESTRING
             * ------------------------ */
            if ("LineString".equals(geo.getType()) && geo.getLine() != null) {
                geo.getLine().forEach(coord ->
                        path.add(List.of(coord.get(1), coord.get(0))));
            }

            /* ------------------------
             * POINT + name 기반 waypoint
             * ------------------------ */
            if ("Point".equals(geo.getType()) &&
                    geo.getPoint() != null &&
                    prop != null &&
                    (prop.getDescription() != null || prop.getIndex() != null)) {

                double lat = geo.getPoint().get(1);
                double lng = geo.getPoint().get(0);

                String desc = prop.getDescription() != null
                        ? prop.getDescription()
                        : ("지점_" + prop.getIndex());

                waypoints.add(new RouteResponseDTO.Waypoint(lat, lng, desc));
            }

        });

        return RouteResponseDTO.builder()
                .type("car")
                .distance(totalDistance[0])
                .duration(totalTime[0] / 60)
                .path(path)
                .waypoints(waypoints)
                .build();
    }


    private RouteResponseDTO empty(String type) {
        return RouteResponseDTO.builder()
                .type(type)
                .distance(0)
                .duration(0)
                .path(new ArrayList<>())
                .waypoints(new ArrayList<>())
                .build();
    }
}
