package com.on.eye.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.on.eye.api.domain.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String name);
}
