package br.com.fiap.petcare360_java.security;

import java.util.List;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.fiap.petcare360_java.model.AppUser;
import br.com.fiap.petcare360_java.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
			UserDetailsService userDetailsService) throws Exception {
		AuthenticationEntryPoint unauthorized = (request, response, exception) -> {
			response.setStatus(401);
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("WWW-Authenticate", "Bearer");
			response.getWriter().write("{\"status\":401,\"message\":\"Token ausente, invalido ou expirado\"}");
		};
		http
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.requestCache(cache -> cache.disable())
				.logout(logout -> logout.disable())
				.oauth2ResourceServer(oauth -> oauth
						.authenticationEntryPoint(unauthorized)
						.jwt(jwt -> jwt.decoder(jwtService::decode).jwtAuthenticationConverter(token -> {
							try {
								var user = userDetailsService.loadUserByUsername(token.getSubject());
								if (!user.isEnabled() || !user.isAccountNonLocked() || !user.isAccountNonExpired()
										|| !user.isCredentialsNonExpired()) {
									throw new OAuth2AuthenticationException("invalid_token");
								}
								return new JwtAuthenticationToken(token, user.getAuthorities(), user.getUsername());
							} catch (AuthenticationException exception) {
								throw new OAuth2AuthenticationException("invalid_token");
							}
						})))
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/auth/register", "/auth/login", "/swagger-ui/**", "/swagger-ui.html", "/v3/**").permitAll()
						.requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
						.requestMatchers("/vet", "/vet/**").hasRole("VETERINARIO")
						.requestMatchers("/tutor", "/tutor/**").hasRole("CLIENTE")
						.requestMatchers("/veterinarians/**").hasAnyRole("CLIENTE", "ADMIN", "VETERINARIO")
						.requestMatchers("/pets/**", "/messages/**", "/appointments/**", "/recommendations/**").hasAnyRole("CLIENTE", "ADMIN", "VETERINARIO")
						.requestMatchers("/api/iot/**").hasAnyRole("ADMIN", "VETERINARIO")
						.anyRequest().authenticated())
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(unauthorized)
						.accessDeniedHandler((request, response, accessDeniedException) ->
								{
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
									response.setContentType("application/json;charset=UTF-8");
									response.getWriter().write("{\"status\":403,\"message\":\"Acesso negado\"}");
								}));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public UserDetailsService userDetailsService(AppUserRepository userRepository) {
		return email -> {
			AppUser user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("Usuário não localizado"));

			return User.builder()
					.username(user.getEmail())
					.password(user.getPasswordHash())
					.disabled(Boolean.FALSE.equals(user.getEnabled()))
					.authorities(user.getRoles().stream()
							.map(role -> role.getName())
							.toArray(String[]::new))
					.build();
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
