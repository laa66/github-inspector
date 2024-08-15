package com.laa66.githubInspector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laa66.githubInspector.dto.ExceptionDto;
import com.laa66.githubInspector.dto.RepositoryDto;
import com.laa66.githubInspector.response.Branch;
import com.laa66.githubInspector.response.Commit;
import com.laa66.githubInspector.response.Owner;
import com.laa66.githubInspector.response.Repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock
public class GithubInspectorIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void getUserRepositories_success() throws JsonProcessingException {
        Owner owner = new Owner("test-user");
        List<Repository> repositories = List.of(
                new Repository("repo1", owner, false),
                new Repository("repo2", owner, true),
                new Repository("repo3", owner, false),
                new Repository("repo4", owner, false)
        );

        List<Branch> branches = List.of(
                new Branch("main", new Commit("sha256")),
                new Branch("dev", new Commit("256sha"))
        );

        stubFor(get(urlEqualTo("/users/test-user/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(repositories))));

        stubFor(get(urlEqualTo("/repos/test-user/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(branches))));

        stubFor(get(urlEqualTo("/repos/test-user/repo2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(branches))));

        stubFor(get(urlEqualTo("/repos/test-user/repo3/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(branches))));

        stubFor(get(urlEqualTo("/repos/test-user/repo4/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(branches))));

        webTestClient.get()
                .uri("/{username}", "test-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(RepositoryDto.class)
                .hasSize(3);
    }

    @Test
    void getUserRepositories_userEmptyRepositories() throws JsonProcessingException {
        stubFor(get(urlEqualTo("/users/empty-user/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(Collections.emptyList()))));

        webTestClient.get()
                .uri("/{username}", "empty-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(RepositoryDto.class)
                .hasSize(0);
    }

    @Test
    void getUserRepositories_notFoundUser() {
        stubFor(get(urlEqualTo("/users/wrong-user/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));
        webTestClient.get()
                .uri("/{username}", "wrong-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ExceptionDto.class)
                .value(response -> {
                     assertEquals(404, response.statusCode());
                     assertEquals("User not found", response.message());
                });

    }

    @Test
    void getUserRepositories_4xxClientError() {
        stubFor(get(urlEqualTo("/users/overload-user/repos"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")));
        webTestClient.get()
                .uri("/{username}", "overload-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ExceptionDto.class)
                .value(response -> {
                    assertEquals(404, response.statusCode());
                    assertEquals("User not found", response.message());
                });
    }

    @Test
    void getUserRepositories_notFoundRepository() throws JsonProcessingException {
        Owner errorRepoOwner = new Owner("not-found-branches-user");
        List<Repository> noBranchesRepo = List.of(
                new Repository("repo", errorRepoOwner, false)
        );

        stubFor(get(urlEqualTo("/users/not-found-branches-user/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(noBranchesRepo))));

        stubFor(get(urlEqualTo("/repos/not-found-branches-user/repo/branches"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));
        webTestClient.get()
                .uri("/{username}", "not-found-branches-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(ExceptionDto.class)
                .value(response -> {
                    assertEquals(404, response.statusCode());
                    assertEquals("Repository not found", response.message());
                });
    }

    @Test
    void getUserRepositories_repo5xxServerError() {
        stubFor(get(urlEqualTo("/users/breaking-user/repos"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));

        webTestClient.get()
                .uri("/{username}", "breaking-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void getUserRepositories_branch5xxServerError() throws JsonProcessingException {
        Owner branchErrorRepoOwner = new Owner("breaking-branch-user");
        List<Repository> errorBranchRepo = List.of(
                new Repository("breaking-repo", branchErrorRepoOwner, false)
        );

        stubFor(get(urlEqualTo("/users/breaking-branch-user/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(errorBranchRepo))));

        stubFor(get(urlEqualTo("/repos/breaking-branch-user/breaking-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));
        webTestClient.get()
                .uri("/{username}", "breaking-branch-user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

}

