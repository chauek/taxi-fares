package models

import models.resources.RidesDAO
import org.scalatestplus.play.PlaySpec
import scala.concurrent.duration._
import scala.concurrent.Await
import java.time.Instant


class RideSpec extends PlaySpec {

  val testRideId = "_testForBorderTariffPoints"

  "RideModel: Ride" should {
    "have valid tariff break point on position 1" in {
      Ride.create(new RidePoint(testRideId, 51.4915756, -0.2031735, Instant.parse("2015-01-02T05:59:00Z").getEpochSecond()*1000))
      val r = Ride.addRidePoint(new RidePoint(testRideId, 51.5029021, -0.1527106, Instant.parse("2015-01-02T06:01:00Z").getEpochSecond()*1000))
      r.points(1).toString mustBe new RidePoint(testRideId, 51.49723885, -0.17794205000000002, Instant.parse("2015-01-02T06:00:00.000Z").getEpochSecond()*1000, 1856.7925703435471, 60000).toString
    }
    "not have second break point" in {
      val r = Ride.addRidePoint(new RidePoint(testRideId, 51.5129021, -0.1027106, Instant.parse("2015-01-02T06:03:00Z").getEpochSecond()*1000))
      r.points.length mustBe 4
    }
    "have 2 break points" in {
      Ride.addRidePoint(new RidePoint(testRideId, 51.5229021, -0.0527106, Instant.parse("2015-01-02T19:50:00Z").getEpochSecond()*1000))
      val r = Ride.addRidePoint(new RidePoint(testRideId, 51.5329021, -0.1027106, Instant.parse("2015-01-02T22:10:00Z").getEpochSecond()*1000))
      r.points.length mustBe 8
    }
    "have 1 break point after midnight" in {
      val r = Ride.addRidePoint(new RidePoint(testRideId, 51.5429021, -0.0527106, Instant.parse("2015-01-03T06:50:00Z").getEpochSecond()*1000))
      r.points.length mustBe 10
    }
    "clear data in database"  in {
      val r = Await.result(RidesDAO.removeRide(testRideId), 5 seconds)
      (r > 0) mustBe true
    }
  }
}
