package com.fitlink.repository;

import com.fitlink.domain.TestGeneral;
import com.fitlink.domain.TestKookmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestKookminRepository extends JpaRepository<TestKookmin, Long> {

    List<TestGeneral> findByUserId(Long userId);
}
