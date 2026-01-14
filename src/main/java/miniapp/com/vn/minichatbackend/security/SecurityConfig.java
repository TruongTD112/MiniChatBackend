package miniapp.com.vn.minichatbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@Order(1)
public class SecurityConfig {
	private final EndpointAuthFilter endpointAuthFilter;
	private final RequestResponseLoggingFilter requestResponseLoggingFilter;

	public SecurityConfig(EndpointAuthFilter endpointAuthFilter, RequestResponseLoggingFilter requestResponseLoggingFilter) {
		this.endpointAuthFilter = endpointAuthFilter;
		this.requestResponseLoggingFilter = requestResponseLoggingFilter;
	}

	@Bean
	@Order(1)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/auth/**", "/health-check").permitAll()
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
				.anyRequest().permitAll()
			)
			.addFilterBefore(requestResponseLoggingFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(endpointAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.httpBasic(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-Request-Id"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}


	public static void generatePassword(String rawPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encodedPassword = encoder.encode(rawPassword);

		System.out.println("=== BCrypt Password Generation ===");
		System.out.println("Raw Password: " + rawPassword);
		System.out.println("Encoded Password: " + encodedPassword);
		System.out.println("================================");
	}

	public static void main(String[] args) {
		generatePassword("Truong1112@@");
	}

}
