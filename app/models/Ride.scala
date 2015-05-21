package models

import models.resources.{FaresResource, RidesDAO}
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.math._

class Ride(val points: List[RidePoint]) {
  override def toString = "Ride(\n" + points.mkString("\n") + "\n)"
}


object Ride {

  def create(ridePoint: RidePoint) : Ride = {
    Await.result(RidesDAO.addRidePoint(ridePoint), 5 seconds)
    new Ride(List(ridePoint))
  }

  def addRidePoint(ridePoint: RidePoint): Ride = {
    val ridePoints = Await.result(RidesDAO.findRidePoints(ridePoint.rideId), 5 seconds).toList ++: List(ridePoint)
    val ride = calculateTimeDiffs(calculatePartialDistances(addTariffBorderPoints(new Ride(ridePoints))))
    Await.result(RidesDAO.addRidePoint(ride.points.last), 5 seconds)
    ride
  }

  private def generateTariffBorderPoint(p1: RidePoint, p2: RidePoint, time: Long): RidePoint = {
    val dt1 = (p2.time - p1.time).toDouble
    val dt3 = (p2.time - time).toDouble
    new RidePoint(p1.rideId, p2.lat-(dt3/dt1)*(p2.lat-p1.lat), p2.lon-(dt3/dt1)*(p2.lon-p1.lon), time, 0.0, 0)
  }

  private def addTariffBorderPoints(ride: Ride): Ride = {
    def addTariffBorders(points: List[RidePoint]): List[RidePoint] = points match {
      case List() => points
      case List(x) => points
      case x :: xs => x :: (
          (
            for (t <- FaresResource.findBorderPointsBetween(x.time, xs.head.time))
              yield generateTariffBorderPoint(x, xs.head, t)
            ) ++
          addTariffBorders(xs)
        )
    }
    new Ride(addTariffBorders(ride.points))
  }


  private def calculatePartialDistances(ride: Ride): Ride = {
    def getPointWithDistanceFrom(to: RidePoint, from: RidePoint): List[RidePoint] = {
      if (to.distance > 0) List(to)
      else List(to.setDistanceFrom(from))
    }
    def partialDistances(points: List[RidePoint]): List[RidePoint] = points match {
        case List() => points
        case List(x) => points
        case x :: xs => x :: partialDistances(getPointWithDistanceFrom(xs.head, x) ++ xs.tail)
      }
    new Ride(partialDistances(ride.points))
  }

  def calculateDistance(ride: List[RidePoint]): Double = ride match {
    case List() => 0.0
    case List(x) => 0.0
    case x :: xs => (
        if (xs.head.distance > 0) xs.head.distance
        else calculateDistanceBetween2Points(x.getPosTuple, xs.head.getPosTuple)
      ) +
      calculateDistance(xs)
  }

  def calculateTimeDiffs(ride: Ride): Ride = {
    def calculateTimeDiffsOnList(points: List[RidePoint]): List[RidePoint] = points match {
      case List() => points
      case List(x) => points
      case x :: xs => x :: calculateTimeDiffsOnList(xs.head.setTimeDiff(x) :: xs.tail)
    }
    new Ride(calculateTimeDiffsOnList(ride.points))
  }

  def calculateTimeDiff(p1: RidePoint, p2: RidePoint): Long = p2.time - p1.time

  def calculateTime(ride: List[RidePoint]) = {
    ride.last.time - ride.head.time
  }

  private def toRad(d: Double): Double = d * Pi / 180

  /**
   * Earth radius in m
   * @return Int
   */
  private def earthRadius = 6371000

  /**
   *
   * @param p1 Tuple (latitude, longitude)
   * @param p2 Tuple (latitude, longitude)
   * @return Distance in m
   */
  def calculateDistanceBetween2Points(p1: (Double, Double), p2: (Double, Double)): Double = {
    val dLat = toRad(p2._1 - p1._1)
    val dLon = toRad(p2._2 - p1._2)

    val a = sin(dLat / 2) * sin(dLat / 2) +
      cos(toRad(p1._1)) * cos(toRad(p2._1)) *
        sin(dLon / 2) * sin(dLon / 2)

    earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))
  }
}
