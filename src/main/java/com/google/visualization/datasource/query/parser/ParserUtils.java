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

package com.google.visualization.datasource.query.parser;

import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class for the QueryParser. The functions here are called from the .jj file.
 *
 * Note on errors: Since this class handles a user generated query, all errors detected cause both
 * logging, and an exception that is thrown to the user.
 *
 * @author Yonatan B.Y.
 */
/* package */ final class ParserUtils {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(ParserUtils.class.getName());

  /**
   * The message that should be in the exception thrown when parsing an invalid
   * date string, parameterized by the erroneous string itself.
   */
  private static final String dateMessage = "Invalid date literal [%1$s]. "
      + "Date literals should be of form yyyy-MM-dd.";

  /**
   * The message that should be in the exception thrown when parsing an invalid
   * timeofday string, parameterized by the erroneous string itself.
   */
  private static final String timeOfDayMessage = "Invalid timeofday "
      + "literal [%1$s]. Timeofday literals should be of form HH:mm:ss[.SSS]";

  /**
   * The message that should be in the exception thrown when parsing an invalid
   * datetime string, parameterized by the erroneous string itself.
   */
  private static final String dateTimeMessage =
      "Invalid datetime literal [%1$s]. Datetime literals should "
          + " be of form yyyy-MM-dd HH:mm:ss[.SSS]";

  /**
   * Parses a string into a date value, for the query parser. The dates parsed are always in the
   * format: yyyy-MM-dd.
   * 
   * @param s The string to parse.
   * 
   * @return The parsed date.
   * 
   * @throws InvalidQueryException If the string is not parse-able as a date of format yyyy-MM-dd.
   */
  public static DateValue stringToDate(String s) throws InvalidQueryException {
    String[] split = s.split("-");
    if (split.length != 3) {
      log.error(String.format(dateMessage, s));
      throw new InvalidQueryException(String.format(dateMessage, s));
    }
    try {
      int year = Integer.parseInt(split[0]);
      int month = Integer.parseInt(split[1]);
      month--; // normalize 1-12 to 0-11.
      int day = Integer.parseInt(split[2]);
      return new DateValue(year, month, day);
    } catch (NumberFormatException e) {
      log.error(String.format(dateMessage, s));
      throw new InvalidQueryException(String.format(dateMessage, s));
    } catch (IllegalArgumentException e) {
      log.error(String.format(dateMessage, s));
      throw new InvalidQueryException(String.format(dateMessage, s));
    }
  }

  /**
   * Parses a string into a time-of-day value, for the query parser. The values parsed are always
   * in the format: HH:mm:ss[.SSS].
   * 
   * @param s The string to parse.
   * 
   * @return The parsed time-of-day.
   * 
   * @throws InvalidQueryException If the string can not be parsed into a time-of-day of format
   *     HH:mm:ss[.SSS].
   */
  public static TimeOfDayValue stringToTimeOfDay(String s)
      throws InvalidQueryException {
    String[] split = s.split(":");
    if (split.length != 3) {
      log.error(String.format(timeOfDayMessage, s));
      throw new InvalidQueryException(String.format(timeOfDayMessage, s));
    }
    try {
      int hour = Integer.parseInt(split[0]);
      int minute = Integer.parseInt(split[1]);
      int second;
      if (split[2].contains(".")) {
        String[] secondMilliSplit = split[2].split(".");
        if (secondMilliSplit.length != 2) {
          log.error(String.format(timeOfDayMessage, s));
          throw new InvalidQueryException(String.format(timeOfDayMessage, s));
        }
        second = Integer.parseInt(secondMilliSplit[0]);
        int milli = Integer.parseInt(secondMilliSplit[1]);
        return new TimeOfDayValue(hour, minute, second, milli);
      } else {
        second = Integer.parseInt(split[2]);
        return new TimeOfDayValue(hour, minute, second);
      }
    } catch (NumberFormatException e) {
      log.error(String.format(timeOfDayMessage, s));
      throw new InvalidQueryException(String.format(timeOfDayMessage, s));
    } catch (IllegalArgumentException e) {
      log.error(String.format(timeOfDayMessage, s));
      throw new InvalidQueryException(String.format(timeOfDayMessage, s));
    }
  }

  /**
   * Parses a string into a date-time value, for the query parser. The values parsed are always
   * in the format: yyyy-MM-dd HH:mm:ss[.SSS].
   * 
   * @param s The string to parse.
   * 
   * @return The parsed date-time
   * 
   * @throws InvalidQueryException If the string can not be parsed into a date-time of format
   *     yyyy-MM-dd HH:mm:ss[.SSS].
   */
  public static DateTimeValue stringToDatetime(String s)
      throws InvalidQueryException {
    String[] mainSplit = s.split(" ");
    if (mainSplit.length != 2) {
      log.error(String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    }
    String[] dateSplit = mainSplit[0].split("-");
    String[] timeSplit = mainSplit[1].split(":");
    if ((dateSplit.length != 3) || (timeSplit.length != 3)) {
      log.error(String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    }
    try {
      int year = Integer.parseInt(dateSplit[0]);
      int month = Integer.parseInt(dateSplit[1]);
      month--; // normalize 1-12 to 0-11.
      int day = Integer.parseInt(dateSplit[2]);
      int hour = Integer.parseInt(timeSplit[0]);
      int minute = Integer.parseInt(timeSplit[1]);
      int second;
      int milli = 0;
      if (timeSplit[2].contains(".")) {
        String[] secondMilliSplit = timeSplit[2].split("\\.");
        if (secondMilliSplit.length != 2) {
          log.error(String.format(dateTimeMessage, s));
          throw new InvalidQueryException(String.format(dateTimeMessage, s));
        }
        second = Integer.parseInt(secondMilliSplit[0]);
        milli = Integer.parseInt(secondMilliSplit[1]);
      } else {
        second = Integer.parseInt(timeSplit[2]);
      }
      return new DateTimeValue(year, month, day, hour, minute, second, milli);
    } catch (NumberFormatException e) {
      log.error(String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    } catch (IllegalArgumentException e) {
      log.error(String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    }
  }


  /**
   * Strips the first and last characters from a string.
   * Throws a runtime exception if the string is less than 2 characters long. Used for stripping
   * quotes (whether single, double, or back-quotes) from, for example, "foo", 'bar', and `baz`.
   *
   * @param s The string from which to strip the quotes.
   *
   * @return The stripped string.
   */
  public static String stripQuotes(String s) {
    if (s.length() < 2) {
      throw new RuntimeException("String is of length < 2 on call to "
          + "stripQuotes: " + s);
    }
    return s.substring(1, s.length() - 1);
  }

  /**
   * Private constructor, to prevent instantiation.
   */
  private ParserUtils() {
  }
}
