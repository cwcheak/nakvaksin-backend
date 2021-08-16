<br />

<h3 align="center">NakVaksin</h3>

<p align="center">
A simple, easier way to get notified on your MySejahtera vaccination appointment.
<br />
<br />
<a href="https://www.nakvaksin.com/">View the App (Under development)</a>
</p>

## About The  Project
[Problem statement] Some [MySejahtera](https://mysejahtera.malaysia.gov.my/intro_en/) users do not receive any notification for their vaccination appointment and thus missed their appointment. So users are forced to check their MySejahtera application daily for the very same reason.

This application provides a simple, easier way to get notified on user's vaccination appointment.

This repository contains the backend of the NakVaksin application. The frontend repository is located [HERE](https://github.com/nubpro/nakvaksin).

### Build with
- [Quarkus](https://quarkus.io/) - the Supersonic Subatomic Java Framework.
- [Firestore](https://cloud.google.com/firestore) - NoSQL document database for data persistence.
- [Google Cloud Run](https://cloud.google.com/run) - Google's highly scalable serverless platform for containerized applications.

## Getting Started

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

### Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.<br/>

### Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/nakvaksin-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

### Deploy to Google Cloud Run

#### Build docker image and push to Cloud Run
```shell script
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-nakvaksin-jvm .

docker tag <TAG_NAME> gcr.io/nakvaksin/quarkus-nakvaksin-jvm

docker push gcr.io/nakvaksin/quarkus-nakvaksin-jvm
```

#### Access container remotely

```shell script
docker run -it --entrypoint sh gcr.io/nakvaksin/quarkus-nakvaksin-jvm:latest
```
