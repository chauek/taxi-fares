package models

import models.resources.FaresResource

class TariffRidePoint (
                  val tariff: String,
                  val distance: Double, // meters
                  val time: Long,       // millisec
                  val speed: Double     // m/s
                        ) {

  def this(tariff: String, distance: Double, time: Long) = this(tariff, distance, time, distance/time*1000)

  override def toString =
    "TariffRidePoint(" + tariff + ", " + distance.toString + ", " + time.toString + ", " + speed.toString + ")"
}

object Fare {
  def calculateFare(ride: Ride): Double = {
    val tariffRidePoints = sumTariffRidePoints(getTariffHistory(ride))
    calcFareByTariffRidePoints(tariffRidePoints)
  }

  def calcFareByTariffRidePoints(points: List[TariffRidePoint]): Double = {
    (calcMinimalFare(points, 0.0, 0.0, 0)*100).ceil/100 // to xxx.xx
  }

  protected def calcMinimalFare(points: List[TariffRidePoint], cost: Double, distance: Double, time: Long): Double = {
    //      println("calcMinimalFare: cost: "+cost+" , distance: "+ distance+" , time: "+time)
    //      println(runtime.ScalaRunTime.stringOf(points))
    if (points.isEmpty) return cost

    val current = points.head
    val minFare = FaresResource.getMinimalFareForTariff(current.tariff)

    // the point stays inside the minimal fare
    if ((distance + current.distance) <= minFare.min_dist && (time + current.time) <= minFare.min_time) {
      calcMinimalFare(points.tail, minFare.min_cost, distance + current.distance, time + current.time)
    }
    else {
      // breaking minimal cost point
      if (isChargedPerDistance(current.speed)) {
        // count by distance
        val newTRPDistance = current.distance-(minFare.min_dist-distance)
        calcFareByDistance(points, distance, time, minFare.min_cost, newTRPDistance, calcMediumFare _)
      }
      else {
        // count by time
        val newTRPTime = (current.time-(minFare.min_time-time)).toInt
        calcFareByTime(points, distance, time, minFare.min_cost, newTRPTime, calcMediumFare _)
      }
    }
  }

  protected def calcMediumFare(points: List[TariffRidePoint], cost: Double, distance: Double, time: Long): Double = {
    //      println("calcMediumFare: cost: "+cost+" , distance: "+ distance+" , time: "+time)
    //      println(runtime.ScalaRunTime.stringOf(points))
    if (points.isEmpty) return cost

    val current = points.head
    val medFare = FaresResource.getMediumFareForTariff(current.tariff)

    val pointMaxDistanceCost = ((current.distance/medFare.charge_dist).ceil * medFare.charge_price)
    val pointMaxTimeCost = ((current.time.toDouble/medFare.charge_time).ceil * medFare.charge_price)

    // the point stays inside the maximal cost
    if (cost + pointMaxDistanceCost < medFare.max_cost && cost + pointMaxTimeCost < medFare.max_cost) {
      val newCost = cost + (if (pointMaxDistanceCost >= pointMaxTimeCost) pointMaxDistanceCost else pointMaxTimeCost)
      calcMediumFare(points.tail, newCost, distance + current.distance, time + current.time)
    }
    else {
      // breaking overcharge point
      if (isChargedPerDistance(current.speed)) {
        // count by distance
        val newTRPDistance = ((((cost + pointMaxDistanceCost - medFare.max_cost) / pointMaxDistanceCost) * current.distance)*10).round / 10 // round to xxx.x
        calcFareByDistance(points, distance, time, medFare.max_cost, newTRPDistance, calcChangeoverFare _)
      }
      else {
        // count by time
        val newTRPTime = (((cost + pointMaxTimeCost - medFare.max_cost) / pointMaxTimeCost) * current.time).toInt
        calcFareByTime(points, distance, time, medFare.max_cost, newTRPTime, calcChangeoverFare _)
      }
    }
  }

  protected def calcChangeoverFare(points: List[TariffRidePoint], cost: Double, distance: Double, time: Long): Double = {
    //      println("calcChangeoverFare: cost: "+cost+" , distance: "+ distance+" , time: "+time)
    //      println(runtime.ScalaRunTime.stringOf(points))
    if (points.isEmpty) return cost

    val current = points.head
    val changeoverFare = FaresResource.getChangeoverFare

    val pointMaxDistanceCost = ((current.distance/changeoverFare.charge_dist).ceil * changeoverFare.charge_price)
    val pointMaxTimeCost = ((current.time.toDouble/changeoverFare.charge_time).ceil * changeoverFare.charge_price)

    val newCost = cost + (if (pointMaxDistanceCost >= pointMaxTimeCost) pointMaxDistanceCost else pointMaxTimeCost)

    calcChangeoverFare(points.tail, newCost , distance + current.distance, time + current.time)
  }

  protected def calcFareByDistance(points: List[TariffRidePoint], distance: Double, time: Long, cost: Double, newTRPDistance: Double, callback: (List[TariffRidePoint], Double, Double, Long) => Double): Double = {
    val current = points.head
    val newTRPTime = (newTRPDistance / current.speed * 1000).toInt
    calcFareByDistanceAndTime(points, distance, time, cost, newTRPDistance, newTRPTime, callback)
  }

  protected def calcFareByTime(points: List[TariffRidePoint], distance: Double, time: Long, cost: Double, newTRPTime: Int, callback: (List[TariffRidePoint], Double, Double, Long) => Double): Double = {
    val current = points.head
    val newTRPDistance = current.speed * newTRPTime / 1000
    calcFareByDistanceAndTime(points, distance, time, cost, newTRPDistance, newTRPTime, callback)
  }

  protected def calcFareByDistanceAndTime(points: List[TariffRidePoint], distance: Double, time: Long, cost: Double, newTRPDistance: Double, newTRPTime: Int, callback: (List[TariffRidePoint], Double, Double, Long) => Double): Double = {
    val current = points.head
    val newLocalDistance = distance + current.distance - newTRPDistance
    val newLocalTime = time + current.time - newTRPTime
    val newHead = new TariffRidePoint(current.tariff, newTRPDistance, newTRPTime, current.speed)
    callback(newHead :: points.tail, cost, newLocalDistance, newLocalTime)
  }

  def sumTariffRidePoints(list: List[TariffRidePoint]): List[TariffRidePoint] = list match {
    case List() => list
    case List(x) => list
    case x :: xs => if (x.tariff == xs.head.tariff)
        sumTariffRidePoints( new TariffRidePoint(x.tariff, x.distance+xs.head.distance, x.time+xs.head.time) :: xs.tail)
      else
        x :: sumTariffRidePoints(xs)
  }

  protected def getTariffHistory(ride: Ride): List[TariffRidePoint] = for (rp <- ride.points) yield new TariffRidePoint(FaresResource.findTariff(rp.time), rp.distance, rp.timeDiff)

  protected def isChargedPerDistance(speed: Double): Boolean = speed >= FaresResource.changeoverSpeedMS

}

class MinimalFare(
                   val tariff: String,
                   val min_cost: Double,
                   val min_dist: Double,
                   val min_time: Long // ms
                   )

class MediumFare(
                val tariff: String,
                val max_cost: Double,
                val charge_dist: Double,
                val charge_time: Long, // ms
                val charge_price: Double
                  )

class ChangeoverFare(
                  val charge_dist: Double,
                  val charge_time: Long, // ms
                  val charge_price: Double
                  )