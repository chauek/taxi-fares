package controllers

import javax.inject.{Singleton, Inject}

import play.api.mvc._
import services.UUIDGenerator

@Singleton
class Application @Inject() (uuidGenerator: UUIDGenerator) extends Controller with RidesController {

  def getUUID: String = uuidGenerator.generate.toString

}
