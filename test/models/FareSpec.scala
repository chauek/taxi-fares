package models

import models.resources.RidesDAO
import org.scalatestplus.play.PlaySpec
import scala.concurrent.Await
import scala.concurrent.duration._
import java.time.Instant

class FareSpec extends PlaySpec {

  "Fare: calculateFare" should {
    "have minimal fare cost for one point (T1)" in {
      val r = Ride.create(new RidePoint("_testFareCostShortT1", 51.4915756, -0.2031735, Instant.parse("2015-01-02T07:00:00Z").getEpochSecond()*1000))
      //println(r.toString)
      val cost = Fare.calculateFare(r)
      cost mustBe 2.4
    }
    "have valid fare cost for short T1 (T2)" in {
      Ride.create(new RidePoint("_testFareCostShortT1", 51.4915756, -0.2031735, Instant.parse("2015-01-02T07:00:00Z").getEpochSecond()*1000))
      val r = Ride.addRidePoint(new RidePoint("_testFareCostShortT1", 51.5029021, -0.1527106, Instant.parse("2015-01-02T07:00:20Z").getEpochSecond()*1000))
      //println(r.toString)
      val cost = Fare.calculateFare(r)
      cost mustBe 8.0
    }
    "have valid fare cost (T3)" in {
      Ride.create(new RidePoint("_testFareCountingCost", 51.4915756, -0.2031735, Instant.parse("2015-01-02T05:58:00Z").getEpochSecond()*1000))
      Ride.addRidePoint(new RidePoint("_testFareCountingCost", 51.5029021, -0.1527106, Instant.parse("2015-01-02T05:59:00Z").getEpochSecond()*1000))
      Ride.addRidePoint(new RidePoint("_testFareCountingCost", 51.5129021, -0.1027106, Instant.parse("2015-01-02T06:03:00Z").getEpochSecond()*1000))
      val r = Ride.addRidePoint(new RidePoint("_testFareCountingCost", 51.5229021, -0.0527106, Instant.parse("2015-01-02T06:05:00Z").getEpochSecond()*1000))
      //println(r.toString)
      val cost = Fare.calculateFare(r)
      cost mustBe 24.8
    }
  }

  "Fare: sumTariffRidePoints" should {
    "sum multi tariff points with different tariffs properly (T4)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 2, 2000),
        new TariffRidePoint("T1", 2, 2000),
        new TariffRidePoint("T1", 2, 2000),
        new TariffRidePoint("T1", 2, 2000),
        new TariffRidePoint("T1", 2, 2000),
        new TariffRidePoint("T3", 2, 2000),
        new TariffRidePoint("T3", 2, 2000),
        new TariffRidePoint("T3", 2, 2000),
        new TariffRidePoint("T1", 2, 2000),
        new TariffRidePoint("T3", 2, 2000)
      )
      val points = Fare.sumTariffRidePoints(tariffHist)
      points.toString mustBe "List(TariffRidePoint(T1, 10.0, 10000, 1.0), TariffRidePoint(T3, 6.0, 6000, 1.0), TariffRidePoint(T1, 2.0, 2000, 1.0), TariffRidePoint(T3, 2.0, 2000, 1.0))"
    }
  }

  "Fare: calcFareByTariffHist" should {
    "have minimal fare cost for one point (T5)" in {
      val tariffHist = List(new TariffRidePoint("T1", 200, 52100))
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.4
    }
    "have minimal fare cost for two points (T6)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 152.1, 14100),
        new TariffRidePoint("T1", 52.1, 14100)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.4
    }

    // ABOVE MINIMAL
    "have fare cost above minimal for one point; counted by distance (T7)" in {
      val tariffHist = List(new TariffRidePoint("T1", 504.8, 2000))
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.8 // 2.40 + 2*0.20
    }
    "have fare cost above minimal for one point; counted by time (T8)" in {
      val tariffHist = List(new TariffRidePoint("T1", 2, 108400))
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.8 // 2.40 + 2*0.20
    }
    "have fare cost above minimal for two points; counted by distance (T9)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 200.0, 2000),
        new TariffRidePoint("T1", 104.8, 2000)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.6
    }
    "have fare cost above minimal for two points; counted by time (T10)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 2, 50200),
        new TariffRidePoint("T1", 2, 8000)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.6
    }

    // ABOVE Changeover
    "have fare cost above changeover for two points; counted by distance (T11)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 200.0, 20000),
        new TariffRidePoint("T1", 9717.4, 971740)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 18.00
    }
    "have fare cost above changeover for two points; counted by time (T12)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 50.2, 50200),
        new TariffRidePoint("T1", 213.07, 2130700)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 18.60
    }

    // DIFFERENT TARIFFS
    "have fare cost above minimal for two points changing tariff; counted by distance (T13)" in {
      val tariffHist = List(
        new TariffRidePoint("T3", 200.0, 2000),
        new TariffRidePoint("T1", 104.8, 2000)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 2.80
    }
    "have fare cost above changeover for two points changing tariff; counted by distance (T14)" in {
      val tariffHist = List(
        new TariffRidePoint("T1", 9707.4, 970740),
        new TariffRidePoint("T3", 300, 3000)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 18.20
    }
    "have fare cost above minimal for two points changing tariff 3; counted by distance (T15)" in {
      val tariffHist = List(
        new TariffRidePoint("T3", 200.0, 2000),
        new TariffRidePoint("T1", 300, 3000)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 3.20
    }
    "have fare cost above minimal for two points changing tariff 3; counted by distance (T16)" in {
      val tariffHist = List(
        new TariffRidePoint("T3", 9671.9, 96719),
        new TariffRidePoint("T1", 30, 300)
      )
      val cost = Fare.calcFareByTariffRidePoints(tariffHist)
      cost mustBe 25.60
    }

    "clear data in database"  in {
      val r = Await.result(RidesDAO.removeRide("_testFareCostShortT1"), 5 seconds)
      (r > 0) mustBe true
    }
    "clear data in database 2"  in {
      val r = Await.result(RidesDAO.removeRide("_testFareCountingCost"), 5 seconds)
      (r > 0) mustBe true
    }
  }

}
