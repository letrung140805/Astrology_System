package com.example.astrology.controller;

import java.security.Principal;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
			Principal principal,
			Model model) {
		var session = astrologyChatService.load(accessToken, conversationId, principalName(principal));
		model.addAttribute("accessToken", session.accessToken());
		model.addAttribute("conversationId", session.conversationId());
		model.addAttribute("chartName", session.chartName());
		model.addAttribute("messages", session.messages());
		return "chat";
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
						sendEvent(emitter, Map.of("error", explainFailure(error)));
						emitter.complete();
					},
					() -> {
						try {
							String finalAnswer = fullContent.toString();
							if (!finalAnswer.isBlank()) {
								Long savedId = astrologyChatService.saveExchange(
										accessToken, conversationId, userPrincipal, cleanQuestion, finalAnswer);
								sendEvent(emitter, Map.of("done", true, "conversationId", savedId));
							} else {
								// Stream completed with no content (e.g. reasoning-only stream on free models)
								logger.info("Stream completed with blank content. Attempting synchronous fallback...");
								var reply = astrologyChatService.send(
										accessToken, conversationId, userPrincipal, cleanQuestion);
								sendEvent(emitter, Map.of("token", reply.answer()));
								if (reply.persisted()) {
									sendEvent(emitter, Map.of("done", true, "conversationId", reply.conversationId()));
								} else {
									sendEvent(emitter, Map.of("done", true));
								}
							}
						} catch (Exception saveEx) {
							logger.warn("Stream completed but persistence failed", saveEx);
							sendEvent(emitter, Map.of("done", true, "warning",
									"Câu trả lời đã hoàn tất nhưng chưa lưu được vào lịch sử."));
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
			return "Chưa cấu hình API Key OpenRouter hoặc API Key không hợp lệ. Vui lòng thiết lập biến môi trường OPENROUTER_API_KEY.";
		}
		if (message.contains("insufficient_quota") || message.contains("billing")
				|| message.contains("exceeded your current quota")) {
			return "Tài khoản OpenRouter hết credit hoặc upstream chưa bật thanh toán. Kiểm tra Usage và Credits.";
		}
		if (message.contains("429") || message.contains("rate limit")) {
			return "OpenRouter đang giới hạn tốc độ yêu cầu. Chờ một lúc rồi gửi lại.";
		}
		if (message.contains("model_not_found") || message.contains("model not found")
				|| message.contains("does not exist") || message.contains("404")) {
			return "Model không tồn tại hoặc API key không có quyền dùng model này. Kiểm tra OPENROUTER_MODEL.";
		}
		if (message.contains("stream failed") || message.contains("unexpected end of stream")
				|| message.contains("connection reset") || message.contains("broken pipe")
				|| message.contains("connect timed out") || message.contains("sockettimeout")
				|| message.contains("timeout")) {
			return "Kết nối tới mô hình AI bị gián đoạn (timeout hoặc nhà cung cấp OpenRouter quá tải). Vui lòng thử gửi lại câu hỏi hoặc chọn model khác.";
		}
		if (message.contains("unknownhost") || message.contains("connection refused")) {
			return "Không kết nối được tới OpenRouter. Kiểm tra mạng, proxy hoặc firewall.";
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
