package com.example.astrology.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.calculation.chart.StarId;
import com.example.astrology.calculation.chart.TracuutuviChart;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider;
import com.example.astrology.entity.AppUser;
import com.example.astrology.entity.AstrologyPalace;
import com.example.astrology.entity.BirthChart;
import com.example.astrology.entity.BirthProfile;
import com.example.astrology.entity.ChartPalaceStar;
import com.example.astrology.entity.Star;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.ChartHistoryItem;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;
import com.example.astrology.model.StoredBirthChartPageData;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.repository.AstrologyPalaceRepository;
import com.example.astrology.repository.BirthChartRepository;
import com.example.astrology.repository.BirthProfileRepository;
import com.example.astrology.repository.ChartPalaceStarRepository;
import com.example.astrology.repository.StarRepository;

@Service
public class BirthChartPersistenceService {

	private static final EnumSet<StarId> MAIN_STARS = EnumSet.range(StarId.TU_VI, StarId.PHA_QUAN);
	private static final List<String> CHART_LIMITATIONS = List.of(
			"Can Chi tháng/ngày/giờ chưa được tính; Can Chi ngày tại giờ Tý dùng quy ước đổi ngày lúc 00:00.",
			"Bảng hiện chỉ cho biết ngũ hành Nạp Âm năm, chưa xuất tên nạp âm đầy đủ.",
			"Chưa tính Chủ Mệnh/Chủ Thân, độ sáng sao và các nhóm phụ tinh khác.");

	private final AppUserRepository appUserRepository;
	private final BirthProfileRepository birthProfileRepository;
	private final BirthChartRepository birthChartRepository;
	private final AstrologyPalaceRepository astrologyPalaceRepository;
	private final ChartPalaceStarRepository chartPalaceStarRepository;
	private final StarRepository starRepository;
	private final ObjectMapper objectMapper;
	private final VietnameseLunarCalendarProvider lunarCalendarProvider;

	public BirthChartPersistenceService(
			AppUserRepository appUserRepository,
			BirthProfileRepository birthProfileRepository,
			BirthChartRepository birthChartRepository,
			AstrologyPalaceRepository astrologyPalaceRepository,
			ChartPalaceStarRepository chartPalaceStarRepository,
			StarRepository starRepository,
			ObjectMapper objectMapper,
			VietnameseLunarCalendarProvider lunarCalendarProvider) {
		this.appUserRepository = appUserRepository;
		this.birthProfileRepository = birthProfileRepository;
		this.birthChartRepository = birthChartRepository;
		this.astrologyPalaceRepository = astrologyPalaceRepository;
		this.chartPalaceStarRepository = chartPalaceStarRepository;
		this.starRepository = starRepository;
		this.objectMapper = objectMapper;
		this.lunarCalendarProvider = lunarCalendarProvider;
	}

	@Transactional
	public UUID save(
			AstrologyCalculationInput input,
			BirthChartPageData pageData,
			String principalName) {
		TracuutuviChart chart = requireCompleteChart(input, pageData);
		AppUser owner = principalName == null ? null : findOwner(principalName);
		UUID accessToken = UUID.randomUUID();

		BirthProfile profile = new BirthProfile();
		profile.setUser(owner);
		profile.setFullName(input.name());
		profile.setGender("Nam".equals(input.gender()) ? "MALE" : "FEMALE");
		profile.setBirthDate(input.birthDate());
		profile.setBirthTime(input.birthTime());
		profile.setBirthTimeUnknown(false);
		profile.setBirthCity(input.birthplace());
		profile.setBirthCountry("Vietnam");
		profile = birthProfileRepository.saveAndFlush(profile);

		BirthChart savedChart = new BirthChart();
		savedChart.setUser(owner);
		savedChart.setBirthProfile(profile);
		savedChart.setLunarDay(pageData.lunarDate().day());
		savedChart.setLunarMonth(pageData.lunarDate().month());
		savedChart.setLunarYear(pageData.lunarDate().year());
		savedChart.setLunarLeapMonth(pageData.lunarDate().leapMonth());
		savedChart.setYearCan(chart.input().yearStem().vietnameseName());
		savedChart.setYearChi(chart.input().yearBranch().vietnameseName());
		savedChart.setMenhName(chart.positions().mingPalace().vietnameseName());
		savedChart.setMenhElement(pageData.yearNayinElement());
		savedChart.setCucName(chart.positions().bureau().vietnameseName());
		savedChart.setMingPalaceBranch(chart.positions().mingPalace().vietnameseName());
		savedChart.setThanPalaceBranch(chart.positions().thanPalace().vietnameseName());
		savedChart.setThanLocation(chart.positions().thanPalace().vietnameseName());
		savedChart.setCalculationVersion(chart.ruleSetVersion());
		savedChart.setRawCalculationData(serialize(chart));
		savedChart.setAccessTokenHash(hashAccessToken(accessToken));
		savedChart = birthChartRepository.saveAndFlush(savedChart);

		for (PalaceFunction function : PalaceFunction.values()) {
			EarthlyBranch branch = chart.positions().palaces().positions().get(function);
			if (branch == null) {
				throw new IllegalStateException("Lá số tính được thiếu cung " + function.vietnameseName() + ".");
			}

			AstrologyPalace palace = new AstrologyPalace();
			palace.setChart(savedChart);
			palace.setPalaceOrder(function.ordinal() + 1);
			palace.setPosition(function.ordinal() + 1);
			palace.setPalaceName(function.vietnameseName());
			palace.setEarthlyBranch(branch.name());
			palace = astrologyPalaceRepository.saveAndFlush(palace);

			for (StarId starId : StarId.values()) {
				if (chart.stars().branchOf(starId).filter(branch::equals).isEmpty()) {
					continue;
				}

				Star star = findOrCreateStar(starId);
				ChartPalaceStar chartPalaceStar = new ChartPalaceStar();
				chartPalaceStar.setPalace(palace);
				chartPalaceStar.setStar(star);
				chartPalaceStar.setStarOrder(starId.ordinal());
				chartPalaceStar.setMainStar(MAIN_STARS.contains(starId));
				chartPalaceStarRepository.saveAndFlush(chartPalaceStar);
			}
		}
		if (astrologyPalaceRepository.findAllByChart_IdOrderByPalaceOrderAsc(savedChart.getId()).size() != 12) {
			throw new IllegalStateException("Không thể lưu lá số nếu thiếu một trong 12 cung.");
		}
		return accessToken;
	}

