package com.example.server
import akka.http.scaladsl.server.Route

object Server extends ServerBase {
  override val corsApiRouteFilter: Route => Route = identity
  override val webServiceRoute: Route = {
    val assetsPath = "public/dist"
    concat(
      pathSingleSlash {
        get {
          encodeResponse {
            getFromResource(s"$assetsPath/index.html")
          }
        }
      },
      pathPrefix("assets" / Remaining) { file =>
        // optionally compresses the response with Gzip or Deflate
        // if the client accepts compressed responses
        encodeResponse {
          getFromResource(s"$assetsPath/" + file)
        }
      },
      path("favicon.ico") {
        complete("")
      }
    )
  }
}
