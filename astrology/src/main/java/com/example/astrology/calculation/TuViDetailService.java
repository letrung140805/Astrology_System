package com.example.astrology.calculation;

import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;
import com.example.astrology.calculation.calendar.NayinElement;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.chart.BirthSex;
import com.example.astrology.calculation.chart.FiveElementsBureau;
import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.calculation.chart.PalaceLayout;
import com.example.astrology.calculation.chart.StarId;
import com.example.astrology.calculation.chart.TracuutuviChart;
import com.example.astrology.calculation.chart.TracuutuviYearStarsCalculator;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;

@Service
public class TuViDetailService {

	private final CanLuongCalculator canLuongCalculator;
	private final NayinElementCalculator nayinElementCalculator;

	public TuViDetailService(CanLuongCalculator canLuongCalculator, NayinElementCalculator nayinElementCalculator) {
		this.canLuongCalculator = canLuongCalculator;
		this.nayinElementCalculator = nayinElementCalculator;
	}

	public record FullChartDetails(
			String canLuong,
			String chuMenh,
			String chuThan,
			String laiNhanCung,
			String amDuongNamNu,
			String amDuongLy,
			String banMenhFull,
			String menhCucSinhKhac,
			String monthCanChi,
			String dayCanChi,
			String hourCanChi,
			String viewYearInfo,
			String tuanMarkerPosition,
			String trietMarkerPosition,
			List<ChartPalaceDisplay> gridPalaces
	) {
		public FullChartDetails(
				String canLuong,
				String chuMenh,
				String chuThan,
				String laiNhanCung,
				String amDuongNamNu,
				String amDuongLy,
				String banMenhFull,
				String menhCucSinhKhac,
				String monthCanChi,
				String dayCanChi,
				String hourCanChi,
				String viewYearInfo,
				List<ChartPalaceDisplay> gridPalaces
		) {
			this(canLuong, chuMenh, chuThan, laiNhanCung, amDuongNamNu, amDuongLy,
					banMenhFull, menhCucSinhKhac, monthCanChi, dayCanChi, hourCanChi,
					viewYearInfo, null, null, gridPalaces);
		}
	}

	private static final String[] TRANG_SINH_12 = {
			"Tràng Sinh", "Mộc Dục", "Quan Đới", "Lâm Quan",
			"Đế Vượng", "Suy", "Bệnh", "Tử",
			"Mộ", "Tuyệt", "Thai", "Dưỡng"
	};

	// 14 Major stars brightness by EarthlyBranch
	// Branch ordinals: 0:TY, 1:SUU, 2:DAN, 3:MAO, 4:THIN, 5:TI, 6:NGO, 7:MUI, 8:THAN, 9:DAU, 10:TUAT, 11:HOI
	private static final Map<StarId, String[]> MAJOR_BRIGHTNESS = Map.ofEntries(
			Map.entry(StarId.TU_VI,        new String[]{"(B)", "(V)", "(V)", "(V)", "(V)", "(Đ)", "(M)", "(M)", "(V)", "(V)", "(V)", "(Đ)"}),
			Map.entry(StarId.THIEN_CO,     new String[]{"(Đ)", "(H)", "(V)", "(M)", "(M)", "(B)", "(Đ)", "(H)", "(B)", "(M)", "(M)", "(V)"}),
			Map.entry(StarId.THAI_DUONG,   new String[]{"(H)", "(Đ)", "(V)", "(V)", "(V)", "(M)", "(M)", "(Đ)", "(H)", "(H)", "(H)", "(H)"}),
			Map.entry(StarId.VU_KHUC,      new String[]{"(V)", "(M)", "(V)", "(Đ)", "(M)", "(H)", "(V)", "(M)", "(V)", "(Đ)", "(M)", "(H)"}),
			Map.entry(StarId.THIEN_DONG,   new String[]{"(V)", "(H)", "(M)", "(Đ)", "(H)", "(H)", "(V)", "(H)", "(M)", "(H)", "(H)", "(Đ)"}),
			Map.entry(StarId.LIEM_TRINH,   new String[]{"(V)", "(Đ)", "(V)", "(H)", "(M)", "(H)", "(V)", "(Đ)", "(V)", "(H)", "(M)", "(H)"}),
			Map.entry(StarId.THIEN_PHU,    new String[]{"(M)", "(Đ)", "(M)", "(B)", "(M)", "(V)", "(M)", "(Đ)", "(M)", "(B)", "(M)", "(V)"}),
			Map.entry(StarId.THAI_AM,      new String[]{"(M)", "(Đ)", "(H)", "(H)", "(H)", "(H)", "(H)", "(Đ)", "(Đ)", "(M)", "(M)", "(M)"}),
			Map.entry(StarId.THAM_LANG,    new String[]{"(H)", "(M)", "(Đ)", "(H)", "(M)", "(H)", "(H)", "(M)", "(Đ)", "(H)", "(M)", "(H)"}),
			Map.entry(StarId.CU_MON,       new String[]{"(V)", "(H)", "(V)", "(M)", "(Đ)", "(H)", "(V)", "(H)", "(V)", "(M)", "(Đ)", "(Đ)"}),
			Map.entry(StarId.THIEN_TUONG,  new String[]{"(V)", "(Đ)", "(M)", "(H)", "(V)", "(Đ)", "(V)", "(Đ)", "(M)", "(H)", "(V)", "(Đ)"}),
			Map.entry(StarId.THIEN_LUONG,  new String[]{"(V)", "(V)", "(V)", "(Đ)", "(M)", "(H)", "(M)", "(V)", "(V)", "(H)", "(M)", "(H)"}),
			Map.entry(StarId.THAT_SAT,     new String[]{"(M)", "(Đ)", "(M)", "(H)", "(H)", "(Đ)", "(M)", "(Đ)", "(M)", "(H)", "(H)", "(Đ)"}),
			Map.entry(StarId.PHA_QUAN,     new String[]{"(M)", "(V)", "(H)", "(H)", "(Đ)", "(H)", "(M)", "(V)", "(H)", "(H)", "(Đ)", "(H)"})
	);