	@Transactional(readOnly = true)
	public StoredBirthChartPageData load(UUID accessToken, String principalName) {
		BirthChart savedChart = principalName == null
				? birthChartRepository
						.findByAccessTokenHashAndUserIsNull(hashAccessToken(accessToken))
						.orElseThrow(BirthChartPersistenceService::notFound)
				: loadOwnedChart(accessToken, principalName);
		return toPageData(savedChart, accessToken);
	}

	@Transactional(readOnly = true)
	public StoredBirthChartPageData loadOwnedChart(Long chartId, String principalName) {
		AppUser owner = appUserRepository.findByEmail(principalName)
				.orElseThrow(BirthChartPersistenceService::notFound);
		BirthChart chart = birthChartRepository.findByIdAndUser_Id(chartId, owner.getId())
				.orElseThrow(BirthChartPersistenceService::notFound);
		return toPageData(chart, null);
	}

	@Transactional(readOnly = true)
	public List<ChartHistoryItem> history(String principalName, List<UUID> guestTokens) {
		List<BirthChart> charts;
		Map<String, UUID> guestTokensByHash = new HashMap<>();
		if (principalName != null) {
			AppUser owner = appUserRepository.findByEmail(principalName)
					.orElseThrow(BirthChartPersistenceService::notFound);
			charts = birthChartRepository.findAllByUser_IdOrderByGeneratedAtDesc(owner.getId());
		} else if (guestTokens.isEmpty()) {
			return List.of();
		} else {
			List<String> hashes = guestTokens.stream()
					.map(token -> {
						String hash = hashAccessToken(token);
						guestTokensByHash.put(hash, token);
						return hash;
					})
					.toList();
			charts = birthChartRepository
					.findAllByAccessTokenHashInAndUserIsNullOrderByGeneratedAtDesc(hashes);
		}

		return charts.stream()
				.map(chart -> {
					String resultUrl = principalName == null
							? "/result/" + guestTokensByHash.get(chart.getAccessTokenHash())
							: "/result/id/" + chart.getId();
					return new ChartHistoryItem(
							resultUrl,
							chart.getBirthProfile().getFullName(),
							chart.getBirthProfile().getBirthDate(),
							chart.getGeneratedAt(),
							chart.getMenhName(),
							chart.getCucName());
				})
				.toList();
	}

