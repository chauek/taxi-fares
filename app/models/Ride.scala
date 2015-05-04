package models

import scala.math._

object Ride {

  def calculateDistance(ride: List[RidePoint]): Double = ride match {
    case List() => 0.0
    case List(x) => 0.0
    case x :: xs => calculateDistanceBetween2Points(x.getPosTuple, xs.head.getPosTuple) + calculateDistance(xs)
  }

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
