# JME Consumer Driven Contract Testing Example Consumer 2

## Overview
This project demonstrates consumer-driven contract testing using Pact in a Java-based microservice environment.
It includes a sample consumer service that interacts with a provider service. The contract between the consumer and
provider is defined and verified using Pact JVM and a Pact Broker.

The main difference between this project and the [JME CDCT Consumer Example](https://github.com/jme-admin-ch/jme-cdct-consumer-example)
project is that this project demonstrate the usage of the legacy Pact Java DSL to specify pacts instead of the current Pact Java DSL.

## Prerequisites

Before you begin, ensure you have the following installed:
- **Java 25** or higher
- **Docker** and **Docker Compose**

**Related Project:** This consumer project works with the [JME CDCT Provider Example](https://github.com/jme-admin-ch/jme-cdct-provider-example)
project for complete contract testing.

## Getting Started

### 1. Start the Local Pact Broker

This project includes a Docker Compose setup to run a local instance of the Pact Broker for demonstration and
development purposes. The Pact Broker is required by both the consumer and provider projects during their builds,
so it must be started before running any builds.

Start the Pact Broker:

```bash
docker-compose -f ./docker/docker-compose.yml up -d 
```

The Pact Broker UI will be available at: **http://localhost:9292**

### 2. Generate the Consumer Contract = Pact

Run the consumer tests to generate pact files:

```bash
./mvnw test
```

Running the tests will generate the pact files (contract descriptions) in the `target/pacts` directory.

### 3. Publish the Pact to the Broker

Publish the consumer's pact to the local Pact Broker:

```bash
./mvnw install -Plocal-pact-broker -Pcdct-enable-publishing-local
```

This command will:
- Run the consumer tests
- Generate the pact files
- Publish them to the local Pact Broker

**Maven Profile Explanations:**
- `local-pact-broker`: Configures the Pact Broker URL to point to your local instance (http://localhost:9292)
- `cdct-enable-publishing-local`: Enables pact publishing from local (non-CI) builds

Note: Usually, pacts are published in CI/CD pipelines only (to a remote Pact Broker instance).

### 4. View the Published Pact

Open the Pact Broker UI in your browser: [http://localhost:9292](http://localhost:9292)

The Pact Broker UI displays:
- All consumer-provider relationships
- The pacts for each relationship
- The verification status of each pact
- Different versions of pacts
- A network diagram of service dependencies

### 5. Verify the Pact (Provider Side)

The provider project can now verify against this published pact. See the 
[JME CDCT Provider Example](https://github.com/jme-admin-ch/jme-cdct-provider-example) project for instructions.

## Managing the Pact Broker

### Stopping the Pact Broker

```bash
docker-compose -f ./docker/docker-compose.yml down
```

### Removing Pact Broker Data

To stop the Pact Broker and remove persisted data volumes:

```bash
docker-compose -f ./docker/docker-compose.yml down -v
```

## Consumer Contract Specification

This project uses the legacy Pact Java DSL to define the consumer contract but still provides the contract in the V4 Pact specification.
The consumer's interactions with the provider are specified in the `TaskClientConsumerPactTest` class:

- Methods annotated with `@Pact` define the expected interactions
- Methods annotated with `@PactTestFor` contain tests that verify the consumer's behavior against the defined pact

## Pact Maven Plugin Configuration

To publish the generated pact to a Pact Broker, the Pact Maven plugin is used. It is configured as plugin in the `pom.xml`
and executes during the `install` phase:

```xml
<plugin>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>maven</artifactId>
</plugin>
```
## Running the Consumer Service Locally

Unlike other jEAP microservice example projects, the primary focus of this project is to execute Pact-related tasks
during the build process, rather than running the microservice itself. However, you can still run the consumer service
locally to test its integration with provider services.

Before running the consumer service, ensure that the provider service and the authorization mock server from the
[JME CDCT Provider Example](https://github.com/jme-admin-ch/jme-cdct-provider-example) project are running.

Then run the consumer service locally using the following command:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
The consumer service uses the provider's mock authorization server to obtain the access tokens required for
authenticating its requests to the provider service.

Use swagger-ui to interact with the consumer service API: [http://localhost:8081/jme-cdct-consumer-2-service/swagger-ui/index.html?urls.primaryName=public-api](http://localhost:8081/jme-cdct-consumer-2-service/swagger-ui/index.html?urls.primaryName=public-api)

## Troubleshooting

### Pact Broker Not Accessible

If you cannot access the Pact Broker UI:
1. Verify Docker containers are running: `docker ps`
2. Check the logs: `docker-compose -f ./docker/docker-compose.yml logs`
3. Ensure port 9292 is not in use by another application

### Publishing Fails

If pact publishing fails:
1. Verify the Pact Broker is running and accessible
2. Check that both profiles are enabled: `-Plocal-pact-broker -Pcdct-enable-publishing-local`
3. Check that the pact files in `target/pacts` are present


## Development Guidelines

This project needs to be versioned using [Semantic Versioning](http://semver.org/), and all changes need to be
documented in [CHANGELOG.md](./CHANGELOG.md) following the format defined
in [Keep a Changelog](http://keepachangelog.com/).

## Changes

Change log is available at [CHANGELOG.md](./CHANGELOG.md)

## Note

This repository is part of the open source distribution of jEAP. See [github.com/jme-admin-ch/jme](https://github.com/jme-admin-ch/jme)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
