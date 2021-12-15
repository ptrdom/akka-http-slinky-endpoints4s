package com.example.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.scaladsl.WebHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.example.BuildInfo
import com.example.ServiceHandler
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object Server extends Directives {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def startHttpServer()(implicit actorSystem: ActorSystem[_]): Unit = {
    import actorSystem.executionContext

    val service: PartialFunction[HttpRequest, Future[HttpResponse]] = {
      ServiceHandler.partial(new ServiceImpl())
    }

    val indexAndAssets = new WebService().route

    implicit val corsSettings: CorsSettings = if (BuildInfo.environmentMode.equalsIgnoreCase("development")) {
      CorsSettings.defaultSettings
    } else {
      WebHandler.defaultCorsSettings
    }
    val grpcWebServiceHandlers = WebHandler.grpcWebHandler(service)

    val handlerRoute: Route = { ctx =>
      grpcWebServiceHandlers(ctx.request).map(RouteResult.Complete)
    }

    val route = concat(
      indexAndAssets,
      handlerRoute
    )

    val binding = Http()
      .newServerAt(
        interface = "0.0.0.0",
        port = 9000
      )
      .bind(Route.toFunction(route))

    binding.onComplete {
      case Success(binding) =>
        logger.info(s"gRPC server bound to: ${binding.localAddress}")
      case Failure(ex) =>
        logger.error(s"gRPC server binding failed", ex)
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
    ActorSystem[Nothing](rootBehavior, "akka-grpc-slinky-grpcweb", conf)
  }
}
