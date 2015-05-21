package models.resources

import models.{ChangeoverFare, MinimalFare, MediumFare}
import org.joda.time.DateTime

object FaresResource {

  private val changeoverSpeedMPH = 10.4

  val changeoverSpeedMS = changeoverSpeedMPH * 0.44704

  /**
   * (tariff, min cost, min dist, min time ms)
   */
  private val minimalFare = List(
    new MinimalFare("T1", 2.40, 252.4, 54200),
    new MinimalFare("T2", 2.40, 205.0, 44000),
    new MinimalFare("T3", 2.40, 165.4, 35600)
  )

  /**
   * (tariff, max medium cost, charge dist, charge time ms, charge price, max distance, max time)
   *
   */
  private val mediumFare = List(
    new MediumFare("T1", 17.40, 126.2, 27100, 0.20), // changeover at 9717.4m or 2086700ms
    new MediumFare("T2", 21.00, 102.5, 22000, 0.20), // changeover ar 9737.5m or 2090000ms
    new MediumFare("T3", 25.40, 82.7, 17800, 0.20) // changeover at 9675.9m or 2082600ms
  )

  /**
   * (charge dist, charge time ms, charge price)
   */
  private val changeoverFare = List(
    new ChangeoverFare(88.5, 19000, 0.20)
  )

  private val borderTime = List(
    // h, m, s, ms
    (6, 0, 0, 0),
    (20, 0, 0, 0),
    (22, 0, 0, 0)
  )

  private val holidaySymbol = "holiday"

  private val tariffsSchedule = List(
    (0, 6, List(holidaySymbol), "T3"),
    (6, 20, List(holidaySymbol), "T3"),
    (20, 22, List(holidaySymbol), "T3"),
    (22, 24, List(holidaySymbol), "T3"),

    (0, 6, List("Sat", "Sun"), "T3"),
    (6, 20, List("Sat", "Sun"), "T2"),
    (20, 22, List("Sat", "Sun"), "T2"),
    (22, 6, List("Sat", "Sun"), "T3"),
    (22, 24, List("Sat", "Sun"), "T3"),

    (0, 6, List("Mon", "Tue", "Wed", "Thu", "Fri"), "T3"),
    (6, 20, List("Mon", "Tue", "Wed", "Thu", "Fri"), "T1"),
    (20, 22, List("Mon", "Tue", "Wed", "Thu", "Fri"), "T2"),
    (22, 24, List("Mon", "Tue", "Wed", "Thu", "Fri"), "T3")
  )

  def findBorderPointsBetween(t1: Long, t2: Long): List[Long] = {
    val d1: DateTime = new DateTime(t1)
    val d2: DateTime = new DateTime(t2)
    val timeStamps = for {bt <- borderTime
                          p = new DateTime(d1).withTime(bt._1, bt._2, bt._3, bt._4).getMillis()
                          if (t1 < p && p < t2)} yield p

    if (d1.getDayOfMonth != d2.getDayOfMonth)
      timeStamps.toList ++ (for {bt <- borderTime
                                 p = new DateTime(d2).withTime(bt._1, bt._2, bt._3, bt._4).getMillis()
                                 if (t1 < p && p < t2)} yield p)
    else
      timeStamps.toList
  }

  def findTariff(time: Long): String = {
    val dateTime: DateTime = new DateTime(time)
    val h = dateTime.toString("HH").toInt
    val d = getDaySymbol(dateTime)
    tariffsSchedule.filter(x => h >= x._1 && h < x._2).filter(x => !x._3.filter(y => y == d).isEmpty).head._4
  }

  /**
   *
   * @param tariff
   * @return (tariff, min cost, min dist, min time)
   */
  def getMinimalFareForTariff(tariff: String): MinimalFare = minimalFare.filter(_.tariff == tariff).head

  /**
   *
   * @param tariff
   * @return (tariff, max medium cost, charge dist, charge time ms, charge price)
   */
  def getMediumFareForTariff(tariff: String): MediumFare = mediumFare.filter(_.tariff == tariff).head

  def getChangeoverFare = changeoverFare.head

  private def getDaySymbol(dateTime: DateTime): String = {
    if (HolidaysResource.isAHoliday(dateTime))
      holidaySymbol
    else
      dateTime.toString("E")
  }
}


  /*
There are three standard tariffs depending on the day of the week and the time of day you travel. If a journey goes
through more than one tariff, the new charge will be applied from the start time of the new tariff. The meter
automatically adds a charge based on time for any part of the journey when the speed drops below 10.4mph. Other extras
may be included in the final fare.

  Tariff 1
Monday to Friday between 06:00 and 20:00, other than on a public holiday:

For the first 252.4 metres or 54.2 seconds (whichever is reached first) there is a minimum charge of £2.40
For each additional 126.2 metres or 27.1 seconds (whichever is reached first), or part thereof, if the fare is less than
£17.40 then there is a charge of 20p
Once the fare is £17.40 or greater then there is a charge of 20p for each additional 88.5 metres or 19 seconds (whichever
is reached first), or part thereof

  Tariff 2
Monday to Friday between 20:00 and 22:00 or during Saturday or Sunday between 06:00 and
22:00, other than on a public holiday:

For the first 205 metres or 44 seconds (whichever is reached first) there is a minimum charge of £2.40
For each additional 102.5 metres or 22 seconds (whichever is reached first), or part thereof, if the fare is less than
£21 there is a charge of 20p
Once the fare is £21 or greater then there is a charge of 20p for each additional 88.5 metres or 19 seconds (whichever
is reached first), or part thereof

  Tariff 3
For any hiring between 22:00 on any day and 06:00 the following day or at any time on a public holiday:

For the first 165.4 metres or 35.6 seconds (whichever is reached first) there is a minimum charge of £2.40
For each additional 82.7 metres or 17.8 seconds (whichever is reached first), or part thereof, if the fare is less than
£25.40 there is a charge of 20p
Once the fare is £25.40 or greater then there is a charge of 20p for each additional 88.5 metres or 19 seconds (whichever
is reached first)

  Changeover fare
The rate at which fares increase goes up once a taxi journey goes over six miles. This is known as changeover fare.

For tariff 1, this fare is £17.40. For tariff 2, it is £21 and for tariff 3, it is £25.40.

The information on this page is effective from Saturday 5 April 2014.
   */
