package controllers

import java.time.Instant

import play.api.libs.json.Json

import scala.concurrent.Future

import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class RidesControllerSpec extends PlaySpec with Results {

  class TestController(val uuid: String, val time: Long) extends Controller with RidesController {
    def this(uuid: String)  = this(uuid, new java.util.Date().getTime())
    def this()  = this("aabbccdd")
    def getUUID: String = uuid
    override def getCurrentTimestamp = time
  }

  def addRidePoint(requestType: String, uuid: String, lat: Double, lon: Double, date: String): String = {
    val controller = new TestController(uuid, Instant.parse(date).getEpochSecond())
    val request = FakeRequest(requestType, "/rides")
    val result: Future[Result] = if (requestType == "GET") controller.startTracking(lat, lon).apply(request)
    else controller.addRidePoint(uuid, lat, lon).apply(request)
    contentAsString(result)
  }

  "RidesController#index" should {
    "have valid response" in {
      val controller = new TestController()
      val result: Future[Result] = controller.index.apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText mustBe "{\"response\":\"index\"}"
    }
  }
  "RidesController#startTracking" should {
    "have valid response" in {
      addRidePoint("GET", "aabbccdd", 51.4915756, -0.2031735, "2015-01-01T12:00:00Z") matches
        """\{\"rideId\":\"([^\"]*)\"\}"""
    }
  }
  "RidesController#addRidePoint" should {
    "have valid response" in {
      addRidePoint("PUT", "aabbccdd", 51.5029021, -0.1527106, "2015-01-01T12:01:00Z") mustBe
        Json.obj(
          "response" -> "OK",
          "distance" -> 3713,
          "time" -> 60
        ).toString()
    }
    "have valid distace at 4 points" in {
      addRidePoint("PUT", "aabbccdd", 51.5129021, -0.1027106, "2015-01-01T12:02:00Z")
      addRidePoint("PUT", "aabbccdd", 51.5229021, -0.0527106, "2015-01-01T12:03:00Z") mustBe
        Json.obj(
          "response" -> "OK",
          "distance" -> 10982,
          "time" -> 180
        ).toString()
    }
  }
}