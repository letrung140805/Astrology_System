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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.astrology.entity.AppUser;
import com.example.astrology.entity.FriendChart;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.FriendCompatibilityResult;
import com.example.astrology.model.FriendHistoryItem;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.repository.FriendChartRepository;

@Service
public class FriendChartPersistenceService {

	private static final Logger logger = LoggerFactory.getLogger(FriendChartPersistenceService.class);
	private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter BIRTH_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final AppUserRepository appUserRepository;
	private final FriendChartRepository friendChartRepository;

	public FriendChartPersistenceService(
			AppUserRepository appUserRepository,
			FriendChartRepository friendChartRepository) {
		this.appUserRepository = appUserRepository;
		this.friendChartRepository = friendChartRepository;
	}

	@Transactional
	public FriendChart save(
			CouplePersonData p1,
			CouplePersonData p2,
			FriendCompatibilityResult compatibility,
			String principalName) {

		AppUser owner = principalName != null ? findOwner(principalName) : null;
		UUID accessToken = UUID.randomUUID();

		LocalDate birthDate1 = parseLocalDate(p1.birthDate(), p1.solarYear(), p1.solarMonth(), p1.solarDay());
		LocalTime birthTime1 = parseLocalTime(p1.birthTime());

		LocalDate birthDate2 = parseLocalDate(p2.birthDate(), p2.solarYear(), p2.solarMonth(), p2.solarDay());
		LocalTime birthTime2 = parseLocalTime(p2.birthTime());

		FriendChart chart = new FriendChart();
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

		chart.setPartnerScore(compatibility.partnerScore());
		chart.setCommunicationScore(compatibility.communicationScore());
		chart.setVisionScore(compatibility.visionScore());
		chart.setValuesScore(compatibility.valuesScore());
		chart.setMoneyScore(compatibility.moneyScore());

		chart.setPartnerInterpretation(compatibility.partnerInterpretation());
		chart.setCommunicationInterpretation(compatibility.communicationInterpretation());
		chart.setVisionInterpretation(compatibility.visionInterpretation());
		chart.setValuesInterpretation(compatibility.valuesInterpretation());
		chart.setMoneyInterpretation(compatibility.moneyInterpretation());

		chart.setConclusion(compatibility.conclusion());
		chart.setAdvice(compatibility.advice());
		chart.setAccessTokenHash(hashAccessToken(accessToken));

		return friendChartRepository.saveAndFlush(chart);
	}

	@Transactional(readOnly = true)
	public FriendChart loadOwned(Long id, String principalName) {
		AppUser owner = findOwner(principalName);
		return friendChartRepository.findByIdAndUser_Id(id, owner.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lá số tương hợp bạn bè"));
	}

	@Transactional(readOnly = true)
	public FriendChart loadByToken(UUID token) {
		return friendChartRepository.findByAccessTokenHash(hashAccessToken(token))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lá số tương hợp bạn bè"));
	}

	@Transactional(readOnly = true)
	public List<FriendHistoryItem> history(String principalName, List<UUID> guestTokens) {
		List<FriendChart> charts;
		Map<String, UUID> guestTokensByHash = new HashMap<>();

		if (principalName != null) {
			AppUser owner = appUserRepository.findByEmail(principalName)
					.or(() -> appUserRepository.findByUsername(principalName))
					.orElse(null);
			if (owner == null) {
				return List.of();
			}
			charts = friendChartRepository.findAllByUser_IdOrderByCreatedAtDesc(owner.getId());
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
			charts = friendChartRepository.findAllByAccessTokenHashInAndUserIsNullOrderByCreatedAtDesc(hashes);
		}

		return charts.stream()
				.map(chart -> {
					String viewUrl = principalName != null
							? "/tuong-hop-ban-be/id/" + chart.getId()
							: "/tuong-hop-ban-be/token/" + guestTokensByHash.get(chart.getAccessTokenHash());
					return new FriendHistoryItem(
							chart.getId(),
							viewUrl,
							chart.getName1(),
							chart.getName2(),
							chart.getTotalScore() != null ? chart.getTotalScore() : 0,
							chart.getRating() != null ? chart.getRating() : "",
							chart.getRatingColor() != null ? chart.getRatingColor() : "badge-primary",
							chart.getCreatedAt(),
							chart.getPartnerScore(),
							chart.getCommunicationScore(),
							chart.getVisionScore(),
							chart.getValuesScore(),
							chart.getMoneyScore()
					);
				})
				.toList();
	}

	@Transactional
	public void delete(Long id, String principalName) {
		AppUser owner = findOwner(principalName);
		FriendChart chart = friendChartRepository.findByIdAndUser_Id(id, owner.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lá số tương hợp bạn bè"));
		friendChartRepository.delete(chart);
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
