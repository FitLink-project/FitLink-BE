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

    /* ---------------------------
     *   1) 도보 경로
     * --------------------------- */
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
            "searchOption": "0",
            "reqCoordType": "WGS84GEO",
            "resCoordType": "WGS84GEO"
        }
        """, oLng, oLat, dLng, dLat);

        TmapRouteDTO dto = post(url, body);
        return convertWalkCar("walk", dto);
    }

    /* ---------------------------
     *   2) 자동차 경로
     * --------------------------- */
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
            "searchOption": "0",
            "reqCoordType": "WGS84GEO",
            "resCoordType": "WGS84GEO"
        }
        """, oLng, oLat, dLng, dLat);

        TmapRouteDTO dto = post(url, body);
        return convertWalkCar("car", dto);
    }

    /* ---------------------------
     *   3) 대중교통 경로
     * --------------------------- */
    public RouteResponseDTO getTransitRoute(float oLat, float oLng, float dLat, float dLng) {

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/transit/routes")
                .queryParam("version", 1)
                .queryParam("startX", oLng)
                .queryParam("startY", oLat)
                .queryParam("endX", dLng)
                .queryParam("endY", dLat)
                .queryParam("reqCoordType", "WGS84GEO")
                .queryParam("resCoordType", "WGS84GEO")
                .queryParam("sort", 0)
                .queryParam("lang", 0)
                .queryParam("format", "json")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey);

        ResponseEntity<TransitRouteDTO> response =
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TransitRouteDTO.class);

        return convertTransit(response.getBody());
    }

    /* ---------------------------
     *   POST 공통
     * --------------------------- */
    private TmapRouteDTO post(String url, String body) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> rawResponse =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        System.out.println("[Tmap Raw Response] = " + rawResponse.getBody());

        try {
            return new ObjectMapper().readValue(rawResponse.getBody(), TmapRouteDTO.class);

        } catch (Exception e) {
            System.err.println("DTO 변환 오류: " + e.getMessage());
            return null;
        }
    }

    /* ---------------------------
     *   Walk / Car 변환
     * --------------------------- */
    private RouteResponseDTO convertWalkCar(String type, TmapRouteDTO dto) {

        if (dto == null || dto.getFeatures() == null || dto.getFeatures().isEmpty()) {
            return RouteResponseDTO.builder()
                    .type(type)
                    .distance(0)
                    .duration(0)
                    .path(new ArrayList<>())
                    .waypoints(new ArrayList<>())
                    .build();
        }

        int distance = dto.getFeatures().get(0).getProperties().getTotalDistance();
        int seconds = dto.getFeatures().get(0).getProperties().getTotalTime();
        int minutes = seconds / 60;

        List<List<Double>> path = new ArrayList<>();
        List<RouteResponseDTO.Waypoint> waypoints = new ArrayList<>();

        dto.getFeatures().forEach(feature -> {

            var geo = feature.getGeometry();
            var prop = feature.getProperties();

            if (geo == null || geo.getType() == null) return;

            switch (geo.getType()) {

                case "LineString" -> {
                    if (geo.getLine() != null)
                        geo.getLine().forEach(coord ->
                                path.add(List.of(coord.get(1), coord.get(0))));
                }

                case "Point" -> {
                    if (geo.getPoint() != null) {

                        var c = geo.getPoint();
                        double lat = c.get(1);
                        double lng = c.get(0);

                        path.add(List.of(lat, lng));

                        // ★ turnType이 있으면 경유지로 추가
                        if (prop != null && prop.getTurnType() != null) {

                            waypoints.add(
                                    new RouteResponseDTO.Waypoint(
                                            lat,
                                            lng,
                                            prop.getDescription()
                                    )
                            );
                        }
                    }
                }

                case "MultiLineString" -> {
                    if (geo.getMulti() != null)
                        geo.getMulti().forEach(line ->
                                line.forEach(coord ->
                                        path.add(List.of(coord.get(1), coord.get(0)))));
                }
            }
        });

        return RouteResponseDTO.builder()
                .type(type)
                .distance(distance)
                .duration(minutes)
                .path(path)
                .waypoints(waypoints)
                .build();
    }

    /* ---------------------------
     *   Transit 변환
     * --------------------------- */
    private RouteResponseDTO convertTransit(TransitRouteDTO dto) {

        TransitRouteDTO.Itinerary it =
                dto.getMetaData().getPlan().getItineraries().get(0);

        int totalDuration = it.getDuration() / 60;

        return RouteResponseDTO.builder()
                .type("transit")
                .duration(totalDuration)
                .build();
    }
}
