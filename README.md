# Cloud Foundry Java Buildpack System Tests

The purpose of this repository is to exercise the [Cloud Foundry Java Buildpack][b] together with its dependencies and associated services to ensure that the whole works together correctly.

The tests are JUnit tests in a Maven build which push the [Java Test Applications][a] to Cloud Foundry.

The [Cloud Foundry Java Client][c] is used to drive the Cloud Foundry Cloud Controller REST API.

## Running Tests
The project depends on Java 8.  You must also have cloned built the [Java Test Applications][a] in a repository that is a peer to this one.  To run the tests, run the following:

```shell
$ ./mvnw clean test
```

## Configuration
The tests are extremely configurable allowing fine-grained control over what applications and services are tested.

### Application Types:
* `EJB`
* `GROOVY`
* `JAVAMAIN`
* `RATPACK`
* `SPRINGBOOTCLI`
* `SPRINGBOOTCLIJAR`
* `WEB`
* `WEBSERVLET2`

### Service Types

| Type | Service | Plan
| ---- | ------- | ----
| `mongodb` | `mongolab` | `sandbox`
| `mysql` | `cleardb` | `amp`
| `postgresql` | `elephantsql` | `hippo`
| `rabbitmq` | `cloudamqp` | `lemur`
| `redis` | `rediscloud` | `30mb`

### General Configuration
The following configuration elements are required and define where the tests will run.

| Environment Variable | Value
| -------------------- | -----
| `TEST_BUILDPACK` | The buildpack to use when staging test applications
| `TEST_HOST` | The host of the Cloud Foundry being tested on
| `TEST_ORGANIZATION` | The organization to test in
| `TEST_PASSWORD` | The password of the user
| `TEST_SKIPSSLVALIDATION` | Whether to skip SSL validation against the Cloud Foundry
| `TEST_SPACE` | The space to test in
| `TEST_USERNAME` | The name of the user

### Test Configuration
The following configuration elements define what tests will run.

| Environment Variable | Value
| -------------------- | -----
| `TEST_HEALTH_<APPLICATION>` | Whether the health tests for a particular application are enabled
| `TEST_MONGODB_<APPLICATION>` | Whether the MongoDB tests for a particular application are enabled
| `TEST_MYSQL_<APPLICATION>` | Whether the MySQL tests for a particular application are enabled
| `TEST_POSTGRESQL_<APPLICATION>` | Whether the PostgreSQL tests for a particular application are enabled
| `TEST_RABBITMQ_<APPLICATION>` | Whether the RabbitMQ tests for a particular application are enabled
| `TEST_REDIS_<APPLICATION>` | Whether the Redis tests for a particular application are enabled

### Application Configuration
The following configuration elements define details about the test applications.

| Environment Variable | Value
| -------------------- | -----
| `APPLICATIONS_ROOT` | The path to the built `java-test-applications` repository
| `APPLICATIONS_<APPLICATION>_ENABLED` | Whether the application is pushed to Cloud Foundry
| `APPLICATIONS_<APPLICATION>_MEMORY` | The amount of memory allocated for the application, overriding the value in the `manifest.yml`
| `APPLICATIONS_<APPLICATION>_PREFIX` | The application name prefix for the application


### Service Configuration
The following configuration elements define details about the test services.

| Environment Variable | Value
| -------------------- | -----
| `SERVICES_<SERVICE>_ENABLED` | Whether to create the service
| `SERVICES_<APPLICATION>_NAME` | The name of the service
| `SERVICES_<APPLICATION>_SERVICE` | The service provider to use
| `SERVICES_<APPLICATION>_PLAN` | The service plan to use

## Contributing
[Pull requests][p] are welcome; see the [contributor guidelines][g] for details.

## License
This project is released under version 2.0 of the [Apache License][l].


[a]: https://github.com/cloudfoundry/java-test-applications
[b]: https://github.com/cloudfoundry/java-buildpack
[c]: https://github.com/cloudfoundry/cf-java-client
[g]: CONTRIBUTING.md
[l]: http://www.apache.org/licenses/LICENSE-2.0
[p]: http://help.github.com/send-pull-requests
