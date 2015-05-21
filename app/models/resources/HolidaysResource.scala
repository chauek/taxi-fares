package models.resources

import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import scala.io.Source
import java.io.File

object HolidaysResource {
  val resourceFile = "holidays.txt"

  def isAHoliday(dateTime: DateTime): Boolean = {
    val file =new File(ConfigFactory.load().getString("files.db.path") + File.separator + resourceFile)

    if (file.exists()) {
      val iterator = Source.fromFile(file.toString).getLines()
      val date = dateTime.toString("yyy-MM-dd")
      while (iterator.hasNext)
        if (date == iterator.next())
          return true
    }
    false
  }

}
