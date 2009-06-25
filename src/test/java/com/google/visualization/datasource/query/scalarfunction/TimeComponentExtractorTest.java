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

package com.google.visualization.datasource.query.scalarfunction;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests for the TimeComponentExtractor
 *
 * @author Liron L.
 */
public class TimeComponentExtractorTest extends TestCase {
  private TimeComponentExtractor year =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.YEAR);

  private TimeComponentExtractor month =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.MONTH);

  private TimeComponentExtractor day =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.DAY);

  private TimeComponentExtractor hour =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.HOUR);

  private TimeComponentExtractor minute =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.MINUTE);

  private TimeComponentExtractor second =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.SECOND);

  private TimeComponentExtractor millisecond =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.MILLISECOND);

  private TimeComponentExtractor quarter =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.QUARTER);

  private TimeComponentExtractor dayofweek =
      TimeComponentExtractor.getInstance(
          TimeComponentExtractor.TimeComponent.DAY_OF_WEEK);

  public void testValidateParameters() throws InvalidQueryException {
    List<ValueType> types = Lists.newArrayList(ValueType.DATE);

    // Validate date paremeter.
    year.validateParameters(types);
    month.validateParameters(types);
    day.validateParameters(types);

    // Should throw an exception.
    try {
      second.validateParameters(types);
      fail("Should have thrown a ScalarFunctionException: The second function "
          + "was given a date parameter to validate.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
    // Validate datetime paremeter.
    types = Lists.newArrayList(ValueType.DATETIME);
    year.validateParameters(types);
    month.validateParameters(types);
    day.validateParameters(types);
    hour.validateParameters(types);
    minute.validateParameters(types);
    second.validateParameters(types);
    millisecond.validateParameters(types);

    // Should throw an exception.
    try {
      hour.validateParameters(Lists.newArrayList(ValueType.NUMBER));
      fail("Should have thrown a ScalarFunctionException: The hour function "
          + "was given a number parameter to validate.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }

    // Validate timeofday paremeter.
    types = Lists.newArrayList(ValueType.TIMEOFDAY);
    hour.validateParameters(types);
    minute.validateParameters(types);
    second.validateParameters(types);
    millisecond.validateParameters(types);

    // Should throw an exception.
    try {
      year.validateParameters(types);
      fail("Should have thrown a ScalarFunctionException: The year function "
          + "was given a timeofday parameter to validate.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }

    // Should throw an exception.
    try {
      quarter.validateParameters(Lists.newArrayList(ValueType.NUMBER));
      fail("Should have thrown a ScalarFunctionException: The quarter "
          + "function was given a number parameter to validate.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }

    // Should throw an exception.
    try {
      dayofweek.validateParameters(Lists.newArrayList(ValueType.NUMBER));
      fail("Should have thrown a ScalarFunctionException: The dayofweek "
          + "function was given a number parameter to validate.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }

    // Validate datetime paremeter.
    types = Lists.newArrayList(ValueType.DATETIME);
    quarter.validateParameters(types);
    dayofweek.validateParameters(types);

    // Should throw an exception.
    try {
      types.add(ValueType.DATETIME);
      quarter.validateParameters(types);
      fail("Should have thrown a ScalarFunctionException: The quarter "
          + "function was given 2 parameters.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testEvaluate() {
    List<Value> dateValues =
        Lists.newArrayList((Value) new DateValue(2008, 7, 13));
    assertEquals(new NumberValue(2008), year.evaluate(dateValues));
    assertEquals(new NumberValue(7), month.evaluate(dateValues));
    assertEquals(new NumberValue(13), day.evaluate(dateValues));

    List<Value> dateTimeValues =
        Lists.newArrayList((Value) new DateTimeValue(
            2001, 2, 3, 9, 12, 22, 333));
    assertEquals(new NumberValue(2001), year.evaluate(dateTimeValues));
    assertEquals(new NumberValue(2), month.evaluate(dateTimeValues));
    assertEquals(new NumberValue(3), day.evaluate(dateTimeValues));
    assertEquals(new NumberValue(9), hour.evaluate(dateTimeValues));
    assertEquals(new NumberValue(12), minute.evaluate(dateTimeValues));
    assertEquals(new NumberValue(22), second.evaluate(dateTimeValues));
    assertEquals(new NumberValue(333), millisecond.evaluate(dateTimeValues));

    List<Value> timeOfDayValues =
        Lists.newArrayList((Value) new TimeOfDayValue(23, 22, 21, 20));
    assertEquals(new NumberValue(23), hour.evaluate(timeOfDayValues));
    assertEquals(new NumberValue(22), minute.evaluate(timeOfDayValues));
    assertEquals(new NumberValue(21), second.evaluate(timeOfDayValues));
    assertEquals(new NumberValue(20), millisecond.evaluate(timeOfDayValues));
    timeOfDayValues = Lists.newArrayList((Value) new TimeOfDayValue(9, 12, 22));
    // Tests that milliseconds return 0 when the the TimeOfDayValue is
    // initialized without the milliseconds.
    assertEquals(new NumberValue(0), millisecond.evaluate(timeOfDayValues));

    List<Value> dateValues1 =
        Lists.newArrayList((Value) new DateValue(2008, 1, 13));
    List<Value> dateValues2 =
        Lists.newArrayList((Value) new DateValue(2008, 4, 13));
    List<Value> dateValues3 =
        Lists.newArrayList((Value) new DateValue(2008, 8, 13));
    List<Value> dateValues4 =
        Lists.newArrayList((Value) new DateValue(2008, 11, 13));
    List<Value> dateValues5 =
        Lists.newArrayList((Value) new DateValue(2008, 5, 13));

    assertEquals(new NumberValue(1), quarter.evaluate(dateValues1));
    assertEquals(new NumberValue(2), quarter.evaluate(dateValues2));
    assertEquals(new NumberValue(3), quarter.evaluate(dateValues3));
    assertEquals(new NumberValue(4), quarter.evaluate(dateValues4));
    assertEquals(new NumberValue(2), quarter.evaluate(dateValues5));

    assertEquals(new NumberValue(4), dayofweek.evaluate(dateValues1));
    assertEquals(new NumberValue(3), dayofweek.evaluate(dateValues2));
    assertEquals(new NumberValue(7), dayofweek.evaluate(dateValues3));
    assertEquals(new NumberValue(7), dayofweek.evaluate(dateValues4));
    assertEquals(new NumberValue(6), dayofweek.evaluate(dateValues5));

    List<Value> dateTimeValues1 =
        Lists.newArrayList((Value) new DateTimeValue(
            2001, 0, 3, 9, 12, 22, 333));
    List<Value> dateTimeValues2 =
        Lists.newArrayList((Value) new DateTimeValue(
            2001, 3, 3, 9, 12, 22, 333));
    List<Value> dateTimeValues3 =
        Lists.newArrayList((Value) new DateTimeValue(
            2001, 8, 3, 9, 12, 22, 333));
    List<Value> dateTimeValues4 =
        Lists.newArrayList((Value) new DateTimeValue(
            2001, 10, 3, 9, 12, 22, 333));
    List<Value> dateTimeValues5 =
        Lists.newArrayList((Value) new DateTimeValue(
            2001, 7, 3, 9, 12, 22, 333));

    assertEquals(new NumberValue(1), quarter.evaluate(dateTimeValues1));
    assertEquals(new NumberValue(2), quarter.evaluate(dateTimeValues2));
    assertEquals(new NumberValue(3), quarter.evaluate(dateTimeValues3));
    assertEquals(new NumberValue(4), quarter.evaluate(dateTimeValues4));
    assertEquals(new NumberValue(3), quarter.evaluate(dateTimeValues5));

    assertEquals(new NumberValue(4), dayofweek.evaluate(dateTimeValues1));
    assertEquals(new NumberValue(3), dayofweek.evaluate(dateTimeValues2));
    assertEquals(new NumberValue(2), dayofweek.evaluate(dateTimeValues3));
    assertEquals(new NumberValue(7), dayofweek.evaluate(dateTimeValues4));
    assertEquals(new NumberValue(6), dayofweek.evaluate(dateTimeValues5));
  }

  // Tests that 2 instances of the same kind of TimeComponentExtractor are equal
  // (i.e., they're the same instance).
  public void testSingelton() {
    TimeComponentExtractor anotherYear = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.YEAR);
    TimeComponentExtractor anotherMonth = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.MONTH);
    TimeComponentExtractor anotherDay = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.DAY);
    TimeComponentExtractor anotherHour = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.HOUR);
    TimeComponentExtractor anotherMinute = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.MINUTE);
    TimeComponentExtractor anotherSecond = TimeComponentExtractor.getInstance(
        TimeComponentExtractor.TimeComponent.SECOND);
    TimeComponentExtractor anotherMillisecond =
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.MILLISECOND);
    TimeComponentExtractor anotherQuarter =
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.QUARTER);
    TimeComponentExtractor anotherDayOfWeek =
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY_OF_WEEK);
    assertTrue(year == anotherYear);
    assertTrue(month == anotherMonth);
    assertTrue(day == anotherDay);
    assertTrue(hour == anotherHour);
    assertTrue(minute == anotherMinute);
    assertTrue(second == anotherSecond);
    assertTrue(millisecond == anotherMillisecond);
    assertTrue(quarter == anotherQuarter);
    assertTrue(dayofweek == anotherDayOfWeek);
  }
}
