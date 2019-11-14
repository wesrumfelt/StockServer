package services

import java.io.FileNotFoundException

import yahoofinance.{Stock, YahooFinance}
import java.math.BigDecimal

class Stock(val stockId: String) {
    def isValidStock() : Boolean = {
      try {
        val stock: yahoofinance.Stock = YahooFinance.get(stockId)
        return true
      } catch {
        case x: FileNotFoundException => {
          println("Exception: Stock not found")
        }
        return false
      }
    }
    def getStockVal() : BigDecimal = {
      val stock: yahoofinance.Stock = YahooFinance.get(stockId)
      stock.getQuote.getPrice
    }
}