package com.fitlink.repository.LinkuRepository;

import com.fitlink.domain.Linku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkuRepository extends JpaRepository<Linku, Long>, LinkuRepositoryCustom {
}

