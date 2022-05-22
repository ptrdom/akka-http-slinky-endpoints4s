import com.typesafe.sbt.packager.docker.DockerAlias
import com.typesafe.sbt.packager.docker.ExecCmd

scalaVersion in ThisBuild := "2.13.4"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val akkaVersion = "2.6.10"
lazy val akkaHttpVersion = "10.2.6"
lazy val endpoints4sVersion = "1.7.0"
lazy val endpoints4sCirceVersion = "2.0.0"
lazy val endpoints4sFetchClientVersion = "2.0.0"
lazy val endpoints4sAkkaHttpServerVersion = "6.1.0"
lazy val logbackVersion = "1.2.3"
lazy val scalaTestPlusPlayVersion = "5.0.0"
lazy val scalaJsDomVersion = "1.1.0"
lazy val scalaJsScriptsVersion = "1.1.4"
lazy val slinkyVersion = "0.7.0"
lazy val reactVersion = "16.12.0"
lazy val reactProxyVersion = "1.1.8"
lazy val circeVersion = "0.14.1"

lazy val `akka-http-slinky-endpoints4s` = (project in file("."))
  .aggregate(
    clientBase,
    clientDev,
    clientProd,
    serverBase,
    serverDev,
    serverProd
  )

lazy val api =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("api"))
    .settings(
      libraryDependencies += "org.endpoints4s" %%% "algebra" % endpoints4sVersion,
      libraryDependencies += "org.endpoints4s" %%% "algebra-circe" % endpoints4sCirceVersion,
      libraryDependencies += "io.circe" %%% "circe-generic" % circeVersion
    )

lazy val apiJS = api.js
lazy val apiJVM = api.jvm

lazy val clientBase = project
  .in(file("client/base"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion,
    libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion,
    libraryDependencies += "org.endpoints4s" %%% "fetch-client" % endpoints4sFetchClientVersion,
    libraryDependencies += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",
    scalacOptions += "-Ymacro-annotations"
  )
  .dependsOn(apiJS)

lazy val clientCommonSettings = Seq(
  npmDependencies in Compile += "react" -> reactVersion,
  npmDependencies in Compile += "react-dom" -> reactVersion,
  npmDependencies in Compile += "react-proxy" -> reactProxyVersion,
  npmDevDependencies in Compile += "html-webpack-plugin" -> "5.5.0",
  webpackCliVersion := "4.9.0",
  webpackResources := (baseDirectory in ThisBuild).value / "client" / "webpack" * "*",
  webpackConfigFile in Test := Some(
    (baseDirectory in ThisBuild).value / "client" / "webpack" / "webpack-core.config.js"
  ),
  requireJsDomEnv in Test := true
)

lazy val clientDev = project
  .in(file("client/dev"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    clientCommonSettings,
    scalaJSStage := FastOptStage,
    webpackConfigFile in fastOptJS := Some(
      (baseDirectory in ThisBuild).value / "client" / "webpack" / "webpack-fastopt.config.js"
    ),
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()
  )
  .dependsOn(clientBase)

lazy val clientProd = project
  .in(file("client/prod"))
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    clientCommonSettings,
    scalaJSStage := FullOptStage,
    webpackConfigFile in fullOptJS := Some(
      (baseDirectory in ThisBuild).value / "client" / "webpack" / "webpack-opt.config.js"
    )
  )
  .dependsOn(clientBase)

lazy val serverBase = project
  .in(file("server/base"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.typesafe.akka" %% "akka-pki" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,
      "org.endpoints4s" %% "akka-http-server" % endpoints4sAkkaHttpServerVersion,
      "ch.megard" %% "akka-http-cors" % "1.1.2",
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.1.1" % Test
    )
  )
  .dependsOn(apiJVM)

lazy val serverCommonSettings = Seq(
  Compile / mainClass := Some("com.example.server.Server")
)

lazy val serverDev = project
  .in(file("server/dev"))
  .settings(
    serverCommonSettings
  )
  .dependsOn(serverBase)

lazy val serverProd = project
  .in(file("server/prod"))
  .enablePlugins(
    WebScalaJSBundlerPlugin,
    JavaAppPackaging,
    DockerPlugin
  )
  .settings(
    serverCommonSettings,
    scalaJSProjects := Seq(clientProd),
    pipelineStages := Seq(scalaJSPipeline),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .settings(
    dockerAliases in Docker += DockerAlias(
      None,
      None,
      "akka-http-slinky-endpoints4s",
      None
    ),
    packageName in Docker := "akka-http-slinky-endpoints4s",
    dockerBaseImage := "openjdk:8-alpine",
    dockerCommands := {
      val (stage0, stage1) = dockerCommands.value.splitAt(8)
      val (stage1part1, stage1part2) = stage1.splitAt(5)
      stage0 ++ stage1part1 ++ Seq(
        ExecCmd("RUN", "apk", "add", "--no-cache", "bash")
      ) ++ stage1part2
    },
    dockerExposedPorts ++= Seq(9000)
  )
  .dependsOn(serverBase)

addCommandAlias("startServerDev", "~serverDev/reStart")
addCommandAlias(
  "startClientDev",
  "clientDev/fastOptJS::startWebpackDevServer;~clientDev/fastOptJS"
)
