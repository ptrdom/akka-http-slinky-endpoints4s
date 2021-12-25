package com.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.util.Failure
import scala.util.Success

trait ServerBase extends Directives {

  val corsApiRouteFilter: Route => Route
  val webServiceRoute: Route

  private val logger = LoggerFactory.getLogger(this.getClass)

  def startHttpServer()(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext

    val route = concat(
      webServiceRoute,
      corsApiRouteFilter(new ApiServer().routes)
    )

    val binding = Http()
      .newServerAt(
        interface = "0.0.0.0",
        port = 9000
      )
      .bind(Route.toFunction(route))

    binding.onComplete {
      case Success(binding) =>
        logger.info(s"HTTP server bound to: ${binding.localAddress}")
      case Failure(ex) =>
        logger.error(s"HTTP server binding failed", ex)
        actorSystem.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      startHttpServer()(context.system)
      Behaviors.empty
    }
    val conf = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    ActorSystem[Nothing](rootBehavior, "akka-http-slinky-endpoints4s", conf)
  }
}
