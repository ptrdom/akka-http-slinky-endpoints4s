package com.example.server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.after
import akka.stream.scaladsl.Source
import endpoints4s.akkahttp.server
import com.example.api.Api
import com.example.api.ApiResponse
import com.example.server.utility.ChunkedJsonEntities

import scala.concurrent.Future
import scala.concurrent.duration._

class ApiServer()(implicit as: ActorSystem[_])
    extends Api
    with server.Endpoints
    with server.JsonEntitiesFromCodecs
    with ChunkedJsonEntities {

  val unaryRoute = unary.implementedByAsync(in =>
    after(2.seconds) {
      val message = s"Received [${in.payload}]"
      println(message)
      Future.successful(ApiResponse(message))
    }
  )

  val serverStreamingRoute = serverStreaming
    .implementedBy(in =>
      Source
        .repeat(in)
        .zipWithIndex
        .map { case (in, idx) =>
          val message = s"Received [${in.payload}] idx [$idx]"
          println(message)
          ApiResponse(message)
        }
        .throttle(1, 0.5.seconds)
        .take(20)
    )

  val routes: Route = unaryRoute ~ serverStreamingRoute
}
