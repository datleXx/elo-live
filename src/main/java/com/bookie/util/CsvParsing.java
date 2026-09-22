package com.bookie.util;

import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class CsvParsing {
  private static final DateTimeFormatter DATE_4Y = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_2Y =
      new DateTimeFormatterBuilder()
          .appendPattern("dd/MM/")
          .appendValueReduced(ChronoField.YEAR, 2, 2, 1950)
          .toFormatter();

  public static LocalDate parseMatchDate(String raw) {
    return raw.length() == 10 ? LocalDate.parse(raw, DATE_4Y) : LocalDate.parse(raw, DATE_2Y);
  }

  public static BigDecimal parseOdds(CSVRecord record, String column) {
    if (!record.isMapped(column)) {
      return null;
    }
    String value = record.get(column);
    if (value.isBlank()) {
      return null;
    }
    return new BigDecimal(value);
  }
}
