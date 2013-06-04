package hu.jupi.play.authentication

import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class WithAuthentication(private val request: Request[AnyContent]) extends WrappedRequest(request)
case class Authenticated[A](principal: A)(implicit private val request: Request[AnyContent]) extends WithAuthentication(request)
case class Guest(implicit private val request: Request[AnyContent]) extends WithAuthentication(request)

trait Authentication[A] {
  def principal(requestHeader: RequestHeader): Future[Option[A]]
  def WithAuthentication(f: WithAuthentication => Result) = Action {
    implicit request => AsyncResult {
      principal(request) map (p => p match {
        case Some(p) => f(Authenticated(p))
        case _ => f(Guest())
      })
    }
  }
}
