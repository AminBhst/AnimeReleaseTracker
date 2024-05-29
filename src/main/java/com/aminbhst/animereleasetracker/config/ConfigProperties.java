package com.aminbhst.animereleasetracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

    private String myAnimeListClientId;

    private String telegramBotToken;

    private String forceInitializeSeason;

    private Integer forceInitializeYear;
}
