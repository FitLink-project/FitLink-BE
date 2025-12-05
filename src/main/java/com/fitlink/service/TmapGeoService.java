package com.fitlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlink.web.dto.ReverseAddressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class TmapGeoService {

    private final RestTemplate restTemplate;

    @Value("${tmap.app-key}")
    private String appKey;

    public ReverseAddressDTO reverseGeocode(double lat, double lon) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://apis.openapi.sk.com/tmap/geo/reversegeocoding")
                .queryParam("version", 1)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("coordType", "WGS84GEO")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        try {
            JsonNode root = new ObjectMapper().readTree(response.getBody());
            String fullAddress = root
                    .path("addressInfo")
                    .path("fullAddress")
                    .asText();

            return new ReverseAddressDTO(fullAddress);

        } catch (Exception e) {
            throw new RuntimeException("Reverse geocoding parsing error", e);
        }
    }
}
