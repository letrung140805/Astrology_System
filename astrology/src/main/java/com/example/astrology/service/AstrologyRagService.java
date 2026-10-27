package com.example.astrology.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.astrology.entity.AstrologyKnowledge;
import com.example.astrology.entity.KnowledgeChunk;
import com.example.astrology.repository.AstrologyKnowledgeRepository;
import com.example.astrology.repository.KnowledgeChunkRepository;

@Service
public class AstrologyRagService {

	private static final Logger logger = LoggerFactory.getLogger(AstrologyRagService.class);

	private static final List<String> ASTROLOGY_KEYWORDS = List.of(
			"tử vi", "thiên cơ", "thái dương", "vũ khúc", "thiên đồng", "liêm trinh",
			"thiên phủ", "thái âm", "tham lang", "cự môn", "thiên tướng", "thiên lương",
			"thất sát", "phá quân", "hóa lộc", "hóa quyền", "hóa khoa", "hóa kỵ",
			"lộc tồn", "thiên khôi", "thiên việt", "thiên mã", "văn xương", "văn khúc",
			"tả phù", "hữu bật", "thái tuế", "trường sinh", "bác sĩ", "sát phá tham",
			"tử phủ vũ tướng", "cơ nguyệt đồng lương", "cự nhật",
			"mệnh", "thân", "quan lộc", "tài bạch", "phu thê", "phúc đức",
			"điền trạch", "thiên di", "tật ách", "tử tức", "huynh đệ", "phụ mẫu", "nô bộc",
			"cách cục", "hôn nhân", "sự nghiệp", "tài lộc", "tiền tài", "sức khỏe", "đại vận");

	private final AstrologyKnowledgeRepository knowledgeRepository;
	private final KnowledgeChunkRepository chunkRepository;

