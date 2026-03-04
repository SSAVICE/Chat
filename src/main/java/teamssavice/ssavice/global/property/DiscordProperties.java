package teamssavice.ssavice.global.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("discord")
public record DiscordProperties (
    String webhookUrl
){
}
