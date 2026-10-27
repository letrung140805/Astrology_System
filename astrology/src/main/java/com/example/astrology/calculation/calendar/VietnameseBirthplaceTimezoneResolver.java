package com.example.astrology.calculation.calendar;

import java.text.Normalizer;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public class VietnameseBirthplaceTimezoneResolver {

	private static final ZoneId VIETNAM_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final Set<String> VIETNAMESE_PLACES = Set.of(
			"an giang", "ba ria vung tau", "bac giang", "bac kan", "bac lieu", "bac ninh",
			"ben tre", "bien hoa", "binh dinh", "binh duong", "binh phuoc", "binh thuan",
			"ca mau", "can tho", "cao bang", "da lat", "da nang", "dak lak", "dak nong",
			"dien bien", "dong nai", "dong thap", "gia lai", "ha giang", "ha nam", "ha noi",
			"ha tinh", "hai duong", "hai phong", "hau giang", "hoa binh", "ho chi minh",
			"hoi an", "hung yen", "hue", "khanh hoa", "kien giang", "kon tum", "lai chau",
			"lam dong", "lang son", "lao cai", "long an", "nam dinh", "nghe an", "ninh binh",
			"ninh thuan", "phan rang", "phan thiet", "phu tho", "phu yen", "quang binh",
			"quang nam", "quang ngai", "quang ninh", "quang tri", "rach gia", "sa dec",
			"sai gon", "soc trang", "son la", "tam ky", "tay ninh", "thai binh", "thai nguyen",
			"thanh hoa", "thua thien hue", "tien giang", "tra vinh", "tuyen quang", "vinh",
			"vinh long", "vinh phuc", "vung tau", "yen bai");

	public ZoneId resolve(String birthplace) {
		if (birthplace == null || birthplace.isBlank()) {
			throw new IllegalArgumentException("Cần cung cấp địa điểm sinh để xác định múi giờ.");
		}

		boolean recognized = Arrays.stream(birthplace.split("[,;]"))
				.map(VietnameseBirthplaceTimezoneResolver::normalize)
				.anyMatch(VIETNAMESE_PLACES::contains);
		if (!recognized) {
			throw new IllegalArgumentException("Địa điểm sinh chưa được nhận diện tại Việt Nam.");
		}
		return VIETNAM_TIME_ZONE;
	}

	private static String normalize(String value) {
		String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "")
				.replace('đ', 'd')
				.replace('Đ', 'D')
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", " ")
				.trim()
				.replaceAll("\\b(viet nam|vietnam)\\b", " ")
				.replaceAll("\\b(thanh pho|tp|tinh|city|province)\\b", " ")
				.replaceAll("\\s+", " ")
				.trim();
		return normalized;
	}
}
