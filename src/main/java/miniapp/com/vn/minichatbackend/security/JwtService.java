package miniapp.com.vn.minichatbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
	// For demo; move to config/secret storage
	private static final String SECRET = "bXktdmVyeS1sb25nLXNlY3JldC1rZXktZm9yLWRlbW8tb25seS0xMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMw==";
	private static final long EXP_MS = 1000L * 60 * 60 * 240; // 240h

	private static Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public static String generateToken(String subject, Map<String, Object> claims) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + EXP_MS);
		return Jwts.builder()
			.setSubject(subject)
			.addClaims(claims)
			.setIssuedAt(now)
			.setExpiration(exp)
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public Jws<Claims> parse(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token);
	}

	public Integer extractUserId(String token) {
		Claims c = parse(token).getBody();
		Object uid = c.get("uid");
		if (uid instanceof Integer) return (Integer) uid;
		if (uid instanceof Number) return ((Number) uid).intValue();
		if (uid instanceof String) return Integer.parseInt((String) uid);
		return null;
	}

	public String extractEmail(String token) {
		Claims c = parse(token).getBody();
		Object email = c.get("email");
		return email != null ? email.toString() : null;
	}

	public static void main(String[] args) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", 1);
		claims.put("email", "truongtb1999@gmail.com");
		claims.put("provider", "Facebook");
		String jwt = generateToken("user:" + 1, claims);
		System.out.println(jwt);
	}
}
