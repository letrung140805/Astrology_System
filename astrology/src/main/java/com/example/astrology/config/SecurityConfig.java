package com.example.astrology.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/account/**").authenticated()
						.requestMatchers(
								"/",
								"/about",
								"/login",
								"/login/**",
								"/register",
								"/register/**",
								"/result",
								"/result/**",
								"/chat",
								"/chat/**",
								"/history",
								"/history/**",
								"/css/**",
								"/js/**",
								"/images/**",
								"/favicon.ico"
						).permitAll()
						.anyRequest().authenticated()
				)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login?required=true"))
				)
				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("email")
						.passwordParameter("password")
						.defaultSuccessUrl("/", false)
						.failureHandler((request, response, exception) -> {
							String email = request.getParameter("email");
							String emailParam = (email != null && !email.isBlank())
									? "&email=" + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8)
									: "";
							if (exception instanceof BadCredentialsException) {
								response.sendRedirect("/login?error=bad_credentials" + emailParam);
							} else {
								response.sendRedirect("/login?error=true" + emailParam);
							}
						})
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout=true")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll()
				);

		return http.build();
	}
}
