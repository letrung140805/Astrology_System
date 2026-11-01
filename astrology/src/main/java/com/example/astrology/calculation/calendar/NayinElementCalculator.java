package com.example.astrology.calculation.calendar;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.chart.FiveElementsBureau;

@Service
public class NayinElementCalculator {

	public static final String SOURCE_REVISION = "79ebf331e95e8f2005a0c8ef62b82a2a845fc893";
	public static final String SOURCE_URL = "https://github.com/HAHVAC/CEStu-vi-ai/blob/"
			+ SOURCE_REVISION + "/lib/ziwei/constants.ts";

	private static final List<NayinElement> ELEMENTS_BY_PAIR = List.of(
			NayinElement.METAL, NayinElement.FIRE, NayinElement.WOOD, NayinElement.EARTH,
			NayinElement.METAL, NayinElement.FIRE, NayinElement.WATER, NayinElement.EARTH,
			NayinElement.METAL, NayinElement.WOOD, NayinElement.WATER, NayinElement.EARTH,
			NayinElement.FIRE, NayinElement.WOOD, NayinElement.WATER, NayinElement.METAL,
			NayinElement.FIRE, NayinElement.WOOD, NayinElement.EARTH, NayinElement.METAL,
			NayinElement.FIRE, NayinElement.WATER, NayinElement.EARTH, NayinElement.METAL,
			NayinElement.WOOD, NayinElement.WATER, NayinElement.EARTH, NayinElement.FIRE,
			NayinElement.WOOD, NayinElement.WATER);

	private static final List<String> FULL_NAMES_BY_PAIR = List.of(
			"Hải Trung Kim", "Lư Trung Hỏa", "Đại Lâm Mộc", "Lộ Bàng Thổ",
			"Kiếm Phong Kim", "Sơn Đầu Hỏa", "Giản Hạ Thủy", "Thành Đầu Thổ",
			"Bạch Lạp Kim", "Dương Liễu Mộc", "Tuyền Trung Thủy", "Ốc Thượng Thổ",
			"Tích Lịch Hỏa", "Tùng Bách Mộc", "Trường Lưu Thủy", "Sa Trung Kim",
			"Sơn Hạ Hỏa", "Bình Địa Mộc", "Bích Thượng Thổ", "Kim Bạch Kim",
			"Phúc Đăng Hỏa", "Thiên Hà Thủy", "Đại Trạch Thổ", "Thoa Xuyến Kim",
			"Tang Đố Mộc", "Đại Khê Thủy", "Sa Trung Thổ", "Thiên Thượng Hỏa",
			"Thạch Lựu Mộc", "Đại Hải Thủy");

	private final SexagenaryYearCalculator sexagenaryYearCalculator;

	public NayinElementCalculator(SexagenaryYearCalculator sexagenaryYearCalculator) {
		this.sexagenaryYearCalculator = Objects.requireNonNull(
				sexagenaryYearCalculator, "Bộ tính Can Chi năm không được để trống.");
	}

	public NayinElement calculate(int lunarYear) {
		int cyclePosition = sexagenaryYearCalculator.cyclePosition(lunarYear);
		return ELEMENTS_BY_PAIR.get(cyclePosition / 2);
	}

	public String calculateFullName(int lunarYear) {
		int cyclePosition = sexagenaryYearCalculator.cyclePosition(lunarYear);
		return FULL_NAMES_BY_PAIR.get(cyclePosition / 2);
	}

	public String calculateRelationship(NayinElement menhElement, FiveElementsBureau bureau) {
		if (menhElement == null || bureau == null) {
			return "";
		}
		NayinElement cucElement = switch (bureau) {
			case WATER_TWO -> NayinElement.WATER;
			case WOOD_THREE -> NayinElement.WOOD;
			case METAL_FOUR -> NayinElement.METAL;
			case EARTH_FIVE -> NayinElement.EARTH;
			case FIRE_SIX -> NayinElement.FIRE;
		};

		if (menhElement == cucElement) {
			return "Mệnh Cục tỷ hòa";
		}

		// Menh sinh Cuc
		if ((menhElement == NayinElement.WOOD && cucElement == NayinElement.FIRE)
				|| (menhElement == NayinElement.FIRE && cucElement == NayinElement.EARTH)
				|| (menhElement == NayinElement.EARTH && cucElement == NayinElement.METAL)
				|| (menhElement == NayinElement.METAL && cucElement == NayinElement.WATER)
				|| (menhElement == NayinElement.WATER && cucElement == NayinElement.WOOD)) {
			return "Mệnh " + menhElement.vietnameseName() + " sinh Cục " + cucElement.vietnameseName();
		}

		// Cuc sinh Menh
		if ((cucElement == NayinElement.WOOD && menhElement == NayinElement.FIRE)
				|| (cucElement == NayinElement.FIRE && menhElement == NayinElement.EARTH)
				|| (cucElement == NayinElement.EARTH && menhElement == NayinElement.METAL)
				|| (cucElement == NayinElement.METAL && menhElement == NayinElement.WATER)
				|| (cucElement == NayinElement.WATER && menhElement == NayinElement.WOOD)) {
			return "Cục " + cucElement.vietnameseName() + " sinh Mệnh " + menhElement.vietnameseName();
		}

		// Menh khac Cuc
		if ((menhElement == NayinElement.WOOD && cucElement == NayinElement.EARTH)
				|| (menhElement == NayinElement.EARTH && cucElement == NayinElement.WATER)
				|| (menhElement == NayinElement.WATER && cucElement == NayinElement.FIRE)
				|| (menhElement == NayinElement.FIRE && cucElement == NayinElement.METAL)
				|| (menhElement == NayinElement.METAL && cucElement == NayinElement.WOOD)) {
			return "Mệnh " + menhElement.vietnameseName() + " khắc Cục " + cucElement.vietnameseName();
		}

		// Cuc khac Menh
		return "Cục " + cucElement.vietnameseName() + " khắc Mệnh " + menhElement.vietnameseName();
	}
}
