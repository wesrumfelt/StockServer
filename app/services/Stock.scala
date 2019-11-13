package services

import yahoofinance.{Stock, YahooFinance}
import java.math.BigDecimal

class Stock {
    def getStockVal(symbol: String) : BigDecimal = {
      val stock: yahoofinance.Stock = YahooFinance.get (symbol)
      stock.getQuote.getPrice
    }
}