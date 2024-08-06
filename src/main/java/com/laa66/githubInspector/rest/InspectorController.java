package com.laa66.githubInspector.rest;

import com.laa66.githubInspector.dto.ExceptionDto;
import com.laa66.githubInspector.dto.RepositoryDto;
import com.laa66.githubInspector.exception.UserNotFoundException;
import com.laa66.githubInspector.service.GithubDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
public class InspectorController {

    private final GithubDataService githubDataService;

    @GetMapping("/{username}")
    public Flux<RepositoryDto> getUserRepositories(@PathVariable String username) {
        return githubDataService.getUserRepositoriesWithBranches(username);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ExceptionDto handleUserNotFoundException(UserNotFoundException ex) {
        return new ExceptionDto(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage());
    }

}