	private StoredBirthChartPageData toPageData(BirthChart savedChart, UUID accessToken) {
		BirthProfile profile = savedChart.getBirthProfile();
		TracuutuviChart chart = deserialize(savedChart.getRawCalculationData());
		List<AstrologyPalace> palaces =
				astrologyPalaceRepository.findAllByChart_IdOrderByPalaceOrderAsc(savedChart.getId());
		if (palaces.size() != 12) {
			throw new IllegalStateException("Lá số đã lưu không có đủ 12 cung.");
		}

		List<ChartPalaceDisplay> displays = new ArrayList<>();
		for (AstrologyPalace palace : palaces) {
			PalaceFunction function = functionByVietnameseName(palace.getPalaceName());
			EarthlyBranch branch;
			try {
				branch = EarthlyBranch.valueOf(palace.getEarthlyBranch());
			} catch (IllegalArgumentException | NullPointerException exception) {
				throw new IllegalStateException("Địa chi cung đã lưu không hợp lệ.", exception);
			}
			List<ChartStarDisplay> stars = chartPalaceStarRepository
					.findAllByPalace_IdOrderByStarOrderAsc(palace.getId()).stream()
					.map(this::displayStar)
					.toList();
			displays.add(new ChartPalaceDisplay(function, branch, stars));
		}

		AstrologyCalculationInput input = new AstrologyCalculationInput(
				profile.getFullName(),
				"MALE".equals(profile.getGender()) ? "Nam" : "Nữ",
				profile.getBirthDate(),
				profile.getBirthTime(),
				profile.getBirthCity());
		List<CalculationGap> gaps = new ArrayList<>(List.of(
				CalculationGap.CAN_CHI_RULES,
				CalculationGap.TIMEZONE_AND_LOCATION));
		if (profile.getBirthCity() == null || profile.getBirthCity().isBlank()) {
			gaps.add(CalculationGap.BIRTHPLACE);
		}

		return new StoredBirthChartPageData(
				accessToken,
				input,
				chart,
				new VietnameseLunarCalendarProvider.LunarDate(
						chart.input().lunarYear(),
						chart.input().lunarMonth(),
						chart.input().lunarDay(),
						chart.input().leapMonth()),
				lunarCalendarProvider.implementationName(),
				lunarCalendarProvider.implementationVersion(),
				savedChart.getYearCan() + " " + savedChart.getYearChi(),
				savedChart.getMenhElement(),
				NayinElementCalculator.SOURCE_URL,
				displays,
				gaps,
				CHART_LIMITATIONS);
	}

	private TracuutuviChart requireCompleteChart(AstrologyCalculationInput input, BirthChartPageData pageData) {
		if (input.birthTime() == null
				|| pageData == null || pageData.chart() == null || pageData.lunarDate() == null
				|| pageData.yearCanChi() == null || pageData.yearNayinElement() == null
				|| pageData.chartPalaces().size() != 12
				|| pageData.chart().positions().palaces().positions().size() != 12) {
			throw new IllegalArgumentException("Không lưu được hồ sơ vì lá số chưa tính đầy đủ 12 cung.");
		}
		return pageData.chart();
	}

	private AppUser findOwner(String principalName) {
		return appUserRepository.findByEmail(principalName)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Không tìm thấy tài khoản ứng với principal hiện tại."));
	}

	private BirthChart loadOwnedChart(UUID accessToken, String principalName) {
		AppUser owner = appUserRepository.findByEmail(principalName)
				.orElseThrow(BirthChartPersistenceService::notFound);
		return birthChartRepository
				.findByAccessTokenHashAndUser_Id(hashAccessToken(accessToken), owner.getId())
				.orElseThrow(BirthChartPersistenceService::notFound);
	}

	private Star findOrCreateStar(StarId starId) {
		return starRepository.findByCode(starId.name()).orElseGet(() -> {
			Star star = new Star();
			star.setCode(starId.name());
			star.setName(starId.vietnameseName());
			star.setVietnameseName(starId.vietnameseName());
			star.setStarType(MAIN_STARS.contains(starId) ? "CHINH_TINH" : "PHU_TINH");
			return starRepository.saveAndFlush(star);
		});
	}

	static String hashAccessToken(UUID accessToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(accessToken.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("Không thể tạo mã truy cập an toàn cho hồ sơ.", exception);
		}
	}

	private String serialize(TracuutuviChart chart) {
		try {
			return objectMapper.writeValueAsString(chart);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Không thể mã hóa kết quả lá số để lưu.", exception);
		}
	}

	private TracuutuviChart deserialize(String data) {
		try {
			return objectMapper.readValue(data, TracuutuviChart.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Dữ liệu lá số đã lưu không thể đọc được.", exception);
		}
	}

	private static PalaceFunction functionByVietnameseName(String palaceName) {
		for (PalaceFunction function : PalaceFunction.values()) {
			if (function.vietnameseName().equals(palaceName)) {
				return function;
			}
		}
		throw new IllegalStateException("Tên cung đã lưu không hợp lệ.");
	}

	private ChartStarDisplay displayStar(ChartPalaceStar chartPalaceStar) {
		try {
			StarId starId = StarId.valueOf(chartPalaceStar.getStar().getCode());
			if (!starId.vietnameseName().equals(chartPalaceStar.getStar().getVietnameseName())) {
				throw new IllegalStateException("Mã sao đã lưu không khớp dữ liệu hiển thị.");
			}
			return new ChartStarDisplay(starId.vietnameseName(), starId.element());
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new IllegalStateException("Mã sao đã lưu không hợp lệ.", exception);
		}
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ hoặc bạn không có quyền truy cập.");
	}
}
