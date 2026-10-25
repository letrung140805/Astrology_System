package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import com.example.astrology.calculation.calendar.HoNgocDucLunarCalendarProvider;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.calendar.SexagenaryYearCalculator;
import com.example.astrology.calculation.chart.FiveElementsBureauCalculator;
import com.example.astrology.calculation.chart.StarPlacementService;
import com.example.astrology.calculation.chart.TracuutuviChangShengCalculator;
import com.example.astrology.calculation.chart.TracuutuviChartCalculator;
import com.example.astrology.calculation.chart.TracuutuviFourTransformationsCalculator;
import com.example.astrology.calculation.chart.TracuutuviMajorStarsCalculator;
import com.example.astrology.calculation.chart.TracuutuviMonthHourStarsCalculator;
import com.example.astrology.calculation.chart.TracuutuviPositionsCalculator;
import com.example.astrology.calculation.chart.TracuutuviYearStarsCalculator;
import com.example.astrology.entity.AppUser;
import com.example.astrology.repository.AstrologyPalaceRepository;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.repository.BirthChartRepository;
import com.example.astrology.repository.BirthProfileRepository;
import com.example.astrology.repository.ChartPalaceStarRepository;
import com.example.astrology.repository.StarRepository;

@DataJpaTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:astroai;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false"
})
@Import({
		BirthChartPersistenceService.class,
		HoNgocDucLunarCalendarProvider.class,
		BirthChartPersistenceServiceTests.JsonConfiguration.class
})
class BirthChartPersistenceServiceTests {

	@Autowired
	private BirthChartPersistenceService persistenceService;

	@Autowired
	private BirthProfileRepository birthProfileRepository;

	@Autowired
	private BirthChartRepository birthChartRepository;

	@Autowired
	private AstrologyPalaceRepository astrologyPalaceRepository;

	@Autowired
	private ChartPalaceStarRepository chartPalaceStarRepository;

	@Autowired
	private StarRepository starRepository;

	@Autowired
	private AppUserRepository appUserRepository;

	@MockitoSpyBean
	private ChartPalaceStarRepository chartPalaceStarRepositorySpy;

	@Test
	void savesAndReloadsBirthProfileAndChartWithTwelvePalaces() {
		UUID accessToken = persistenceService.save(input(LocalTime.of(10, 30)), calculatedChart(), null);

		var saved = persistenceService.load(accessToken, null);
		var chart = birthChartRepository
				.findByAccessTokenHashAndUserIsNull(hash(accessToken)).orElseThrow();
		var palaces = astrologyPalaceRepository.findAllByChart_IdOrderByPalaceOrderAsc(chart.getId());
		int savedStars = palaces.stream()
				.mapToInt(palace -> chartPalaceStarRepository
						.findAllByPalace_IdOrderByStarOrderAsc(palace.getId()).size())
				.sum();

		assertEquals(1, birthProfileRepository.count());
		assertEquals(1, birthChartRepository.count());
		assertEquals(12, palaces.size());
		assertEquals(12, saved.chartPalaces().size());
		assertTrue(savedStars > 0);
		assertEquals("tracuutuvi-public-rules-2026-10-24+quanshu-supplement-1+cestu-79ebf331",
				chart.getCalculationVersion());
		assertNotNull(chart.getGeneratedAt());
		assertNotNull(chart.getRawCalculationData());
		assertEquals(64, chart.getAccessTokenHash().length());
		assertTrue(!chart.getAccessTokenHash().equals(accessToken.toString()));
		assertEquals(1, persistenceService.history(null, java.util.List.of(accessToken)).size());
	}

	@Test
	void chartOwnedByAnotherPrincipalIsNotAccessible() {
		AppUser owner = appUserRepository.saveAndFlush(user("owner@example.test"));
		appUserRepository.saveAndFlush(user("other@example.test"));
		UUID accessToken = persistenceService.save(input(LocalTime.of(10, 30)), calculatedChart(), owner.getEmail());

		assertThrows(ResponseStatusException.class,
				() -> persistenceService.load(accessToken, "other@example.test"));
		assertThrows(ResponseStatusException.class, () -> persistenceService.load(accessToken, null));
		assertNotNull(persistenceService.load(accessToken, owner.getEmail()));
		Long chartId = birthChartRepository
				.findByAccessTokenHashAndUser_Id(hash(accessToken), owner.getId())
				.orElseThrow().getId();
		assertThrows(ResponseStatusException.class,
				() -> persistenceService.loadOwnedChart(chartId, "other@example.test"));
	}

	@Test
	void incompleteCalculationDoesNotPersistProfileOrChart() {
		var incomplete = calculationService().calculate(input(null));

		assertThrows(IllegalArgumentException.class,
				() -> persistenceService.save(input(null), incomplete, null));

		assertEquals(0, birthProfileRepository.count());
		assertEquals(0, birthChartRepository.count());
		assertEquals(0, astrologyPalaceRepository.count());
		assertEquals(0, chartPalaceStarRepository.count());
		assertEquals(0, starRepository.count());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void failureWhileSavingStarsRollsBackProfileChartAndPalaces() {
		doThrow(new IllegalStateException("injected persistence failure"))
				.when(chartPalaceStarRepositorySpy).saveAndFlush(any());

		assertThrows(IllegalStateException.class,
				() -> persistenceService.save(input(LocalTime.of(10, 30)), calculatedChart(), null));

		assertEquals(0, birthProfileRepository.count());
		assertEquals(0, birthChartRepository.count());
		assertEquals(0, astrologyPalaceRepository.count());
		assertEquals(0, chartPalaceStarRepository.count());
		assertEquals(0, starRepository.count());
	}

	private com.example.astrology.model.BirthChartPageData calculatedChart() {
		return calculationService().calculate(input(LocalTime.of(10, 30)));
	}

	private static BirthChartApplicationService calculationService() {
		var yearCalculator = new SexagenaryYearCalculator();
		return new BirthChartApplicationService(
				new AstrologyCalculationService(new BirthInputNormalizationService()),
				new TracuutuviChartCalculator(
						new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator()),
						new TracuutuviMajorStarsCalculator(new StarPlacementService()),
						new TracuutuviYearStarsCalculator(new StarPlacementService()),
						new TracuutuviMonthHourStarsCalculator(new StarPlacementService()),
						new TracuutuviFourTransformationsCalculator(new StarPlacementService()),
						new TracuutuviChangShengCalculator(new StarPlacementService())),
				new HoNgocDucLunarCalendarProvider(),
				yearCalculator,
				new NayinElementCalculator(yearCalculator));
	}

	private static AstrologyCalculationInput input(LocalTime time) {
		return new AstrologyCalculationInput(
				"Kiểm thử", "Nữ", LocalDate.of(1999, 6, 12), time, "Hà Nội");
	}

	private static AppUser user(String email) {
		AppUser user = new AppUser();
		user.setEmail(email);
		user.setUsername(email);
		user.setPasswordHash("test-password-hash");
		user.setFullName(email);
		return user;
	}

	private static String hash(UUID accessToken) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(accessToken.toString().getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class JsonConfiguration {

		@Bean
		@Primary
		ObjectMapper testObjectMapper() {
			return new ObjectMapper().findAndRegisterModules();
		}
	}
}
