package com.example.astrology.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.astrology.entity.FriendChart;

@Repository
public interface FriendChartRepository extends JpaRepository<FriendChart, Long> {

	List<FriendChart> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

	Optional<FriendChart> findByIdAndUser_Id(Long id, Long userId);

	Optional<FriendChart> findByAccessTokenHash(String accessTokenHash);

	List<FriendChart> findAllByAccessTokenHashInAndUserIsNullOrderByCreatedAtDesc(List<String> accessTokenHashes);

	Optional<FriendChart> findFirstByUser_IdAndName1AndBirthDate1AndName2AndBirthDate2OrderByCreatedAtDesc(
			Long userId, String name1, LocalDate birthDate1, String name2, LocalDate birthDate2);
}
