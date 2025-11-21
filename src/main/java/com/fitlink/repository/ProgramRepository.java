package com.fitlink.repository;

import com.fitlink.domain.Program;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("""
    SELECT p.name 
    FROM Program p
    WHERE p.facility.id = :facilityId
    ORDER BY p.id ASC
    """)
    List<String> findTop2NamesByFacilityId(@Param("facilityId") Long facilityId, Pageable pageable);

    @Query("""
    SELECT p FROM Program p
    WHERE p.facility.id = :facilityId
    """)
    List<Program> findByFacilityId(@Param("facilityId") Long facilityId);//체육시설ID로 프로그램 조회


}

