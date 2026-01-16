package miniapp.com.vn.minichatbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Order(2) // Run after logging filter
public class EndpointAuthFilter extends OncePerRequestFilter {
	private static final String[] PROTECTED_PATTERNS = new String[] {
		"/api/business/**",           // Business APIs - cần authentication
		"/api/product/**",            // Product APIs - cần authentication
		"/api/back-office/auth/me"
	};
	
	private static final String REQUEST_ID_KEY = "requestId";

	private final JwtService jwtService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	public EndpointAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		String requestId = MDC.get(REQUEST_ID_KEY);
		
		if (requiresAuth(path)) {
			String header = request.getHeader("Authorization");
			if (header == null || !header.startsWith("Bearer ")) {
				log.warn("Missing or invalid Authorization header for path: {} - RequestId: {}", path, requestId);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Missing or invalid Authorization header");
				return;
			}
			String token = header.substring(7);
			try {
				Integer uid = jwtService.extractUserId(token);
				if (uid == null) {
					log.warn("Invalid token for path: {} - RequestId: {}", path, requestId);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.getWriter().write("Invalid token");
					return;
				}
				
				// Kiểm tra role cho admin APIs
				if (path.startsWith("/api/admin/") || "me".equalsIgnoreCase(path)) {
					if (!isAdminToken(token)) {
						log.warn("Access denied - Admin role required for path: {} - RequestId: {}", path, requestId);
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						response.getWriter().write("Access denied - Admin role required");
						return;
					}
				}
				
				// Kiểm tra role cho seller APIs
				if (path.startsWith("/api/seller/") || "me".equalsIgnoreCase(path)) {
					if (!isSellerOrAdminToken(token)) {
						log.warn("Access denied - Seller or Admin role required for path: {} - RequestId: {}", path, requestId);
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						response.getWriter().write("Access denied - Seller or Admin role required");
						return;
					}
				}
				
				log.info("Authentication successful for user: {} on path: {} - RequestId: {}", uid, path, requestId);
				Authentication auth = new UidAuthenticationToken(uid);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception e) {
				log.error("Exception during token validation for path: {} - RequestId: {}", path, requestId, e);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Invalid token");
				return;
			}
		} else {
			log.info("Non-protected request: {} {} - RequestId: {}", request.getMethod(), path, requestId);
		}
		
		// Continue with the filter chain
		filterChain.doFilter(request, response);
	}

	private boolean isMatch(String pattern, String path) {
		return pathMatcher.match(pattern, path);
	}

	private boolean requiresAuth(String path) {
		for (String p : PROTECTED_PATTERNS) {
			if (isMatch(p, path)) return true;
		}
		return false;
	}

	/**
	 * Kiểm tra token có phải của admin không
	 */
	private boolean isAdminToken(String token) {
		try {
			Jws<Claims> claims = jwtService.parse(token);
			String role = claims.getBody().get("role", String.class);
			return "ADMIN".equalsIgnoreCase(role);
		} catch (Exception e) {
			log.error("Error checking admin role from token", e);
			return false;
		}
	}

	/**
	 * Kiểm tra token có phải của seller hoặc admin không
	 */
	private boolean isSellerOrAdminToken(String token) {
		try {
			Jws<Claims> claims = jwtService.parse(token);
			String role = claims.getBody().get("role", String.class);
			return "SELLER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
		} catch (Exception e) {
			log.error("Error checking seller/admin role from token", e);
			return false;
		}
	}

	static class UidAuthenticationToken extends AbstractAuthenticationToken {
		private final Integer uid;

		UidAuthenticationToken(Integer uid) {
			super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
			this.uid = uid;
			setAuthenticated(true);
		}

		@Override
		public Object getCredentials() { return ""; }

		@Override
		public Object getPrincipal() { return uid; }

		public Integer getUserId() { return uid; }
	}
}