	public FullChartDetails buildDetails(
			TracuutuviChart chart,
			LocalDate solarDate,
			BirthSex sex) {

		HeavenlyStem yearStem = chart.input().yearStem();
		EarthlyBranch yearBranch = chart.input().yearBranch();
		int lunarMonth = chart.input().lunarMonth();
		int lunarDay = chart.input().lunarDay();
		HourBranch hour = chart.input().birthHour();
		EarthlyBranch hourBranch = hour.earthlyBranch();
		EarthlyBranch mingPalace = chart.positions().mingPalace();
		PalaceLayout layout = chart.positions().palaces();
		FiveElementsBureau bureau = chart.positions().bureau();

		// 1. Cân Lượng
		var canLuongResult = canLuongCalculator.calculate(yearStem, yearBranch, lunarMonth, lunarDay, hourBranch);
		String canLuong = canLuongResult.formattedText();

		// 2. Can Chi 4 Trụ
		int jd = julianDay(solarDate != null ? solarDate : LocalDate.of(chart.input().lunarYear(), 1, 1));
		int dayStemIndex = (jd + 9) % 10;
		int dayBranchIndex = (jd + 1) % 12;
		HeavenlyStem dayStem = HeavenlyStem.values()[dayStemIndex];
		EarthlyBranch dayBranch = EarthlyBranch.values()[dayBranchIndex];
		String dayCanChi = dayStem.vietnameseName() + " " + dayBranch.vietnameseName();

		int hourStartCan = ((dayStemIndex % 5) * 2) % 10;
		int hourStemIndex = (hourStartCan + hourBranch.ordinal()) % 10;
		HeavenlyStem hourStem = HeavenlyStem.values()[hourStemIndex];
		String hourCanChi = hourStem.vietnameseName() + " " + hourBranch.vietnameseName();

		int monthStartCan = ((yearStem.ordinal() % 5) * 2 + 2) % 10;
		int monthStemIndex = (monthStartCan + (lunarMonth - 1)) % 10;
		HeavenlyStem monthStem = HeavenlyStem.values()[monthStemIndex];
		int monthBranchIndex = (lunarMonth + 1) % 12;
		EarthlyBranch monthBranch = EarthlyBranch.values()[monthBranchIndex];
		String monthCanChi = monthStem.vietnameseName() + " " + monthBranch.vietnameseName();

		// 3. Năm xem
		int currentYear = LocalDate.now().getYear();
		int age = currentYear - chart.input().lunarYear() + 1;
		int yearCycle = (currentYear - 4) % 60;
		if (yearCycle < 0) yearCycle += 60;
		HeavenlyStem viewStem = HeavenlyStem.values()[yearCycle % 10];
		EarthlyBranch viewBranch = EarthlyBranch.values()[yearCycle % 12];
		String viewYearInfo = viewStem.vietnameseName() + " " + viewBranch.vietnameseName() + " (" + currentYear + "), " + age + " tuổi";

		// 4. Âm Dương Nam Nữ
		boolean isStemYang = yearStem.isYang();
		boolean isMale = sex == BirthSex.MALE;
		String amDuongNamNu = isMale ? (isStemYang ? "Dương Nam" : "Âm Nam") : (isStemYang ? "Dương Nữ" : "Âm Nữ");

		boolean isMingYang = (mingPalace.ordinal() % 2 == 0);
		String amDuongLy = (isMingYang == isStemYang) ? "Âm Dương thuận lý" : "Âm Dương nghịch lý";

		// 5. Bản Mệnh & Mệnh Cục Sinh Khắc
		String nayinFullName = nayinElementCalculator.calculateFullName(chart.input().lunarYear());
		NayinElement menhElement = nayinElementCalculator.calculate(chart.input().lunarYear());
		String banMenhFull = nayinFullName + " - " + bureau.vietnameseName();
		String menhCucSinhKhac = "(" + nayinElementCalculator.calculateRelationship(menhElement, bureau) + ")";

		// 6. Chủ Mệnh & Chủ Thân
		String chuMenh = switch (mingPalace) {
			case TY -> "Tham Lang";
			case SUU, HOI -> "Cự Môn";
			case DAN, TUAT -> "Lộc Tồn";
			case MAO, DAU -> "Văn Khúc";
			case THIN, THAN -> "Liêm Trinh";
			case TI, MUI -> "Vũ Khúc";
			case NGO -> "Phá Quân";
		};

		String chuThan = switch (yearBranch) {
			case TY -> "Linh Tinh";
			case SUU, MUI -> "Thiên Tướng";
			case DAN, THAN -> "Thiên Lương";
			case MAO, DAU -> "Thiên Đồng";
			case THIN, TUAT -> "Văn Xương";
			case TI, HOI -> "Thiên Cơ";
			case NGO -> "Hỏa Tinh";
		};

		// 7. Lai Nhân Cung
		int danStartStem = ((yearStem.ordinal() % 5) * 2 + 2) % 10;
		EarthlyBranch laiNhanBranch = EarthlyBranch.HOI;
		for (EarthlyBranch b : EarthlyBranch.values()) {
			int offsetFromDan = (b.ordinal() - EarthlyBranch.DAN.ordinal() + 12) % 12;
			int palaceStem = (danStartStem + offsetFromDan) % 10;
			if (palaceStem == yearStem.ordinal()) {
				laiNhanBranch = b;
				break;
			}
		}
		final EarthlyBranch finalLaiNhanBranch = laiNhanBranch;
		PalaceFunction laiNhanFunction = layout.positions().entrySet().stream()
				.filter(e -> e.getValue() == finalLaiNhanBranch)
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(PalaceFunction.MENH);
		String laiNhanCung = laiNhanFunction.vietnameseName();

		// 8. Tuần & Triệt
		int delta = (yearStem.ordinal() - yearBranch.ordinal() + 12) % 12;
		Set<EarthlyBranch> tuanBranches = switch (delta) {
			case 0 -> Set.of(EarthlyBranch.TUAT, EarthlyBranch.HOI);
			case 2 -> Set.of(EarthlyBranch.THAN, EarthlyBranch.DAU);
			case 4 -> Set.of(EarthlyBranch.NGO, EarthlyBranch.MUI);
			case 6 -> Set.of(EarthlyBranch.THIN, EarthlyBranch.TI);
			case 8 -> Set.of(EarthlyBranch.DAN, EarthlyBranch.MAO);
			default -> Set.of(EarthlyBranch.TY, EarthlyBranch.SUU);
		};

		Set<EarthlyBranch> trietBranches = switch (yearStem) {
			case JIA, JI -> Set.of(EarthlyBranch.THAN, EarthlyBranch.DAU);
			case YI, GENG -> Set.of(EarthlyBranch.NGO, EarthlyBranch.MUI);
			case BING, XIN -> Set.of(EarthlyBranch.THIN, EarthlyBranch.TI);
			case DING, REN -> Set.of(EarthlyBranch.DAN, EarthlyBranch.MAO);
			case WU, GUI -> Set.of(EarthlyBranch.TY, EarthlyBranch.SUU);
		};

		// 9. Build 12 palaces in 4x4 visual perimeter order
		// Physical perimeter order on Tử Vi chart:
		// Row 1: TI (Tỵ), NGO (Ngọ), MUI (Mùi), THAN (Thân)
		// Right: DAU (Dậu), TUAT (Tuất)
		// Row 4 (right to left): HOI (Hợi), TY (Tý), SUU (Sửu), DAN (Dần)
		// Left: MAO (Mão), THIN (Thìn)
		List<EarthlyBranch> gridOrder = List.of(
				EarthlyBranch.TI, EarthlyBranch.NGO, EarthlyBranch.MUI, EarthlyBranch.THAN,
				EarthlyBranch.DAU, EarthlyBranch.TUAT,
				EarthlyBranch.HOI, EarthlyBranch.TY, EarthlyBranch.SUU, EarthlyBranch.DAN,
				EarthlyBranch.MAO, EarthlyBranch.THIN
		);

		int direction = TracuutuviYearStarsCalculator.cycleDirection(yearStem, sex);
		EarthlyBranch csStart = bureau.changShengStart();

		// Additional auxiliary stars mapped by branch
		Map<EarthlyBranch, List<ChartStarDisplay>> extraStars = computeExtraStars(
				yearStem, yearBranch, lunarMonth, lunarDay, hourBranch, sex, mingPalace, layout,
				viewStem, viewBranch, chart);

		String[] DV_NAMES = {"MỆNH", "PHỤ", "PHÚC", "ĐIỀN", "QUAN", "NÔ", "DI", "TẬT", "TÀI", "TỬ", "PHỐI", "HUYNH"};
		EarthlyBranch currentDvBranch = mingPalace;
		for (EarthlyBranch b : EarthlyBranch.values()) {
			int steps = direction == 1
					? (b.ordinal() - mingPalace.ordinal() + 12) % 12
					: (mingPalace.ordinal() - b.ordinal() + 12) % 12;
			int startAge = bureau.divisor() + steps * 10;
			if (age >= startAge && (age - startAge) < 10) {
				currentDvBranch = b;
				break;
			}
		}

		List<ChartPalaceDisplay> gridPalaces = new ArrayList<>();
		for (EarthlyBranch branch : gridOrder) {
			PalaceFunction function = layout.positions().entrySet().stream()
					.filter(e -> e.getValue() == branch)
					.map(Map.Entry::getKey)
					.findFirst()
					.orElse(PalaceFunction.MENH);

			// Can Chi name on top left
			int offsetFromDan = (branch.ordinal() - EarthlyBranch.DAN.ordinal() + 12) % 12;
			int palaceStemIndex = (danStartStem + offsetFromDan) % 10;
			HeavenlyStem palaceStem = HeavenlyStem.values()[palaceStemIndex];
			String stemShort = switch (palaceStem) {
				case JIA -> "G.";
				case YI -> "Ấ.";
				case BING -> "B.";
				case DING -> "Đ.";
				case WU -> "M.";
				case JI -> "K.";
				case GENG -> "C.";
				case XIN -> "T.";
				case REN -> "N.";
				case GUI -> "Q.";
			};
			String stemBranchCode = stemShort + branch.vietnameseName();

			String palaceElement = switch (branch.element()) {
				case FIRE -> "Hành Hỏa";
				case WATER -> "Hành Thủy";
				case WOOD -> "Hành Mộc";
				case METAL -> "Hành Kim";
				case EARTH -> "Hành Thổ";
			};

			// Đại hạn
			String sign = palaceStem.isYang() ? "+" : "-";
			String branchElemShort = switch (branch.element()) {
				case FIRE -> "Hoả";
				case WATER -> "Thuỷ";
				case WOOD -> "Mộc";
				case METAL -> "Kim";
				case EARTH -> "Thổ";
			};
			String palaceSignElement = sign + branchElemShort;

			int stepsFromDv = (branch.ordinal() - currentDvBranch.ordinal() + 12) % 12;
			String dvLabel = "ĐV." + DV_NAMES[stepsFromDv];

			int stepsFromMingForLn = (branch.ordinal() - mingPalace.ordinal() + 12) % 12;
			String lnLabel = "LN." + DV_NAMES[stepsFromMingForLn];

			int stepsFromMing = direction == 1
					? (branch.ordinal() - mingPalace.ordinal() + 12) % 12
					: (mingPalace.ordinal() - branch.ordinal() + 12) % 12;
			int daiHan = bureau.divisor() + stepsFromMing * 10;

			// Nguyệt hạn: Th.1 khởi tại Mệnh, đi thuận
			int monthOffset = (branch.ordinal() - mingPalace.ordinal() + 12) % 12;
			String nguyetHan = "Th." + (monthOffset + 1);

			// Tràng Sinh
			int stepsFromCS = direction == 1
					? (branch.ordinal() - csStart.ordinal() + 12) % 12
					: (csStart.ordinal() - branch.ordinal() + 12) % 12;
			String trangSinh = TRANG_SINH_12[stepsFromCS];

			boolean hasTuan = tuanBranches.contains(branch);
			boolean hasTriet = trietBranches.contains(branch);

			// Stars in this palace
			List<ChartStarDisplay> majorStars = new ArrayList<>();
			List<ChartStarDisplay> goodStars = new ArrayList<>();
			List<ChartStarDisplay> badStars = new ArrayList<>();
			List<ChartStarDisplay> allStars = new ArrayList<>();

			// Stars from TracuutuviChart
			for (StarId star : StarId.values()) {
				if (star.ordinal() >= StarId.TRUONG_SINH.ordinal() && star.ordinal() <= StarId.DUONG.ordinal()) {
					continue;
				}
				if (chart.stars().branchOf(star).filter(branch::equals).isPresent()) {
					boolean isMajor = MAJOR_BRIGHTNESS.containsKey(star);
					String brightness = isMajor ? MAJOR_BRIGHTNESS.get(star)[branch.ordinal()] : "";
					boolean isGood = isGoodStar(star);
					var displayStar = new ChartStarDisplay(star.vietnameseName(), star.element(), brightness, isMajor, isGood);
					allStars.add(displayStar);
					if (isMajor) {
						majorStars.add(displayStar);
					} else if (isGood) {
						goodStars.add(displayStar);
					} else {
						badStars.add(displayStar);
					}
				}
			}

			// Add extra auxiliary stars
			List<ChartStarDisplay> extras = extraStars.getOrDefault(branch, List.of());
			for (ChartStarDisplay es : extras) {
				allStars.add(es);
				if (es.isGood()) {
					goodStars.add(es);
				} else {
					badStars.add(es);
				}
			}

			gridPalaces.add(new ChartPalaceDisplay(
					function,
					branch,
					stemBranchCode,
					palaceSignElement,
					palaceElement,
					daiHan,
					nguyetHan,
					trangSinh,
					dvLabel,
					lnLabel,
					hasTuan,
					hasTriet,
					majorStars,
					goodStars,
					badStars,
					allStars
			));
		}

		String tuanMarkerPosition = switch (delta) {
			case 0 -> "tuat-hoi";
			case 2 -> "than-dau";
			case 4 -> "ngo-mui";
			case 6 -> "thin-ti";
			case 8 -> "dan-mao";
			default -> "ty-suu";
		};

		String trietMarkerPosition = switch (yearStem) {
			case JIA, JI -> "than-dau";
			case YI, GENG -> "ngo-mui";
			case BING, XIN -> "thin-ti";
			case DING, REN -> "dan-mao";
			case WU, GUI -> "ty-suu";
		};

		return new FullChartDetails(
				canLuong,
				chuMenh,
				chuThan,
				laiNhanCung,
				amDuongNamNu,
				amDuongLy,
				banMenhFull,
				menhCucSinhKhac,
				monthCanChi,
				dayCanChi,
				hourCanChi,
				viewYearInfo,
				tuanMarkerPosition,
				trietMarkerPosition,
				gridPalaces
		);
	}

