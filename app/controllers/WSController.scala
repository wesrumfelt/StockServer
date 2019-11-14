package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.json._
import akka.actor._
import services.Stock
import scala.collection._
import scala.concurrent.duration._
import play.api.Logger

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
  var stockList = mutable.ListBuffer[Stock]()

  override def receive: Receive = {
    case jsMsg: JsValue =>
      var jsonOut: JsValue = null
      val userID: String = (jsMsg \ "userId").as[String]
      val action: String = (jsMsg \ "action").as[String]
      if (action.equalsIgnoreCase("addStock")) {
        val stockId: String = (jsMsg \ "stockId").as[String]
        val stock: Stock = new Stock(stockId)
        if (!stock.isValidStock()) {
          jsonOut = JsObject(Seq("error" -> JsString("Invalid stockId")))
        } else {
          stockList += stock
          val stockVal = stock.getStockVal()

          jsonOut = JsObject(Seq(
            "stockId" -> JsString(stockId),
            "stockVal" -> JsNumber(stockVal),
            "status" -> JsString("added")
          ))

          val system = akka.actor.ActorSystem("system")
          context.system.scheduler.schedule(5 seconds, 5000.millis, self, SendLatestMessage)(context.system.dispatcher)
        }
      } else if (action.equalsIgnoreCase("removeStock")) {
        val stockId: String = (jsMsg \ "stockId").as[String]
        val newStockList = mutable.ListBuffer[Stock]()
        // filter doesn't seem to be allowed for a List of objects
        stockList.foreach(stock => {
          if (stockId != stock.stockId) {
            newStockList += stock
          }
        })
        stockList = newStockList;
        jsonOut = JsObject(Seq(
          "stockId" -> JsString(stockId),
          "status" -> JsString("removed")
        ))
      } else {
        jsonOut = JsObject(Seq("error" -> JsString("Unknown action")))
      }
      out ! jsonOut
    case msg: String =>
      out ! ("I received your message: " + msg)
    case SendLatestMessage =>
      var jsonOut: JsValue = null
      var bufJson = new JsArray()
      // TODO: Use object to dymanically build json return
      var jsonString: String = """ { "stockIds": ["""
      if (!stockList.isEmpty) {
        stockList.foreach(stock => {
          var stockId = stock.stockId
          var stockVal = stock.getStockVal()
          jsonString +=
            s""" {
          "stockId": "$stockId",
          "stockVal": $stockVal
          },"""
        })
        jsonString = jsonString.dropRight(1)
      }
      jsonString += "] }"
      jsonOut = Json.parse(jsonString);
      out ! jsonOut
  }
}
case object SendLatestMessage