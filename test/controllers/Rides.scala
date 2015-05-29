package controllers

import models.resources.RidesDAO
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import java.time.Instant

class RidesControllerSpec extends PlaySpec with Results {

  class TestController(val uuid: String, val time: Long) extends Controller with RidesController {
    def this(uuid: String)  = this(uuid, new java.util.Date().getTime())
    def this()  = this("_test_aabbccdd")
    def getUUID: String = uuid
    override def getCurrentTimestamp = time
  }

  def addRidePointRequest(requestType: String, uuid: String, lat: Double, lon: Double, date: String): String = {
    val controller = new TestController(uuid, Instant.parse(date).getEpochSecond() * 1000)
    val request = FakeRequest(requestType, "/rides")
    val result: Future[Result] = if (requestType == "START") controller.startTracking(lat, lon).apply(request)
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
      addRidePointRequest("START", "_testForRidesController", 51.4915756, -0.2031735, "2015-01-01T19:00:00Z") matches
        """\{\"rideId\":\"([^\"]*)\"\}"""
    }
  }
  "RidesController#addRidePoint" should {
    "have valid response" in {
      addRidePointRequest("ADD", "_testForRidesController", 51.5029021, -0.1527106, "2015-01-01T19:01:00Z") mustBe
        Json.obj(
          "response" -> "OK",
          "distance" -> 3713,
          "time" -> 60000,
          "fare" -> 8.0
        ).toString()
    }
    "have valid distace at 4 points" in {
      addRidePointRequest("ADD", "_testForRidesController", 51.5129021, -0.1027106, "2015-01-01T19:02:00Z")
      addRidePointRequest("ADD", "_testForRidesController", 51.5229021, -0.0527106, "2015-01-01T19:03:00Z") mustBe
        Json.obj(
          "response" -> "OK",
          "distance" -> 10982,
          "time" -> 180000,
          "fare" -> 20.6
        ).toString()
    }
    "have valid distace at 6 points with tariff border point" in { //  it should add tariff border as 6th point at 20:00"
      addRidePointRequest("ADD", "_testForRidesController", 51.5329021, -0.1027106, "2015-01-01T19:04:00Z")
      addRidePointRequest("ADD", "_testForRidesController", 51.5429021, -0.0527106, "2015-01-01T20:01:00Z") mustBe
        Json.obj(
          "response" -> "OK",
          "distance" -> 18247,
          "time" -> 3660000,
          "fare" -> 64.6
        ).toString()
    }
    "clear data in database 2"  in {
      val r = Await.result(RidesDAO.removeRide("_testForRidesController"), 5 seconds)
      (r > 0) mustBe true
    }
  }
}