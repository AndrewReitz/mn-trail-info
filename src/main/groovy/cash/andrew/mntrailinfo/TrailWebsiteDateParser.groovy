package cash.andrew.mntrailinfo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TrailWebsiteDateParser {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern('MM-dd-yyyy, hh:mm a')

  LocalDateTime parseText(String text) {
    return LocalDateTime.parse(text, formatter)
  }
}
