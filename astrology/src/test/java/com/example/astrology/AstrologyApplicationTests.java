package com.example.astrology;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
class AstrologyApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void resultPageCalculatesLunarDateAndChartFromBasicBirthInput() throws Exception {
		mockMvc.perform(get("/result")
				.param("name", "Lan")
				.param("gender", "Nữ")
				.param("birthDate", "1999-06-12")
				.param("birthTime", "10:30"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Địa chi giờ tính được")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Ngày âm lịch tự quy đổi")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Lá số theo ruleset ứng viên")));
	}

	@Test
	void resultPageRendersCalculatedPalacesWithoutManualLunarInput() throws Exception {
		mockMvc.perform(get("/result")
				.param("name", "Lan")
				.param("gender", "Nữ")
				.param("birthDate", "1999-06-12")
				.param("birthTime", "05:30"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Lá số theo ruleset ứng viên")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Cung Mệnh")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Tử Vi")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("element-")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("astrology-chart-grid")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("palace-thien_di")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("astrology-chart-center")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("LÁ SỐ CÁ NHÂN")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"chưa được xác minh bằng fixture lá số thực")));
	}

}
