package com.fitlink.repository;

import com.fitlink.domain.TestKookmin;
import com.fitlink.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestKookminRepository extends JpaRepository<TestKookmin, Long> {
    Optional<TestKookmin> findTopByUserOrderByCreatedAtDesc(Users user);
    List<TestKookmin> findByUserId(Long userId);
}
