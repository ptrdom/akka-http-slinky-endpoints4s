package com.example.client

import com.example.api.Api
import com.example.client.utility.ChunkedEntities
import com.example.client.utility.ChunkedJsonEntities
import endpoints4s.fetch
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
      Unary(),
      Stream(cancel = false),
      Stream(cancel = true)
    )
  }
}

object App {

  object ApiClient
      extends Api
      with fetch.future.Endpoints
      with fetch.JsonEntitiesFromSchemas
      with ChunkedEntities
      with ChunkedJsonEntities {

    implicit def ec: ExecutionContext =
      org.scalajs.macrotaskexecutor.MacrotaskExecutor

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
