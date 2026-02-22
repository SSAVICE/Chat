package teamssavice.ssavice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import teamssavice.ssavice.global.filter.JwtAuthFilter;
import teamssavice.ssavice.global.property.JwtProperties;

@Configuration
public class FilterConfig {

    @Bean
    public WebFilter JwtAuthFilter(JwtProperties jwtProperties) {
        return new JwtAuthFilter(jwtProperties);
    }
}
