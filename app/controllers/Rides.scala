package controllers

import models._
import play.api.libs.json.Json
import play.api.mvc._
import java.time.Instant

trait RidesController extends Controller {
  this: Controller =>

  def getUUID: String

  def getCurrentTimestamp: Long = Instant.now().getEpochSecond * 1000

  def index = Action {
    Ok(Json.obj("response" -> "index"))
  }

  def startTracking(lat: Double, lon: Double) = Action {
    val ridePoint = new RidePoint(getUUID, lat, lon, getCurrentTimestamp)
    Ride.create(ridePoint)

    Ok(Json.obj(
      "rideId" -> ridePoint.rideId
    ))
  }

  def addRidePoint(rideId: String, lat: Double, lon: Double) = Action {
    val ridePoint = new RidePoint(rideId, lat, lon, getCurrentTimestamp)
    val ride = Ride.addRidePoint(ridePoint)

    Ok(Json.obj(
      "response" -> "OK",
      "distance" -> Ride.calculateDistance(ride.points).toInt,
      "time" -> Ride.calculateTime(ride.points),
      "fare" -> Fare.calculateFare(ride)
    ))
  }
}

