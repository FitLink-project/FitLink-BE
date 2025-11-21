package com.fitlink.repository;

import com.fitlink.domain.Agreement;
import com.fitlink.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    Optional<Agreement> findByUser(Users user);
}

