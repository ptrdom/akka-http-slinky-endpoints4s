import BuildEnvPlugin.autoImport
import BuildEnvPlugin.autoImport.BuildEnv
import com.typesafe.sbt.packager.docker.DockerAlias
import com.typesafe.sbt.packager.docker.ExecCmd

scalaVersion in ThisBuild := "2.13.4"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val akkaVersion = "2.6.10"
lazy val akkaHttpVersion = "10.2.6"
lazy val endpoints4sVersion = "1.6.0"
lazy val logbackVersion = "1.2.3"
lazy val scalaTestPlusPlayVersion = "5.0.0"
lazy val scalaJsDomVersion = "1.1.0"
lazy val scalaJsScriptsVersion = "1.1.4"
lazy val slinkyVersion =
  "0.6.8+8-0b6a9cab" //TODO update to 0.7.0 once released (for scalajs-dom 2.0.0)
lazy val reactVersion = "16.12.0"
lazy val reactProxyVersion = "1.1.8"

lazy val `akka-http-slinky-endpoints4s` = (project in file("."))
  .aggregate(
    client,
    server
  )

lazy val api =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("api"))
    .settings(
      libraryDependencies += "org.endpoints4s" %%% "algebra" % "1.6.0",
      libraryDependencies += "org.endpoints4s" %%% "json-schema-generic" % "1.6.0"
    )

lazy val apiJS = api.js
lazy val apiJVM = api.jvm

lazy val client =
  project
    .in(file("client"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion,
      libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion,
      libraryDependencies += "org.endpoints4s" %%% "fetch-client" % "1.0.0",
      libraryDependencies += "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",
      scalacOptions += "-Ymacro-annotations",
      npmDependencies in Compile += "react" -> reactVersion,
      npmDependencies in Compile += "react-dom" -> reactVersion,
      npmDependencies in Compile += "react-proxy" -> reactProxyVersion,
      npmDevDependencies in Compile += "file-loader" -> "6.2.0",
      npmDevDependencies in Compile += "style-loader" -> "2.0.0",
      npmDevDependencies in Compile += "css-loader" -> "5.0.1",
      npmDevDependencies in Compile += "html-webpack-plugin" -> "4.3.0",
      npmDevDependencies in Compile += "webpack-merge" -> "5.7.3",
      scalaJSStage := {
        autoImport.buildEnv.value match {
          case BuildEnv.Development =>
            FastOptStage
          case _ =>
            FullOptStage
        }
      },
      webpackResources := baseDirectory.value / "webpack" * "*",
      webpackConfigFile in fastOptJS := Some(
        baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
      ),
      webpackConfigFile in fullOptJS := Some(
        baseDirectory.value / "webpack" / "webpack-opt.config.js"
      ),
      webpackConfigFile in Test := Some(
        baseDirectory.value / "webpack" / "webpack-core.config.js"
      ),
      webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
      requireJsDomEnv in Test := true
    )
    .dependsOn(apiJS)

lazy val server = project
  .enablePlugins(
    WebScalaJSBundlerPlugin,
    JavaAppPackaging,
    DockerPlugin,
    BuildInfoPlugin
  )
  .in(file("server"))
  .settings(
    scalaJSProjects := {
      autoImport.buildEnv.value match {
        case BuildEnv.Production =>
          Seq(client)
        case _ =>
          Seq.empty
      }
    },
    pipelineStages in Assets := {
      autoImport.buildEnv.value match {
        case BuildEnv.Production =>
          Seq(scalaJSPipeline)
        case _ =>
          Seq.empty
      }
    },
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.typesafe.akka" %% "akka-pki" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,
      "org.endpoints4s" %% "akka-http-server" % "6.0.0",
      "ch.megard" %% "akka-http-cors" % "1.1.2",
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.1.1" % Test
    ),
    Compile / mainClass := Some("com.example.server.Server"),
    buildInfoKeys ++= Seq[BuildInfoKey](
      "environmentMode" -> autoImport.buildEnv.value
    ),
    buildInfoPackage := "com.example"
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
  .dependsOn(apiJVM)

addCommandAlias("serverDev", "~server/reStart")
addCommandAlias(
  "clientDev",
  "client/fastOptJS::startWebpackDevServer;~client/fastOptJS"
)
