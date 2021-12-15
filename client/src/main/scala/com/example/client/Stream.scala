package com.example.client

import com.example.api.ApiRequest
import com.example.api.ApiResponse
import com.example.client.App.ApiClient
import org.scalajs.dom.ReadableStreamReader
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits._
import scala.scalajs.js.timers.clearTimeout
import scala.scalajs.js.timers.setTimeout
import scala.util.control.NonFatal

@react object Stream {
  case class Props(cancel: Boolean)

  val component: FunctionalComponent[Props] = FunctionalComponent { props =>
    val (status, setStatus) = useState("Request pending")

    useEffect(
      () => {
        val req = ApiRequest(payload = "Hello!")
        //TODO add headers to endpoint
        //val metadata: Metadata = Metadata("custom-header-2" -> "streaming-value")

        var resCount = 0

        //TODO add missing cancellation handle https://github.com/endpoints4s/endpoints4s/issues/977
        ApiClient
          .serverStreaming(req)
          .flatMap { stream =>
            def read(
                reader: ReadableStreamReader[ApiResponse]
            ): Future[Unit] = {
              reader
                .read()
                .flatMap { chunk =>
                  if (chunk.done) {
                    setStatus(s"Received completed")
                    Future.unit

                  } else {
                    resCount += 1
                    setStatus(
                      s"Received element [${chunk.value}] success [$resCount]"
                    )
                    read(reader)
                  }
                }
                .recoverWith { case NonFatal(ex) =>
                  setStatus(s"Received failure: $ex")
                  Future.unit
                }
            }
            read(stream.getReader())
          }
        setStatus("Request sent")

        val maybeTimer = if (props.cancel) {
          Some(
            setTimeout(5000) {
              setStatus(s"Stream stopped by client")
              //TODO use missing cancellation handle
            }
          )
        } else None
        () => {
          //TODO use missing cancellation handle
          maybeTimer.foreach(clearTimeout)
        }
      },
      Seq.empty
    )

    div(
      h2("Stream request:"),
      p(status)
    )
  }
}
