package com.example.client.utility

import org.scalajs.dom
import org.scalajs.dom.ReadableStreamReader

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.scalajs.js.Thenable.Implicits._
import scala.util.control.NonFatal

object Handlers {

  implicit class FutureReadableStreamHandlerOps[A](
      future: Future[dom.ReadableStream[A]]
  ) {
    def handle(
        onElement: A => Unit = identity(_),
        onComplete: () => Unit = () => (),
        onError: Throwable => Unit = identity(_)
    )(implicit ec: ExecutionContext): Unit = {
      future
        .foreach { stream =>
          def read(
              reader: ReadableStreamReader[A]
          ): Future[Unit] = {
            reader
              .read()
              .flatMap { chunk =>
                if (chunk.done) {
                  onComplete()
                  Future.unit
                } else {
                  onElement(chunk.value)
                  read(reader)
                }
              }
              .recoverWith { case NonFatal(ex) =>
                if (ex.getMessage.startsWith("AbortError:")) {
                  Future.unit
                } else {
                  onError(ex)
                  Future.unit
                }
              }
          }
          read(stream.getReader())
        }
      future.failed.foreach(onError)
      ()
    }
  }
}
