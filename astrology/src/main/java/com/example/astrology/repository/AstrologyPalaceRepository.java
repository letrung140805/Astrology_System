package com.example.astrology.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.AstrologyPalace;

public interface AstrologyPalaceRepository extends JpaRepository<AstrologyPalace, Long> {

	List<AstrologyPalace> findAllByChart_IdOrderByPalaceOrderAsc(Long chartId);
}
