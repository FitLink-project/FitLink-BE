package com.fitlink.repository;

import com.fitlink.domain.FitnessResult;
import com.fitlink.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FitnessResultRepository extends JpaRepository<FitnessResult, Long> {

    List<FitnessResult> findByUser(Users user);
}
