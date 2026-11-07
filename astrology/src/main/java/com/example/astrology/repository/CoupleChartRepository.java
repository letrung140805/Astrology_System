package com.example.astrology.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.astrology.entity.CoupleChart;

@Repository
public interface CoupleChartRepository extends JpaRepository<CoupleChart, Long> {

	List<CoupleChart> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

	Optional<CoupleChart> findByIdAndUser_Id(Long id, Long userId);

	Optional<CoupleChart> findByAccessTokenHash(String accessTokenHash);

	List<CoupleChart> findAllByAccessTokenHashInAndUserIsNullOrderByCreatedAtDesc(List<String> accessTokenHashes);
}
