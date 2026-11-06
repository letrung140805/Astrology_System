package com.example.astrology.model;

import java.util.List;

import com.example.astrology.calculation.TuViDetailService.FullChartDetails;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider.LunarDate;
import com.example.astrology.calculation.chart.TracuutuviChart;

public record CouplePersonData(
		String name,
		String gender,
		String birthDate,
		Integer solarYear,
		Integer solarMonth,
		Integer solarDay,
		String birthTime,
		String birthplace,
		String hourBranch,
		TracuutuviChart chart,
		List<ChartPalaceDisplay> chartPalaces,
		LunarDate lunarDate,
		String yearCanChi,
		String yearNayinElement,
		FullChartDetails fullDetails
) {

	public String banMenhFull() {
		return fullDetails != null && fullDetails.banMenhFull() != null ? fullDetails.banMenhFull() : (yearNayinElement != null ? yearNayinElement : "Chưa xác định");
	}

	public String menhCucSinhKhac() {
		return fullDetails != null && fullDetails.menhCucSinhKhac() != null ? fullDetails.menhCucSinhKhac() : "";
	}

	public String amDuongNamNu() {
		return fullDetails != null && fullDetails.amDuongNamNu() != null ? fullDetails.amDuongNamNu() : gender;
	}

	public String amDuongLy() {
		return fullDetails != null && fullDetails.amDuongLy() != null ? fullDetails.amDuongLy() : "";
	}

	public String chuMenh() {
		return fullDetails != null && fullDetails.chuMenh() != null ? fullDetails.chuMenh() : "";
	}

	public String chuThan() {
		return fullDetails != null && fullDetails.chuThan() != null ? fullDetails.chuThan() : "";
	}

	public String laiNhanCung() {
		return fullDetails != null && fullDetails.laiNhanCung() != null ? fullDetails.laiNhanCung() : "";
	}

	public String canLuong() {
		return fullDetails != null && fullDetails.canLuong() != null ? fullDetails.canLuong() : "";
	}

	public String monthCanChi() {
		return fullDetails != null && fullDetails.monthCanChi() != null ? fullDetails.monthCanChi() : "";
	}

	public String dayCanChi() {
		return fullDetails != null && fullDetails.dayCanChi() != null ? fullDetails.dayCanChi() : "";
	}

	public String hourCanChi() {
		return fullDetails != null && fullDetails.hourCanChi() != null ? fullDetails.hourCanChi() : "";
	}

	public String viewYearInfo() {
		return fullDetails != null && fullDetails.viewYearInfo() != null ? fullDetails.viewYearInfo() : "Bính Ngọ (2026), 22 tuổi";
	}

	public String tuanMarkerPosition() {
		return fullDetails != null ? fullDetails.tuanMarkerPosition() : null;
	}

	public String trietMarkerPosition() {
		return fullDetails != null ? fullDetails.trietMarkerPosition() : null;
	}
}

