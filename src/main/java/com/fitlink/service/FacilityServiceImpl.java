package com.fitlink.service;

import com.fitlink.domain.Program;
import com.fitlink.repository.ProgramRepository;
import com.fitlink.service.FacilityService;
import com.fitlink.domain.Facility;
import com.fitlink.service.TmapPoiService;
import com.fitlink.repository.FacilityRepository;
import com.fitlink.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final ProgramRepository programRepository;
    private final double SEARCH_RADIUS = 10000; // 10km
    private final TmapPoiService tmapPoiService;
    private static final double RADIUS_STATION = 20000.0; // 20km
    private static final double RADIUS_REGION = 30000.0;  // 30km (지역명 검색)

    @Override
    public List<NearbyFacilityResponseDTO> getNearbyFacilities(NearByRequestDTO req) {

        List<Object[]> result = facilityRepository.findNearby(
                req.getLatitude(),
                req.getLongitude(),
                SEARCH_RADIUS
        );

        return result.stream()
                .map(obj -> {
                    Facility f = (Facility) obj[0];
                    double distance = (double) obj[1];

                    return NearbyFacilityResponseDTO.builder()
                            .facilityId(f.getId())
                            .facilityName(f.getName())
                            .address(f.getAddress())
                            .latitude(f.getLatitude())
                            .longitude(f.getLongitude())
                            .distance(Math.round(distance))
                            .build();
                })
                .toList();
    }//사용자 근처에 있는 시설 조회

    @Override
    public FacilityDetailResponseDTO getFacilityDetail(Long facilityId) {

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("시설이 존재하지 않습니다."));

        // 프로그램명 2개만 가져오기
        List<String> programNames = programRepository
                .findTop2NamesByFacilityId(facilityId, PageRequest.of(0, 2));

        return FacilityDetailResponseDTO.builder()
                .facilityId(facility.getId())
                .facilityName(facility.getName())
                .address(facility.getAddress())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .homepageUrl(facility.getHomepageUrl())
                .programNames(programNames)
                .build();
    }//시설 상세 조회

    @Override
    public FacilityProgramsResponseDTO getFacilityPrograms(Long facilityId) {

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        List<Program> programs = programRepository.findByFacilityId(facilityId);

        List<FacilityProgramsResponseDTO.ProgramInfoDTO> programDTOs =
                programs.stream()
                        .map(p -> FacilityProgramsResponseDTO.ProgramInfoDTO.builder()
                                .programId(p.getId())
                                .name(p.getName())
                                .target(cleanTarget(p.getTarget()))
                                .days(p.getDays())
                                .time(p.getTime())
                                .capacity(p.getCapacity())
                                .price(p.getPrice())
                                .build()
                        ).toList();

        return FacilityProgramsResponseDTO.builder()
                .facilityId(facility.getId())
                .facilityName(facility.getName())
                .address(facility.getAddress())
                .homepage(facility.getHomepageUrl())
                .programs(programDTOs)
                .build();
    }//시설 프로그램 조회


    private String cleanTarget(String target) {
        if (target == null) return "";
        return target.replace("\"", "");
    }

    @Override
    public Map<String, Object> search(String keyword) {

        // 1) 공공체육시설명 검색
        Facility facility = facilityRepository.findByName(keyword);
        if (facility != null) {
            return buildFacilityDetail(facility);
        }

        // 2) Tmap 검색
        TmapPoiResultDTO result = tmapPoiService.searchPoi(keyword);

        if (result == null ||
                result.getSearchPoiInfo() == null ||
                result.getSearchPoiInfo().getPois() == null ||
                result.getSearchPoiInfo().getPois().getPoi() == null ||
                result.getSearchPoiInfo().getPois().getPoi().isEmpty()) {

            throw new RuntimeException("검색 결과가 없습니다.");
        }

        // 첫 번째 결과
        List<TmapPoiResultDTO.Poi> poiList =
                result.getSearchPoiInfo().getPois().getPoi();

        TmapPoiResultDTO.Poi poi = poiList.get(0);


        // 3) 검색어가 “역”으로 끝남 → 지하철역
        if (keyword.endsWith("역")) {
            return searchStation(keyword, poi);
        }

        // 4) not station → 지역명 검색으로 처리
        return searchRegion(keyword, poi);
    }



    //공공체육시설 상세 조회 처리
    private Map<String, Object> buildFacilityDetail(Facility facility) {

        List<String> programNames = programRepository
                .findTop2NamesByFacilityId(facility.getId(), PageRequest.of(0, 2));

        Map<String, Object> response = new HashMap<>();
        response.put("type", "facility");
        response.put("facility_id", facility.getId());
        response.put("facility_name", facility.getName());
        response.put("address", facility.getAddress());
        response.put("latitude", facility.getLatitude());
        response.put("longitude", facility.getLongitude());
        response.put("homepage", facility.getHomepageUrl());
        response.put("program_count", programNames.size());
        response.put("program_names", programNames);

        return response;
    }

    //지하철역명 검색 처리
    private Map<String, Object> searchStation(String stationName, TmapPoiResultDTO.Poi poi) {

        double lat = Double.parseDouble(poi.getNoorLat());
        double lon = Double.parseDouble(poi.getNoorLon());

        List<Object[]> nearby = facilityRepository.findNearby(lat, lon, RADIUS_STATION);

        List<Map<String, Object>> facilities = nearby.stream()
                .map(obj -> {
                    Facility f = (Facility) obj[0];
                    double distance = (double) obj[1];

                    Map<String, Object> map = new HashMap<>();
                    map.put("facility_id", f.getId());
                    map.put("facility_name", f.getName());
                    map.put("address", f.getAddress());
                    map.put("latitude", f.getLatitude());
                    map.put("longitude", f.getLongitude());
                    map.put("distance", Math.round(distance));
                    return map;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("type", "station");
        result.put("station_name", stationName);
        result.put("station_location", Map.of(
                "latitude", lat,
                "longitude", lon
        ));
        result.put("facilities", facilities);

        return result;
    }



    //지역명 검색 처리
    private Map<String, Object> searchRegion(String regionName, TmapPoiResultDTO.Poi poi) {

        double lat = Double.parseDouble(poi.getNoorLat());
        double lon = Double.parseDouble(poi.getNoorLon());

        List<Object[]> nearby = facilityRepository.findNearby(lat, lon, RADIUS_REGION);

        List<Map<String, Object>> facilities = nearby.stream()
                .map(obj -> {
                    Facility f = (Facility) obj[0];
                    double distance = (double) obj[1];

                    Map<String, Object> map = new HashMap<>();
                    map.put("facility_id", f.getId());
                    map.put("facility_name", f.getName());
                    map.put("address", f.getAddress());
                    map.put("latitude", f.getLatitude());
                    map.put("longitude", f.getLongitude());
                    map.put("distance", Math.round(distance));
                    return map;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("type", "region");
        result.put("region_name", regionName);
        result.put("region_location", Map.of(
                "latitude", lat,
                "longitude", lon
        ));
        result.put("facilities", facilities);

        return result;
    }



}
