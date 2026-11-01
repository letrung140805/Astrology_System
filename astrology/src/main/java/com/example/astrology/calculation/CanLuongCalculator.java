package com.example.astrology.calculation;

import java.util.Map;
import org.springframework.stereotype.Component;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;

/**
 * Cân Xương Tính Số (Lượng Chỉ) theo phương pháp Tử Vi cổ truyền
 * Đơn vị: 1 Lượng = 10 Chỉ.
 */
@Component
public class CanLuongCalculator {

	public record CanLuongResult(int totalChi, int luong, int chi, String formattedText, String summary) {}

	// Trọng lượng 60 Hoa Giáp (theo Can Chi năm, tính bằng số 'chỉ')
	private static final Map<String, Integer> YEAR_WEIGHTS = Map.ofEntries(
			// Tý & Sửu
			Map.entry("Giáp Tý", 12), Map.entry("Ất Sửu", 9),
			Map.entry("Bính Tý", 16), Map.entry("Đinh Sửu", 8),
			Map.entry("Mậu Tý", 15), Map.entry("Kỷ Sửu", 8),
			Map.entry("Canh Tý", 7),  Map.entry("Tân Sửu", 7),
			Map.entry("Nhâm Tý", 5),  Map.entry("Quý Sửu", 7),

			// Dần & Mão
			Map.entry("Giáp Dần", 12), Map.entry("Ất Mão", 8),
			Map.entry("Bính Dần", 6),  Map.entry("Đinh Mão", 7),
			Map.entry("Mậu Dần", 14), Map.entry("Kỷ Mão", 19),
			Map.entry("Canh Dần", 9),  Map.entry("Tân Mão", 12),
			Map.entry("Nhâm Dần", 9),  Map.entry("Quý Mão", 12),

			// Thìn & Tỵ
			Map.entry("Giáp Thìn", 8),  Map.entry("Ất Tỵ", 7),
			Map.entry("Bính Thìn", 8),  Map.entry("Đinh Tỵ", 6),
			Map.entry("Mậu Thìn", 12), Map.entry("Kỷ Tỵ", 5),
			Map.entry("Canh Thìn", 12), Map.entry("Tân Tỵ", 6),
			Map.entry("Nhâm Thìn", 10), Map.entry("Quý Tỵ", 7),

			// Ngọ & Mùi
			Map.entry("Giáp Ngọ", 15), Map.entry("Ất Mùi", 6),
			Map.entry("Bính Ngọ", 13), Map.entry("Đinh Mùi", 8),
			Map.entry("Mậu Ngọ", 19), Map.entry("Kỷ Mùi", 9),
			Map.entry("Canh Ngọ", 9),  Map.entry("Tân Mùi", 8),
			Map.entry("Nhâm Ngọ", 8),  Map.entry("Quý Mùi", 7),

			// Thân & Dậu
			Map.entry("Giáp Thân", 5),  Map.entry("Ất Dậu", 15),
			Map.entry("Bính Thân", 5),  Map.entry("Đinh Dậu", 14),
			Map.entry("Mậu Thân", 14), Map.entry("Kỷ Dậu", 5),
			Map.entry("Canh Thân", 8),  Map.entry("Tân Dậu", 16),
			Map.entry("Nhâm Thân", 7),  Map.entry("Quý Dậu", 8),

			// Tuất & Hợi
			Map.entry("Giáp Tuất", 5),  Map.entry("Ất Hợi", 9),
			Map.entry("Bính Tuất", 6),  Map.entry("Đinh Hợi", 16),
			Map.entry("Mậu Tuất", 14), Map.entry("Kỷ Hợi", 9),
			Map.entry("Canh Tuất", 6),  Map.entry("Tân Hợi", 17),
			Map.entry("Nhâm Tuất", 10), Map.entry("Quý Hợi", 7)
	);

	// Trọng lượng 12 tháng Âm lịch (tính bằng số 'chỉ')
	private static final int[] MONTH_WEIGHTS = {
			0, // 0-index unused
			6,  // Tháng 1: 6 chỉ
			7,  // Tháng 2: 7 chỉ
			18, // Tháng 3: 1 lượng 8 chỉ
			9,  // Tháng 4: 9 chỉ
			5,  // Tháng 5: 5 chỉ
			16, // Tháng 6: 1 lượng 6 chỉ
			9,  // Tháng 7: 9 chỉ
			15, // Tháng 8: 1 lượng 5 chỉ
			18, // Tháng 9: 1 lượng 8 chỉ
			8,  // Tháng 10: 8 chỉ
			9,  // Tháng 11: 9 chỉ
			5   // Tháng 12: 5 chỉ
	};