	private boolean isGoodStar(StarId star) {
		return switch (star) {
			case TU_VI, THIEN_PHU, THAI_DUONG, THAI_AM, THIEN_CO, VU_KHUC, THIEN_DONG,
					THIEN_TUONG, THIEN_LUONG, TA_PHU, HUU_BAT, VAN_XUONG, VAN_KHUC,
					LOC_TON, THIEN_KHOI, THIEN_VIET, THIEN_MA, HOA_LOC, HOA_QUYEN, HOA_KHOA,
					BAC_SI, LUC_SI, THANH_LONG, TUONG_QUAN, TAU_THU, HY_THAN,
					THIEU_DUONG, THIEU_AM, LONG_DUC, PHUC_DUC_STAR,
					TRUONG_SINH, DE_VUONG, LAM_QUAN, QUAN_DOI -> true;
			default -> false;
		};
	}

	private Map<EarthlyBranch, List<ChartStarDisplay>> computeExtraStars(
			HeavenlyStem yearStem,
			EarthlyBranch yearBranch,
			int lunarMonth,
			int lunarDay,
			EarthlyBranch hourBranch,
			BirthSex sex,
			EarthlyBranch mingPalace,
			PalaceLayout layout,
			HeavenlyStem viewStem,
			EarthlyBranch viewBranch,
			TracuutuviChart chart) {

		Map<EarthlyBranch, List<ChartStarDisplay>> map = new EnumMap<>(EarthlyBranch.class);
		for (EarthlyBranch b : EarthlyBranch.values()) {
			map.put(b, new ArrayList<>());
		}

		// Kình Dương & Đà La (from Lộc Tồn)
		EarthlyBranch locTon = switch (yearStem) {
			case JIA -> EarthlyBranch.DAN;
			case YI -> EarthlyBranch.MAO;
			case BING, WU -> EarthlyBranch.TI;
			case DING, JI -> EarthlyBranch.NGO;
			case GENG -> EarthlyBranch.THAN;
			case XIN -> EarthlyBranch.DAU;
			case REN -> EarthlyBranch.HOI;
			case GUI -> EarthlyBranch.TY;
		};
		EarthlyBranch kinhDuong = step(locTon, 1);
		EarthlyBranch daLa = step(locTon, -1);
		map.get(kinhDuong).add(new ChartStarDisplay("Kình Dương", FiveElement.METAL, "(Đ)", false, false));
		map.get(daLa).add(new ChartStarDisplay("Đà La", FiveElement.METAL, "(H)", false, false));

		// Hỏa Tinh & Linh Tinh
		// Chi năm:
		// Hợi, Mão, Mùi: Hỏa khởi Dậu, Linh khởi Tuất
		// Dần, Ngọ, Tuất: Hỏa khởi Sửu, Linh khởi Mão
		// Thân, Tý, Thìn: Hỏa khởi Dần, Linh khởi Tuất
		// Tị, Dậu, Sửu: Hỏa khởi Mão, Linh khởi Tuất
		EarthlyBranch hoaStart;
		EarthlyBranch linhStart;
		if (Set.of(EarthlyBranch.HOI, EarthlyBranch.MAO, EarthlyBranch.MUI).contains(yearBranch)) {
			hoaStart = EarthlyBranch.DAU;
			linhStart = EarthlyBranch.TUAT;
		} else if (Set.of(EarthlyBranch.DAN, EarthlyBranch.NGO, EarthlyBranch.TUAT).contains(yearBranch)) {
			hoaStart = EarthlyBranch.SUU;
			linhStart = EarthlyBranch.MAO;
		} else if (Set.of(EarthlyBranch.THAN, EarthlyBranch.TY, EarthlyBranch.THIN).contains(yearBranch)) {
			hoaStart = EarthlyBranch.DAN;
			linhStart = EarthlyBranch.TUAT;
		} else {
			hoaStart = EarthlyBranch.MAO;
			linhStart = EarthlyBranch.TUAT;
		}

		boolean isYangMale = yearStem.isYang() && sex == BirthSex.MALE;
		boolean isYinFemale = !yearStem.isYang() && sex == BirthSex.FEMALE;
		int hoaDir = (isYangMale || isYinFemale) ? 1 : -1;
		int linhDir = (isYangMale || isYinFemale) ? -1 : 1;

		EarthlyBranch hoaTinh = step(hoaStart, hoaDir * hourBranch.ordinal());
		EarthlyBranch linhTinh = step(linhStart, linhDir * hourBranch.ordinal());
		map.get(hoaTinh).add(new ChartStarDisplay("Hỏa Tinh", FiveElement.FIRE, "(H)", false, false));
		map.get(linhTinh).add(new ChartStarDisplay("Linh Tinh", FiveElement.FIRE, "(H)", false, false));

		// Địa Không & Địa Kiếp: khởi từ Hợi
		EarthlyBranch diaKiep = step(EarthlyBranch.HOI, hourBranch.ordinal());
		EarthlyBranch diaKhong = step(EarthlyBranch.HOI, -hourBranch.ordinal());
		map.get(diaKiep).add(new ChartStarDisplay("Địa Kiếp", FiveElement.FIRE, "", false, false));
		map.get(diaKhong).add(new ChartStarDisplay("Địa Không", FiveElement.FIRE, "(Đ)", false, false));

		// Đào Hoa
		EarthlyBranch daoHoa = switch (yearBranch) {
			case HOI, MAO, MUI -> EarthlyBranch.TY;
			case DAN, NGO, TUAT -> EarthlyBranch.MAO;
			case TI, DAU, SUU -> EarthlyBranch.NGO;
			default -> EarthlyBranch.DAU;
		};
		map.get(daoHoa).add(new ChartStarDisplay("Đào Hoa", FiveElement.WOOD, "", false, true));

		// Hồng Loan & Thiên Hỷ
		EarthlyBranch hongLoan = step(EarthlyBranch.MAO, -yearBranch.ordinal());
		EarthlyBranch thienHy = step(hongLoan, 6);
		map.get(hongLoan).add(new ChartStarDisplay("Hồng Loan", FiveElement.WATER, "", false, true));
		map.get(thienHy).add(new ChartStarDisplay("Thiên Hỷ", FiveElement.WATER, "", false, true));

		// Thiên Hình & Thiên Diêu
		EarthlyBranch thienHinh = step(EarthlyBranch.DAU, lunarMonth - 1);
		EarthlyBranch thienDieu = step(EarthlyBranch.SUU, lunarMonth - 1);
		map.get(thienHinh).add(new ChartStarDisplay("Thiên Hình", FiveElement.FIRE, "(H)", false, false));
		map.get(thienDieu).add(new ChartStarDisplay("Thiên Diêu", FiveElement.WATER, "(Đ)", false, false));

		// Cô Thần & Quả Tú
		EarthlyBranch coThan;
		EarthlyBranch quaTu;
		if (Set.of(EarthlyBranch.HOI, EarthlyBranch.MAO, EarthlyBranch.MUI).contains(yearBranch)) {
			coThan = EarthlyBranch.THAN;
			quaTu = EarthlyBranch.THIN;
		} else if (Set.of(EarthlyBranch.DAN, EarthlyBranch.NGO, EarthlyBranch.TUAT).contains(yearBranch)) {
			coThan = EarthlyBranch.TI;
			quaTu = EarthlyBranch.SUU;
		} else if (Set.of(EarthlyBranch.TI, EarthlyBranch.DAU, EarthlyBranch.SUU).contains(yearBranch)) {
			coThan = EarthlyBranch.DAN;
			quaTu = EarthlyBranch.TUAT;
		} else {
			coThan = EarthlyBranch.HOI;
			quaTu = EarthlyBranch.MUI;
		}
		map.get(coThan).add(new ChartStarDisplay("Cô Thần", FiveElement.EARTH, "", false, false));
		map.get(quaTu).add(new ChartStarDisplay("Quả Tú", FiveElement.EARTH, "", false, false));

		// Kiếp Sát
		EarthlyBranch kiepSat = switch (yearBranch) {
			case HOI, MAO, MUI -> EarthlyBranch.THAN;
			case DAN, NGO, TUAT -> EarthlyBranch.HOI;
			case TI, DAU, SUU -> EarthlyBranch.DAN;
			default -> EarthlyBranch.TI;
		};
		map.get(kiepSat).add(new ChartStarDisplay("Kiếp Sát", FiveElement.FIRE, "", false, false));

		// Hoa Cái
		EarthlyBranch hoaCai = switch (yearBranch) {
			case HOI, MAO, MUI -> EarthlyBranch.MUI;
			case DAN, NGO, TUAT -> EarthlyBranch.TUAT;
			case TI, DAU, SUU -> EarthlyBranch.SUU;
			default -> EarthlyBranch.THIN;
		};
		map.get(hoaCai).add(new ChartStarDisplay("Hoa Cái", FiveElement.METAL, "", false, true));

		// Phá Toái
		EarthlyBranch phaToai = switch (yearBranch) {
			case TY, NGO, MAO, DAU -> EarthlyBranch.TI;
			case DAN, THAN, HOI -> EarthlyBranch.DAU;
			default -> EarthlyBranch.SUU;
		};
		map.get(phaToai).add(new ChartStarDisplay("Phá Toái", FiveElement.FIRE, "", false, false));

		// Thiên La & Địa Võng
		map.get(EarthlyBranch.THIN).add(new ChartStarDisplay("Thiên La", FiveElement.EARTH, "", false, false));
		map.get(EarthlyBranch.TUAT).add(new ChartStarDisplay("Địa Võng", FiveElement.EARTH, "", false, false));

		// Thiên Thương & Thiên Sứ
		EarthlyBranch noBoc = layout.positions().get(PalaceFunction.NO_BOC);
		EarthlyBranch tatAch = layout.positions().get(PalaceFunction.TAT_ACH);
		if (noBoc != null) map.get(noBoc).add(new ChartStarDisplay("Thiên Thương", FiveElement.EARTH, "", false, false));
		if (tatAch != null) map.get(tatAch).add(new ChartStarDisplay("Thiên Sứ", FiveElement.WATER, "", false, false));

		// Quốc Ấn & Đường Phù
		EarthlyBranch quocAn = step(locTon, 8);
		EarthlyBranch duongPhu = step(locTon, 5);
		map.get(quocAn).add(new ChartStarDisplay("Quốc Ấn", FiveElement.EARTH, "", false, true));
		map.get(duongPhu).add(new ChartStarDisplay("Đường Phù", FiveElement.WOOD, "", false, true));

		// Thiên Quan & Thiên Phúc
		EarthlyBranch thienQuan = switch (yearStem) {
			case JIA -> EarthlyBranch.MUI;
			case YI -> EarthlyBranch.THIN;
			case BING -> EarthlyBranch.TI;
			case DING -> EarthlyBranch.DAN;
			case WU -> EarthlyBranch.MAO;
			case JI -> EarthlyBranch.DAU;
			case GENG -> EarthlyBranch.HOI;
			case XIN -> EarthlyBranch.DAU;
			case REN -> EarthlyBranch.TUAT;
			case GUI -> EarthlyBranch.NGO;
		};
		EarthlyBranch thienPhuc = switch (yearStem) {
			case JIA -> EarthlyBranch.DAU;
			case YI -> EarthlyBranch.THAN;
			case BING -> EarthlyBranch.TY;
			case DING -> EarthlyBranch.HOI;
			case WU -> EarthlyBranch.MAO;
			case JI -> EarthlyBranch.DAN;
			case GENG -> EarthlyBranch.NGO;
			case XIN -> EarthlyBranch.TI;
			case REN -> EarthlyBranch.NGO;
			case GUI -> EarthlyBranch.TI;
		};
		map.get(thienQuan).add(new ChartStarDisplay("Thiên Quan", FiveElement.FIRE, "", false, true));
		map.get(thienPhuc).add(new ChartStarDisplay("Thiên Phúc", FiveElement.EARTH, "", false, true));

		// Long Trì & Phượng Các
		EarthlyBranch longTri = step(EarthlyBranch.THIN, yearBranch.ordinal());
		EarthlyBranch phuongCac = step(EarthlyBranch.TUAT, -yearBranch.ordinal());
		map.get(longTri).add(new ChartStarDisplay("Long Trì", FiveElement.WATER, "", false, true));
		map.get(phuongCac).add(new ChartStarDisplay("Phượng Các", FiveElement.EARTH, "", false, true));

		// Thiên Đức & Nguyệt Đức
		EarthlyBranch thienDuc = step(EarthlyBranch.DAU, yearBranch.ordinal());
		EarthlyBranch nguyetDuc = step(EarthlyBranch.TI, yearBranch.ordinal());
		map.get(thienDuc).add(new ChartStarDisplay("Thiên Đức", FiveElement.FIRE, "", false, true));
		map.get(nguyetDuc).add(new ChartStarDisplay("Nguyệt Đức", FiveElement.FIRE, "", false, true));

		// Ân Quang & Thiên Quý (từ Xương Khúc và ngày sinh)
		EarthlyBranch vanXuong = step(EarthlyBranch.TUAT, -hourBranch.ordinal());
		EarthlyBranch vanKhuc = step(EarthlyBranch.THIN, hourBranch.ordinal());
		EarthlyBranch anQuang = step(vanXuong, lunarDay - 2);
		EarthlyBranch thienQuy = step(vanKhuc, -(lunarDay - 2));
		map.get(anQuang).add(new ChartStarDisplay("Ân Quang", FiveElement.WOOD, "", false, true));
		map.get(thienQuy).add(new ChartStarDisplay("Thiên Quý", FiveElement.EARTH, "", false, true));

		// Tam Thai & Bát Tọa (từ Tả Phù, Hữu Bật và ngày sinh)
		EarthlyBranch taPhu = step(EarthlyBranch.THIN, lunarMonth - 1);
		EarthlyBranch huuBat = step(EarthlyBranch.TUAT, -(lunarMonth - 1));
		EarthlyBranch tamThai = step(taPhu, lunarDay - 1);
		EarthlyBranch batToa = step(huuBat, -(lunarDay - 1));
		map.get(tamThai).add(new ChartStarDisplay("Tam Thai", FiveElement.WATER, "", false, true));
		map.get(batToa).add(new ChartStarDisplay("Bát Tọa", FiveElement.WATER, "", false, true));

		// Thai Phụ & Phong Cáo
		EarthlyBranch thaiPhu = step(vanKhuc, 2);
		EarthlyBranch phongCao = step(vanKhuc, -2);
		map.get(thaiPhu).add(new ChartStarDisplay("Thai Phụ", FiveElement.EARTH, "", false, true));
		map.get(phongCao).add(new ChartStarDisplay("Phong Cáo", FiveElement.WATER, "", false, true));

		// Thiên Tài & Thiên Thọ
		EarthlyBranch thanPalace = layout.positions().get(PalaceFunction.THIEN_DI);
		if (thanPalace == null) thanPalace = mingPalace;
		EarthlyBranch thienTai = step(mingPalace, yearBranch.ordinal());
		EarthlyBranch thienTho = step(thanPalace, yearBranch.ordinal());
		map.get(thienTai).add(new ChartStarDisplay("Thiên Tài", FiveElement.EARTH, "", false, true));
		map.get(thienTho).add(new ChartStarDisplay("Thiên Thọ", FiveElement.EARTH, "", false, true));

		// Thiên Trù
		EarthlyBranch thienTru = switch (yearStem) {
			case JIA, DING -> EarthlyBranch.TI;
			case YI, WU -> EarthlyBranch.NGO;
			case BING -> EarthlyBranch.TY;
			case JI -> EarthlyBranch.THAN;
			case GENG -> EarthlyBranch.DAN;
			case XIN -> EarthlyBranch.MUI;
			case REN -> EarthlyBranch.DAU;
			case GUI -> EarthlyBranch.TUAT;
		};
		map.get(thienTru).add(new ChartStarDisplay("Thiên Trù", FiveElement.EARTH, "", false, true));

		// Thiên Y
		EarthlyBranch thienY = step(EarthlyBranch.SUU, lunarMonth - 1);
		map.get(thienY).add(new ChartStarDisplay("Thiên Y", FiveElement.WATER, "", false, true));

		// Thiên Khốc & Thiên Hư
		EarthlyBranch thienKhoc = step(EarthlyBranch.NGO, -yearBranch.ordinal());
		EarthlyBranch thienHu = step(EarthlyBranch.NGO, yearBranch.ordinal());
		map.get(thienKhoc).add(new ChartStarDisplay("Thiên Khốc", FiveElement.WATER, "(H)", false, false));
		map.get(thienHu).add(new ChartStarDisplay("Thiên Hư", FiveElement.WATER, "(H)", false, false));

		// Giải Thần (cùng cung Phượng Các)
		map.get(phuongCac).add(new ChartStarDisplay("Giải Thần", FiveElement.WOOD, "", false, true));

		// Địa Giải & Thiên Giải
		EarthlyBranch diaGiai = step(EarthlyBranch.MUI, lunarMonth - 1);
		map.get(diaGiai).add(new ChartStarDisplay("Địa Giải", FiveElement.EARTH, "", false, true));
		EarthlyBranch thienGiai = step(EarthlyBranch.THAN, lunarMonth - 1);
		map.get(thienGiai).add(new ChartStarDisplay("Thiên Giải", FiveElement.FIRE, "", false, true));

		// Đầu Quân
		EarthlyBranch dauQuan = step(step(yearBranch, -(lunarMonth - 1)), hourBranch.ordinal());
		map.get(dauQuan).add(new ChartStarDisplay("Đầu Quân", FiveElement.FIRE, "", false, false));

		// Lưu Hà
		EarthlyBranch luuHa = switch (yearStem) {
			case JIA -> EarthlyBranch.DAU;
			case YI -> EarthlyBranch.TUAT;
			case BING -> EarthlyBranch.MUI;
			case DING -> EarthlyBranch.THIN;
			case WU -> EarthlyBranch.TI;
			case JI -> EarthlyBranch.NGO;
			case GENG -> EarthlyBranch.THAN;
			case XIN -> EarthlyBranch.MAO;
			case REN -> EarthlyBranch.HOI;
			case GUI -> EarthlyBranch.DAN;
		};
		map.get(luuHa).add(new ChartStarDisplay("Lưu Hà", FiveElement.WATER, "", false, false));

		// === LƯU NIÊN STARS (L.Sao) FOR VIEWING YEAR ===
		if (viewBranch != null && viewStem != null) {
			// L.Thái Tuế
			map.get(viewBranch).add(new ChartStarDisplay("L.Thái Tuế", FiveElement.FIRE, "", false, false));

			// L.Tang Môn & L.Bạch Hổ
			EarthlyBranch lTangMon = step(EarthlyBranch.DAN, viewBranch.ordinal());
			EarthlyBranch lBachHo = step(lTangMon, 6);
			map.get(lTangMon).add(new ChartStarDisplay("L.Tang Môn", FiveElement.WOOD, "", false, false));
			map.get(lBachHo).add(new ChartStarDisplay("L.Bạch Hổ", FiveElement.METAL, "", false, false));

			// L.Thiên Khốc & L.Thiên Hư
			EarthlyBranch lThienKhoc = step(EarthlyBranch.NGO, -viewBranch.ordinal());
			EarthlyBranch lThienHu = step(EarthlyBranch.NGO, viewBranch.ordinal());
			map.get(lThienKhoc).add(new ChartStarDisplay("L.Thiên Khốc", FiveElement.WATER, "", false, false));
			map.get(lThienHu).add(new ChartStarDisplay("L.Thiên Hư", FiveElement.WATER, "", false, false));

			// L.Lộc Tồn, L.Kình Dương, L.Đà La
			EarthlyBranch lLocTon = switch (viewStem) {
				case JIA -> EarthlyBranch.DAN;
				case YI -> EarthlyBranch.MAO;
				case BING, WU -> EarthlyBranch.TI;
				case DING, JI -> EarthlyBranch.NGO;
				case GENG -> EarthlyBranch.THAN;
				case XIN -> EarthlyBranch.DAU;
				case REN -> EarthlyBranch.HOI;
				case GUI -> EarthlyBranch.TY;
			};
			EarthlyBranch lKinhDuong = step(lLocTon, 1);
			EarthlyBranch lDaLa = step(lLocTon, -1);
			map.get(lLocTon).add(new ChartStarDisplay("L.Lộc Tồn", FiveElement.EARTH, "", false, true));
			map.get(lKinhDuong).add(new ChartStarDisplay("L.Kình Dương", FiveElement.METAL, "", false, false));
			map.get(lDaLa).add(new ChartStarDisplay("L.Đà La", FiveElement.METAL, "", false, false));

			// L.Thiên Mã
			EarthlyBranch lThienMa = switch (viewBranch) {
				case DAN, NGO, TUAT -> EarthlyBranch.THAN;
				case THAN, TY, THIN -> EarthlyBranch.DAN;
				case TI, DAU, SUU -> EarthlyBranch.HOI;
				default -> EarthlyBranch.TI;
			};
			map.get(lThienMa).add(new ChartStarDisplay("L.Thiên Mã", FiveElement.FIRE, "", false, true));

			// L.Thiên Khôi & L.Thiên Việt
			EarthlyBranch lKhoi, lViet;
			switch (viewStem) {
				case JIA, WU, GENG -> { lKhoi = EarthlyBranch.SUU; lViet = EarthlyBranch.MUI; }
				case YI, JI -> { lKhoi = EarthlyBranch.TY; lViet = EarthlyBranch.THAN; }
				case BING, DING -> { lKhoi = EarthlyBranch.HOI; lViet = EarthlyBranch.DAU; }
				case XIN -> { lKhoi = EarthlyBranch.NGO; lViet = EarthlyBranch.DAN; }
				default -> { lKhoi = EarthlyBranch.MAO; lViet = EarthlyBranch.TI; }
			}
			map.get(lKhoi).add(new ChartStarDisplay("L.Thiên Khôi", FiveElement.FIRE, "", false, true));
			map.get(lViet).add(new ChartStarDisplay("L.Thiên Việt", FiveElement.FIRE, "", false, true));

			// L.Đào Hoa & L.Hồng Loan
			EarthlyBranch lDaoHoa = switch (viewBranch) {
				case HOI, MAO, MUI -> EarthlyBranch.TY;
				case DAN, NGO, TUAT -> EarthlyBranch.MAO;
				case TI, DAU, SUU -> EarthlyBranch.NGO;
				default -> EarthlyBranch.DAU;
			};
			EarthlyBranch lHongLoan = step(EarthlyBranch.MAO, -viewBranch.ordinal());
			map.get(lDaoHoa).add(new ChartStarDisplay("L.Đào Hoa", FiveElement.WOOD, "", false, true));
			map.get(lHongLoan).add(new ChartStarDisplay("L.Hồng Loan", FiveElement.WATER, "", false, true));

			// L.Kiếp Sát
			EarthlyBranch lKiepSat = switch (viewBranch) {
				case HOI, MAO, MUI -> EarthlyBranch.THAN;
				case DAN, NGO, TUAT -> EarthlyBranch.HOI;
				case TI, DAU, SUU -> EarthlyBranch.DAN;
				default -> EarthlyBranch.TI;
			};
			map.get(lKiepSat).add(new ChartStarDisplay("L.Kiếp Sát", FiveElement.FIRE, "", false, false));

			// L.Văn Xương & L.Văn Khúc
			EarthlyBranch lVanXuong = step(EarthlyBranch.TUAT, -viewBranch.ordinal());
			EarthlyBranch lVanKhuc = step(EarthlyBranch.THIN, viewBranch.ordinal());
			map.get(lVanXuong).add(new ChartStarDisplay("L.Văn Xương", FiveElement.METAL, "", false, true));
			map.get(lVanKhuc).add(new ChartStarDisplay("L.Văn Khúc", FiveElement.WATER, "", false, true));

			// Lưu Tứ Hóa
			placeLuuTuHoa(map, chart, viewStem, vanXuong, vanKhuc);
		}

		return map;
	}

