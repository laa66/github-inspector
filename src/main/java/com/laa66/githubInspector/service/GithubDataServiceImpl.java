package com.laa66.githubInspector.service;

import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor
public class GithubDataServiceImpl implements GithubDataService {

    private final WebClient webClient;

}
