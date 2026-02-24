package teamssavice.ssavice.global.filter;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.auth.JwtTokenProvider;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.AuthenticationException;

@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {
    private static final String TOKEN_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String jwtToken = resolveToken(exchange.getRequest());
        if(jwtToken == null) {
            return chain.filter(exchange);
        }

        try {
            Claims claims = jwtTokenProvider.getClaim(jwtToken);
            Long subject = Long.parseLong(claims.getSubject());

            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put("subject", subject));
        }  catch (Exception e) {
            return Mono.error(new AuthenticationException(ErrorCode.UNKNOWN_TOKEN_ERROR));
        }
    }

    private String resolveToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }

        String token = request.getQueryParams().getFirst("token");
        if(token != null && !token.isBlank()) {
            return token;
        }

        return null;
    }
}
