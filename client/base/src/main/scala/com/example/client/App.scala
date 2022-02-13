package com.example.client

import com.example.api.Api
import endpoints4s.fetch
import org.scalajs.macrotaskexecutor.MacrotaskExecutor
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.web.html._

import scala.concurrent.ExecutionContext
import scala.scalajs.LinkingInfo

@react class App extends StatelessComponent {
  type Props = Unit

  def render() = {
    Fragment(
      h1("Hello world!"),
      Unary(abort = false),
      Unary(abort = true),
      Stream(abort = false),
      Stream(abort = true)
    )
  }
}

object App {

  implicit def globalExecutionContext: ExecutionContext = MacrotaskExecutor

  object ApiClient
      extends Api
      with fetch.future.Endpoints
      with fetch.JsonEntitiesFromCodecs
      with fetch.ChunkedJsonResponseEntities {

    implicit def ec: ExecutionContext = globalExecutionContext

    lazy val settings: fetch.EndpointsSettings = fetch
      .EndpointsSettings()
      .withBaseUri(
        if (LinkingInfo.developmentMode) {
          Some("http://localhost:9000")
        } else {
          None
        }
      )
  }
}
