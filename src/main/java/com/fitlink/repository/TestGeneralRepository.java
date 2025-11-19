package com.fitlink.repository;

import com.fitlink.domain.TestGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestGeneralRepository extends JpaRepository<TestGeneral, Long> {

    List<TestGeneral> findByUserId(Long userId);
}
