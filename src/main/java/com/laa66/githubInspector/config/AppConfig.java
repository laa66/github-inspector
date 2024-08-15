package com.laa66.githubInspector.config;

import com.laa66.githubInspector.service.GithubDataService;
import com.laa66.githubInspector.service.GithubDataServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${github.api.url}")
    private String apiUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    @Bean
    public GithubDataService githubDataService(WebClient webClient) {
        return new GithubDataServiceImpl(webClient);
    }

}
