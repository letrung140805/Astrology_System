package com.example.astrology.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.astrology.entity.AppUser;
import com.example.astrology.entity.CoupleChart;
import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CoupleHistoryItem;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.repository.CoupleChartRepository;

@Service
public class CoupleChartPersistenceService {

	private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter BIRTH_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final AppUserRepository appUserRepository;
	private final CoupleChartRepository coupleChartRepository;

	public CoupleChartPersistenceService(
			AppUserRepository appUserRepository,
			CoupleChartRepository coupleChartRepository) {
		this.appUserRepository = appUserRepository;
		this.coupleChartRepository = coupleChartRepository;
	}

	@Transactional
	public CoupleChart save(
			CouplePersonData p1,
			CouplePersonData p2,
			CoupleCompatibilityResult compatibility,
			String principalName) {

		AppUser owner = principalName != null ? findOwner(principalName) : null;
		UUID accessToken = UUID.randomUUID();

		LocalDate birthDate1 = parseLocalDate(p1.birthDate(), p1.solarYear(), p1.solarMonth(), p1.solarDay());
		LocalTime birthTime1 = parseLocalTime(p1.birthTime());

		LocalDate birthDate2 = parseLocalDate(p2.birthDate(), p2.solarYear(), p2.solarMonth(), p2.solarDay());
		LocalTime birthTime2 = parseLocalTime(p2.birthTime());

		CoupleChart chart = new CoupleChart();
		chart.setUser(owner);

		// Person 1
		chart.setName1(p1.name());
		chart.setGender1(p1.gender());
		chart.setBirthDate1(birthDate1);
		chart.setBirthTime1(birthTime1);
		chart.setBirthplace1(p1.birthplace());
		chart.setBanMenh1(p1.banMenhFull());
		chart.setCanChi1(p1.yearCanChi());

		// Person 2
		chart.setName2(p2.name());
		chart.setGender2(p2.gender());
		chart.setBirthDate2(birthDate2);
		chart.setBirthTime2(birthTime2);
		chart.setBirthplace2(p2.birthplace());
		chart.setBanMenh2(p2.banMenhFull());
		chart.setCanChi2(p2.yearCanChi());

		// Compatibility scores & interpretations
		chart.setTotalScore(compatibility.totalScore());
		chart.setRating(compatibility.rating());
		chart.setRatingColor(compatibility.ratingColor());

		chart.setEmotionalScore(compatibility.emotionalScore());
		chart.setCommunicationScore(compatibility.communicationScore());
		chart.setFinancialScore(compatibility.financialScore());
		chart.setStabilityScore(compatibility.stabilityScore());
		chart.setLivingTogetherScore(compatibility.livingTogetherScore());

		chart.setEmotionalInterpretation(compatibility.emotionalInterpretation());
		chart.setCommunicationInterpretation(compatibility.communicationInterpretation());
		chart.setFinancialInterpretation(compatibility.financialInterpretation());
		chart.setStabilityInterpretation(compatibility.stabilityInterpretation());
		chart.setLivingTogetherInterpretation(compatibility.livingTogetherInterpretation());

		chart.setConclusion(compatibility.conclusion());
		chart.setAdvice(compatibility.advice());
		chart.setAccessTokenHash(hashAccessToken(accessToken));

		return coupleChartRepository.saveAndFlush(chart);
	}

	@Transactional(readOnly = true)
	public CoupleChart loadOwned(Long id, String principalName) {
		AppUser owner = findOwner(principalName);
		return coupleChartRepository.findByIdAndUser_Id(id, owner.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lá số tương hợp cặp đôi"));
	}

	@Transactional(readOnly = true)
	public CoupleChart loadByToken(UUID token) {
		return coupleChartRepository.findByAccessTokenHash(hashAccessToken(token))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lá số tương hợp cặp đôi"));
	}

	@Transactional(readOnly = true)
	public List<CoupleHistoryItem> history(String principalName, List<UUID> guestTokens) {
		List<CoupleChart> charts;
		Map<String, UUID> guestTokensByHash = new HashMap<>();

		if (principalName != null) {
			AppUser owner = appUserRepository.findByEmail(principalName)
					.or(() -> appUserRepository.findByUsername(principalName))
					.orElse(null);
			if (owner == null) {
				return List.of();
			}
			charts = coupleChartRepository.findAllByUser_IdOrderByCreatedAtDesc(owner.getId());
		} else if (guestTokens == null || guestTokens.isEmpty()) {
			return List.of();
		} else {
			List<String> hashes = guestTokens.stream()
					.map(token -> {
						String hash = hashAccessToken(token);
						guestTokensByHash.put(hash, token);
						return hash;
					})
					.toList();
			charts = coupleChartRepository.findAllByAccessTokenHashInAndUserIsNullOrderByCreatedAtDesc(hashes);
		}

		return charts.stream()
				.map(chart -> {
					String viewUrl = principalName != null
							? "/tuong-hop/id/" + chart.getId()
							: "/tuong-hop/token/" + guestTokensByHash.get(chart.getAccessTokenHash());
					return new CoupleHistoryItem(
							chart.getId(),
							viewUrl,
							chart.getName1(),
							chart.getName2(),
							chart.getTotalScore() != null ? chart.getTotalScore() : 0,
							chart.getRating() != null ? chart.getRating() : "",
							chart.getRatingColor() != null ? chart.getRatingColor() : "badge-primary",
							chart.getCreatedAt(),
							chart.getEmotionalScore(),
							chart.getCommunicationScore(),
							chart.getFinancialScore(),
							chart.getStabilityScore(),
							chart.getLivingTogetherScore()
					);
				})
				.toList();
	}

	@Transactional
	public void delete(Long id, String principalName) {
		AppUser owner = findOwner(principalName);
		CoupleChart chart = coupleChartRepository.findByIdAndUser_Id(id, owner.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lá số tương hợp cặp đôi"));
		coupleChartRepository.delete(chart);
	}

	private AppUser findOwner(String principalName) {
		if (principalName == null || principalName.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập");
		}
		return appUserRepository.findByEmail(principalName)
				.or(() -> appUserRepository.findByUsername(principalName))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin tài khoản người dùng"));
	}

	private LocalDate parseLocalDate(String dateStr, Integer year, Integer month, Integer day) {
		if (dateStr != null && !dateStr.isBlank()) {
			try {
				return LocalDate.parse(dateStr, BIRTH_DATE_FORMAT);
			} catch (Exception ignored) {
			}
		}
		if (year != null && month != null && day != null) {
			return LocalDate.of(year, month, day);
		}
		return LocalDate.now();
	}

	private LocalTime parseLocalTime(String timeStr) {
		if (timeStr == null || timeStr.isBlank()) {
			return null;
		}
		try {
			return LocalTime.parse(timeStr, BIRTH_TIME_FORMAT);
		} catch (Exception ex) {
			try {
				return LocalTime.parse(timeStr);
			} catch (Exception ignored) {
				return null;
			}
		}
	}

	private String hashAccessToken(UUID token) {
		if (token == null) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedHash = digest.digest(token.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(encodedHash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 algorithm is not available", ex);
		}
	}
}
