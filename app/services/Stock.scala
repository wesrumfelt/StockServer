package services

import java.io.FileNotFoundException

import yahoofinance.{Stock, YahooFinance}
import java.math.BigDecimal

class Stock(val stockId: String) {
  //TODO : utilize dependency injection to inject the yahoo finance
    def isValidStock() : Boolean = {
      try {
        val stock: yahoofinance.Stock = YahooFinance.get(stockId)
      } catch {
        case x: FileNotFoundException => {
          println("Exception: Stock not found")
        }
        return false
      }
      return true
    }
    def getStockVal() : BigDecimal = {
      val stock: yahoofinance.Stock = YahooFinance.get(stockId)
      stock.getQuote.getPrice
    }
}