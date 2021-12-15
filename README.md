# Akka gRPC Slinky gRPC-web

This template project was the result of investigation into full-stack `Scala` web development experience with following requirements:

- Compile time type safety
- Streaming API
- Quick hot reload in development
- Single deployment in production

Decision to base the implementation on `gRPC-Web` came after testing out popular 
implementations of `REST`([autowire](https://github.com/lihaoyi/autowire), 
[endpoints4s](https://github.com/endpoints4s/endpoints4s)) and `GraphQL` 
([sangria](https://github.com/sangria-graphql/sangria), 
[caliban](https://github.com/ghostdogpr/caliban)) 
and realisation that none of existing solutions satisfy each requirement, 
while `gRPC`'s basic functionality has all the required features.   

## How to run

### Development mode

Using `sbt`:
- `~serverDev`
- `~clientDev`

Using `IntelliJ` (tested with `2020.3`):
- Use `dev` Run/Debug configurations provided in `/.run`.

`~serverDev` starts back-end based on Akka HTTP in `watch` mode. 
It serves `gRPC` endpoints for front-end to consume on `localhost:9000`.

`~clientDev` starts `webpack dev server` with `HMR` enabled for front-end development with 
`Scala.js` and `Slinky`. Opening `localhost:8080` in the browser will serve `index.html`.

IntelliJ's `sbt shell` must be enabled to make sbt plugins run during compile - 
integrated Scala compile server won't trigger them.

### Production mode

Using `sbt`:
- Start `sbt` with production flag enabled - `sbt "-Denv=prod"`
- `server/docker:publishLocal`

Using `IntelliJ` (tested with `2020.3`):
- Use `server prod` Run/Debug configuration provided in `/.run`.

In production mode, optimized front-end bundle and all assets are packaged together with the back-end, 
then served with aggressive caching enabled by fingerprinting. Server serves both `HTTP` and `gRPC` 
endpoints.

## Slinky IntelliJ support

https://slinky.dev/docs/installation/ describes how to add support `@react` for macro.
If documented approach fails, manual plugin installation can be done by downloading 
https://mvnrepository.com/artifact/me.shadaj/slinky-core-ijext `.jar` 
and manually installing it as IntelliJ plugin through `Settings -> Plugins -> Install Plugin from Disk...`. 
Tested with IntelliJ IDEA `2020.3.1` and Slinky `0.6.6`.

## Built on:
- https://github.com/akka/akka-grpc
- https://github.com/scalapb/scalapb-grpcweb
- https://github.com/sbt/sbt-web
- https://github.com/vmunier/sbt-web-scalajs
- https://github.com/scalacenter/scalajs-bundler
- https://github.com/shadaj/slinky