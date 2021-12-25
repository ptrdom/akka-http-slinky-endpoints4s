package com.example.client

import com.example.api.ApiRequest
import com.example.client.App.ApiClient
import com.example.client.utility.Handlers._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import scala.scalajs.js.timers.clearTimeout
import scala.scalajs.js.timers.setTimeout

@react object Stream {
  case class Props(abort: Boolean)

  val component: FunctionalComponent[Props] = FunctionalComponent { props =>
    val (status, setStatus) = useState("Request pending")

    useEffect(
      () => {
        val req = ApiRequest(payload = "Hello!")
        //TODO add headers to endpoint
        //val metadata: Metadata = Metadata("custom-header-2" -> "streaming-value")

        var resCount = 0

        val result = ApiClient.serverStreaming(req)
        setStatus("Request sent")

        result.future
          .handle(
            element => {
              resCount += 1
              setStatus(
                s"Received element [$element] success [$resCount]"
              )
            },
            () => setStatus(s"Received completed"),
            ex => setStatus(s"Received failure: $ex")
          )

        val maybeTimer = if (props.abort) {
          Some(
            setTimeout(5000) {
              setStatus(s"Request aborted by client")
              result.abort()
            }
          )
        } else None

        () => {
          result.abort()
          maybeTimer.foreach(clearTimeout)
        }
      },
      Seq.empty
    )

    div(
      h2(s"Stream request${if (props.abort) " (with abort)" else ""}:"),
      p(status)
    )
  }
}
