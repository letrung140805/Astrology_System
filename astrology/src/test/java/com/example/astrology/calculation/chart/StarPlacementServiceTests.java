package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;

class StarPlacementServiceTests {

	private final StarPlacementService service = new StarPlacementService();

	@Test
	void allowsMultipleStarsAtOneBranchWithoutOverwritingEarlierPlacements() {
		var evidence = verifiedEvidence(StarRuleGroup.FOURTEEN_MAJOR_STARS);
		var initial = StarChart.empty().withPlacement(StarId.TU_VI, EarthlyBranch.TY);

		var updated = service.addVerifiedPlacement(
				initial, StarId.THIEN_CO, EarthlyBranch.TY, evidence);

		assertNotSame(initial, updated);
		assertEquals(1, initial.starsAt(EarthlyBranch.TY).size());
		assertEquals(2, updated.starsAt(EarthlyBranch.TY).size());
		assertEquals(EarthlyBranch.TY, updated.branchOf(StarId.THIEN_CO).orElseThrow());
	}

	@Test
	void rejectsConflictingPlacementInsteadOfOverwriting() {
		var chart = StarChart.empty().withPlacement(StarId.TU_VI, EarthlyBranch.TY);

		assertThrows(IllegalArgumentException.class,
				() -> chart.withPlacement(StarId.TU_VI, EarthlyBranch.SUU));
		assertEquals(EarthlyBranch.TY, chart.branchOf(StarId.TU_VI).orElseThrow());
	}

	@Test
	void addingIdenticalPlacementIsIdempotent() {
		var chart = StarChart.empty().withPlacement(StarId.TU_VI, EarthlyBranch.TY);

		assertSame(chart, chart.withPlacement(StarId.TU_VI, EarthlyBranch.TY));
	}

	@Test
	void refusesToApplyAnUnverifiedRuleGroup() {
		var pending = StarRuleEvidence.pending(
				StarRuleGroup.FOUR_TRANSFORMATIONS, "viet-tu-vi-pending-1");

		assertThrows(IllegalStateException.class, () -> service.addVerifiedPlacement(
				StarChart.empty(), StarId.TU_VI, EarthlyBranch.TY, pending));
	}

	@Test
	void verifiedRuleEvidenceMustCarrySourceAndVersion() {
		assertThrows(IllegalArgumentException.class, () -> new StarRuleEvidence(
				StarRuleGroup.STAR_BRIGHTNESS,
				RuleStatus.VERIFIED,
				"rules-1",
				List.of()));
	}

	@Test
	void everyStarRuleGroupStartsPendingUntilItHasDocumentedEvidence() {
		var ruleSet = StarRuleSet.pendingInitial();

		assertEquals(StarRuleGroup.values().length, ruleSet.evidence().size());
		assertFalse(ruleSet.isFullyVerified());
		ruleSet.evidence().values().forEach(evidence ->
				assertEquals(RuleStatus.PENDING_CONFIRMATION, evidence.status()));
	}

	private StarRuleEvidence verifiedEvidence(StarRuleGroup group) {
		return new StarRuleEvidence(
				group,
				RuleStatus.VERIFIED,
				"test-rules-1",
				List.of(new RuleSource(
						"Test source",
						URI.create("https://example.com/verified-rules"),
						"edition-1",
						"section-1")));
	}
}
