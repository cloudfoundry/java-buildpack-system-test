# Cloud Foundry Java Buildpack System Test

The purpose of this repository is to exercise the [Cloud Foundry Java Buildpack][] together with its dependencies and associated services to ensure that the whole works together correctly.

The tests are JUnit tests in a gradle build which push the [Java test applications][] submodule (in `vendor/java-test-applications`) to Cloud Foundry.

The Cloud Foundry Client Library from the [CF Java Client repository][] is used to drive the Cloud Foundry Cloud Controller REST API.

[Cloud Foundry Java Buildpack]: https://github.com/cloudfoundry/java-buildpack
[Java test applications]: https://github.com/cloudfoundry/java-test-applications
[CF Java Client repository]: https://github.com/cloudfoundry/cf-java-client

## Building

This project is built with Gradle.

```plain
./gradlew
```

## Running Tests
To run the tests, do the following:

```bash
./gradlew
```

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: CONTRIBUTING.md

## License
This project is released under version 2.0 of the [Apache License][].

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0

