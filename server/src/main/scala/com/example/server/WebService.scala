package com.example.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import com.example.BuildInfo
import com.example.server.WebService._

class WebService() extends Directives {

  val route: Route = concat(
    pathSingleSlash {
      get {
        if (BuildInfo.environmentMode.equalsIgnoreCase("development")) {
          redirect(Uri("http://localhost:8080"), StatusCodes.TemporaryRedirect)
        } else {
          encodeResponse {
            getFromResource(s"$AssetsPath/index.html")
          }
        }
      }
    },
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource(s"$AssetsPath/" + file)
      }
    },
    path("favicon.ico") {
      complete("")
    }
  )
}

object WebService {
  val AssetsPath: String =
    if (BuildInfo.environmentMode.equalsIgnoreCase("production")) {
      "public/dist"
    } else {
      "public"
    }
}
