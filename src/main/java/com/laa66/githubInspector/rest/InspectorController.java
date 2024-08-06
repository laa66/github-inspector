package com.laa66.githubInspector.rest;

import com.laa66.githubInspector.dto.RepositoryDto;
import com.laa66.githubInspector.service.GithubDataService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController("/github")
@AllArgsConstructor
public class InspectorController {

    private final GithubDataService githubDataService;

    @GetMapping("/{username}")
    public Flux<RepositoryDto> getUserRepositories(@PathVariable String username) {
        return githubDataService.getUserRepositoriesWithBranches(username);
    }

}
