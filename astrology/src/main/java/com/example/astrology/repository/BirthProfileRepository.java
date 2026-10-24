package com.example.astrology.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.BirthProfile;

public interface BirthProfileRepository extends JpaRepository<BirthProfile, Long> {

	Optional<BirthProfile> findByIdAndUser_Id(Long id, Long userId);

	List<BirthProfile> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
