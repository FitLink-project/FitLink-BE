package com.fitlink.repository;

import com.fitlink.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {


    Facility findByName(String name);
    // facility 중복 체크용

    @Query("""
        SELECT f,
            (6371000 * acos(
                cos(radians(:lat)) * cos(radians(f.latitude)) *
                cos(radians(f.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(f.latitude))
            )) AS distance
        FROM Facility f
        HAVING distance <= :radius
        ORDER BY distance ASC
    """)//거리 계산 및 반경 10KM 내로 조회
    List<Object[]> findNearby(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius
    );
}
