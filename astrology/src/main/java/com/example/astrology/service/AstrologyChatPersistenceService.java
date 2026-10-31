package com.example.astrology.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.astrology.entity.AstrologyPalace;
import com.example.astrology.entity.BirthChart;
import com.example.astrology.entity.ChatMessage;
import com.example.astrology.entity.Conversation;
import com.example.astrology.model.ConversationHistoryItem;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.repository.AstrologyPalaceRepository;
import com.example.astrology.repository.BirthChartRepository;
import com.example.astrology.repository.ChatMessageRepository;
import com.example.astrology.repository.ChartPalaceStarRepository;
import com.example.astrology.repository.ConversationRepository;

@Service
public class AstrologyChatPersistenceService {

	private final AppUserRepository appUserRepository;
	private final BirthChartRepository birthChartRepository;
	private final AstrologyPalaceRepository astrologyPalaceRepository;
	private final ChartPalaceStarRepository chartPalaceStarRepository;
	private final ConversationRepository conversationRepository;
	private final ChatMessageRepository chatMessageRepository;

	public AstrologyChatPersistenceService(
			AppUserRepository appUserRepository,
			BirthChartRepository birthChartRepository,
			AstrologyPalaceRepository astrologyPalaceRepository,
			ChartPalaceStarRepository chartPalaceStarRepository,
			ConversationRepository conversationRepository,
			ChatMessageRepository chatMessageRepository) {
		this.appUserRepository = appUserRepository;
		this.birthChartRepository = birthChartRepository;
		this.astrologyPalaceRepository = astrologyPalaceRepository;
		this.chartPalaceStarRepository = chartPalaceStarRepository;
		this.conversationRepository = conversationRepository;
		this.chatMessageRepository = chatMessageRepository;
	}

