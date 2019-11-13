package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.libs.json._
import akka.actor._
import services.Stock

class WSController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  def socket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef { out =>
      messageActor.props(out)
    }
  }
}

object messageActor {
  def props(out: ActorRef) = Props(new messageActor(out))
}

class messageActor(out: ActorRef) extends Actor {

  override def receive: Receive = {
    case jsMsg: JsValue =>
//      val jsonString = """{
//      "hits": "hiiiiiiiiiiiiii"
//      }"""
      val stockID = (jsMsg \ "stockID").as[String]
      val stock: Stock = new Stock()
      val stockVal = stock.getStockVal(stockID)

      val json: JsValue = JsObject(Seq(
        "stockID" -> JsString(stockID),
        "stockVal" -> JsNumber(stockVal)
      ))

      //val msg = f"""{ "stockVal": $stockVal }"""
      //val s = """{ "stockVal": %d }""".format(stockVal)
//      out ! Json.parse(msg)
      out ! json
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}


//class WSController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
//  extends AbstractController(cc) {
//
//  def socket = WebSocket.accept[String, String] { request =>
//    ActorFlow.actorRef { out =>
//      MyWebSocketActor.props(out)
//    }
//  }
//}
//
//object MyWebSocketActor {
//  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
//}
//
//class MyWebSocketActor(out: ActorRef) extends Actor {
//  def receive = {
//    case msg: String =>
//      out ! ("I received your message: " + msg)
//  }
//}