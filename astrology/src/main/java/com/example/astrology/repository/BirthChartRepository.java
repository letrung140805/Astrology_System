package com.example.astrology.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.BirthChart;

public interface BirthChartRepository extends JpaRepository<BirthChart, Long> {

	Optional<BirthChart> findByIdAndUser_Id(Long id, Long userId);

	List<BirthChart> findAllByUser_IdOrderByGeneratedAtDesc(Long userId);
}
