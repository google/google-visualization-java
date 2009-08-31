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
 * A value of type date. Used to represent a specific day in a given year. This differs from
 * {@link DateTimeValue}, which represents a specific day in a given year as well as a specific
 * time during that day. 
 * Date is represented internally by three values: year, month and dayOfMonth.
 * This class stores only legitimate dates where the validation is done using
 * ibm.icu.GregorianCalendar.
 *
 * @author Hillel M.
 */
public class DateValue extends Value {

  /**
   * A single static null value.
   */
  private static final DateValue NULL_VALUE = new DateValue();

  /**
   * Static method to return the null value (same one for all calls).
   *
   * @return Null value.
   */
  public static DateValue getNullValue() {
    return NULL_VALUE;
  }

  /**
   * Underlying value: year.
   */
  private int year;

  /**
   * Underlying value: month. Note we use the java convention for months, this is:
   * January = 0, February = 1, ..., December = 11.
   */
  private int month;

  /**
   * Underlying value: day of month.
   */
  private int dayOfMonth;

  /**
   * The hashCode for this DateValue. The hashCode of a NULL_VALUE is zero.
   */
  private Integer hashCode = null;

  /**
   * Create a new date value. This constructor is private and is used only to
   * create a NULL_VALUE for this class.
   */
  private DateValue() {
    hashCode = 0;
  }

  /**
   * Creates a new date value.
   * The input is checked using a GregorianCalendar.
   * Note that we use java convention for months:
   * January = 0, ..., December = 11.
   *
   * @param year The year.
   * @param month The month.
   * @param dayOfMonth The day in the month.
   *
   * @throws IllegalArgumentException Thrown when one of the
   *     parameters is illegal.
   */
  public DateValue(int year, int month, int dayOfMonth) {
    // Constructs a GregorianCalendar with the given date set in
    // the default time zone.
    GregorianCalendar calendar =
        new GregorianCalendar(year, month, dayOfMonth);

    // Input check. If the date is invalid the calendar object will output
    // different fields for year, month and/or dayOfMonth.
    // A RunTimeException is thrown here since it is very unusual for structured
    // data to be incorrect.
    if ((calendar.get(GregorianCalendar.YEAR) != year)
        || (calendar.get(GregorianCalendar.MONTH) != month)
        || (calendar.get(GregorianCalendar.DAY_OF_MONTH) != dayOfMonth)) {
      throw new IllegalArgumentException("Invalid java date (yyyy-MM-dd): "
          + year + '-' + month + '-' + dayOfMonth);
    }
    // Assign internal variables.
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
  }

  /**
   * Creates a new instance based on the given {@code GregorianCalendar}.
   * The given calendar's time zone must be set to "GMT" as a precondition to
   * use this constructor.
   * Note: The date values: year, month, dayOfMonth correspond to the values
   * returned by calendar.get(field) of the given calendar.
   *
   * @param calendar A gregorian to extract this instance values: year, month
   *     and dayOfMonth.
   *
   * @throws IllegalArgumentException When calendar time zone is not set
   *     to GMT.
   */
  public DateValue(GregorianCalendar calendar) {
    if (!calendar.getTimeZone().equals(TimeZone.getTimeZone("GMT"))) {
      throw new IllegalArgumentException(
           "Can't create DateValue from GregorianCalendar that is not GMT.");
    }
    this.year = calendar.get(GregorianCalendar.YEAR);
    this.month = calendar.get(GregorianCalendar.MONTH);
    this.dayOfMonth = calendar.get(GregorianCalendar.DAY_OF_MONTH);
  }

  @Override
  public ValueType getType() {
    return ValueType.DATE;
  }

  /**
   * Returns the dateValue as a String using temporary formatting.
   *
   * @return The dateValue as a String using temporary formatting.
   */
  @Override
  public String toString() {
    if (this == NULL_VALUE) {
      return "null";
    }
    return String.format("%1$d-%2$02d-%3$02d", year, month + 1, dayOfMonth);

  }

  /**
   * Tests whether this value a logical null.
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
      return 0; // If same value, or both are null.
    }
    DateValue otherDate = (DateValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherDate.isNull()) {
      return 1;
    }
    // Compare year.
    if (this.year > otherDate.year) {
      return 1;
    } else if (this.year < otherDate.year) {
      return -1;
    }
    // Compare month.
    if (this.month > otherDate.month) {
      return 1;
    } else if (this.month < otherDate.month) {
      return -1;
    }
    // Compare dayOfMonth.
    if (this.dayOfMonth > otherDate.dayOfMonth) {
      return 1;
    } else if (this.dayOfMonth < otherDate.dayOfMonth) {
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
    // Compute and store hashCode for this DateValue.
    int hash  = 1279; // Some arbitrary prime number.
    hash = (hash * 17) + year;
    hash = (hash * 17) + month;
    hash = (hash * 17) + dayOfMonth;
    hashCode = hash;
    return hashCode;
 }

  @Override
  public Calendar getObjectToFormat() {
    if (isNull()) {
      return null;
    }
    GregorianCalendar cal = new GregorianCalendar(year, month, dayOfMonth);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    return cal;
  }

  /**
   * Returns the underlying year.
   *
   * @return The underlying year.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getYear() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return year;
  }

  /**
   * Returns the underlying month.
   *
   * @return The underlying month.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getMonth() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return month;
  }

  /**
   * Returns the underlying dayOfMonth.
   *
   * @return the underlying dayOfMonth.
   *
   * @throws NullValueException Thrown when this Value is NULL_VALUE.
   */
  public int getDayOfMonth() {
    if (isNull()) {
      throw new NullValueException("This object is null");
    }
    return dayOfMonth;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String innerToQueryString() {
    return "DATE '" + year + "-" + (month + 1) + "-" + dayOfMonth + "'";
  }
}
