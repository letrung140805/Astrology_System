package com.example.astrology.service;

import java.util.List;

import reactor.core.publisher.Flux;

public interface AstrologyAnswerGenerator {

	String generate(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question);

	default Flux<String> stream(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question) {
		String answer = generate(chartContext, history, question);
		return Flux.just(answer);
	}

	String modelName();
}
