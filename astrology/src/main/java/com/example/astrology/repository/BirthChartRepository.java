package com.example.astrology.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.BirthChart;

public interface BirthChartRepository extends JpaRepository<BirthChart, Long> {

	Optional<BirthChart> findByAccessTokenHashAndUser_Id(String accessTokenHash, Long userId);

	Optional<BirthChart> findByAccessTokenHashAndUserIsNull(String accessTokenHash);

	Optional<BirthChart> findByIdAndUser_Id(Long id, Long userId);

	List<BirthChart> findAllByUser_IdOrderByGeneratedAtDesc(Long userId);

	long countByUser_Id(Long userId);

	List<BirthChart> findAllByAccessTokenHashInAndUserIsNullOrderByGeneratedAtDesc(List<String> accessTokenHashes);
}
