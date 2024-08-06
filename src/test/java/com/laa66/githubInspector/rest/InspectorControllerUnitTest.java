package com.laa66.githubInspector.rest;

import com.laa66.githubInspector.dto.ExceptionDto;
import com.laa66.githubInspector.dto.RepositoryDto;
import com.laa66.githubInspector.exception.UserNotFoundException;
import com.laa66.githubInspector.service.GithubDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(InspectorController.class)
public class InspectorControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GithubDataService githubDataService;

    @Test
    public void getUserRepositories_userNotFound() {
        when(githubDataService.getUserRepositoriesWithBranches(anyString()))
                .thenReturn(Flux.error(new UserNotFoundException("User not found")));

        webTestClient.get()
                .uri("/{username}", "nonexistentUser")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ExceptionDto.class)
                .value(response -> {
                    assert response.statusCode() == 404;
                    assert response.message().equals("User not found");
                });
    }

    @Test
    public void getUserRepositories_success() {
        RepositoryDto repositoryDto = new RepositoryDto("test-repo", "test-user", List.of());

        when(githubDataService.getUserRepositoriesWithBranches(anyString()))
                .thenReturn(Flux.just(repositoryDto));

        webTestClient.get()
                .uri("/{username}", "test-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoryDto.class)
                .value(repositories -> {
                    assert repositories.size() == 1;
                });
    }
}
