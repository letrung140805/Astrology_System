package com.example.astrology.calculation.chart;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Map;

import com.example.astrology.calculation.calendar.BirthDateInput;
import com.example.astrology.calculation.calendar.CycleDateAdjustment;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;
import com.example.astrology.calculation.calendar.ZiHourDayBoundary;

final class TuviVnReferenceFixtures {

	private static final LocalDate SOLAR_DATE = LocalDate.of(2005, 8, 14);
	private static final LocalTime BIRTH_TIME = LocalTime.of(18, 30);
	private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final BirthDateInput BIRTH_DATE = BirthDateInput.solar(SOLAR_DATE);

	private TuviVnReferenceFixtures() {
	}

	static ReferenceChartFixture birthChart() {
		return new ReferenceChartFixture(
				"tuvi-vn-2005-anonymous-01",
				ReferenceFixtureStatus.READY_FOR_COMPARISON,
				new ReferenceChartInput(
						BIRTH_DATE,
						BIRTH_TIME.toString(),
						"Nam",
						null,
						TIME_ZONE.getId(),
						null,
						ZiHourDayBoundary.UNSPECIFIED),
				new NormalizedInputSnapshot(
						BIRTH_DATE,
						BIRTH_TIME,
						TIME_ZONE.getId(),
						ZoneOffset.ofHours(7).getId(),
						HourBranch.YOU,
						CycleDateAdjustment.NONE),
				new ReferenceChartSource(
						"Tuvi.vn generated chart (anonymized)",
						URI.create("https://tuvi.vn/la-so-am-nam-am-duong-thuan-ly-10-7-2005-dau-80841"),
						LocalDate.of(2026, 10, 24),
						"Manual transcription of structured chart labels",
						"Solar 2005-08-14, 18:30, GMT+7, male; displayed lunar date 10/7 (non-leap). "
								+ "Personal name and query parameters omitted. The site does not publish the "
								+ "calculation rules for this chart.",
						"User-provided chart; only structural calculation fields are recorded."),
				expectedChart());
	}

	static LunarChartInput lunarChartInput() {
		return new LunarChartInput(
				2005,
				HeavenlyStem.YI,
				EarthlyBranch.DAU,
				7,
				false,
				10,
				HourBranch.YOU,
				BirthSex.MALE);
	}

	private static ChartSnapshot expectedChart() {
		var palaces = Map.ofEntries(
				Map.entry(PalaceFunction.MENH, EarthlyBranch.HOI),
				Map.entry(PalaceFunction.HUYNH_DE, EarthlyBranch.TUAT),
				Map.entry(PalaceFunction.PHU_THE, EarthlyBranch.DAU),
				Map.entry(PalaceFunction.TU_TUC, EarthlyBranch.THAN),
				Map.entry(PalaceFunction.TAI_BACH, EarthlyBranch.MUI),
				Map.entry(PalaceFunction.TAT_ACH, EarthlyBranch.NGO),
				Map.entry(PalaceFunction.THIEN_DI, EarthlyBranch.TI),
				Map.entry(PalaceFunction.NO_BOC, EarthlyBranch.THIN),
				Map.entry(PalaceFunction.QUAN_LOC, EarthlyBranch.MAO),
				Map.entry(PalaceFunction.DIEN_TRACH, EarthlyBranch.DAN),
				Map.entry(PalaceFunction.PHUC_DUC, EarthlyBranch.SUU),
				Map.entry(PalaceFunction.PHU_MAU, EarthlyBranch.TY));
		var majorStars = Map.ofEntries(
				Map.entry(StarId.TU_VI.name(), EarthlyBranch.MAO),
				Map.entry(StarId.THIEN_CO.name(), EarthlyBranch.DAN),
				Map.entry(StarId.THAI_DUONG.name(), EarthlyBranch.TY),
				Map.entry(StarId.VU_KHUC.name(), EarthlyBranch.HOI),
				Map.entry(StarId.THIEN_DONG.name(), EarthlyBranch.TUAT),
				Map.entry(StarId.LIEM_TRINH.name(), EarthlyBranch.MUI),
				Map.entry(StarId.THIEN_PHU.name(), EarthlyBranch.SUU),
				Map.entry(StarId.THAI_AM.name(), EarthlyBranch.DAN),
				Map.entry(StarId.THAM_LANG.name(), EarthlyBranch.MAO),
				Map.entry(StarId.CU_MON.name(), EarthlyBranch.THIN),
				Map.entry(StarId.THIEN_TUONG.name(), EarthlyBranch.TI),
				Map.entry(StarId.THIEN_LUONG.name(), EarthlyBranch.NGO),
				Map.entry(StarId.THAT_SAT.name(), EarthlyBranch.MUI),
				Map.entry(StarId.PHA_QUAN.name(), EarthlyBranch.HOI));
		return new ChartSnapshot(
				EnumSet.of(
						ChartSection.FIVE_ELEMENTS_BUREAU,
						ChartSection.MING_PALACE,
						ChartSection.THAN_PALACE,
						ChartSection.TWELVE_PALACES,
						ChartSection.STARS),
				Map.of(),
				null,
				"Thổ Ngũ Cục",
				EarthlyBranch.HOI,
				EarthlyBranch.TI,
				palaces,
				majorStars,
				Map.of(),
				Map.of());
	}
}
