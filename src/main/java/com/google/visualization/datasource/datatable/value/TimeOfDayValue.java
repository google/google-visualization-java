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
 * A value of type time-of-day.
 * Time is represented internally by four fields: hours, minutes, seconds and milliseconds.
 *
 * @author Hillel M.
 */
public class TimeOfDayValue extends Value {

  /**
   * A single static null value.
   */
  private static final TimeOfDayValue NULL_VALUE = new TimeOfDayValue();

  /**
   * Static method to return the null value (same one for all calls).
   *
   * @return Null value.
   */
  public static TimeOfDayValue getNullValue() {
    return NULL_VALUE;
  }

  /**
   * Underlying value: hours (24 hours).
   */
  private int hours;

  /**
   * Underlying value: minutes (60 minutes).
   */
  private int minutes;

  /**
   * Underlying value: seconds (60 seconds).
   */
  private int seconds;

  /**
   * Underlying value: milliseconds (1000 milliseconds).
   */
  private int milliseconds;

  /**
   * The hashCode for this TimeOfDayValue. The hashCode of NULL_VALUE is zero.
   */
  private Integer hashCode = null;

  /**
   * Creates a new time value. This constructor is private and is used only to
   * create a NULL_VALUE for this class.
   */
  private TimeOfDayValue() {
    hashCode = 0;
  }

  /**
   * Creates a new TimeOfDayValue.
   *
   * @param hours The hours.
   * @param minutes The minutes.
   * @param seconds The seconds.
   *
   * @throws IllegalArgumentException Thrown when one of the
   * paramters is illegal.
   */
  public TimeOfDayValue(int hours, int minutes, int seconds) {
    this(hours, minutes, seconds, 0);
  }

  /**
   * Creates a new TimeOfDayValue.
   *
   * @param hours The hours.
   * @param minutes The minutes.
   * @param seconds The seconds.
   * @param milliseconds The milliseconds.
   *
   * @throws IllegalArgumentException Thrown when one of the
   *     parameters is illegal.
   */
  public TimeOfDayValue(int hours, int minutes, int seconds, int milliseconds) {
    // Input checks.
    // A RunTimeException is thrown here since it is very unusual for structured
    // data to be incorrect.
    if ((hours >= 24) || (hours < 0)) {
      throw new IllegalArgumentException("This hours value is invalid: "
          + hours);
    }
    if ((minutes >= 60) || (minutes < 0)) {
      throw new IllegalArgumentException("This minutes value is invalid: "
          + minutes);
    }
    if ((seconds >= 60) || (seconds < 0)) {
      throw new IllegalArgumentException("This seconds value is invalid: "
          + seconds);
    }
    if ((milliseconds >= 1000) || (milliseconds < 0)) {
      throw new IllegalArgumentException("This milliseconds value is invalid: "
          + milliseconds);
    }
    // Assign internal variables.
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
    this.milliseconds = milliseconds;
  }

  /**
   * Creates a new instance based on the given {@code GregorianCalendar}.
   * The given calendar's time zone must be set to "GMT" as a precondition to
   * use this constructor.
   * Note: The date time values: hour, minute, second and millisecond
   * correspond to the values returned by calendar.get(field) of the given
   * calendar.
   *
   * @param calendar A GregorianCalendar from which to extract this instance
   *     values: hour, minutes, seconds and milliseconds.
   *
   * @throws IllegalArgumentException When calendar time zone is not set
   *     to GMT.
   */
  public TimeOfDayValue(GregorianCalendar calendar) {
    if (!calendar.getTimeZone().equals(TimeZone.getTimeZone("GMT"))) {
      throw new IllegalArgumentException(
          "Can't create TimeOfDayValue from GregorianCalendar that is not GMT.");
    }
    this.hours = calendar.get(GregorianCalendar.HOUR_OF_DAY);
    this.minutes = calendar.get(GregorianCalendar.MINUTE);
    this.seconds = calendar.get(GregorianCalendar.SECOND);
    this.milliseconds = calendar.get(GregorianCalendar.MILLISECOND);
  }

  @Override
  public ValueType getType() {
    return ValueType.TIMEOFDAY;
  }

  /**
   * Returns the timeValue as a String using default formatting.
   *
   * @return The timeValue as a String using default formatting.
   */
  @Override
  public String toString() {
    if (this == NULL_VALUE) {
      return "null";
    }
   String result = String.format("%1$02d:%2$02d:%3$02d", hours, minutes,
       seconds);
    if (milliseconds > 0) {
      result += "." + String.format("%1$3d", milliseconds);
    }
    return result;
  }

  /**
   * Tests whether this value is a logical null.
   *
   * @return Indication of whether the value is null.
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
    TimeOfDayValue otherTimeOfDay = (TimeOfDayValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherTimeOfDay.isNull()) {
      return 1;
    }
    // Compare hours.
    if (this.hours > otherTimeOfDay.hours) {
      return 1;
    } else if (this.hours < otherTimeOfDay.hours) {
      return -1;
    }
    // Compare minutes.
    if (this.minutes > otherTimeOfDay.minutes) {
      return 1;
    } else if (this.minutes < otherTimeOfDay.minutes) {
      return -1;
    }
    // Compare seconds.
    if (this.seconds > otherTimeOfDay.seconds) {
      return 1;
    } else if (this.seconds < otherTimeOfDay.seconds) {
      return -1;
    }
    // Compare milliseconds.
    if (this.milliseconds > otherTimeOfDay.milliseconds) {
      return 1;
    } else if (this.milliseconds < otherTimeOfDay.milliseconds) {
      return -1;
    }
    // Equal Values.
    return 0;
  }


  @Override
  public int hashCode() {
    if (null != hashCode) {
      return hashCode;
    }
    // Compute and store hashCode for this TimeOfDayValue - done only once.
    int hash = 1193; // Some arbitrary prime number.
    hash = (hash * 13) + hours;
    hash = (hash * 13) + minutes;
    hash = (hash * 13) + seconds;
    hash = (hash * 13) + milliseconds;
    hashCode = hash;
    return hashCode;
  }

  /**
   * A method to retrieve a formattable object for this object.
   * It is important to set the GMT TimeZone to avoid conversions related to TimeZone.
   */
  @Override
  public Calendar getObjectToFormat() {
    if (isNull()) {
      return null;
    }

    // Set GMT TimeZone.
    Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    // Set to some predefined default. Don't change this default.
    cal.set(Calendar.YEAR, 1899);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.DAY_OF_MONTH, 30);
    // Set the TimeOfDay based on this TimeOfDayValue.
    cal.set(Calendar.HOUR_OF_DAY, hours);
    cal.set(Calendar.MINUTE, minutes);
    cal.set(Calendar.SECOND, seconds);
    cal.set(Calendar.MILLISECOND, milliseconds);

    return cal;
  }

  /**
   * Returns the underlying hours.
   *
   * @return The underlying hours.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getHours() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return hours;
  }

  /**
   * Returns the underlying minutes.
   *
   * @return The underlying minutes.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getMinutes() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return minutes;
  }

  /**
   * Returns the underlying seconds.
   *
   * @return The underlying seconds.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getSeconds() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return seconds;
  }

  /**
   * Returns the underlying milliseconds.
   *
   * @return The underlying milliseconds.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getMilliseconds() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return milliseconds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String innerToQueryString() {
    String s = "TIMEOFDAY '" + hours + ":" + minutes + ":" + seconds;
    if (milliseconds != 0) {
      s += "." + milliseconds;
    }
    s += "'";
    return s;
  }
}
