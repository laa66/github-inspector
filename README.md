# GitHub Inspector REST API

GitHub Inspector is a RESTful API built with Java 21 and Spring Boot 3. 
The API allows users to retrieve a list of repositories for a given GitHub user, along with branch information for each repository. 
It uses Spring WebClient to interact with the GitHub API.

<hr>

## Getting started

#### Prerequisites
- Maven
- Java 21

#### Installation

      # Clone this repository to your local machine
      $ git clone https://github.com/laa66/github-inspector.git

<!-- end -->

      # Navigate to project directory
      $ cd github-inspector

<!-- end -->
      # Build the project with maven
      $ mvn clean install

<!-- end -->
      # Run github-inspector
      $ mvn spring-boot:run

#### API will be accessible at ```http://localhost:8080```.

#### Tests
To run tests use following command:

      # Run tests
      $ mvn test

## Endpoints 🗺️

#### Get User Repositories and Branches
* URL: ``/github/{username}``
* Method: ``GET``
* Description: Retrieves a list of non-fork repositories for the specified GitHub user, along with branch information for each repository.

#### Response
* Status 200: List of repositories with branch information
* Status 404: User not found

#### Example response

```json
[
    {
        "name": "repo1",
        "owner": "owner",
        "branches": [
            {
                "name": "main",
                "lastCommitSha": "3913kd"
            },
            {
                "name": "develop",
                "lastCommitSha": "dwafkmw22"
            }
        ]
    },
    {
        "name": "repo2",
        "owner": "owner",
        "branches": [
            {
                "name": "master",
                "lastCommitSha": "ddk2334"
            }
        ]
    }
]
```

#### Error response
```json
{
  "status": 404,
  "message": "User not found"
}
```

## Features 📌

* Retrieve a list of non-fork repositories for a specified GitHub user.
* For each repository, retrieve information about its branches, including the last commit SHA.
* Handle user not found scenarios with appropriate error responses.


## Built with 🔨

#### Technologies & tools used:

- Java 21
- Spring Boot 3
- Spring WebFlux
- Reactor
- Mockito