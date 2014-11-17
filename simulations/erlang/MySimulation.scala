package erlang

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class MySimulation extends Simulation {

  val httpProtocol = http
    .baseURLs("http://192.168.48.1:8080", "http://192.168.48.2:8080", "http://192.168.48.3:8080", "http://192.168.48.4:8080",
    "http://192.168.48.5:8080", "http://192.168.48.6:8080", "http://192.168.48.7:8080", "http://192.168.48.8:8080")
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("""text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3""")
    .connection("""keep-alive""")
    .userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0""")

    val headers_0 = Map("""Cache-Control""" -> """max-age=0""")

    val uri1 = """spitfire.tique.grp"""

    val scn = scenario("MySimulation")
    .exec(http("request_0")
        .get("""/""")
        .headers(headers_0))
        .pause(15)

    val nbUsers = Integer.getInteger("users", 1)
    val myRamp  = java.lang.Long.getLong("ramp", 0L)
    // setUp(scn.inject(rampUsers(nbUsers) over (myRamp seconds))).protocols(httpProtocol)
    setUp(scn.inject(rampUsers(nbUsers) over (myRamp seconds)))
    .protocols(httpProtocol)
//    .throttle(reachRps(10000) in (10 seconds))
}
