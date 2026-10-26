# myapp

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

Generate a local JWT signing key outside the repository before starting the
application or running tests:

```shell
mkdir -p ~/.myapp
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
  -out ~/.myapp/jwt-dev-private.pem
openssl pkey -in ~/.myapp/jwt-dev-private.pem -pubout \
  -out ~/.myapp/jwt-dev-public.pem
chmod 600 ~/.myapp/jwt-dev-private.pem
```

Development and test profiles use these files by default. You can override the
locations with `JWT_SIGNING_KEY_LOCATION` and
`JWT_VERIFY_PUBLIC_KEY_LOCATION`.

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

For production, provide `JWT_SIGNING_KEY_LOCATION` as the path to a
deployment-managed private-key secret and `JWT_VERIFY_PUBLIC_KEY_LOCATION` as
the path to its matching public key. Do not use the local development key in
production. The previously committed key must be considered compromised: rotate
any deployed keypair that matches it and invalidate tokens signed with the old
key.

Production also requires a configured PostgreSQL connection
(`QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, and
`QUARKUS_DATASOURCE_PASSWORD`), `CORS_ORIGINS` set to the deployed frontend
origin(s), and an `AWS_S3_BUCKET`. Set `SMTP_HOST`, `SMTP_PORT`,
`QUARKUS_MAILER_USERNAME`, `QUARKUS_MAILER_PASSWORD`, and `MAIL_FROM` to
configure a real SMTP service and verified sender. Required production values
are intentionally not defaulted to local or example services. The mailer is
mocked only in dev and test profiles.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/myapp-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
