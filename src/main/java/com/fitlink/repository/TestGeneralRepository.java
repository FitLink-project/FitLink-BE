package com.fitlink.repository;

import com.fitlink.domain.TestGeneral;
import com.fitlink.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestGeneralRepository extends JpaRepository<TestGeneral, Long> {
    Optional<TestGeneral> findTopByUserOrderByCreatedAtDesc(Users user);
    List<TestGeneral> findByUserId(Long userId);
}
