package controllers


import play.api.libs.json.Json
import play.api.mvc._

trait FaresController extends Controller {
  this: Controller =>

  def getUUID: String

  def index = Action {
    Ok(Json.obj("response" -> "index"))
  }

  def startTracking = Action {

    val uuid = getUUID

    val json = Json.obj(
      "fareId" -> uuid,
      "lon" -> "0.00",
      "lat" -> "0.00",
      "created" -> new java.util.Date().getTime())

    Ok(Json.obj(
      "fareId" -> uuid
    ))
  }

  def addFarePoint(fareId: String) = Action {
    Ok(Json.obj(
      "response" -> "OK"
    ))
  }
}

