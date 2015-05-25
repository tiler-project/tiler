package io.tiler.time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class TimePeriodParser {
  private static final Pattern timePeriodPattern = Pattern.compile("([1-9][0-9]+|[0-9])(\\.[0-9]+)?[usmhdw]");
  private static final String UNIT_MICROSECONDS = "u";
  private static final String UNIT_SECONDS = "s";
  private static final String UNIT_MINUTES = "m";
  private static final String UNIT_HOURS = "h";
  private static final String UNIT_DAYS = "d";
  private static final String UNIT_WEEKS = "w";
  private static final BigDecimal MILLISECONDS_PER_SECOND = new BigDecimal(1000l);
  private static final BigDecimal MILLISECONDS_PER_MINUTE = new BigDecimal(60 * 1000l);
  private static final BigDecimal MILLISECONDS_PER_HOUR = new BigDecimal(60 * 60 * 1000l);
  private static final BigDecimal MILLISECONDS_PER_DAY = new BigDecimal(24 * 60 * 60 * 1000l);
  private static final BigDecimal MILLISECONDS_PER_WEEK = new BigDecimal(7 * 24 * 60 * 60 * 1000l);
  private static final BigDecimal MICROSECONDS_PER_MICROSECOND = new BigDecimal(1l);
  private static final BigDecimal MICROSECONDS_PER_SECOND = new BigDecimal(1000 * 1000l);
  private static final BigDecimal MICROSECONDS_PER_MINUTE = new BigDecimal(60 * 1000 * 1000l);
  private static final BigDecimal MICROSECONDS_PER_HOUR = new BigDecimal(60 * 60 * 1000 * 1000l);
  private static final BigDecimal MICROSECONDS_PER_DAY = new BigDecimal(24 * 60 * 60 * 1000 * 1000l);
  private static final BigDecimal MICROSECONDS_PER_WEEK = new BigDecimal(7 * 24 * 60 * 60 * 1000 * 1000l);

  public static boolean isATimePeriod(String value) {
    return timePeriodPattern.matcher(value).matches();
  }

  public static long parseTimePeriodToMilliseconds(String value) {
    return getQuantityFromTimePeriod(value)
      .multiply(getMillisecondsPerUnit(getUnitFromTimePeriod(value)))
      .setScale(0, RoundingMode.HALF_EVEN)
      .longValueExact();
  }

  public static long parseTimePeriodToMicroseconds(String value) {
    return getQuantityFromTimePeriod(value)
      .multiply(getMicrosecondsPerUnit(getUnitFromTimePeriod(value)))
      .setScale(0, RoundingMode.HALF_EVEN)
      .longValueExact();
  }

  private static BigDecimal getQuantityFromTimePeriod(String value) {
    return new BigDecimal(value.substring(0, value.length() - 1));
  }

  private static String getUnitFromTimePeriod(String value) {
    return value.substring(value.length() - 1);
  }

  private static BigDecimal getMicrosecondsPerUnit(String unit) {
    switch (unit) {
      case UNIT_MICROSECONDS:
        return MICROSECONDS_PER_MICROSECOND;
      case UNIT_SECONDS:
        return MICROSECONDS_PER_SECOND;
      case UNIT_MINUTES:
        return MICROSECONDS_PER_MINUTE;
      case UNIT_HOURS:
        return MICROSECONDS_PER_HOUR;
      case UNIT_DAYS:
        return MICROSECONDS_PER_DAY;
      case UNIT_WEEKS:
        return MICROSECONDS_PER_WEEK;
      default:
        throw new IllegalArgumentException("Unsupported unit");
    }
  }

  private static BigDecimal getMillisecondsPerUnit(String unit) {
    switch (unit) {
      case UNIT_SECONDS:
        return MILLISECONDS_PER_SECOND;
      case UNIT_MINUTES:
        return MILLISECONDS_PER_MINUTE;
      case UNIT_HOURS:
        return MILLISECONDS_PER_HOUR;
      case UNIT_DAYS:
        return MILLISECONDS_PER_DAY;
      case UNIT_WEEKS:
        return MILLISECONDS_PER_WEEK;
      default:
        throw new IllegalArgumentException("Unsupported unit");
    }
  }
}
