package models

import org.joda.time.DateTime

case class RidePoint (
      val rideId: String,
      val lat: Double,
      val lon: Double,
      val time: Long,
      val distance: Double,
      val timeDiff: Long) {

  def this(rideId: String) = this(rideId, 0.0, 0.0, 0, 0.0, 0)
  def this(rideId: String, time: Long) = this(rideId, 0.0, 0.0, time, 0.0, 0)
  def this(rideId: String, lat: Double, lon: Double, time: Long) = this(rideId, lat, lon, time, 0.0, 0)

  override def toString =
    "RidePoint(" + rideId + ", " + lat.toString + ", " + lon.toString + ", " + new DateTime(time) + ", " + distance.toString + ", " + timeDiff.toString + ")"

  def toTuple = (rideId, lat, lon, time, distance, timeDiff)

  def getPosTuple: (Double, Double) = (lat, lon)

  def setDistanceFrom(point: RidePoint): RidePoint = {
    new RidePoint(
      rideId,
      lat,
      lon,
      time,
      Ride.calculateDistanceBetween2Points(getPosTuple, point.getPosTuple),
      timeDiff
    )
  }

  def setTimeDiff(point: RidePoint): RidePoint = {
    new RidePoint(
      rideId,
      lat,
      lon,
      time,
      distance,
      time - point.time
    )
  }
}
