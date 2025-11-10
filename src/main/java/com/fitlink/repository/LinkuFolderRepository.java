package com.fitlink.repository;

import com.fitlink.domain.mapping.LinkuFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkuFolderRepository  extends JpaRepository<LinkuFolder, Long> {
    Optional<Object> findById(long l);
}

