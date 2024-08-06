package com.laa66.githubInspector.service;

import com.laa66.githubInspector.dto.BranchDto;
import com.laa66.githubInspector.dto.RepositoryDto;
import com.laa66.githubInspector.exception.UserNotFoundException;
import com.laa66.githubInspector.response.Branch;
import com.laa66.githubInspector.response.Repository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class GithubDataServiceImpl implements GithubDataService {

    private final WebClient webClient;

    @Override
    public Flux<RepositoryDto> getUserRepositoriesWithBranches(String username) {
        return getUserNotForkRepos(username)
                .flatMap(repository -> getRepoBranches(repository.getOwner().getLogin(), repository.getName())
                        .map(branch -> new BranchDto(
                                branch.getName(),
                                branch.getCommit().getSha()))
                        .collectList()
                        .map(branches -> new RepositoryDto(
                                repository.getName(),
                                repository.getOwner().getLogin(),
                                branches)))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode().is4xxClientError())
                        return Flux.error(new UserNotFoundException("User not found"));
                    return Flux.error(ex);
                });
    }

    @Override
    public Flux<Repository> getUserNotForkRepos(String username) {
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(new UserNotFoundException("User not found")))
                .bodyToFlux(Repository.class)
                .filter(repository -> !repository.isFork());
    }

    @Override
    public Flux<Branch> getRepoBranches(String username, String repo) {
        return webClient.get()
                .uri("/repos/{username}/{repo}/branches", username, repo)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new UserNotFoundException("Repository not found")))
                .bodyToFlux(Branch.class);
    }


}
