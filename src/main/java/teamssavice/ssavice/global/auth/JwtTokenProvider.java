package teamssavice.ssavice.global.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.exception.AuthenticationException;
import teamssavice.ssavice.global.property.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public Claims getClaim(String accessToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (SecurityException | MalformedJwtException e) {
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException(ErrorCode.MISSING_TOKEN);
        } catch (Exception e) {
            throw new AuthenticationException(ErrorCode.UNKNOWN_TOKEN_ERROR);
        }
    }
}
