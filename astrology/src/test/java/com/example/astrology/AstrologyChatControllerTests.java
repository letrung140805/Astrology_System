package com.example.astrology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.astrology.repository.ChatMessageRepository;
import com.example.astrology.service.AstrologyAnswerGenerator;

import reactor.core.publisher.Flux;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:astroai-chat;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.ai.openai.api-key=test-key",
		"spring.ai.openai.chat.model=test-model"
})
@AutoConfigureMockMvc
class AstrologyChatControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@MockitoBean
	private AstrologyAnswerGenerator answerGenerator;

	@BeforeEach
	void setUp() {
		chatMessageRepository.deleteAll();
	}

	@Test
	void guestCanAskAboutSavedChartAndReloadPersistedConversation() throws Exception {
		when(answerGenerator.generate(anyString(), anyList(), anyString()))
				.thenReturn("Cung Mệnh có dữ liệu sao đã lưu; đây là diễn giải tham khảo.");
		when(answerGenerator.modelName()).thenReturn("test-model");

		MvcResult chartCreation = mockMvc.perform(post("/result").with(csrf())
				.param("name", "Người hỏi")
				.param("gender", "Nữ")
				.param("birthDate", "1999-06-12")
				.param("birthTime", "10:30"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		String resultPath = URI.create(chartCreation.getResponse().getRedirectedUrl()).getPath();
		UUID accessToken = UUID.fromString(resultPath.substring("/result/".length()));

		mockMvc.perform(get("/chat/{accessToken}", accessToken))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Người hỏi")));

		MvcResult chatPost = mockMvc.perform(post("/chat/{accessToken}", accessToken).with(csrf())
				.param("message", "Các sao nào nằm ở cung Mệnh?"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		String chatUrl = chatPost.getResponse().getRedirectedUrl();

		mockMvc.perform(get(chatUrl))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Các sao nào nằm ở cung Mệnh?")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString(
						"Cung Mệnh có dữ liệu sao đã lưu; đây là diễn giải tham khảo.")));

		assertEquals(2, chatMessageRepository.count());
		assertTrue(chatUrl.contains("conversationId="));
	}

	@Test
	void streamReturnsSseAndPersistsMessage() throws Exception {
		when(answerGenerator.stream(anyString(), anyList(), anyString()))
				.thenReturn(Flux.just("Tử Vi ", "ở ", "cung Mệnh."));
		when(answerGenerator.modelName()).thenReturn("test-model");

		MvcResult chartCreation = mockMvc.perform(post("/result").with(csrf())
				.param("name", "Người test stream")
				.param("gender", "Nam")
				.param("birthDate", "2000-01-01")
				.param("birthTime", "08:00"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		String resultPath = URI.create(chartCreation.getResponse().getRedirectedUrl()).getPath();
		UUID accessToken = UUID.fromString(resultPath.substring("/result/".length()));

		MvcResult asyncResult = mockMvc.perform(post("/chat/{accessToken}/stream", accessToken).with(csrf())
				.param("message", "Sao Tử Vi ý nghĩa gì?"))
				.andExpect(request().asyncStarted())
				.andReturn();

		mockMvc.perform(asyncDispatch(asyncResult))
				.andExpect(status().isOk());

		assertEquals(2, chatMessageRepository.count());
	}

	@Test
	void streamFallsBackToSynchronousWhenStreamFails() throws Exception {
		when(answerGenerator.stream(anyString(), anyList(), anyString()))
				.thenReturn(Flux.error(new RuntimeException("Stream failed")));
		when(answerGenerator.generate(anyString(), anyList(), anyString()))
				.thenReturn("Câu trả lời dự phòng khi stream bị lỗi.");
		when(answerGenerator.modelName()).thenReturn("test-model");

		MvcResult chartCreation = mockMvc.perform(post("/result").with(csrf())
				.param("name", "Người test fallback")
				.param("gender", "Nữ")
				.param("birthDate", "2001-05-15")
				.param("birthTime", "14:20"))
				.andExpect(status().is3xxRedirection())
				.andReturn();
		String resultPath = URI.create(chartCreation.getResponse().getRedirectedUrl()).getPath();
		UUID accessToken = UUID.fromString(resultPath.substring("/result/".length()));

		MvcResult asyncResult = mockMvc.perform(post("/chat/{accessToken}/stream", accessToken).with(csrf())
				.param("message", "Theo lá số này thì tôi phù hợp ngành gì?"))
				.andExpect(request().asyncStarted())
				.andReturn();

		mockMvc.perform(asyncDispatch(asyncResult))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"done\":true")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("\"token\":")));

		assertEquals(2, chatMessageRepository.count());
	}
}

