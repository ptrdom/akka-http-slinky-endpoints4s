package com.example.client.utility

import endpoints4s.fetch
import org.scalajs.dom.AbortController
import org.scalajs.dom.Fetch
import org.scalajs.dom.{RequestInit => FetchRequestInit}
import org.scalajs.dom.{Response => FetchResponse}

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.|

//TODO migrate to https://github.com/endpoints4s/endpoints4s/issues/977 once released
trait Endpoints extends fetch.Endpoints with EndpointsWithCustomErrors

trait EndpointsWithCustomErrors extends fetch.EndpointsWithCustomErrors {
  abstract class Result[A](val future: Future[A]) {
    val abort: js.Function0[Unit]
  }

  override def endpoint[A, B](
      request: Request[A],
      response: Response[B],
      docs: EndpointDocs = EndpointDocs()
  ): Endpoint[A, B] = new Endpoint[A, B](request, response) {
    def apply(a: A) = {
      val promise = Promise[B]()

      def mapPartialResponseEntity[A, B](
          entity: ResponseEntity[A]
      )(f: A => Either[Throwable, B]): ResponseEntity[B] =
        response =>
          entity(response).`then`((responseEntity: Either[Throwable, A]) =>
            responseEntity.flatMap(f): Either[Throwable, B] | js.Thenable[
              Either[Throwable, B]
            ]
          )

      val requestData = request(a)
      val requestInit = new FetchRequestInit {}
      requestInit.method = requestData.method
      requestData.prepare(requestInit)
      requestData.entity(requestInit)
      val abortController = new AbortController
      requestInit.signal = abortController.signal
      val f = Fetch.fetch(
        settings.baseUri.getOrElse("") + request.href(a),
        requestInit
      )
      f.`then`(
        (fetchResponse: FetchResponse) => {
          val maybeResponse = response(fetchResponse)

          def maybeClientErrors =
            clientErrorsResponse(fetchResponse)
              .map(
                mapPartialResponseEntity[ClientErrors, B](_)(clientErrors =>
                  Left(
                    new Exception(
                      clientErrorsToInvalid(clientErrors).errors.mkString(
                        ". "
                      )
                    )
                  )
                )
              )

          def maybeServerError =
            serverErrorResponse(fetchResponse).map(
              mapPartialResponseEntity[ServerError, B](_)(serverError =>
                Left(serverErrorToThrowable(serverError))
              )
            )

          maybeResponse
            .orElse(maybeClientErrors)
            .orElse(maybeServerError) match {
            case None =>
              promise.failure(
                new Exception(
                  s"Unexpected response status: ${fetchResponse.status}"
                )
              )
              js.Promise.resolve[Unit](()): Unit | js.Thenable[Unit]
            case Some(entityB) =>
              entityB(fetchResponse)
                .`then`(
                  (v: Either[Throwable, B]) => {
                    v.fold(
                      promise.failure,
                      promise.success
                    )
                    (): Unit | js.Thenable[Unit]
                  },
                  js.defined((e: Any) => {
                    e match {
                      case th: Throwable => promise.failure(th)
                      case _             => promise.failure(js.JavaScriptException(e))
                    }
                    (): Unit | js.Thenable[Unit]
                  }): js.UndefOr[js.Function1[Any, Unit | js.Thenable[Unit]]]
                ): Unit | js.Thenable[Unit]
          }
        },
        js.defined { (e: Any) =>
          e match {
            case th: Throwable => promise.failure(th)
            case _             => js.JavaScriptException(e)
          }
          (): Unit | js.Thenable[Unit]
        }: js.UndefOr[js.Function1[Any, Unit | js.Thenable[Unit]]]
      )
      new Result(promise.future) { val abort = () => abortController.abort() }
    }
  }
}
