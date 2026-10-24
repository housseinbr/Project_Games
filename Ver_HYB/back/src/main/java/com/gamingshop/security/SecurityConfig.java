package com.gamingshop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final CustomUserDetailsService userDetailsService;

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth

						// 🔓 AUTH PUBLIC (uniquement login/register — /api/auth/me reste protégé)
						.requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

						// 🔓 PUBLIC GET
						.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

						// 🔓 CHAT PUBLIC (creation session, messages, suivi par téléphone)
						.requestMatchers("/api/chat/session").permitAll()
						.requestMatchers("/api/chat/session/*/messages").permitAll()
						.requestMatchers("/api/chat/message").permitAll().requestMatchers("/api/chat/sessions/by-phone")
						.permitAll()

						// 🔓 WEBSOCKET
						.requestMatchers("/ws/**").permitAll()

						// 🔒 CLIENT CONNECTÉ : mes commandes (JWT requis)
						.requestMatchers("/api/chat/my-sessions").authenticated()

						// 🔒 ADMIN ONLY
						.requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/chat/admin/**")
						.hasRole("ADMIN")

						// 🔒 AUTRES
						.anyRequest().authenticated())
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/*
	 * @Bean public CorsConfigurationSource corsConfigurationSource() {
	 * CorsConfiguration config = new CorsConfiguration();
	 * config.setAllowedOrigins(Arrays.asList( "http://192.168.1.68:4200" ));
	 * config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE",
	 * "OPTIONS")); config.setAllowedHeaders(Arrays.asList("*"));
	 * config.setAllowCredentials(true);
	 * 
	 * UrlBasedCorsConfigurationSource source = new
	 * UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",
	 * config); return source; }
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		System.out.println("✅ CORS config chargée — all origins allowed");
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(Arrays.asList("*"));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}