	// Trọng lượng 30 ngày Âm lịch (tính bằng số 'chỉ')
	private static final int[] DAY_WEIGHTS = {
			0, // 0-index unused
			5,  // Mùng 1: 5 chỉ
			10, // Mùng 2: 1 lượng 0 chỉ
			8,  // Mùng 3: 8 chỉ
			15, // Mùng 4: 1 lượng 5 chỉ
			16, // Mùng 5: 1 lượng 6 chỉ
			15, // Mùng 6: 1 lượng 5 chỉ
			8,  // Mùng 7: 8 chỉ
			16, // Mùng 8: 1 lượng 6 chỉ
			8,  // Mùng 9: 8 chỉ
			16, // Mùng 10: 1 lượng 6 chỉ
			9,  // Ngày 11: 9 chỉ
			17, // Ngày 12: 1 lượng 7 chỉ
			8,  // Ngày 13: 8 chỉ
			17, // Ngày 14: 1 lượng 7 chỉ
			10, // Ngày 15: 1 lượng 0 chỉ
			8,  // Ngày 16: 8 chỉ
			9,  // Ngày 17: 9 chỉ
			18, // Ngày 18: 1 lượng 8 chỉ
			5,  // Ngày 19: 5 chỉ
			15, // Ngày 20: 1 lượng 5 chỉ
			10, // Ngày 21: 1 lượng 0 chỉ
			9,  // Ngày 22: 9 chỉ
			8,  // Ngày 23: 8 chỉ
			9,  // Ngày 24: 9 chỉ
			15, // Ngày 25: 1 lượng 5 chỉ
			18, // Ngày 26: 1 lượng 8 chỉ
			7,  // Ngày 27: 7 chỉ
			8,  // Ngày 28: 8 chỉ
			16, // Ngày 29: 1 lượng 6 chỉ
			6   // Ngày 30: 6 chỉ
	};

	public CanLuongResult calculate(HeavenlyStem stem, EarthlyBranch branch, int lunarMonth, int lunarDay, EarthlyBranch hourBranch) {
		String canChiKey = stem.vietnameseName() + " " + branch.vietnameseName();
		int yearWeight = YEAR_WEIGHTS.getOrDefault(canChiKey, 10);

		int monthIndex = Math.max(1, Math.min(12, lunarMonth));
		int monthWeight = MONTH_WEIGHTS[monthIndex];

		int dayIndex = Math.max(1, Math.min(30, lunarDay));
		int dayWeight = DAY_WEIGHTS[dayIndex];

		int hourWeight = switch (hourBranch) {
			case TY -> 16;   // Giờ Tý: 1 lượng 6 chỉ
			case SUU -> 6;   // Giờ Sửu: 6 chỉ
			case DAN -> 7;   // Giờ Dần: 7 chỉ
			case MAO -> 10;  // Giờ Mão: 1 lượng 0 chỉ
			case THIN -> 9;  // Giờ Thìn: 9 chỉ
			case TI -> 16;   // Giờ Tỵ: 1 lượng 6 chỉ
			case NGO -> 10;  // Giờ Ngọ: 1 lượng 0 chỉ
			case MUI -> 6;   // Giờ Mùi: 6 chỉ
			case THAN -> 8;  // Giờ Thân: 8 chỉ
			case DAU -> 9;   // Giờ Dậu: 9 chỉ
			case TUAT -> 6;  // Giờ Tuất: 6 chỉ
			case HOI -> 5;   // Giờ Hợi: 5 chỉ
		};

		int totalChi = yearWeight + monthWeight + dayWeight + hourWeight;
		int luong = totalChi / 10;
		int chi = totalChi % 10;

		String formatted = chi > 0 ? (luong + " lượng " + chi + " chỉ") : (luong + " lượng");
		String summary;
		if (totalChi < 36) {
			summary = "Tiền vận bôn ba vất vả, trung hậu vận dần tích lũy thành tựu.";
		} else if (totalChi <= 58) {
			summary = "Số trung bình khá, cuộc sống an ổn ấm êm, hậu vận hanh thông.";
		} else {
			summary = "Số đại phú đại quý, có căn cơ phước lộc hiển vinh.";
		}

		return new CanLuongResult(totalChi, luong, chi, formatted, summary);
	}
}

