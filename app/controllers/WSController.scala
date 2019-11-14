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
import scala.collection._

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
  val stockList = mutable.MutableList[Stock]()

  override def receive: Receive = {
    case jsMsg: JsValue =>
      var jsonOut: JsValue = null
      val userID: String = (jsMsg \ "userID").as[String]
      val action: String = (jsMsg \ "action").as[String]
      if (action.equalsIgnoreCase("addStock")) {
        val stockID: String = (jsMsg \ "stockID").as[String]
        val stock: Stock = new Stock(stockID)
        if (!stock.isValidStock()) {
          jsonOut = JsObject(Seq("error" -> JsString("Invalid stockID")))
        } else {
          stockList += stock
          val stockVal = stock.getStockVal()

          jsonOut = JsObject(Seq(
            "stockID" -> JsString(stockID),
            "stockVal" -> JsNumber(stockVal)
          ))

          val system = akka.actor.ActorSystem("system")
          import system.dispatcher
          import scala.concurrent.duration._
          context.system.scheduler.schedule(5 seconds, 5000.millis, self, SendLatestMessage)(context.system.dispatcher)
        }
      }
      out ! jsonOut
    case msg: String =>
      out ! ("I received your message: " + msg)
    case SendLatestMessage =>
      var jsonOut: JsValue = null
      var bufJson = new JsArray()
      // TODO: Use object to dymanically build json return
      var jsonString: String = """ { "stockIds": ["""
      stockList.foreach(stock => {
        println("stock" + stock.stockId)
        var stockId = stock.stockId
        var stockVal = stock.getStockVal()
        jsonString += s""" {
          "stockID": "$stockId",
          "stockVal": $stockVal
          },"""
      })
      jsonString = jsonString.dropRight(1)
      jsonString += "] }"
      println(jsonString)
      jsonOut = Json.parse(jsonString);
      out ! jsonOut
  }
}
case object SendLatestMessage
//case class Message()
//object scheduleActor {
//  def props(out: ActorRef) = Props(new scheduleActor(out))
//}
//class scheduleActor(out: ActorRef) extends Actor {
//  //def receive = { case Message() => println("Do something in actor") }
//  override def receive: Receive = {
//    case msg: String =>
//    out ! ("I received your message: ")
//  }
//}


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