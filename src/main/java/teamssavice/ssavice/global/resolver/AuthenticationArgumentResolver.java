package teamssavice.ssavice.global.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import teamssavice.ssavice.global.annotation.CurrentAuth;
import teamssavice.ssavice.global.constants.ErrorCode;
import teamssavice.ssavice.global.dto.Auth;
import teamssavice.ssavice.global.exception.AuthenticationException;

public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentAuth.class)
                && parameter.getParameterType().equals(Auth.class);
    }

    @Override
    public Mono<Object> resolveArgument(
            MethodParameter parameter,
            BindingContext bindingContext,
            ServerWebExchange exchange
    ) {
        return Mono.deferContextual(ctx -> {
            if (!ctx.hasKey("subject")) {
                return Mono.error(new AuthenticationException(ErrorCode.MISSING_TOKEN));
            }
            return Mono.just(Auth.of(ctx.get("subject")));
        });
    }
}
