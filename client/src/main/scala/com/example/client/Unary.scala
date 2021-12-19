package com.example.client

import com.example.api.ApiRequest
import com.example.client.App.ApiClient
import com.example.client.App.globalExecutionContext
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import scala.scalajs.js.timers.clearTimeout
import scala.scalajs.js.timers.setTimeout
import scala.util.Failure
import scala.util.Success

@react object Unary {
  case class Props(abort: Boolean)

  val component: FunctionalComponent[Props] = FunctionalComponent { props =>
    val (status, setStatus) = useState("Request pending")

    useEffect(
      () => {
        val req = ApiRequest(payload = "Hello!")
        //TODO add headers to endpoint
        //val metadata: Metadata = Metadata("custom-header-1" -> "unary-value")

        val result = ApiClient.unary(req)
        setStatus("Request sent")

        result.future
          .onComplete {
            case Success(value) =>
              setStatus(s"Request success: ${value.payload}")
            case Failure(ex) =>
              setStatus(s"Request failure: $ex")
          }

        val maybeTimer = if (props.abort) {
          Some(
            setTimeout(1000) {
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
      h2(s"Unary request${if (props.abort) " (with abort)" else ""}:"),
      p(status)
    )
  }
}
