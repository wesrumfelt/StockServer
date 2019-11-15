package services

import java.io.FileNotFoundException

import yahoofinance.{Stock, YahooFinance}
import java.math.BigDecimal
import org.slf4j.{Logger, LoggerFactory}

class Stock(val stockId: String) {
  //TODO : utilize dependency injection to inject the yahoo finance
    def log : Logger = LoggerFactory.getLogger(classOf[Stock])
    def isValidStock() : Boolean = {
      try {
        val stock: yahoofinance.Stock = YahooFinance.get(stockId)
      } catch {
        case x: FileNotFoundException => {
          log.warn(s"Exception: Stock ($stockId) not found")
        }
        return false
      }
      return true
    }
    def getStockVal() : BigDecimal = {
      log.info(s"Getting stock val for $stockId")
      val stock: yahoofinance.Stock = YahooFinance.get(stockId)
      stock.getQuote.getPrice
    }
}