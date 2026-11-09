package com.example.astrology.model;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

public class CoupleCalculationForm {

	private String name1;
	private String gender1 = "Nam";
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birthDate1;
	private Integer birthDay1;
	private Integer birthMonth1;
	private Integer birthYear1;
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime birthTime1;
	private String birthplace1;

	private String name2;
	private String gender2 = "Nữ";
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birthDate2;
	private Integer birthDay2;
	private Integer birthMonth2;
	private Integer birthYear2;
	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	private LocalTime birthTime2;
	private String birthplace2;

	private Integer viewYear = 2026;

	public CoupleCalculationForm() {
	}

	public CoupleCalculationForm(
			String name1, String gender1, LocalDate birthDate1, Integer birthDay1, Integer birthMonth1, Integer birthYear1, LocalTime birthTime1, String birthplace1,
			String name2, String gender2, LocalDate birthDate2, Integer birthDay2, Integer birthMonth2, Integer birthYear2, LocalTime birthTime2, String birthplace2,
			Integer viewYear) {
		this.name1 = name1;
		this.gender1 = gender1 != null ? gender1 : "Nam";
		this.birthDate1 = birthDate1;
		this.birthDay1 = birthDay1;
		this.birthMonth1 = birthMonth1;
		this.birthYear1 = birthYear1;
		this.birthTime1 = birthTime1;
		this.birthplace1 = birthplace1;

		this.name2 = name2;
		this.gender2 = gender2 != null ? gender2 : "Nữ";
		this.birthDate2 = birthDate2;
		this.birthDay2 = birthDay2;
		this.birthMonth2 = birthMonth2;
		this.birthYear2 = birthYear2;
		this.birthTime2 = birthTime2;
		this.birthplace2 = birthplace2;

		this.viewYear = viewYear != null ? viewYear : 2026;
	}

	public LocalDate resolveBirthDate1() throws DateTimeException {
		if (birthDate1 != null) {
			return birthDate1;
		}
		if (birthDay1 != null && birthMonth1 != null && birthYear1 != null) {
			return LocalDate.of(birthYear1, birthMonth1, birthDay1);
		}
		return null;
	}

	public LocalDate resolveBirthDate2() throws DateTimeException {
		if (birthDate2 != null) {
			return birthDate2;
		}
		if (birthDay2 != null && birthMonth2 != null && birthYear2 != null) {
			return LocalDate.of(birthYear2, birthMonth2, birthDay2);
		}
		return null;
	}

	public String getName1() { return name1; }
	public void setName1(String name1) { this.name1 = name1; }

	public String getGender1() { return gender1; }
	public void setGender1(String gender1) { this.gender1 = gender1; }

	public LocalDate getBirthDate1() { return birthDate1; }
	public void setBirthDate1(LocalDate birthDate1) { this.birthDate1 = birthDate1; }

	public Integer getBirthDay1() { return birthDay1; }
	public void setBirthDay1(Integer birthDay1) { this.birthDay1 = birthDay1; }

	public Integer getBirthMonth1() { return birthMonth1; }
	public void setBirthMonth1(Integer birthMonth1) { this.birthMonth1 = birthMonth1; }

	public Integer getBirthYear1() { return birthYear1; }
	public void setBirthYear1(Integer birthYear1) { this.birthYear1 = birthYear1; }

	public LocalTime getBirthTime1() { return birthTime1; }
	public void setBirthTime1(LocalTime birthTime1) { this.birthTime1 = birthTime1; }

	public String getBirthplace1() { return birthplace1; }
	public void setBirthplace1(String birthplace1) { this.birthplace1 = birthplace1; }

	public String getName2() { return name2; }
	public void setName2(String name2) { this.name2 = name2; }

	public String getGender2() { return gender2; }
	public void setGender2(String gender2) { this.gender2 = gender2; }

	public LocalDate getBirthDate2() { return birthDate2; }
	public void setBirthDate2(LocalDate birthDate2) { this.birthDate2 = birthDate2; }

	public Integer getBirthDay2() { return birthDay2; }
	public void setBirthDay2(Integer birthDay2) { this.birthDay2 = birthDay2; }

	public Integer getBirthMonth2() { return birthMonth2; }
	public void setBirthMonth2(Integer birthMonth2) { this.birthMonth2 = birthMonth2; }

	public Integer getBirthYear2() { return birthYear2; }
	public void setBirthYear2(Integer birthYear2) { this.birthYear2 = birthYear2; }

	public LocalTime getBirthTime2() { return birthTime2; }
	public void setBirthTime2(LocalTime birthTime2) { this.birthTime2 = birthTime2; }

	public String getBirthplace2() { return birthplace2; }
	public void setBirthplace2(String birthplace2) { this.birthplace2 = birthplace2; }

	public Integer getViewYear() { return viewYear; }
	public void setViewYear(Integer viewYear) { this.viewYear = viewYear; }
}
