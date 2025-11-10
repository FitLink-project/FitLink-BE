package com.fitlink.repository;

import com.fitlink.domain.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findByDomainTail(String domainTail);

    Optional<Domain> findById(long l);
}

