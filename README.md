# Akka HTTP - Slinky - endpoints4s

This template project was the result of investigation into full-stack `Scala` web development experience with following requirements:

- Compile time type safety
- Streaming API
- Quick hot reload in development
- Single deployment in production

Initially `gRPC-web` was chosen for API implementation, because `endpoints4s` lacked streaming support, but this capability 
was added with version `1.7.0`. This makes `endpoints4s` a preferred solution, since it eliminates all the friction produced 
by having to deal with a limited `gRPC` model in a full-stack Scala environment. `GraphQL`
([sangria](https://github.com/sangria-graphql/sangria), [caliban](https://github.com/ghostdogpr/caliban)) could become a 
valid option once `stream` and `defer` directives are implemented, but the issue of having to deal with certain `GraphQL`
model limitations would remain.

## How to run

### Development mode

Using `sbt`:
- `startServerDev`
- `startClientDev`

Using `IntelliJ` (tested with `2021.3`):
- Use `server dev start` and `client dev start` Run/Debug configurations provided in `/.run`.

`startServerDev` starts back-end based on Akka HTTP in `watch` mode.
It serves HTTP endpoints for front-end to consume on `localhost:9000`.

`startClientDev` starts `webpack dev server` with `HMR` enabled for front-end development with
`Scala.js` and `Slinky`. Opening `localhost:8080` in the browser will serve `index.html`.

IntelliJ's `sbt shell` must be enabled to make sbt plugins run during compile -
integrated Scala compile server won't trigger them.

### Production mode

Using `sbt`:
- `serverProd/run`

Using `IntelliJ` (tested with `2021.3`):
- Use `server prod run` Run/Debug configuration provided in `/.run`.

In production mode, optimized front-end bundle and all assets are packaged together with the back-end,
then served with aggressive caching enabled by fingerprinting.

Docker image publishing is implemented with [sbt-native-packager](https://github.com/sbt/sbt-native-packager).

## Slinky IntelliJ support

https://slinky.dev/docs/installation/ section `IntelliJ Support` describes how to add support for `@react` macro.

## Built on:
- [endpoints4s](https://github.com/endpoints4s/endpoints4s) 
- [sbt-web](https://github.com/sbt/sbt-web)
- [sbt-web-scalajs](https://github.com/vmunier/sbt-web-scalajs)
- [scalajs-bundler](https://github.com/scalacenter/scalajs-bundler)
- [slinky](https://github.com/shadaj/slinky)