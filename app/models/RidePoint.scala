package models

class RidePoint (
      val rideId: String,
      val lat: Double,
      val lon: Double,
      val time: Long) {

  override def toString =
    rideId + "," + lat.toString + "," + lon.toString + "," + time.toString

  def getPosTuple: (Double, Double) = (lat, lon)

}
