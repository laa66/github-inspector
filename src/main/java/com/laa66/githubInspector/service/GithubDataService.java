package com.laa66.githubInspector.service;

import com.laa66.githubInspector.dto.RepositoryDto;
import com.laa66.githubInspector.response.Branch;
import com.laa66.githubInspector.response.Repository;
import reactor.core.publisher.Flux;

public interface GithubDataService {
    Flux<RepositoryDto> getUserRepositoriesWithBranches(String username);
    Flux<Repository> getUserNotForkRepos(String username);
    Flux<Branch> getRepoBranches(String username, String repo);
}
