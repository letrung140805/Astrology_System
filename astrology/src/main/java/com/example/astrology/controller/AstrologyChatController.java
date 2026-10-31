package com.example.astrology.controller;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.astrology.model.ConversationHistoryItem;
import com.example.astrology.service.AstrologyChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Controller
public class AstrologyChatController {

	private static final Logger logger = LoggerFactory.getLogger(AstrologyChatController.class);

	private final AstrologyChatService astrologyChatService;
	private final ObjectMapper objectMapper;

	public AstrologyChatController(
			AstrologyChatService astrologyChatService,
			ObjectMapper objectMapper) {
		this.astrologyChatService = astrologyChatService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/chat")
	public String chatWithoutChart() {
		return "redirect:/history";
	}

	@GetMapping("/chat/{accessToken}")
	public String chat(
			@PathVariable UUID accessToken,
			@RequestParam(required = false) Long conversationId,
			@RequestParam(required = false, defaultValue = "false") boolean newChat,
			Principal principal,
			Model model) {
		String userPrincipal = principalName(principal);
		var historyList = astrologyChatService.listConversations(accessToken, userPrincipal);

		if (conversationId == null && !newChat && !historyList.isEmpty()) {
			conversationId = historyList.get(0).id();
		}

		var session = astrologyChatService.load(accessToken, conversationId, userPrincipal);
		model.addAttribute("accessToken", session.accessToken());
		model.addAttribute("conversationId", session.conversationId());
		model.addAttribute("chartName", session.chartName());
		model.addAttribute("messages", session.messages());
		model.addAttribute("conversations", historyList);
		model.addAttribute("isNewChat", newChat || session.conversationId() == null);
		return "chat";
	}

	@GetMapping(value = "/chat/{accessToken}/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<ConversationHistoryItem> getConversations(
			@PathVariable UUID accessToken,
			Principal principal) {
		return astrologyChatService.listConversations(accessToken, principalName(principal));
	}

	@PostMapping("/chat/{accessToken}/delete/{conversationId}")
	public String deleteConversation(
			@PathVariable UUID accessToken,
			@PathVariable Long conversationId,
			Principal principal,
			RedirectAttributes redirectAttributes) {
		try {
			astrologyChatService.deleteConversation(accessToken, conversationId, principalName(principal));
			redirectAttributes.addFlashAttribute("chatNotice", "Đã xóa cuộc đàm đạo thành công.");
		} catch (Exception ex) {
			logger.warn("Could not delete conversation", ex);
			redirectAttributes.addFlashAttribute("chatError", "Không thể xóa cuộc đàm đạo này.");
		}
		return "redirect:/chat/" + accessToken;
	}

	@PostMapping("/chat/{accessToken}")
	public String send(
			@PathVariable UUID accessToken,
			@RequestParam(required = false) Long conversationId,
			@RequestParam String message,
			Principal principal,
			RedirectAttributes redirectAttributes) {
		try {
			var reply = astrologyChatService.send(
					accessToken, conversationId, principalName(principal), message);
			if (reply.persisted()) {
				return "redirect:/chat/" + accessToken + "?conversationId=" + reply.conversationId();
			}
			redirectAttributes.addFlashAttribute("chatWarning",
					"AI đã trả lời nhưng chưa lưu được lịch sử. Hãy chạy tuvi_AI.sql để cập nhật bảng conversations.");
			redirectAttributes.addFlashAttribute("chatQuestion", message);
			redirectAttributes.addFlashAttribute("chatAnswer", reply.answer());
		} catch (ResponseStatusException exception) {
			throw exception;
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("chatError", exception.getMessage());
			redirectAttributes.addFlashAttribute("chatDraft", message);
		} catch (DataAccessException exception) {
			logger.warn("Could not load chart data for astrology chat.", exception);
			redirectAttributes.addFlashAttribute("chatError",
					"Không đọc được lá số từ database. Kiểm tra MySQL và cấu hình kết nối.");
			redirectAttributes.addFlashAttribute("chatDraft", message);
		} catch (RuntimeException exception) {
			logger.warn("Could not generate an astrology chat response.", exception);
			redirectAttributes.addFlashAttribute("chatError", explainFailure(exception));
			redirectAttributes.addFlashAttribute("chatDraft", message);
		}
		String redirect = "redirect:/chat/" + accessToken;
		return conversationId == null ? redirect : redirect + "?conversationId=" + conversationId;
	}

	@RequestMapping(value = "/chat/{accessToken}/stream", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(
			@PathVariable UUID accessToken,
			@RequestParam(required = false) Long conversationId,
			@RequestParam String message,
			Principal principal) {
		SseEmitter emitter = new SseEmitter(180_000L);
		String userPrincipal = principalName(principal);

		if (message == null || message.isBlank()) {
			sendEvent(emitter, Map.of("error", "Hãy nhập câu hỏi trước khi gửi."));
			emitter.complete();
			return emitter;
		}

		String cleanQuestion = message.trim();
		if (cleanQuestion.length() > 2000) {
			sendEvent(emitter, Map.of("error", "Câu hỏi không được dài quá 2.000 ký tự."));
			emitter.complete();
			return emitter;
		}

		StringBuilder fullContent = new StringBuilder();

		try {
			Flux<String> streamFlux = astrologyChatService.stream(
					accessToken, conversationId, userPrincipal, cleanQuestion);

			streamFlux.subscribe(
					token -> {
						fullContent.append(token);
						sendEvent(emitter, Map.of("token", token));
					},
					error -> {
						logger.warn("Error during astrology chat streaming: {}", error.getMessage());
						// Fallback: if stream dropped before sending any content, try synchronous generation
						if (fullContent.length() == 0) {
							logger.info("Stream failed before sending tokens. Attempting synchronous fallback generation...");
							try {
								var reply = astrologyChatService.send(
										accessToken, conversationId, userPrincipal, cleanQuestion);
								sendEvent(emitter, Map.of("token", reply.answer()));
								if (reply.persisted()) {
									sendEvent(emitter, Map.of("done", true, "conversationId", reply.conversationId()));
								} else {
									sendEvent(emitter, Map.of("done", true, "warning",
											"Câu trả lời đã hoàn tất nhưng chưa lưu được vào lịch sử."));
								}
								emitter.complete();
								return;
							} catch (Exception fallbackEx) {
								logger.warn("Fallback synchronous generation also failed", fallbackEx);
								sendEvent(emitter, Map.of("error", explainFailure(fallbackEx)));
								emitter.complete();
								return;
							}
						}
						// If partial content was already sent, stream interrupted mid-flight
						logger.info("Stream interrupted after partial tokens. Preserving partial response...");
						String interruptNotice = "\n\n*(Lưu ý: Đoạn phản hồi này bị gián đoạn do kết nối máy chủ AI. Quý khách có thể gửi lại câu hỏi để nhận luận giải trọn vẹn)*";
						String partialAnswer = fullContent.toString() + interruptNotice;
						sendEvent(emitter, Map.of("token", interruptNotice));
						try {
							Long savedId = astrologyChatService.saveExchange(
									accessToken, conversationId, userPrincipal, cleanQuestion, partialAnswer);
							sendEvent(emitter, Map.of("done", true, "conversationId", savedId));
						} catch (Exception saveEx) {
							logger.warn("Could not persist partial interrupted stream", saveEx);
							sendEvent(emitter, Map.of("done", true, "warning", explainFailure(error)));
						}
						emitter.complete();
					},
					() -> {
						try {
							String finalAnswer = fullContent.toString();
							if (!finalAnswer.isBlank()) {
								try {
									Long savedId = astrologyChatService.saveExchange(
											accessToken, conversationId, userPrincipal, cleanQuestion, finalAnswer);
									sendEvent(emitter, Map.of("done", true, "conversationId", savedId));
								} catch (Exception saveEx) {
									logger.warn("Stream completed but persistence failed", saveEx);
									sendEvent(emitter, Map.of("done", true, "warning",
											"Câu trả lời đã hoàn tất nhưng chưa lưu được vào lịch sử."));
								}
							} else {
								// Stream completed with no content (e.g. reasoning-only stream on free models)
								logger.info("Stream completed with blank content. Attempting synchronous fallback...");
								try {
									var reply = astrologyChatService.send(
											accessToken, conversationId, userPrincipal, cleanQuestion);
									sendEvent(emitter, Map.of("token", reply.answer()));
									if (reply.persisted()) {
										sendEvent(emitter, Map.of("done", true, "conversationId", reply.conversationId()));
									} else {
										sendEvent(emitter, Map.of("done", true));
									}
								} catch (Exception fallbackEx) {
									logger.warn("Synchronous fallback also failed", fallbackEx);
									sendEvent(emitter, Map.of("error", explainFailure(fallbackEx)));
								}
							}
						} finally {
							emitter.complete();
						}
					}
			);
		} catch (Exception ex) {
			logger.warn("Could not initiate streaming for astrology chat, attempting synchronous fallback...", ex);
			try {
				var reply = astrologyChatService.send(
						accessToken, conversationId, userPrincipal, cleanQuestion);
				sendEvent(emitter, Map.of("token", reply.answer()));
				if (reply.persisted()) {
					sendEvent(emitter, Map.of("done", true, "conversationId", reply.conversationId()));
				} else {
					sendEvent(emitter, Map.of("done", true, "warning",
							"Câu trả lời đã hoàn tất nhưng chưa lưu được vào lịch sử."));
				}
			} catch (Exception fallbackEx) {
				logger.warn("Fallback synchronous generation also failed", fallbackEx);
				sendEvent(emitter, Map.of("error", explainFailure(fallbackEx)));
			} finally {
				emitter.complete();
			}
		}

		emitter.onTimeout(emitter::complete);
		emitter.onError(err -> emitter.complete());

		return emitter;
	}

	private void sendEvent(SseEmitter emitter, Map<String, Object> data) {
		try {
			emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(data)));
		} catch (Exception ex) {
			logger.debug("Failed sending SSE event to client", ex);
		}
	}

