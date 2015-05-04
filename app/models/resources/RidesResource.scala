package models.resources

import java.io.{FileWriter, File}
import com.typesafe.config.ConfigFactory
import models.RidePoint

import scala.io.Source
import scala.math._

object RideResource {
  val resourceBaseDir = "rides"

  def create(ride: RidePoint) = {
    val path = getPath(ride.rideId)
    if (!path.exists()) path.mkdirs()

    val file = getFile(ride.rideId, getPath(ride.rideId))
    if (file.exists()) file.delete()

    addToFile(ride, file)
  }

  def update(ride: RidePoint) = {
    val file = getFile(ride.rideId, getPath(ride.rideId))
    if (file.exists())
      addToFile(ride, file)
  }

  def find(rideId: String): List[RidePoint] = {
    val ride = new RidePoint(rideId,0.0,0.0,0)

    val file = getFile(ride.rideId, getPath(ride.rideId))
    readRides(rideId, file).toList
  }

  private def getPath(rideId: String): File = {
    val path: String = ConfigFactory.load().getString("files.db.path") + File.separator +
                      resourceBaseDir + File.separator +
                      rideId.take(2) + File.separator + rideId.substring(2,4)

    new File(path)
  }

  private def getFile(rideId: String, path: File): File = {
    new File(path.toString + File.separator + rideId)
  }

  private def addToFile(ride: RidePoint, file: File) = {
    val fw = new FileWriter(file.toString, true)
    fw.write(ride.lat.toString + "," + ride.lon.toString + "," + ride.time.toString + "\n")
    fw.close()
  }

  private def readRides(rideId: String, file: File): Iterator[RidePoint] = { //:
    if (file.exists()) {
      for (line <- Source.fromFile(file.toString).getLines()) yield {
        val columns = line.split(",")
         new RidePoint(rideId, columns(0).toDouble, columns(1).toDouble, columns(2).toLong)
      }
    }
    else {
      Iterator()
    }
  }



}
