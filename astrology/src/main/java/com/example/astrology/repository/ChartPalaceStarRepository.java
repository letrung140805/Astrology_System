package com.example.astrology.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.ChartPalaceStar;

public interface ChartPalaceStarRepository extends JpaRepository<ChartPalaceStar, Long> {

	List<ChartPalaceStar> findAllByPalace_IdOrderByStarOrderAsc(Long palaceId);
}
