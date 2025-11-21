package com.fitlink.service;

import com.fitlink.domain.Program;
import com.fitlink.repository.ProgramRepository;
import com.fitlink.service.FacilityService;
import com.fitlink.domain.Facility;
import com.fitlink.repository.FacilityRepository;
import com.fitlink.web.dto.FacilityDetailResponseDTO;
import com.fitlink.web.dto.FacilityProgramsResponseDTO;
import com.fitlink.web.dto.NearByRequestDTO;
import com.fitlink.web.dto.NearbyFacilityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final ProgramRepository programRepository;
    private final double SEARCH_RADIUS = 10000; // 10km

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
    }

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
    }

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
    }


    private String cleanTarget(String target) {
        if (target == null) return "";
        return target.replace("\"", "");
    }


}
