package models.resources

import models.RidePoint
import com.typesafe.config.ConfigFactory
import slick.driver.SQLiteDriver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object RidesDAO {

  val dbDriver = ConfigFactory.load().getString("db.default.driver")
  val dbUrl = ConfigFactory.load().getString("db.default.url")
  val db = Database.forURL(dbUrl, driver = dbDriver)

  class RidePointsTable(tag : Tag) extends Table[(String,Double,Double,Long,Double,Long)](tag, "ridePoints") {
    def rideId = column[String]("RIDEID")
    def lat = column[Double]("LAT")
    def lon = column[Double]("LON")
    def time = column[Long]("TIME")
    def distance = column[Double]("DISTANCE")
    def timeDiff = column[Long]("TIMEDIFF")
    def * = (rideId, lat, lon, time, distance, timeDiff)
  }

  val ridePoints = TableQuery[RidePointsTable]

  def createDdl = Await.result(createTableIfNotExists(ridePoints), 5 seconds)

  def addRidePoint(ridePoint: RidePoint) = {createDdl; db.run(ridePoints += ridePoint.toTuple)}

  def findRidePoints(rideIdFilter: String): scala.concurrent.Future[Seq[models.RidePoint]] =  {
    createDdl
    val q: Query[RidePointsTable, (String,Double,Double,Long,Double,Long), Seq] = ridePoints.filter(_.rideId === rideIdFilter)
    db.run( q.result.map(r => r.map(p => RidePoint(p._1, p._2, p._3, p._4, p._5, p._6))) )
  }

  def findRidePoints() =  {
    createDdl
    val q: Query[RidePointsTable, (String,Double,Double,Long,Double,Long), Seq] = ridePoints
    db.run( q.result.map(r => r.map(p => RidePoint(p._1, p._2, p._3, p._4, p._5, p._6))) )
  }

  def removeRide(rideIdFilter: String) = {
    createDdl
    val q = ridePoints.filter(_.rideId === rideIdFilter)
    db.run( q.delete)
  }

  private def createTableIfNotExists(tables: TableQuery[_ <: Table[_]]*): Future[Seq[Unit]] = {
    Future.sequence(
      tables map { table =>
        db.run(MTable.getTables(table.baseTableRow.tableName)).flatMap { result =>
          if (result.isEmpty) {
            db.run(table.schema.create)
          } else {
            Future.successful(())
          }
        }
      }
    )
  }
}
