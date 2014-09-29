package controllers

import akka.actor.{PoisonPill, Actor, Props, ActorRef}
import org.jsoup.Jsoup
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent._
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._
import redis.RedisClient
import scala.concurrent.ExecutionContext.Implicits.global

case class FetchingRequest(url: String, ref: ActorRef)

case class OpenGraphRequest(urls: List[String])

object OpenGraphRequest {
  implicit val openGraphRequestJsonFormat = Json.format[OpenGraphRequest]
  implicit val openGraphRequestFrameFormat = FrameFormatter.jsonFrame[OpenGraphRequest]
}

case class OpenGraph(title: Option[String], `type`: Option[String], url: Option[String], image: Option[String])

object OpenGraph {
  implicit val openGraphJsonFormat = Json.format[OpenGraph]
  implicit val openGraphFrameFormat = FrameFormatter.jsonFrame[OpenGraph]
}

case class OpenGraphResponse(url: String, openGraph: OpenGraph)

object OpenGraphResponse {
  implicit val openGraphResponseJsonFormat = Json.format[OpenGraphResponse]
  implicit val openGraphResponseFrameFormat = FrameFormatter.jsonFrame[OpenGraphResponse]
}

object OpenGraphFetcher extends Controller {
  def index(login: String, authToken: String) = WebSocket.tryAcceptWithActor[OpenGraphRequest, OpenGraphResponse] { request =>
    redis.get(s"auth:token:$authToken").map {
      case Some(l) if l.utf8String == login  => Right(WebSocketHandlerActor.props)
      case _ => Left(Forbidden("You shall not pass!"))
    }
  }

  private implicit val actorSystem = Akka.system
  private val redis = RedisClient(host = "localhost", port = 6379)
}

object WebSocketHandlerActor {
  def props(out: ActorRef) = Props(new WebSocketHandlerActor(out))
}

class WebSocketHandlerActor(out: ActorRef) extends Actor {
  override def receive: Receive = {
    case request: OpenGraphRequest =>
      request.urls.foreach { url =>
        Akka.system.actorOf(Props[OpenGraphFetchingActor]) ! FetchingRequest(url, out)
      }
  }

  override def preStart() = Logger.info(s"Starting actor $self")

  override def postStop() = Logger.info(s"Stopped actor $self")
}

class OpenGraphFetchingActor extends Actor {
  override def receive: Receive = {
    case request: FetchingRequest =>
      WS.url(request.url).get().map { response =>
        val og = getOpenGraphFromBody(response.body)
        request.ref ! OpenGraphResponse(request.url, og)
      }
      self ! PoisonPill
  }

  override def preStart() = Logger.info(s"Starting actor $self")

  override def postStop() = Logger.info(s"Stopped actor $self")

  private def getOpenGraphFromBody(body: String): OpenGraph = {
    val metaElements = Jsoup.parse(body).getElementsByTag("meta")
    val metaTitle = Option(metaElements.select("meta[property=og:title]").first()).map(_.attr("content"))
    val metaType = Option(metaElements.select("meta[property=og:type]").first()).map(_.attr("content"))
    val metaUrl = Option(metaElements.select("meta[property=og:url]").first()).map(_.attr("content"))
    val metaImage = Option(metaElements.select("meta[property=og:image]").first()).map(_.attr("content"))
    OpenGraph(metaTitle, metaType, metaUrl, metaImage)
  }
}
