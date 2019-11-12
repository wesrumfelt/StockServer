package controllers

import javax.inject._
import play.api.mvc._
import services.Counter
import yahoofinance.{Stock, YahooFinance}

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class creates an
 * `Action` that shows an incrementing count to users. The [[Counter]]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class StockController @Inject()(cc: ControllerComponents,
                                counter: Counter) extends AbstractController(cc) {

  /**
   * Create an action that responds with the [[Counter]]'s current
   * count. The result is plain text. This `Action` is mapped to
   * `GET /count` requests by an entry in the `routes` config file.
   */

  import java.math.BigDecimal

  val stock: Stock = YahooFinance.get("LUV")

  val price: BigDecimal = stock.getQuote.getPrice
  val change: BigDecimal = stock.getQuote.getChangeInPercent
  val peg: BigDecimal = stock.getStats.getPeg
  val dividend: BigDecimal = stock.getDividend.getAnnualYieldPercent

  def count = Action { Ok(price.toString) }
}
