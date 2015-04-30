package controllers

import scala.concurrent.Future

import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class FaresControllerSpec extends PlaySpec with Results {

  class TestController() extends Controller with FaresController {
    def getUUID: String = "aaa"
  }

  "Taxi Fares#index" should {
    "should be valid" in {
      val controller = new TestController()
      val result: Future[Result] = controller.index.apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText mustBe "{\"response\":\"index\"}"
    }
  }
  "Taxi Fares#fareGET" should {
    "should be valid" in {
      val controller = new TestController()
      val request = FakeRequest().withHeaders(
        ("Date", "2014-10-05T22:00:00")
      )
      val result: Future[Result] = controller.startTracking.apply(request)
      val bodyText: String = contentAsString(result)
      bodyText matches """\{\"fareId\":\"([^\"]*)\"\}"""
    }
  }
  "Taxi Fares#farePUT" should {
    "should be valid" in {
      val controller = new TestController()
      val request = FakeRequest("PUT", "/fare").withHeaders(
        ("Date", "2014-10-05T22:00:00")
      )
      val result: Future[Result] = controller.addFarePoint("a1a1").apply(request)
      val bodyText: String = contentAsString(result)
      bodyText mustBe "{\"response\":\"OK\"}"
    }
  }
}