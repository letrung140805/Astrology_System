package com.example.astrology.model;

import java.util.List;
import java.util.Objects;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.chart.PalaceFunction;

public record ChartPalaceDisplay(
		PalaceFunction function,
		EarthlyBranch branch,
		String stemBranchCode,
		String palaceSignElement,
		String palaceElement,
		int daiHan,
		String nguyetHan,
		String trangSinh,
		String dvLabel,
		String lnLabel,
		boolean hasTuan,
		boolean hasTriet,
		List<ChartStarDisplay> majorStars,
		List<ChartStarDisplay> goodStars,
		List<ChartStarDisplay> badStars,
		List<ChartStarDisplay> stars) {

	public ChartPalaceDisplay {
		Objects.requireNonNull(function, "Cung chức không được để trống.");
		Objects.requireNonNull(branch, "Địa chi không được để trống.");
		stars = List.copyOf(Objects.requireNonNull(stars, "Danh sách sao không được để trống."));
		majorStars = majorStars == null ? List.of() : List.copyOf(majorStars);
		goodStars = goodStars == null ? List.of() : List.copyOf(goodStars);
		badStars = badStars == null ? List.of() : List.copyOf(badStars);
		stemBranchCode = stemBranchCode == null ? branch.vietnameseName() : stemBranchCode;
		palaceSignElement = palaceSignElement == null ? "" : palaceSignElement;
		palaceElement = palaceElement == null ? "" : palaceElement;
		nguyetHan = nguyetHan == null ? "" : nguyetHan;
		trangSinh = trangSinh == null ? "" : trangSinh;
		dvLabel = dvLabel == null ? "" : dvLabel;
		lnLabel = lnLabel == null ? "" : lnLabel;
	}

	public ChartPalaceDisplay(
			PalaceFunction function,
			EarthlyBranch branch,
			String stemBranchCode,
			String palaceElement,
			int daiHan,
			String nguyetHan,
			String trangSinh,
			boolean hasTuan,
			boolean hasTriet,
			List<ChartStarDisplay> majorStars,
			List<ChartStarDisplay> goodStars,
			List<ChartStarDisplay> badStars,
			List<ChartStarDisplay> stars) {
		this(function, branch, stemBranchCode, "", palaceElement, daiHan, nguyetHan, trangSinh, "", "", hasTuan, hasTriet, majorStars, goodStars, badStars, stars);
	}

	public ChartPalaceDisplay(PalaceFunction function, EarthlyBranch branch, List<ChartStarDisplay> stars) {
		this(function, branch, branch.vietnameseName(), "", "", 0, "", "", "", "", false, false, List.of(), List.of(), List.of(), stars);
	}
}
