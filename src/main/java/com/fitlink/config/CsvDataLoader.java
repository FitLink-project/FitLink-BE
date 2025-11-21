package com.fitlink.config;

import com.fitlink.domain.Facility;

import com.fitlink.domain.Program;
import com.fitlink.repository.FacilityRepository;
import com.fitlink.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvDataLoader implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final ProgramRepository programRepository;

    @Override
    public void run(String... args) throws Exception {

        //이미 db에 있으면 건너뛰기
        if (facilityRepository.count() > 0) {
            log.info("CSV Import skipped (already loaded)");
            return;
        }

        // ClassPathResource를 사용하여 JAR 내부 리소스 접근
        ClassPathResource resource = new ClassPathResource("data/final_facilities_programs_with_geo.csv");
        
        if (!resource.exists()) {
            log.warn("CSV file not found: data/final_facilities_programs_with_geo.csv. Skipping data import.");
            return;
        }

        Reader reader = new InputStreamReader(resource.getInputStream(), "UTF-8");
        BufferedReader br = new BufferedReader(reader);
        String line;
        br.readLine();

        while ((line = br.readLine()) != null) {

            // 콤마 포함 문자열 split
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            String ctp = data[0];
            String signgu = data[1];
            String facilityName = data[2];
            String address = data[3];
            // null 또는 빈 값 안전 파싱
            Double lat = safeParseDouble(data[4]);
            Double lng = safeParseDouble(data[5]);

            // 시설 홈페이지 URL (index 6 - 확정)
            String homepageUrl = data[6];

            //  프로그램 정보
            String programName = data[7];
            String target = data[8];
            String time = data[9];
            String days = data[10];

            Integer capacity = safeParseInt(data[11]);
            Integer price = safeParseIntFromDecimal(data[12]);


            // Facility 저장
            Facility facility = facilityRepository.findByName(facilityName);

            if (facility == null) {
                facility = Facility.builder()
                        .name(facilityName)
                        .address(address)
                        .latitude(lat)
                        .longitude(lng)
                        .homepageUrl(homepageUrl)
                        .build();

                facility = facilityRepository.save(facility);
            }

            // Program 저장
            Program program = Program.builder()
                    .facility(facility)
                    .name(programName)
                    .target(target)
                    .time(time)
                    .days(days)
                    .capacity(capacity)
                    .price(price)
                    .build();

            programRepository.save(program);
        }

        br.close();
        System.out.println("CSV Import completed successfully!");
    }

    // 안전 파싱 함수들
    private Double safeParseDouble(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.isEmpty()) return null;
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer safeParseInt(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.isEmpty()) return null;
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer safeParseIntFromDecimal(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.isEmpty()) return null;
        try {
            return Integer.parseInt(value.split("\\.")[0].replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
