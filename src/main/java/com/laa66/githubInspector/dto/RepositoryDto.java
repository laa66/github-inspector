package com.laa66.githubInspector.dto;

import java.util.Collection;

public record RepositoryDto(String repositoryName, String ownerLogin, Collection<BranchDto> branches) {
}
