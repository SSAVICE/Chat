package teamssavice.ssavice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import teamssavice.ssavice.global.resolver.AuthenticationArgumentResolver;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Bean
    public AuthenticationArgumentResolver authenticationArgumentResolver() {
        return new AuthenticationArgumentResolver();
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(authenticationArgumentResolver());
    }
}