	private void placeLuuTuHoa(
			Map<EarthlyBranch, List<ChartStarDisplay>> map,
			TracuutuviChart chart,
			HeavenlyStem viewStem,
			EarthlyBranch natalVanXuong,
			EarthlyBranch natalVanKhuc) {

		StarId starLoc = null, starQuyen = null, starKhoa = null, starKy = null;
		switch (viewStem) {
			case JIA -> { starLoc = StarId.LIEM_TRINH; starQuyen = StarId.PHA_QUAN; starKhoa = StarId.VU_KHUC; starKy = StarId.THAI_DUONG; }
			case YI -> { starLoc = StarId.THIEN_CO; starQuyen = StarId.THIEN_LUONG; starKhoa = StarId.TU_VI; starKy = StarId.THAI_AM; }
			case BING -> { starLoc = StarId.THIEN_DONG; starQuyen = StarId.THIEN_CO; starKhoa = null; starKy = StarId.LIEM_TRINH; }
			case DING -> { starLoc = StarId.THAI_AM; starQuyen = StarId.THIEN_DONG; starKhoa = StarId.THIEN_CO; starKy = StarId.CU_MON; }
			case WU -> { starLoc = StarId.THAM_LANG; starQuyen = StarId.THAI_AM; starKhoa = StarId.HUU_BAT; starKy = StarId.THIEN_CO; }
			case JI -> { starLoc = StarId.VU_KHUC; starQuyen = StarId.THAM_LANG; starKhoa = StarId.THIEN_LUONG; starKy = null; }
			case GENG -> { starLoc = StarId.THAI_DUONG; starQuyen = StarId.VU_KHUC; starKhoa = StarId.THAI_AM; starKy = StarId.THIEN_DONG; }
			case XIN -> { starLoc = StarId.CU_MON; starQuyen = StarId.THAI_DUONG; starKhoa = null; starKy = null; }
			case REN -> { starLoc = StarId.THIEN_LUONG; starQuyen = StarId.TU_VI; starKhoa = StarId.THIEN_PHU; starKy = StarId.VU_KHUC; }
			case GUI -> { starLoc = StarId.PHA_QUAN; starQuyen = StarId.CU_MON; starKhoa = StarId.THAI_AM; starKy = StarId.THAM_LANG; }
		}

		EarthlyBranch bLoc = findStarBranch(chart, starLoc, natalVanXuong, natalVanKhuc);
		EarthlyBranch bQuyen = findStarBranch(chart, starQuyen, natalVanXuong, natalVanKhuc);
		EarthlyBranch bKhoa = (viewStem == HeavenlyStem.BING) ? natalVanXuong :
				(viewStem == HeavenlyStem.XIN) ? natalVanKhuc : findStarBranch(chart, starKhoa, natalVanXuong, natalVanKhuc);
		EarthlyBranch bKy = (viewStem == HeavenlyStem.JI) ? natalVanKhuc :
				(viewStem == HeavenlyStem.XIN) ? natalVanXuong : findStarBranch(chart, starKy, natalVanXuong, natalVanKhuc);

		if (bLoc != null) map.get(bLoc).add(new ChartStarDisplay("L.Hóa Lộc", FiveElement.WOOD, "", false, true));
		if (bQuyen != null) map.get(bQuyen).add(new ChartStarDisplay("L.Hóa Quyền", FiveElement.WOOD, "", false, true));
		if (bKhoa != null) map.get(bKhoa).add(new ChartStarDisplay("L.Hóa Khoa", FiveElement.WATER, "", false, true));
		if (bKy != null) map.get(bKy).add(new ChartStarDisplay("L.Hóa Kỵ", FiveElement.WATER, "", false, false));
	}

	private EarthlyBranch findStarBranch(TracuutuviChart chart, StarId starId, EarthlyBranch vanXuong, EarthlyBranch vanKhuc) {
		if (starId == null || chart == null) return null;
		if (starId == StarId.VAN_XUONG) return vanXuong;
		if (starId == StarId.VAN_KHUC) return vanKhuc;
		return chart.stars().branchOf(starId).orElse(null);


	}

	private static EarthlyBranch step(EarthlyBranch start, int steps) {
		EarthlyBranch[] branches = EarthlyBranch.values();
		int index = start.ordinal();
		int remaining = Math.abs(steps);
		int direction = Integer.signum(steps);
		for (int count = 0; count < remaining; count++) {
			index += direction;
			if (index < 0) {
				index = branches.length - 1;
			} else if (index == branches.length) {
				index = 0;
			}
		}
		return branches[index];
	}

	private static int julianDay(LocalDate date) {
		int day = date.getDayOfMonth();
		int month = date.getMonthValue();
		int year = date.getYear();
		int a = (14 - month) / 12;
		int y = year + 4800 - a;
		int m = month + 12 * a - 3;
		int jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
		if (jd < 2299161) {
			return day + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
		}
		return jd;
	}
}

