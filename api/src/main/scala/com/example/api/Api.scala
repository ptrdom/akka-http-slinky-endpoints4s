package com.example.api

import endpoints4s.{algebra, generic}

trait Api
    extends algebra.Endpoints
    with algebra.JsonEntitiesFromSchemas
    with generic.JsonSchemas
    with algebra.ChunkedJsonEntities {

  val unary: Endpoint[ApiRequest, ApiResponse] = endpoint(
    post(path / "unary", jsonRequest[ApiRequest]),
    ok(jsonResponse[ApiResponse])
  )

  val serverStreaming: Endpoint[ApiRequest, Chunks[ApiResponse]] = endpoint(
    post(path / "server-streaming", jsonRequest[ApiRequest]),
    ok(jsonChunksResponse[ApiResponse])
  )

  implicit lazy val apiRequestSchema: JsonSchema[ApiRequest] = genericJsonSchema
  implicit lazy val apiResponseSchema: JsonSchema[ApiResponse] =
    genericJsonSchema
}
