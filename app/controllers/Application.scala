package controllers

import play.api.mvc._
import services.UUIDGenerator
import javax.inject.{Singleton, Inject}

@Singleton
class Application @Inject() (uuidGenerator: UUIDGenerator) extends Controller with RidesController {

  def getUUID: String = uuidGenerator.generate.toString

}
