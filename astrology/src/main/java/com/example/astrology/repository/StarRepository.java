package com.example.astrology.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.Star;

public interface StarRepository extends JpaRepository<Star, Long> {

	Optional<Star> findByCode(String code);
}
