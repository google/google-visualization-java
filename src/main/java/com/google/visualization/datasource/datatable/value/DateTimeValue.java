// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.visualization.datasource.datatable.value;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

/**
 * A value of type date-time. Used to represent a specific day in a given year as well as a
 * specific time during that day. This differs from {@link DateValue}, which represents only a
 * specific day in a given year.
 * DateTime is represented internally by a GregorianCalendar. The
 * calendar is immutable and is kept only for validation of the input.
 *
 * @author Hillel M.
 */
public class DateTimeValue extends Value {

  /**
   * A single static null value.
   */
  private static final DateTimeValue NULL_VALUE = new DateTimeValue();


  /**
   * Static method to return the null value (same one for all calls).
   *
   * @return Null value.
   */
  public static DateTimeValue getNullValue() {
    return NULL_VALUE;
  }

  /**
   * Underlying GregorianCalendar.
   * The calendar is immutable and is set only in the constructor.
   */
  private GregorianCalendar calendar;

  /**
   * Stores the hash code for this DateTime. Do not use GregorianCalendar
   * hash code since it does not consider time values and it might trigger
   * internal changes in the calendar values.
   * The hashCode of NULL_VALUE is zero.
   */
  private Integer hashCode = null;

  /**
   * Creates a new DateTime value. This constructor is private and is used
   * only to create a NULL_VALUE for this class.
   */
  private DateTimeValue() {
    hashCode = 0;
  }

  /**
   * Creates a new DateTime value.
   * The input is checked using a gregorian calendar.
   * Note this uses the java convention for months:
   * January = 0, ..., December = 11.
   *
   * @param year The year.
   * @param month The month.
   * @param dayOfMonth The day of month.
   * @param hours The hours.
   * @param minutes The minutes.
   * @param seconds The seconds.
   * @param milliseconds The milliseconds.
   *
   * @throws IllegalArgumentException Thrown if one of the
   *     parameters is illegal.
   */
  public DateTimeValue(int year, int month, int dayOfMonth, int hours,
      int minutes, int seconds, int milliseconds) {
    // Constructs a GregorianCalendar with the given date and time.
    calendar = new GregorianCalendar(year, month, dayOfMonth, hours,
        minutes, seconds);
    calendar.set(GregorianCalendar.MILLISECOND, milliseconds);
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

    // Check input.
    // A RunTimeException is thrown here since it is very unusual for structured
    // data to be incorrect.
    if ((getYear() != year)
        || (getMonth() != month)
        || (getDayOfMonth() != dayOfMonth)
        || (getHourOfDay() != hours)
        || (getMinute() != minutes)
        || (getSecond() != seconds)
        || (getMillisecond() != milliseconds)) {
      throw new IllegalArgumentException("Invalid java date "
          + "(yyyy-MM-dd hh:mm:ss.S): "
          + year + '-' + month + '-' + dayOfMonth + ' ' + hours + ':'
          + minutes + ':' + seconds + '.' + milliseconds);
    }
  }

  /**
   * Creates a new instance based on the given {@code GregorianCalendar}.
   * The given calendar's time zone must be set to "GMT" as a precondition to
   * use this constructor.
   * Note: The date time values: year, month, dayOfMonth, hour, minute, second
   * and millisecond correspond to the values returned by calendar.get(field)
   * of the given calendar.
   *
   * @param calendar A Gregorian calendar on which to base this instance.
   *
   * @throws IllegalArgumentException When calendar time zone is not set
   *     to GMT.
   */
  public DateTimeValue(GregorianCalendar calendar) {
    if (!calendar.getTimeZone().equals(TimeZone.getTimeZone("GMT"))) {
      throw new IllegalArgumentException(
          "Can't create DateTimeValue from GregorianCalendar that is not GMT.");
    }
    this.calendar = (GregorianCalendar) calendar.clone();
  }

  /**
   * Returns the year.
   * 
   * @return The year.
   */
  public int getYear() {
    return calendar.get(GregorianCalendar.YEAR);
  }

  /**
   * Returns the month. The values are from 0 (January) to 11 (December).
   *
   * @return The month.
   */
  public int getMonth() {
    return calendar.get(GregorianCalendar.MONTH);
  }

  /**
   * Returns the day of month. The first day of the month is 1.
   * 
   * @return The day of month.
   */
  public int getDayOfMonth() {
    return calendar.get(GregorianCalendar.DAY_OF_MONTH);
  }

  /**
   * Returns the hour of day.
   * 
   * @return The hour of day.
   */
  public int getHourOfDay() {
    return calendar.get(GregorianCalendar.HOUR_OF_DAY);
  }

  /**
   * Returns the minute.
   * 
   * @return The minute.
   */
  public int getMinute() {
    return calendar.get(GregorianCalendar.MINUTE);
  }

  /**
   * Returns the second.
   * 
   * @return The second.
   */
  public int getSecond() {
    return calendar.get(GregorianCalendar.SECOND);
  }

  /**
   * Returns the millisecond.
   * 
   * @return The millisecond.
   */
  public int getMillisecond() {
    return calendar.get(GregorianCalendar.MILLISECOND);
  }


  @Override
  public ValueType getType() {
    return ValueType.DATETIME;
  }

  /**
   * Returns the DateTimeValue as a String using default formatting.
   *
   * @return The DateTimeValue as a String using default formatting.
   */
  @Override
  public String toString() {
    if (this == NULL_VALUE) {
      return "null";
    }
   String result = String.format("%1$d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
       getYear(), getMonth() + 1, getDayOfMonth(), getHourOfDay(), getMinute(),
       getSecond());
    if (getMillisecond() > 0) {
      result += "." + String.format("%1$03d", getMillisecond());
    }
    return result;
  }

  /**
   * Tests whether this value is a logical null.
   *
   * @return Indication if the value is null.
   */
  @Override
  public boolean isNull() {
    return (this == NULL_VALUE);
  }

  /**
   * Compares this value to another value of the same type.
   *
   * @param other Other value.
   *
   * @return 0 if equal, negative if this is smaller, positive if larger.
   */
  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0;
    }
    DateTimeValue otherDateTime = (DateTimeValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherDateTime.isNull()) {
      return 1;
    }
    // Calendar implements compareTo by converting to milliseconds.
    return (calendar.compareTo(otherDateTime.getCalendar()));
  }

  @Override
  public int hashCode() {
    if (null != hashCode) {
      return hashCode;
    }
    // Compute and store hashCode for this DateTimeValue.
    int hash = 1579; // Some arbitrary prime number.
    hash = (hash * 11) + getYear();
    hash = (hash * 11) + getMonth();
    hash = (hash * 11) + getDayOfMonth();
    hash = (hash * 11) + getHourOfDay();
    hash = (hash * 11) + getMinute();
    hash = (hash * 11) + getSecond();
    hash = (hash * 11) + getMillisecond();
    hashCode = hash;
    return hashCode;
  }

  @Override
  public Calendar getObjectToFormat() {
    if (isNull()) {
      return null;
    }
    return calendar;
  }

  /**
   * Returns the internal GregorianCalendar.
   *
   * @return The internal GregorianCalendar.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public GregorianCalendar getCalendar() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return calendar;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String innerToQueryString() {
    String s = "DATETIME '" + getYear() + "-" + (getMonth() + 1) + "-"
        + getDayOfMonth() + " " + getHourOfDay() + ":" + getMinute() + ":"
        + getSecond();
    int milli = getMillisecond();
    if (milli != 0) {
      s += "." + milli;
    }
    s += "'";
    return s;
  }
}
