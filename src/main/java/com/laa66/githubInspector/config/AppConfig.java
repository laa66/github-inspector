package com.laa66.githubInspector.config;

import com.laa66.githubInspector.service.GithubDataService;
import com.laa66.githubInspector.service.GithubDataServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {


    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    @Bean
    public GithubDataService githubDataService(WebClient webClient) {
        return new GithubDataServiceImpl(webClient);
    }

}
