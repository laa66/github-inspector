package com.laa66.githubInspector.dto;

import reactor.core.publisher.Flux;

import java.util.Collection;

public record RepositoryDto(String repositoryName, String ownerLogin, Collection<BranchDto> branches) {
}
