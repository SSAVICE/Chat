package teamssavice.ssavice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import teamssavice.ssavice.global.auth.JwtTokenProvider;
import teamssavice.ssavice.global.filter.JwtAuthFilter;

@Configuration
public class FilterConfig {

    @Bean
    public WebFilter JwtAuthFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthFilter(jwtTokenProvider);
    }
}