	private static String explainFailure(Throwable exception) {
		StringBuilder details = new StringBuilder();
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause.getMessage() != null) {
				details.append(' ').append(cause.getMessage());
			}
		}
		String message = details.toString().toLowerCase(Locale.ROOT);
		if (message.contains("user not found") || message.contains("no cookie auth")
				|| message.contains("chưa cấu hình api key")
				|| message.contains("api_key") || message.contains("apikey") || message.contains("api key")
				|| message.contains("invalid_api_key") || message.contains("incorrect api key")
				|| message.contains("401") || message.contains("unauthorized")) {
			return "Chưa cấu hình API Key Google Gemini (AI Studio) hoặc API Key không hợp lệ. Vui lòng thiết lập biến môi trường GEMINI_API_KEY.";
		}
		if (message.contains("insufficient_quota") || message.contains("billing")
				|| message.contains("exceeded your current quota")) {
			return "Tài khoản Google AI Studio vượt quá hạn mức sử dụng (quota). Kiểm tra hạn mức trên Google AI Studio.";
		}
		if (message.contains("429") || message.contains("rate limit")) {
			return "Google Gemini đang giới hạn tần suất yêu cầu. Vui lòng chờ một lúc rồi gửi lại.";
		}
		if (message.contains("model_not_found") || message.contains("model not found")
				|| message.contains("does not exist") || message.contains("404")) {
			return "Model Gemini không tồn tại hoặc API key không có quyền dùng model này. Kiểm tra cấu hình GEMINI_MODEL.";
		}
		if (message.contains("stream failed") || message.contains("unexpected end of stream")
				|| message.contains("connection reset") || message.contains("broken pipe")
				|| message.contains("connect timed out") || message.contains("sockettimeout")
				|| message.contains("timeout") || message.contains("503") || message.contains("unavailable")) {
			return "Kết nối tới mô hình Google Gemini bị gián đoạn (timeout hoặc quá tải tạm thời). Vui lòng thử gửi lại câu hỏi.";
		}
		if (message.contains("unknownhost") || message.contains("connection refused")) {
			return "Không kết nối được tới máy chủ Google AI Studio. Kiểm tra kết nối mạng, proxy hoặc firewall.";
		}
		return "Chưa tạo được câu trả lời. Xem terminal chạy Spring Boot để biết lỗi chi tiết: " + (exception.getMessage() != null ? exception.getMessage() : "");
	}

	private static String principalName(Principal principal) {
		if (principal == null || "anonymousUser".equals(principal.getName())) {
			return null;
		}
			return principal.getName();
	}
}
