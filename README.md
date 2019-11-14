# StockServer

## Run StockServer

1) Download StockServer as an IntelliJ project and run

or

2) run standalone with sbt

`%> \path\to\java.exe -Dfile.encoding=UTF8 -Djline.terminal=none -Dsbt.log.noformat=true -Dsbt.global.base=C:\Users\wes\AppData\Local\Temp\sbt-global-plugin2stub -Xms512M -Xmx1024M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -classpath \path\to\sbt-launch.jar xsbt.boot.Boot "project stockserver" run`

## Run client
1) Open StockServer\client\client.html

This is a simple javascript/html cient implimentaion taht will connect to the StockerServer running at localhost:9000 via websocket. The user can add/remove stocks from a watchlist that will receive periodic stock prices in real-time through the webspcket.