	@Transactional(readOnly = true)
	public List<ConversationHistoryItem> listConversations(UUID accessToken, String principalName) {
		BirthChart chart = findChart(accessToken, principalName);
		List<Conversation> conversations = conversationRepository.findAllByChart_IdOrderByUpdatedAtDesc(chart.getId());
		return conversations.stream()
				.map(c -> new ConversationHistoryItem(
						c.getId(),
						c.getTitle() != null && !c.getTitle().isBlank() ? c.getTitle() : "Cuộc đàm đạo #" + c.getId(),
						c.getCreatedAt(),
						c.getUpdatedAt(),
						chatMessageRepository.countByConversation_Id(c.getId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public ChatSession load(UUID accessToken, Long conversationId, String principalName) {
		BirthChart chart = findChart(accessToken, principalName);
		List<ChatTurn> turns = conversationId == null
				? List.of()
				: loadConversation(chart, conversationId).stream()
						.map(message -> new ChatTurn(message.getSenderType(), message.getContent()))
						.toList();
		return new ChatSession(accessToken, conversationId,
				chart.getBirthProfile().getFullName(), chartContext(chart), turns);
	}

	@Transactional
	public Long saveExchange(
			UUID accessToken,
			Long conversationId,
			String principalName,
			String question,
			String answer,
			String modelName) {
		BirthChart chart = findChart(accessToken, principalName);
		Conversation conversation = conversationId == null
				? new Conversation()
				: conversationRepository.findByIdAndChart_Id(conversationId, chart.getId())
						.orElseThrow(AstrologyChatPersistenceService::notFound);
		if (conversation.getId() != null && !sameOwner(conversation, chart)) {
			throw notFound();
		}

		conversation.setUser(chart.getUser());
		conversation.setChart(chart);
		if (conversation.getTitle() == null) {
			conversation.setTitle(question.substring(0, Math.min(question.length(), 255)));
		}
		conversation = conversationRepository.saveAndFlush(conversation);

		ChatMessage userMessage = new ChatMessage();
		userMessage.setConversation(conversation);
		userMessage.setSenderType("USER");
		userMessage.setContent(question);

		ChatMessage assistantMessage = new ChatMessage();
		assistantMessage.setConversation(conversation);
		assistantMessage.setSenderType("ASSISTANT");
		assistantMessage.setContent(answer);
		assistantMessage.setModelName(modelName);
		assistantMessage.setContextUsed("calculation_version=" + chart.getCalculationVersion());
		chatMessageRepository.saveAllAndFlush(List.of(userMessage, assistantMessage));
		return conversation.getId();
	}

	@Transactional
	public void deleteConversation(UUID accessToken, Long conversationId, String principalName) {
		BirthChart chart = findChart(accessToken, principalName);
		Conversation conversation = conversationRepository.findByIdAndChart_Id(conversationId, chart.getId())
				.orElseThrow(AstrologyChatPersistenceService::notFound);
		if (!sameOwner(conversation, chart)) {
			throw notFound();
		}
		chatMessageRepository.deleteAllByConversation_Id(conversationId);
		conversationRepository.delete(conversation);
	}

	private List<ChatMessage> loadConversation(BirthChart chart, Long conversationId) {
		Conversation conversation = conversationRepository.findByIdAndChart_Id(conversationId, chart.getId())
				.orElseThrow(AstrologyChatPersistenceService::notFound);
		if (!sameOwner(conversation, chart)) {
			throw notFound();
		}
		List<ChatMessage> messages = chatMessageRepository
				.findAllByConversation_IdOrderByCreatedAtAsc(conversationId);
		int firstIncluded = Math.max(0, messages.size() - 40);
		return List.copyOf(messages.subList(firstIncluded, messages.size()));
	}

	private BirthChart findChart(UUID accessToken, String principalName) {
		String tokenHash = BirthChartPersistenceService.hashAccessToken(accessToken);
		if (principalName == null) {
			return birthChartRepository.findByAccessTokenHashAndUserIsNull(tokenHash)
					.orElseThrow(AstrologyChatPersistenceService::notFound);
		}
		var user = appUserRepository.findByEmail(principalName)
				.or(() -> appUserRepository.findByUsername(principalName))
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản hiện tại."));
		return birthChartRepository.findByAccessTokenHashAndUser_Id(tokenHash, user.getId())
				.or(() -> {
					List<BirthChart> userCharts = birthChartRepository.findAllByUser_IdOrderByGeneratedAtDesc(user.getId());
					for (BirthChart c : userCharts) {
						UUID expected = UUID.nameUUIDFromBytes(("chart:" + c.getId() + ":" + user.getId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
						if (expected.equals(accessToken)) {
							c.setAccessTokenHash(tokenHash);
							birthChartRepository.save(c);
							return java.util.Optional.of(c);
						}
					}
					return java.util.Optional.empty();
				})
				.orElseThrow(AstrologyChatPersistenceService::notFound);
	}

	private String chartContext(BirthChart chart) {
		var profile = chart.getBirthProfile();
		StringBuilder context = new StringBuilder();
		context.append("Hồ sơ: ").append(profile.getFullName())
				.append("; giới tính: ").append(profile.getGender())
				.append("; ngày sinh dương lịch: ").append(profile.getBirthDate())
				.append("; giờ sinh địa phương: ").append(profile.getBirthTime())
				.append("; nơi sinh: ").append(profile.getBirthCity()).append('\n');
		context.append("Ngày âm lịch: ").append(chart.getLunarDay()).append('/')
				.append(chart.getLunarMonth()).append('/')
				.append(chart.getLunarYear())
				.append(Boolean.TRUE.equals(chart.getLunarLeapMonth()) ? " (tháng nhuận)" : "")
				.append("; Can Chi năm: ").append(chart.getYearCan()).append(' ')
				.append(chart.getYearChi()).append('\n');
		context.append("Ngũ hành Nạp Âm năm: ").append(chart.getMenhElement())
				.append("; cung Mệnh: ").append(chart.getMenhName())
				.append(" tại ").append(chart.getMingPalaceBranch())
				.append("; cung Thân: ").append(chart.getThanPalaceBranch())
				.append("; Cục: ").append(chart.getCucName()).append('\n');
		context.append("Các cung và sao trên lá số:\n");
		for (AstrologyPalace palace : astrologyPalaceRepository
				.findAllByChart_IdOrderByPalaceOrderAsc(chart.getId())) {
			context.append("- Cung ").append(palace.getPalaceName())
					.append(" tại ").append(palace.getEarthlyBranch()).append(':');
			var stars = chartPalaceStarRepository.findAllByPalace_IdOrderByStarOrderAsc(palace.getId());
			if (stars.isEmpty()) {
				context.append(" chưa có sao");
			} else {
				for (var link : stars) {
					context.append(' ').append(link.getStar().getVietnameseName());
					if (link.isMainStar()) {
						context.append(" (chính tinh)");
					}
					if (link.getBrightness() != null) {
						context.append(" độ sáng: ").append(link.getBrightness());
					}
					if (link.getTransformation() != null) {
						context.append(" tứ hóa: ").append(link.getTransformation());
					}
					context.append(';');
				}
			}
			context.append('\n');
		}
		return context.toString();
	}

	private static boolean sameOwner(Conversation conversation, BirthChart chart) {
		Long conversationOwner = conversation.getUser() == null ? null : conversation.getUser().getId();
		Long chartOwner = chart.getUser() == null ? null : chart.getUser().getId();
		return Objects.equals(conversationOwner, chartOwner);
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND,
				"Không tìm thấy lá số hoặc bạn không có quyền truy cập.");
	}

	public record ChatSession(
			UUID accessToken,
			Long conversationId,
			String chartName,
			String chartContext,
			List<ChatTurn> messages) {
		public ChatSession {
			messages = List.copyOf(new ArrayList<>(messages));
		}
	}

	public record ChatTurn(String senderType, String content) {
	}
}
