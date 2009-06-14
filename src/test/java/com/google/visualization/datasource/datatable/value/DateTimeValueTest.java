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

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import junit.framework.TestCase;

/**
 * Test for DateTimeValue.
 *
 * @author Hillel M.
 */
public class DateTimeValueTest extends TestCase {

  public void testNullValue() {
    DateTimeValue value = DateTimeValue.getNullValue();
    assertTrue(value.isNull());
    value = new DateTimeValue(2007, 10, 11, 23, 34, 56, 609);
    assertFalse(value.isNull());
  }


  public void testConstructorOutOFRangeValuesMonth() {
    // Check that the Exception is thrown.
    try {
      new DateTimeValue(2007, 13, 11, 23, 34, 56, 609);      
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      new DateTimeValue(2007, -1, 11, 23, 34, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testConstructorOutOfRangeValuesDayOfMonth() {
    // Check that the Exception is thrown.
    try {
      new DateTimeValue(2007, 10, 32, 23, 34, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      new DateTimeValue(2007, 10, 0, 23, 34, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testConstructorOutOfRangeValuesHours() {
    // Check that the Exception is thrown.
    try {
      new DateTimeValue(2007, 8, 20, 24, 34, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      new DateTimeValue(2007, 8, 20, -1, 34, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testConstructorOutOfRangeValuesMinutes() {
    // Check that the Exception is thrown.
    try {
      new DateTimeValue(2007, 7, 11, 14, 62, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      new DateTimeValue(2007, 7, 11, 14, -1, 56, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testConstructorOutOfRangeValuesSeconds() {
    // Check that the Exception is thrown.
    try {
      new DateTimeValue(2007, 8, 11, 23, 34, 63, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      new DateTimeValue(2007, 8, 11, 23, 34, -10, 609);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testConstructorOutOfRangeValuesMilliseconds() {
    // Check that the Exception is thrown.
    try {
      new DateTimeValue(2007, 7, 11, 23, 34, 56, 1000);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      new DateTimeValue(2007, 7, 11, 23, 34, 56, -1);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }


  public void testConstructorSpecialValuesOfDate() {
    try {
      // February doesn't have 30 days.
      new DateTimeValue(1990, 1, 30, 23, 11, 11, 100);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      // February doesn't have 29 days in 2007.
      new DateTimeValue(2007, 1, 29, 23, 10, 10, 200);
      fail();
    } catch (IllegalArgumentException e) {
    }
    try {
      // September doesn't have 31 days.
      new DateTimeValue(2007, 8, 31, 11, 5, 5, 5);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  /**
   * Months in DateValue are represented by the numbers {0, .., 11} as in Java.
   */
  public void testJavaMonthConvention() {
    // Check the Exception is thrown.
    try {
      new DateTimeValue(1990, 12, 10, 1, 1, 1, 1);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void testTimestampConstructor() {
    // Test if the construction does not fail.
    try {
      // 1st of February 2300.
      DateTimeValue value = new DateTimeValue(2300, 1, 1, 15, 15, 15, 150);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // Should not be here
      assertFalse("An exception was not supposed to be thorwn", true);
    }
    try {
      // 1st of January 130.
      DateTimeValue value = new DateTimeValue(130, 0, 1, 0, 0, 0, 0);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // Should not be here
      assertFalse("An exception was not supposed to be thorwn. "
          + e.getMessage(), true);
    }
    // This is the 29th of February of 2009 which is a real date.
    try {
      DateTimeValue value = new DateTimeValue(2008, 1, 29, 15, 12, 10, 600);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // should not be here
      assertFalse("An exception was not supposed to be thorwn", true);
    }
  }

  public void testCalendarConstructor() {
    GregorianCalendar calendar = new GregorianCalendar(2006, 1, 1, 21, 24, 25);
    calendar.set(GregorianCalendar.MILLISECONDS_IN_DAY, 222);
    DateTimeValue value = null;
    calendar.setTimeZone(TimeZone.getTimeZone("IST"));
    // Check exception thrown for non GMT time zone.
    try {
      value = new DateTimeValue(calendar);
      fail();
    } catch (IllegalArgumentException iae) {
      // do nothing
    }
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    // Check exception not thrown for GMT time zone.
    try {
      value = new DateTimeValue(calendar);
    } catch (IllegalArgumentException iae) {
      fail();
    }
    // Verify values - default milliseconds.
    assertEquals(calendar, value.getCalendar());
  }

  public void testGetType() {
    DateTimeValue value = new DateTimeValue(2007, 7, 11, 23, 34, 56, 100);
    assertEquals(value.getType(), ValueType.DATETIME);
    value = DateTimeValue.getNullValue();
    assertEquals(value.getType(), ValueType.DATETIME);

  }

  public void testToString() {
    DateTimeValue value = DateTimeValue.getNullValue();
    assertEquals(value.toString(), "null");
    value = new DateTimeValue(2007, 7, 11, 23, 34, 56, 100);
    assertEquals("2007-08-11 23:34:56.100", value.toString());

    value = new DateTimeValue(1500, 3, 3, 12, 12, 12, 12);
    assertEquals("1500-04-03 12:12:12.012", value.toString());
  }

  public void testCompare(){
    DateTimeValue val1 = DateTimeValue.getNullValue();
    DateTimeValue val2 = new DateTimeValue(1500, 3, 3, 12, 12, 12, 12);
    DateTimeValue val3 = new DateTimeValue(1500, 3, 3, 12, 12, 12, 12);
    DateTimeValue val4 = new DateTimeValue(1499, 3, 3, 10, 12, 12, 12);

    // Equals.
    assertTrue(val2.compareTo(val3) == 0);
    assertTrue(val2.compareTo(val2) == 0);

    // One null one not.
    assertTrue(val2.compareTo(val1) > 0);
    assertTrue(val1.compareTo(val2) < 0);

    // Sanity test for GregorianCalendar.compareTo()
    assertTrue(val3.compareTo(val4) > 0);
  }

   public void testCompareNullCases() {
   // Test null cases and classCast issues.
    DateTimeValue val = new DateTimeValue(10, 10, 10, 10, 10, 10 ,10);
    try {
      val.compareTo(null);
      fail();
    } catch (NullPointerException e) {
    }
    try {
      val.compareTo(new NumberValue(123));
      fail();
    } catch (ClassCastException e) {
    }

    // Test NULL_VALUE cases.
    DateTimeValue val1 = new DateTimeValue(1, 1, 1, 1, 1, 1, 1);
    DateTimeValue valNull = DateTimeValue.getNullValue();

    assertTrue(0 < val1.compareTo(valNull));
    assertTrue(0 > valNull.compareTo(val));

    // Test same object.
    assertTrue(0 == valNull.compareTo(DateTimeValue.getNullValue()));
    assertTrue(0 == val.compareTo(val));

    // Test that compareTo can cast.
    Value val2 = new DateTimeValue(10, 10, 10, 15, 10, 10 ,10);
    assertTrue(0 > val.compareTo(val2));
    Value val3 = DateTimeValue.getNullValue();
    assertTrue(0 < val.compareTo(val3));
  }

  public void testGetCalendar() {
    DateTimeValue val1 = new DateTimeValue(1499, 3, 3, 10, 12, 12, 12);
    DateTimeValue val2 = DateTimeValue.getNullValue();
    assertNotNull(val1.getCalendar());
    try {
      val2.getCalendar();
      assertFalse(true);
    } catch (NullValueException e) {
    }
  }

  /**
   * Checks that the hashCode behaves in a reasonable way and does not map 3 different values to the
   * same key.
   * However,  since this might be the case for some different hashCode function
   * then in that case these 3 values should be replaced.
   */
  public void testHashCode() {
    DateTimeValue val1 = new DateTimeValue(1500, 3, 3, 12, 12, 12, 12);
    DateTimeValue val2 = new DateTimeValue(2400, 7, 3, 12, 18, 12, 129);
    DateTimeValue val3 = new DateTimeValue(1900, 3, 3, 12, 2, 2, 250);

    assertFalse((val1.hashCode() == val2.hashCode())
        || (val1.hashCode() == val3.hashCode()));

    DateTimeValue val = DateTimeValue.getNullValue();
    assertTrue(val.hashCode() == 0);
  }

  public void testGetValueToFormat() {
    DateTimeValue val = new DateTimeValue(2020,3,12,2,31,12,111);
    DateTimeValue valNull = DateTimeValue.getNullValue();

    GregorianCalendar g = new GregorianCalendar(2020,3,12,2,31,12);
    g.set(GregorianCalendar.MILLISECOND, 111);
    g.setTimeZone(TimeZone.getTimeZone("GMT"));

    assertNull(valNull.getObjectToFormat());
    assertEquals(g, val.getObjectToFormat());
  }

  public void testToQueryString() {
    DateTimeValue val1 = new DateTimeValue(2020, 3, 12, 2, 31, 12, 123);
    DateTimeValue val2 = new DateTimeValue(2007, 5, 6, 7, 8, 9, 0);

    assertEquals("DATETIME '2020-4-12 2:31:12.123'", val1.toQueryString());
    assertEquals("DATETIME '2007-6-6 7:8:9'", val2.toQueryString());
  }
}