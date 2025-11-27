package com.fitlink.service;

import com.fitlink.web.dto.TmapPoiResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

@Service
@RequiredArgsConstructor
public class TmapPoiService {

    private final RestTemplate restTemplate;

    @Value("${tmap.base-url}")
    private String baseUrl;

    @Value("${tmap.app-key}")
    private String appKey;

    public TmapPoiResultDTO searchPoi(String keyword) {


        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/tmap/pois")
                .queryParam("version", 1)
                .queryParam("format", "json")
                .queryParam("searchKeyword", keyword.trim())
                .queryParam("searchType", "all")
                .queryParam("searchtypCd", "A")
                .queryParam("reqCoordType", "WGS84GEO")
                .queryParam("resCoordType", "WGS84GEO")
                .queryParam("count", 20)
                .build(false)        // encode 방지
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TmapPoiResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                TmapPoiResultDTO.class
        );

        return response.getBody();
    }
}


