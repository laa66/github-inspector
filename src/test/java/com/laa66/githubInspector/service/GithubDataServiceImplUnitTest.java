package com.laa66.githubInspector.service;


import com.laa66.githubInspector.exception.UserNotFoundException;
import com.laa66.githubInspector.response.Branch;
import com.laa66.githubInspector.response.Commit;
import com.laa66.githubInspector.response.Owner;
import com.laa66.githubInspector.response.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(GithubDataServiceImpl.class)
public class GithubDataServiceImplUnitTest {

    @MockBean
    private WebClient webClient;

    private GithubDataService githubDataService;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(WebClient.ResponseSpec.class);
        githubDataService = new GithubDataServiceImpl(webClient);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersUriSpec.uri(any(), anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    public void getUserNotForkRepos_userNotFound() {
        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.error(new WebClientResponseException(404, "Not Found", null, null, null, null)));

        StepVerifier.create(githubDataService.getUserNotForkRepos("nonexistentUser"))
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    public void getUserNotForkRepos_success() {
        Repository repo1 = new Repository();
        repo1.setName("test-repo-1");
        repo1.setOwner(new Owner("test-user"));
        repo1.setFork(false);

        Repository repo2 = new Repository();
        repo2.setName("test-repo-2");
        repo2.setOwner(new Owner("test-user"));
        repo2.setFork(true);

        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.just(repo1, repo2));

        StepVerifier.create(githubDataService.getUserNotForkRepos("test-user"))
                .expectNextMatches(repo -> repo.getName().equals("test-repo-1") && !repo.isFork())
                .verifyComplete();
    }

    @Test
    public void getRepoBranches_repoNotFound() {
        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Branch.class)).thenReturn(Flux.error(new WebClientResponseException(404, "Not Found", null, null, null, null)));

        StepVerifier.create(githubDataService.getRepoBranches("test-user", "nonexistentRepo"))
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    public void getRepoBranches_success() {
        Branch branch = new Branch();
        branch.setName("main");
        branch.setCommit(new Commit("sha123"));

        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Branch.class)).thenReturn(Flux.just(branch));

        StepVerifier.create(githubDataService.getRepoBranches("test-user", "test-repo"))
                .expectNextMatches(b -> b.getName().equals("main") && b.getCommit().getSha().equals("sha123"))
                .verifyComplete();
    }

    @Test
    public void getUserRepositoriesWithBranches_userNotFound() {
        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.error(new WebClientResponseException(404, "Not Found", null, null, null, null)));

        StepVerifier.create(githubDataService.getUserRepositoriesWithBranches("nonexistentUser"))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    public void getUserRepositoriesWithBranches_success() {
        Repository repository = new Repository();
        repository.setName("test-repo");
        repository.setOwner(new Owner("test-user"));
        repository.setFork(false);

        Branch branch = new Branch();
        branch.setName("main");
        branch.setCommit(new Commit("sha123"));

        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.just(repository));
        when(responseSpec.bodyToFlux(Branch.class)).thenReturn(Flux.just(branch));

        when(webClient.get()
                .uri("/repos/{username}/{repo}/branches", "test-user", "test-repo")
                .retrieve()
                .bodyToFlux(Branch.class))
                .thenReturn(Flux.just(branch));

        StepVerifier.create(githubDataService.getUserRepositoriesWithBranches("test-user"))
                .expectNextMatches(repoDto ->
                        repoDto.repositoryName().equals("test-repo") &&
                                repoDto.branches().size() == 1)
                .verifyComplete();
    }
}
