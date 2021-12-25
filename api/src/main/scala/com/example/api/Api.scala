package com.example.api

import endpoints4s.algebra
import io.circe.generic.auto._

trait Api
    extends algebra.Endpoints
    with algebra.circe.JsonEntitiesFromCodecs
    with algebra.ChunkedJsonEntities {

  val unary: Endpoint[ApiRequest, ApiResponse] = endpoint(
    post(path / "unary", jsonRequest[ApiRequest]),
    ok(jsonResponse[ApiResponse])
  )

  val serverStreaming: Endpoint[ApiRequest, Chunks[ApiResponse]] = endpoint(
    post(path / "server-streaming", jsonRequest[ApiRequest]),
    ok(jsonChunksResponse[ApiResponse])
  )
}
