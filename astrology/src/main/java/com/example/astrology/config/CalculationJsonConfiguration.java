package com.example.astrology.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CalculationJsonConfiguration {

	@Bean
	ObjectMapper calculationObjectMapper() {
		return new ObjectMapper().findAndRegisterModules();
	}
}

