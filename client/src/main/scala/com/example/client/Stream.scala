package com.example.client

import com.example.client.App.serviceStub
import com.example.service.Request
import com.example.service.Response
import io.grpc.stub.StreamObserver
import scalapb.grpcweb.Metadata
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import scala.scalajs.js.timers.clearTimeout
import scala.scalajs.js.timers.setTimeout

@react object Stream {
  case class Props(cancel: Boolean)

  val component: FunctionalComponent[Props] = FunctionalComponent { props =>
    val (status, setStatus) = useState("Request pending")

    useEffect(
      () => {
        val req                = Request(payload = "Hello!")
        val metadata: Metadata = Metadata("custom-header-2" -> "streaming-value")

        var resCount = 0

        val stream = serviceStub.serverStreaming(
          req,
          metadata,
          new StreamObserver[Response] {
            override def onNext(value: Response): Unit = {
              resCount += 1
              setStatus(s"Received success [$resCount]")
            }

            override def onError(ex: Throwable): Unit = {
              setStatus(s"Received failure: $ex")
            }

            override def onCompleted(): Unit = {
              setStatus(s"Received completed")
            }
          }
        )
        setStatus("Request sent")

        val maybeTimer = if (props.cancel) {
          Some(
            setTimeout(5000) {
              setStatus(s"Stream stopped by client")
              stream.cancel()
            }
          )
        } else None
        () => {
          stream.cancel()
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