	public AstrologyRagService(
			AstrologyKnowledgeRepository knowledgeRepository,
			KnowledgeChunkRepository chunkRepository) {
		this.knowledgeRepository = knowledgeRepository;
		this.chunkRepository = chunkRepository;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void initializeKnowledgeBase() {
		try {
			if (chunkRepository.count() == 0) {
				logger.info("Initializing baseline Vietnamese Astrology RAG knowledge base...");
				seedDefaultKnowledge();
				logger.info("Baseline Astrology RAG knowledge base initialized successfully.");
			}
		} catch (Exception exception) {
			logger.warn("Could not auto-seed astrology knowledge base on startup: {}", exception.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public String retrieveRelevantKnowledge(String chartContext, String question) {
		if (question == null || question.isBlank()) {
			return "";
		}

		String lowerQuestion = question.toLowerCase(Locale.ROOT);
		String lowerContext = (chartContext != null) ? chartContext.toLowerCase(Locale.ROOT) : "";

		Map<Long, KnowledgeChunk> matchedChunks = new LinkedHashMap<>();

		// 1. First priority: match keywords present in user question
		for (String keyword : ASTROLOGY_KEYWORDS) {
			if (lowerQuestion.contains(keyword)) {
				List<KnowledgeChunk> results = chunkRepository.searchByTerm(keyword);
				for (KnowledgeChunk chunk : results) {
					matchedChunks.putIfAbsent(chunk.getId(), chunk);
					if (matchedChunks.size() >= 4) {
						break;
					}
				}
			}
			if (matchedChunks.size() >= 4) {
				break;
			}
		}

		// 2. Second priority: if question mentions a palace, search relevant stars in that palace from context
		if (matchedChunks.size() < 3 && chartContext != null) {
			for (String palace : List.of("mệnh", "quan lộc", "tài bạch", "phu thê", "phúc đức", "điền trạch", "thiên di")) {
				if (lowerQuestion.contains(palace)) {
					List<KnowledgeChunk> results = chunkRepository.searchByTerm(palace);
					for (KnowledgeChunk chunk : results) {
						matchedChunks.putIfAbsent(chunk.getId(), chunk);
					}
					break;
				}
			}
		}

		// 3. Fallback: general synthesis if nothing matched specifically
		if (matchedChunks.isEmpty()) {
			List<KnowledgeChunk> general = chunkRepository.searchByTerm("mệnh");
			for (KnowledgeChunk chunk : general) {
				matchedChunks.putIfAbsent(chunk.getId(), chunk);
				if (matchedChunks.size() >= 2) {
					break;
				}
			}
		}

		if (matchedChunks.isEmpty()) {
			return "";
		}

		StringBuilder ragBuilder = new StringBuilder();
		int count = 1;
		for (KnowledgeChunk chunk : matchedChunks.values()) {
			ragBuilder.append(count).append(". [")
					.append(chunk.getChunkTitle()).append("]:\n")
					.append(chunk.getChunkContent().trim()).append("\n\n");
			count++;
			if (count > 4) {
				break;
			}
		}

		return ragBuilder.toString().trim();
	}

	private void seedDefaultKnowledge() {
		// Document 1: 14 Chính Tinh & Luận Giải
		AstrologyKnowledge docStars = new AstrologyKnowledge();
		docStars.setTitle("Cẩm Nang Luận Giải 14 Chính Tinh & Cách Cục Tử Vi");
		docStars.setCategory("CHINH_TINH");
		docStars.setSource("Tử Vi Đẩu Số Toàn Thư & Giáo trình Tử Vi Việt Nam");
		docStars.setAuthor("Ban Nghiên cứu Cổ học");
		docStars.setDescription("Ý nghĩa chi tiết của 14 chính tinh khi tọa thủ các cung Mệnh, Thân, Quan, Tài và các bộ cách cục.");
		docStars.setDocumentContent("Tổng hợp toàn diện 14 chính tinh Tử Vi và các thế sao phối chiếu.");
		docStars.setVersion("1.0");
		docStars.setStatus("ACTIVE");
		docStars = knowledgeRepository.save(docStars);

		List<KnowledgeChunk> chunks = new ArrayList<>();

		KnowledgeChunk chunk1 = new KnowledgeChunk();
		chunk1.setDocument(docStars);
		chunk1.setChunkIndex(1);
		chunk1.setChunkTitle("Bộ Tử Phủ Vũ Tướng Liêm (Đế Tinh & Tài Khố)");
		chunk1.setKeywords("tử vi, thiên phủ, vũ khúc, thiên tướng, liêm trinh, tử phủ vũ tướng, cung mệnh, tài bạch, quan lộc");
		chunk1.setChunkContent("""
				- Tử Vi (Thổ): Đế tinh, chủ quyền quý, phúc đức, lãnh đạo, bao dung, giải trừ tai họa khi miếu vượng. Đóng Mệnh/Quan/Tài chủ tư chất đường hoàng, quang minh chính đại.
				- Thiên Phủ (Thổ): Lệnh tinh, tài khố trời đất, chủ sự chu toàn, cẩn trọng, tích lũy tài sản bền vững, tính tình đôn hậu, giữ chữ tín.
				- Vũ Khúc (Kim): Tài tinh quả quyết, chủ hành động, kinh thương, độc lập, quyết đoán, tài quản lý tài chính, hợp cách lập nghiệp bằng tài năng thực chiến.
				- Thiên Tướng (Thủy): Ấn tinh, phò tá, ngay thẳng, trọng nghĩa khí, hòa nhã, có duyên với tổ chức, hợp công việc quản trị hoặc phò tá lãnh đạo.
				- Liêm Trinh (Hỏa): Tù tinh, đào hoa thứ, nguyên tắc, mưu trí, tự tôn cao; đắc địa thì quyền biến, tài hoa; hãm địa cần đề phòng nóng nảy.
				- Cách cục 'Tử Phủ Vũ Tướng': Mệnh tọa thủ một trong các sao này hội chiếu đủ bộ chủ công danh bền vững, hậu vận phú quý, uy tín xã hội cao.
				""");
		chunks.add(chunk1);

		KnowledgeChunk chunk2 = new KnowledgeChunk();
		chunk2.setDocument(docStars);
		chunk2.setChunkIndex(2);
		chunk2.setChunkTitle("Bộ Sát Phá Liêm Tham (Biến Động, Khai Phá & Đột Phá)");
		chunk2.setKeywords("thất sát, phá quân, tham lang, sát phá tham, biến động, sự nghiệp, kinh doanh, đột phá");
		chunk2.setChunkContent("""
				- Thất Sát (Kim/Hỏa): Quyền tinh dũng mãnh, dũng cảm, quyết đoán, dám chịu trách nhiệm, thích nghi nghịch cảnh, hợp lĩnh vực tiên phong, quân đội, kinh thương biến động.
				- Phá Quân (Thủy): Hao tinh, tiên phong mở đường, phá cũ xây mới, ghét khuôn mẫu gò bó, ưa đổi mới, sáng tạo, cuộc đời có những khúc ngoặt lớn rồi thành công.
				- Tham Lang (Thủy/Mộc): Dục tinh, đa tài đa nghệ, giỏi giao tiếp, nắm bắt tâm lý người khác, nhanh nhạy với thương trường, có duyên với nghệ thuật và ngoại giao.
				- Cách cục 'Sát Phá Tham': Cuộc đời biến động lớn, tự lực cánh sinh, giàu ý chí phấn đấu, thường phát đạt rực rỡ khi dám bước ra khỏi vùng an toàn.
				""");
		chunks.add(chunk2);

		KnowledgeChunk chunk3 = new KnowledgeChunk();
		chunk3.setDocument(docStars);
		chunk3.setChunkIndex(3);
		chunk3.setChunkTitle("Bộ Cơ Nguyệt Đồng Lương (Trí Tuệ, Chuyên Môn & An Nhàn)");
		chunk3.setKeywords("thiên cơ, thái âm, thiên đồng, thiên lương, cơ nguyệt đồng lương, công chức, chuyên môn, an nhàn");
		chunk3.setChunkContent("""
				- Thiên Cơ (Mộc): Thiện tinh, mưu trí, linh hoạt, tư duy phân tích sắc sảo, giỏi lập kế hoạch, có năng khiếu kỹ thuật, thuật toán và tham mưu.
				- Thái Âm (Thủy): Phú tinh, điền sản, chủ tình cảm sâu sắc, tư duy thẩm mỹ, làm ăn tỉ mỉ, tích lũy đất đai nhà cửa, miếu vượng tại Hợi, Tý, Sửu rất đẹp.
				- Thiên Đồng (Thủy): Phúc tinh, an nhàn, tính tình thuần hậu, thích tự do, có duyên với nghệ thuật, phúc thọ tăng dần về hậu vận.
				- Thiên Lương (Mộc): Ấm tinh, thọ tinh, nguyên tắc đạo đức cao, thích giúp đỡ người khác, có duyên với y dược, giáo dục, tư vấn, giải trừ tai ương.
				- Cách cục 'Cơ Nguyệt Đồng Lương': Thích hợp công việc trí thức, nghiên cứu, giáo dục, hành chính, tài chính ổn định, cuộc sống thanh nhã.
				""");
		chunks.add(chunk3);

		KnowledgeChunk chunk4 = new KnowledgeChunk();
		chunk4.setDocument(docStars);
		chunk4.setChunkIndex(4);
		chunk4.setChunkTitle("Bộ Cự Nhật (Khẩu Tài, Quang Minh & Danh Vọng)");
		chunk4.setKeywords("thái dương, cự môn, cự nhật, khẩu tài, danh vọng, xuất ngoại, giáo dục, truyền thông");
		chunk4.setChunkContent("""
				- Thái Dương (Hỏa): Mặt trời quang minh, chủ danh vọng, sự nghiệp công chúng, quang đại, khẳng khái, có chí lớn, hiếu thuận, biểu trưng cho cha và phu quân.
				- Cự Môn (Thủy): Ám tinh, chủ ngôn ngữ, tranh biện, khẩu tài, năng lực khảo cứu tỉ mỉ; đắc địa thì lời nói đanh thép, thuyết phục, hợp nghề luật, diễn thuyết, giảng dạy.
				- Cách cục 'Cự Nhật Dần Thân': Mặt trời xua tan mây mù, chủ sự nghiệp vang danh, giỏi giao tiếp, phát tài qua đối ngoại, kinh doanh quốc tế hoặc truyền thông.
				""");
		chunks.add(chunk4);

		// Document 2: Tứ Hóa & Vận Hạn
		AstrologyKnowledge docTransformations = new AstrologyKnowledge();
		docTransformations.setTitle("Bí Pháp Tứ Hóa & Ý Nghĩa 12 Cung Chức");
		docTransformations.setCategory("TU_HOA_CUNG_CHUC");
		docTransformations.setSource("Kinh điển Nam Phái Tử Vi Đẩu Số");
		docTransformations.setAuthor("Ban Nghiên cứu Cổ học");
		docTransformations.setDescription("Nguyên lý Tứ Hóa (Lộc, Quyền, Khoa, Kỵ) và sự tương tác giữa các cung chức.");
		docTransformations.setDocumentContent("Ý nghĩa của Tứ Hóa và cấu trúc 12 cung chức trong lá số.");
		docTransformations.setVersion("1.0");
		docTransformations.setStatus("ACTIVE");
		docTransformations = knowledgeRepository.save(docTransformations);

		KnowledgeChunk chunk5 = new KnowledgeChunk();
		chunk5.setDocument(docTransformations);
		chunk5.setChunkIndex(5);
		chunk5.setChunkTitle("Ý Nghĩa Bộ Tứ Hóa (Lộc, Quyền, Khoa, Kỵ)");
		chunk5.setKeywords("hóa lộc, hóa quyền, hóa khoa, hóa kỵ, tứ hóa, duyên, tài lộc, sự nghiệp, học vấn, thị phi");
		chunk5.setChunkContent("""
				- Hóa Lộc: Duyên lành, tài vận khởi sắc, tính cách hòa ái, nhiều cơ hội tiền bạc, giảm căng thẳng; đóng Mệnh/Tài/Điền rất đắc cách.
				- Hóa Quyền: Quyền bính, ý chí kiên cường, năng lực lãnh đạo, tính tự lập cao, thích làm chủ hoàn cảnh, quyết đoán vượt khó.
				- Hóa Khoa: Đệ nhất giải ách chi thần, danh dự, học vấn, thi cử đỗ đạt, gặp khó có quý nhân xuất hiện, tiếng tăm thanh bạch.
				- Hóa Kỵ: Chấp niệm, gút thắt tâm lý, nợ duyên, biến động, thị phi thử thách; đòi hỏi sự bình tâm, tu dưỡng, chuyên tâm vào chuyên môn để hóa giải.
				""");
		chunks.add(chunk5);

		KnowledgeChunk chunk6 = new KnowledgeChunk();
		chunk6.setDocument(docTransformations);
		chunk6.setChunkIndex(6);
		chunk6.setChunkTitle("Ý Nghĩa Tam Phương Tứ Chính: Mệnh, Quan Lộc, Tài Bạch & Di");
		chunk6.setKeywords("cung mệnh, cung thân, cung quan lộc, cung tài bạch, cung thiên di, tam phương tứ chính, sự nghiệp, tiền tài");
		chunk6.setChunkContent("""
				- Cung Mệnh: Tiền vận (trước 30-35 tuổi), phản ánh cốt cách bẩm sinh, tư chất trí tuệ, diện mạo và định hướng gốc rễ của đời người.
				- Cung Thân: Hậu vận (sau 30-35 tuổi), phản ánh hành động thực tế, xu hướng dấn thân và đích đến cuộc đời.
				- Cung Quan Lộc: Môi trường nghề nghiệp, phong cách làm việc, cơ hội thăng tiến và cách khẳng định danh tiếng.
				- Cung Tài Bạch: Nguồn sinh tài, cách thức quản lý tài chính, tính tụ tán của đồng tiền, năng lực kiếm tiền độc lập.
				- Cung Thiên Di: Quan hệ ngoại giao, môi trường xã hội bên ngoài khi bước ra khỏi nhà, cơ hội xuất ngoại hay phát triển phương xa.
				- Ba cung Mệnh - Tài - Quan tương tác tam giác khép kín: Mệnh tốt cần Quan Tài nâng đỡ; Quan Tài sáng giúp Mệnh thi thố tài năng.
				""");
		chunks.add(chunk6);

		KnowledgeChunk chunk7 = new KnowledgeChunk();
		chunk7.setDocument(docTransformations);
		chunk7.setChunkIndex(7);
		chunk7.setChunkTitle("Cung Phu Thê, Phúc Đức & Điền Trạch");
		chunk7.setKeywords("cung phu thê, cung phúc đức, cung điền trạch, hôn nhân, tình cảm, bất động sản, gia đạo, họ hàng");
		chunk7.setChunkContent("""
				- Cung Phu Thê: Phản ánh duyên nợ hôn nhân, tính cách, hoàn cảnh gia đình của người phối ngẫu, sự hòa thuận trong đời sống lứa đôi.
				- Cung Phúc Đức: Cung vị trọng yếu bậc nhất về đời sống nội tâm, phúc ấm tổ tiên, thọ yểm, khả năng vượt qua khủng hoảng tinh thần. Cung Phúc chiếu thẳng Phu Thê nên ảnh hưởng trực tiếp đến hạnh phúc gia đình.
				- Cung Điền Trạch: Phản ánh khả năng tích lũy đất đai, nhà cửa, gia sản thừa kế hoặc tự gầy dựng, phong thủy nơi ở và sự yên ấm của gia đạo.
				""");
		chunks.add(chunk7);

		KnowledgeChunk chunk8 = new KnowledgeChunk();
		chunk8.setDocument(docTransformations);
		chunk8.setChunkIndex(8);
		chunk8.setChunkTitle("Vòng Thái Tuế, Vòng Trường Sinh & Lục Cát Tinh");
		chunk8.setKeywords("vòng thái tuế, thái tuế, quan phù, bạch hổ, trường sinh, đế vượng, lục cát tinh, tả phù, hữu bật, văn xương, văn khúc, thiên khôi, thiên việt");
		chunk8.setChunkContent("""
				- Vòng Thái Tuế: Nhóm Thái Tuế - Quan Phù - Bạch Hổ chủ về chính phái, lòng tự trọng, trách nhiệm, được người khác tin cậy. Nhóm Tuế Phá chủ về sự trăn trở, dám nghĩ khác, vượt nghịch cảnh.
				- Vòng Trường Sinh (Trường Sinh, Mộc Dục, Quan Đới, Lâm Quan, Đế Vượng, Suy, Bệnh, Tử, Mộ, Tuyệt, Thai, Dưỡng): Thể hiện sinh khí và chu kỳ thịnh suy của các cung vị.
				- Lục Cát Tinh: Tả Phù - Hữu Bật (quý nhân đồng môn, cộng sự đắc lực); Văn Xương - Văn Khúc (trí tuệ nghệ thuật, bằng cấp, học vấn); Thiên Khôi - Thiên Việt (Thiên Ất quý nhân, người bề trên nâng đỡ, thời cơ bước ngoặt).
				""");
		chunks.add(chunk8);

		chunkRepository.saveAll(chunks);
	}
}

