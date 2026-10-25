package com.example.astrology;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:astroai-web;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.ai.openai.api-key=test-key",
		"spring.ai.openai.chat.model=test-model"
})
@AutoConfigureMockMvc
class AstrologyApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void homeFormRendersCsrfTokenForPostSubmission() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
	}

	@Test
	void resultPageCalculatesAndSavesChartFromBasicBirthInput() throws Exception {
		MvcResult creation = mockMvc.perform(post("/result").with(csrf())
				.param("name", "Lan")
				.param("gender", "Nữ")
				.param("birthDate", "1999-06-12")
				.param("birthTime", "10:30"))
				.andExpect(status().is3xxRedirection())
				.andReturn();

		mockMvc.perform(get(creation.getResponse().getRedirectedUrl()))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Địa chi giờ tính được")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Ngày âm lịch tự quy đổi")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Lá số theo ruleset ứng viên")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("đã lưu và được tải từ cơ sở dữ liệu")));
	}

	@Test
	void savedResultPageRendersCalculatedPalacesWithoutManualLunarInput() throws Exception {
		MvcResult creation = mockMvc.perform(post("/result").with(csrf())
				.param("name", "Lan")
				.param("gender", "Nữ")
				.param("birthDate", "1999-06-12")
				.param("birthTime", "05:30"))
				.andExpect(status().is3xxRedirection())
				.andReturn();

		mockMvc.perform(get(creation.getResponse().getRedirectedUrl()))
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

	@Test
	void historyPageListsSavedGuestChartsFromTheCurrentSession() throws Exception {
		MvcResult creation = mockMvc.perform(post("/result").with(csrf())
				.param("name", "Lịch sử kiểm thử")
				.param("gender", "Nữ")
				.param("birthDate", "1999-06-12")
				.param("birthTime", "05:30"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		MockHttpSession session = (MockHttpSession) creation.getRequest().getSession(false);

		mockMvc.perform(get("/history").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Lịch sử kiểm thử")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						creation.getResponse().getRedirectedUrl())));
	}

}
