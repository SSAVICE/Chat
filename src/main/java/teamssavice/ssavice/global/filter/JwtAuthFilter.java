package teamssavice.ssavice.global.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.AuthenticationException;
import teamssavice.ssavice.global.property.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String jwtToken = resolveToken(exchange.getRequest());
        if(jwtToken == null) {
            return chain.filter(exchange);
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String subject = claims.getSubject();

            // ws용
            exchange.getAttributes().put("subject", subject);

            // rest용
            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put("subject", subject));

        } catch (SecurityException | MalformedJwtException e) {
            return Mono.error(new AuthenticationException(ErrorCode.INVALID_TOKEN));
        } catch (ExpiredJwtException e) {
            return Mono.error(new AuthenticationException(ErrorCode.EXPIRED_TOKEN));
        } catch (UnsupportedJwtException e) {
            return Mono.error(new AuthenticationException(ErrorCode.UNSUPPORTED_TOKEN));
        } catch (IllegalArgumentException e) {
            return Mono.error(new AuthenticationException(ErrorCode.MISSING_TOKEN));
        } catch (Exception e) {
            return Mono.error(new AuthenticationException(ErrorCode.UNKNOWN_TOKEN_ERROR));
        }
    }

    private String resolveToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            return null;
        }

        return authHeader.substring(TOKEN_PREFIX.length());
    }
}
