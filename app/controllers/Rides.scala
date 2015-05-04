package controllers


import models.resources._
import models.{Ride, RidePoint}
import play.api.libs.json.Json
import play.api.mvc._
import java.time.Instant

trait RidesController extends Controller {
  this: Controller =>

  def getUUID: String

  def getCurrentTimestamp: Long = Instant.now().getEpochSecond()

  def index = Action {
    Ok(Json.obj("response" -> "index"))
  }

  def startTracking(lat: Double, lon: Double) = Action {

    val ride = new RidePoint(getUUID, lat, lon, getCurrentTimestamp)
    RideResource.create(ride)

    Ok(Json.obj(
      "rideId" -> ride.rideId
    ))
  }

  def addRidePoint(rideId: String, lat: Double, lon: Double) = Action {

    val ride = new RidePoint(rideId, lat, lon, getCurrentTimestamp)
    RideResource.update(ride)

    Ok(Json.obj(
      "response" -> "OK",
      "distance" -> Ride.calculateDistance(RideResource.find(rideId)).toInt,
      "time" -> Ride.calculateTime(RideResource.find(rideId))
    ))
  }
}

