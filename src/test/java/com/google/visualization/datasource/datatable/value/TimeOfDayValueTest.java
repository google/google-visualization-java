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

import junit.framework.TestCase;

/**
 * Test for TimeOfDayValue.
 *
 * @author Hillel M.
 */
public class TimeOfDayValueTest extends TestCase {

  public void testNullValue() {
    TimeOfDayValue value = TimeOfDayValue.getNullValue();
    assertTrue(value.isNull());
    value = new TimeOfDayValue(1, 2, 31);
    assertFalse(value.isNull());
  }

  public void testConstructorIllegalValues() {
    // Check the Exception is thrown.
    try {
      TimeOfDayValue value = new TimeOfDayValue(24, 0, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(-1, 0, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 70, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, -1, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 0, 65);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 0, -10);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(24, 0, 2, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(-1, 12, 0, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 70, 0, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, -1, 0, 500);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 0, 1500, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 0, -10, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 0, 0, 1500);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(0, 0, 0, -10);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
  }

  public void testConstructor() {
    // Test if the construction does not fail.
    try {
      TimeOfDayValue value = new TimeOfDayValue(12, 12, 31);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // should not be here
      assertFalse("An exception was not supposed to be thorwn",  true);
    }
    try {
      TimeOfDayValue value = new TimeOfDayValue(12, 12, 31, 666);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // should not be here
      assertFalse("An exception was not supposed to be thorwn",  true);
    }
  }

  public void testCalendarConstructor() {
    // All fields are set in the calendar although only hour, minute and seconds
    // are requried.
    GregorianCalendar calendar = new GregorianCalendar(2006, 1, 1, 21, 24, 25);
    TimeOfDayValue value = null;
    calendar.setTimeZone(TimeZone.getTimeZone("IST"));
    // Check exception thrown for non GMT time zone.
    try {
      value = new TimeOfDayValue(calendar);
      fail();
    } catch (IllegalArgumentException iae) {
      // do nothing
    }
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    // Check exception not thrown for GMT time zone.
    try {
      value = new TimeOfDayValue(calendar);
    } catch (IllegalArgumentException iae) {
      fail();
    }
    // Verify values - default milliseconds.
    assertEquals(21, value.getHours());
    assertEquals(24, value.getMinutes());
    assertEquals(25, value.getSeconds());
    assertEquals(0, value.getMilliseconds());

    // Non default milliseconds.
    calendar.set(GregorianCalendar.MILLISECOND, 123);
    value = new TimeOfDayValue(calendar);
    assertEquals(123, value.getMilliseconds());
  }

  public void testGetType() {
    TimeOfDayValue value = new TimeOfDayValue(21, 40, 30);
    assertEquals(value.getType(),  ValueType.TIMEOFDAY);
    value = TimeOfDayValue.getNullValue();
    assertEquals(value.getType(),  ValueType.TIMEOFDAY);
  }

  public void testToString() {
    TimeOfDayValue value = new TimeOfDayValue(20, 10, 5);
    assertEquals(value.toString(),  "20:10:05");
    value = new TimeOfDayValue(20, 10, 5, 987);
    assertEquals(value.toString(),  "20:10:05.987");
    value = TimeOfDayValue.getNullValue();
    assertEquals(value.toString(),  "null");
  }

  public void testGetHours() {
    TimeOfDayValue value = new TimeOfDayValue(12, 23, 31, 134);
    assertTrue(value.getHours() == 12);
  }

  public void testGetMinutes() {
    TimeOfDayValue value = new TimeOfDayValue(12, 23, 31, 134);
    assertTrue(value.getMinutes() == 23);
  }

  public void testGetSeconds() {
    TimeOfDayValue value = new TimeOfDayValue(12, 23, 31, 134);
    assertTrue(value.getSeconds() == 31);
  }

  public void testGetMilliseconds() {
    TimeOfDayValue value = new TimeOfDayValue(12, 23, 31, 134);
    assertTrue(value.getMilliseconds() == 134);
  }

  public void testGetHoursNull(){
    TimeOfDayValue val = TimeOfDayValue.getNullValue();
    try {
      val.getHours();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testGetMinutesNull(){
    TimeOfDayValue val = TimeOfDayValue.getNullValue();
    try {
      val.getMinutes();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testGetSecondsNull(){
    TimeOfDayValue val = TimeOfDayValue.getNullValue();
    try {
      val.getSeconds();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testGetMilliNull(){
    TimeOfDayValue val = TimeOfDayValue.getNullValue();
    try {
      val.getMilliseconds();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testCompare(){
    // Test hours.
    TimeOfDayValue val01 = new TimeOfDayValue(21, 23, 50, 500);
    TimeOfDayValue val02 = new TimeOfDayValue(20, 23, 50, 500);
    TimeOfDayValue val03 = new TimeOfDayValue(22, 23, 50, 500);

    assertTrue(val01.compareTo(val02) > 0);
    assertTrue(val01.compareTo(val03) < 0);

    // Test minutes.
    TimeOfDayValue val11 = new TimeOfDayValue(21, 23, 50, 500);
    TimeOfDayValue val12 = new TimeOfDayValue(21, 20, 50, 500);
    TimeOfDayValue val13 = new TimeOfDayValue(21, 24, 50, 500);

    assertTrue(val11.compareTo(val12) > 0);
    assertTrue(val11.compareTo(val13) < 0);

    // Test seconds.
    TimeOfDayValue val31 = new TimeOfDayValue(21, 23, 50, 500);
    TimeOfDayValue val32 = new TimeOfDayValue(21, 23, 40, 500);
    TimeOfDayValue val33 = new TimeOfDayValue(21, 23, 55, 500);

    assertTrue(val31.compareTo(val32) > 0);
    assertTrue(val31.compareTo(val33) < 0);

    // Test milliseconds.
    TimeOfDayValue val41 = new TimeOfDayValue(21, 23, 50, 600);
    TimeOfDayValue val42 = new TimeOfDayValue(21, 23, 50, 500);
    TimeOfDayValue val43 = new TimeOfDayValue(21, 23, 50, 780);

    assertTrue(val41.compareTo(val42) > 0);
    assertTrue(val41.compareTo(val43) < 0);

    // Test equals
    TimeOfDayValue val51 = new TimeOfDayValue(13, 3, 50, 500);
    TimeOfDayValue val52 = new TimeOfDayValue(13, 3, 50, 500);
    TimeOfDayValue val53 = new TimeOfDayValue(10, 20, 10);
    TimeOfDayValue val54 = new TimeOfDayValue(10, 20, 10);
    TimeOfDayValue val55 = new TimeOfDayValue(10, 20, 10, 0);

    assertTrue(val51.compareTo(val52) == 0);
    assertTrue(val53.compareTo(val54) == 0);
    assertTrue(val53.compareTo(val55) == 0);
  }

  public void testCompareNullCases() {
    // Test null cases and classCast issues.
    TimeOfDayValue val = new TimeOfDayValue(13, 3, 50, 500);
    try {
      val.compareTo(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior.
    }

    try {
      val.compareTo(BooleanValue.FALSE);
      fail();
    } catch (ClassCastException e) {
      // Expected behavior.
    }

    // Test NULL_VALUE cases.
    TimeOfDayValue val1 = new TimeOfDayValue(12, 23, 1, 1);
    TimeOfDayValue valNull = TimeOfDayValue.getNullValue();

    assertTrue(0 < val1.compareTo(valNull));
    assertTrue(0 > valNull.compareTo(val));

    // Test same object.
    assertTrue(0 == valNull.compareTo(TimeOfDayValue.getNullValue()));
    assertTrue(0 == val.compareTo(val));

    // Test that compareTo can cast.
    Value val2 = new TimeOfDayValue(13, 43, 50, 500);
    assertTrue(0 > val.compareTo(val2));
    Value val3 = TimeOfDayValue.getNullValue();
    assertTrue(0 < val.compareTo(val3));
  }

  /**
   * Checks that the hasCode behaves in a reasonable way and does not map 3 different values to the
   * same key.
   * However,  since this might be the case for some different hashCode function
   * then in that case these 3 values should be replaced.
   */
  public void testHashCode() {
    TimeOfDayValue val1 = new TimeOfDayValue(10, 20, 20, 100);
    TimeOfDayValue val2 = new TimeOfDayValue(1, 12, 20, 150);
    TimeOfDayValue val3 = new TimeOfDayValue(10, 20, 25);

    assertFalse((val1.hashCode() == val2.hashCode())
        || (val1.hashCode() == val3.hashCode()));

    TimeOfDayValue val = TimeOfDayValue.getNullValue();
    assertTrue(val.hashCode() == 0);
  }

  public void testGetValueToFormat() {
    TimeOfDayValue val1 = new TimeOfDayValue(12, 23, 12, 111);
    TimeOfDayValue nullVal = TimeOfDayValue.getNullValue();

    assertNull(nullVal.getObjectToFormat());
    GregorianCalendar cal = new GregorianCalendar(1899, 11, 30, 12, 23, 12);
    cal.set(Calendar.MILLISECOND, 111);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    assertEquals(cal, val1.getObjectToFormat());
  }

  public void testToQueryString() {
    TimeOfDayValue val1 = new TimeOfDayValue(12, 23, 12, 111);
    TimeOfDayValue val2 = new TimeOfDayValue(2, 3, 4);

    assertEquals("TIMEOFDAY '12:23:12.111'", val1.toQueryString());
    assertEquals("TIMEOFDAY '2:3:4'", val2.toQueryString());
  }
}