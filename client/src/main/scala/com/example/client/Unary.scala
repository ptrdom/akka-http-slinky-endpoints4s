package com.example.client

import com.example.api.ApiRequest
import com.example.client.App.ApiClient
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.html._

import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import scala.util.Failure
import scala.util.Success

@react object Unary {
  type Props = Unit

  val component: FunctionalComponent[Props] = FunctionalComponent { _ =>
    val (status, setStatus) = useState("Request pending")

    useEffect(
      () => {
        val req = ApiRequest(payload = "Hello!")
        //TODO add headers to endpoint
        //val metadata: Metadata = Metadata("custom-header-1" -> "unary-value")

        //TODO add missing cancellation handle https://github.com/endpoints4s/endpoints4s/issues/977
        ApiClient
          .unary(req)
          .onComplete {
            case Success(value) =>
              setStatus(s"Request success: ${value.payload}")
            case Failure(ex) =>
              setStatus(s"Request failure: $ex")
          }
        setStatus("Request sent")
        () => {
          //TODO use missing cancellation handle
        }
      },
      Seq.empty
    )

    div(
      h2("Unary request:"),
      p(status)
    )
  }
}
