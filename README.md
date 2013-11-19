# Cloud Foundry Java Buildpack System Test

The purpose of this repository is to exercise the [Cloud Foundry Java Buildpack][] together with its dependencies and associated services to ensure that the whole works together correctly.

The tests are JUnit tests in a gradle build which push the [Java test applications][] submodule (in `vendor/java-test-applications`) to Cloud Foundry.

The Cloud Foundry Client Library from the [CF Java Client repository][] is used to drive the Cloud Foundry Cloud Controller REST API.

[Cloud Foundry Java Buildpack]: https://github.com/cloudfoundry/java-buildpack
[Java test applications]: https://github.com/cloudfoundry/java-test-applications
[CF Java Client repository]: https://github.com/cloudfoundry/cf-java-client

## Environment Variables
The following environment variables must be set before building this project or running the tests.

| Environment Variable | Value
| -------------------- | -----
| `CF_ORG` | The Cloud Foundry organisation to use.
| `CF_PASSWORD` | The password corresponding to the Cloud Foundry username.
| `CF_SPACE` | The Cloud Foundry space to use.
| `CF_TARGET` | The Cloud Foundry instance to use, e.g. `https://api.run.pivotal.io`.
| `CF_USERNAME` | A Cloud Foundry username, which often consists of an email address.

The following environment variables may be set before building this project or running the tests.

| Environment Variable | Value
| -------------------- | -----
| `DEFAULT_BUILDPACK` | The buildpack to use if one is not specified in the test applications' `manifest.yml`. This defaults to the Java buildpack if not specified.
| `OVERRIDE_BUILDPACK` | The buildpack to use regardless of whether one is specified in the test applications' `manifest.yml`. If not specified, the buildpack specified in `manifest.yml` (or the default buildpack if that is not specified) is used.

Note: when running tests in Eclipse or IntelliJ IDEA, you can set environment variables in a run configuration.

## Building

This project is built with Gradle. Set the environment variables as described above and then issue:
```plain
./gradlew
```

## Running Tests
To run the tests, set the environment variables as described above and then do the following:
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

