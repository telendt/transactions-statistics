# Transactions statistics

A web service providing RESTful API for transactions statistics.
For more information please see [INSTRUCTIONS](INSTRUCTIONS.md).

## Requirements

Java 1.8 and Maven 3.

## Commands

Run:

    mvn clean spring-boot:run

Install:

    mvn clean install

Run unit tests:

    mvn clean test

Run integration test:

    mvn clean integration-test

## Design rationale

Please see [SOLUTION](SOLUTION.md).

## TODO

- dockerize
- verify thread-safety with [jcstress](https://openjdk.java.net/projects/code-tools/jcstress/)
- improve documentation and test coverage
- parametrize numbers formatting via configuration (number of decimal
  places and rounding)
