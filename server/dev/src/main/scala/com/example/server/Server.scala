package com.example.server
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

object Server extends ServerBase {
  override val corsApiRouteFilter: Route => Route = apiRoute =>
    cors(CorsSettings.defaultSettings)(apiRoute)
  override val webServiceRoute: Route = pathSingleSlash {
    get {
      redirect(Uri("http://localhost:8080"), StatusCodes.TemporaryRedirect)
    }
  }
}
