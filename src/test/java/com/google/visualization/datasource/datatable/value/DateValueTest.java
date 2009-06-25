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
 * Test for DateValue.
 *
 * @author Hillel M.
 */
public class DateValueTest extends TestCase {

  public void testNullValue() {
    DateValue value = DateValue.getNullValue();
    assertTrue(value.isNull());
    value = new DateValue(1990, 2, 1);
    assertFalse(value.isNull());
  }

  /**
   * Check that out of range dayOfMonth  values are not accepted
   * and an exception is thrown.
   */
  public void testConstructorOutOFRangeValuesDayOfMonth() {
    try {
      new DateValue(1990, 11, -1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      DateValue value = new DateValue(1990, 11, 0);
      // Shouldn't be here.
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      new DateValue(1990, 11, 32);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
  }

  /**
   * Check that out of range month values are not accepted
   * and an exception is thrown.
   */
  public void testConstructorOutOFRangeValuesMonth() {
    try {
      new DateValue(1990, 13, 1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      new DateValue(1990, -2, 1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
  }

  public void testConstructorSpecialDatesValues() {
    try {
      // February doesn't have 30 days.
      new DateValue(1990, 1, 30);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      // February doesn't have 29 days in 2007.
      new DateValue(2007, 1, 29);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
    try {
      // September doesn't have 31 days.
      new DateValue(2007, 8, 31);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
  }

  /**
   * Months in DateValue are represented by the numbers {0, .., 11} as in Java.
   */
  public void testJavaMonthConvention() {
    // Check the Exception is thrown.
    try {
      new DateValue(1990, 12, 10);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
  }

  public void testDateConstructor() {
    // Test if the construction does not fail.
    try {
      // 1st of February 2300.
      DateValue value = new DateValue(2300, 1, 1);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // Should not be here
      assertFalse("An exception was not supposed to be thorwn",  true);
    }
    try {
      // 1st of January 130.
      DateValue value = new DateValue(130, 0, 1);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // Should not be here
      assertFalse("An exception was not supposed to be thorwn",  true);
    }
    // This is the 29th of February of 2009 which is a real date.
    try {
      DateValue value = new DateValue(2008, 1, 29);
      assertNotNull(value);
      assertFalse(value.isNull());
    } catch (IllegalArgumentException e) {
      // Should not be here
      assertFalse("An exception was not supposed to be thorwn",  true);
    }
  }

  public void testCalendarConstructor() {
    GregorianCalendar calendar = new GregorianCalendar(2006, 1, 3);
    DateValue value = null;
    calendar.setTimeZone(TimeZone.getTimeZone("IST"));
    // Check exception thrown for non GMT time zone.
    try {
      value = new DateValue(calendar);
      fail();
    } catch (IllegalArgumentException iae) {
      // Expected behavior.
    }
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    // Check that an exception is not thrown for GMT time zone.
    try {
      value = new DateValue(calendar);
    } catch (IllegalArgumentException iae) {
      fail();
    }
    // Verify values - default milliseconds.
    assertEquals(2006, value.getYear());
    assertEquals(1, value.getMonth());
    assertEquals(3, value.getDayOfMonth());
  }

  public void testGetType() {
    DateValue value = new DateValue(1700, 2, 4);
    assertEquals(value.getType(),  ValueType.DATE);
    value = DateValue.getNullValue();
    assertEquals(value.getType(),  ValueType.DATE);
  }

  public void testToString() {
    DateValue value = new DateValue(500, 4, 20);
    assertEquals(value.toString(),  "500-05-20");
    value = new DateValue(2200, 3, 11);
    assertEquals(value.toString(),  "2200-04-11");
    value = DateValue.getNullValue();
    assertEquals(value.toString(),  "null");
  }

  public void testlGetYear() {
    DateValue value = new DateValue(4000, 1, 4);
    assertTrue(value.getYear() == 4000);
  }

  public void testGetMonth() {
    // Note that the month is August and not July in this case.
    DateValue value = new DateValue(2020, 7, 11);
    assertTrue(value.getMonth() == 7);
  }

  public void testlGetDayOfMonth() {
    DateValue value = new DateValue(1414, 9, 17);
    assertTrue(value.getDayOfMonth() == 17);
  }

  public void testGetYearNull(){
    DateValue val = DateValue.getNullValue();
    try {
      val.getYear();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testGetMonthNull(){
    DateValue val = DateValue.getNullValue();
    try {
      val.getMonth();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testGetDayOfMonthNull(){
    DateValue val = DateValue.getNullValue();
    try {
      val.getDayOfMonth();
      assertFalse(true);
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testCompare(){
    // Test hours.
    DateValue val01 = new DateValue(1599, 11, 10);
    DateValue val02 = new DateValue(1500, 11, 10);
    DateValue val03 = new DateValue(1690, 11, 10);

    assertTrue(val01.compareTo(val02) > 0);
    assertTrue(val01.compareTo(val03) < 0);

    // Test minutes.
    DateValue val11 = new DateValue(1599, 8, 10);
    DateValue val12 = new DateValue(1599, 6, 10);
    DateValue val13 = new DateValue(1599, 10, 10);

    assertTrue(val11.compareTo(val12) > 0);
    assertTrue(val11.compareTo(val13) < 0);

    // Test seconds.
    DateValue val31 = new DateValue(1599, 11, 13);
    DateValue val32 = new DateValue(1599, 11, 11);
    DateValue val33 = new DateValue(1599, 11, 17);

    assertTrue(val31.compareTo(val32) > 0);
    assertTrue(val31.compareTo(val33) < 0);

    // Test equals
    DateValue val51 = new DateValue(3111, 6, 10);
    DateValue val52 = new DateValue(3111, 6, 10);

    assertTrue(val51.compareTo(val52) == 0);
  }

  public void testCompareNullCases() {
    // Test null cases and classCast issues.
    DateValue val = new DateValue(1000, 10, 10);
    try {
      val.compareTo(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior.
    }
    try {
      val.compareTo(new NumberValue(123));
      fail();
    } catch (ClassCastException e) {
      // Expected behavior.
    }

    // Test NULL_VALUE cases.
    DateValue val1 = new DateValue(1111, 1, 1);
    DateValue valNull = DateValue.getNullValue();

    assertTrue(0 < val1.compareTo(valNull));
    assertTrue(0 > valNull.compareTo(val));

    // Test same object.
    assertTrue(0 == valNull.compareTo(DateValue.getNullValue()));
    assertTrue(0 == val.compareTo(val));

    // Test that compareTo can cast.
    Value val2 = new DateValue(1000, 10, 12);
    assertTrue(0 > val.compareTo(val2));
    Value val3 = DateValue.getNullValue();
    assertTrue(0 < val.compareTo(val3));
  }

  public void testGetValueToFormat() {
    DateValue val = new DateValue(500, 2, 30);
    DateValue valNull = DateValue.getNullValue();

    assertNull(valNull.getObjectToFormat());
    GregorianCalendar g = new GregorianCalendar(500, 2, 30);
    g.setTimeZone(TimeZone.getTimeZone("GMT"));
    assertEquals(g, val.getObjectToFormat());

  }
  /**
   * Check that the hashCode behaves in a reasonable way and does not map 3 different values to the
   * same key.
   * However,  since this might be the case for some different hashCode function
   * then in that case these 3 values should be replaced.
   */
  public void testHashCode() {
    DateValue val1 = new DateValue(1900, 0, 1);
    DateValue val2 = new DateValue(1800, 0, 20);
    DateValue val3 = new DateValue(2019, 1,  10);

    assertFalse((val1.hashCode() == val2.hashCode())
        || (val1.hashCode() == val3.hashCode()));

    DateValue val = DateValue.getNullValue();
    assertTrue(val.hashCode() == 0);
  }

  public void testToQueryString() {
    DateValue val1 = new DateValue(2007, 6, 20);
    DateValue val2 = new DateValue(2010, 11, 11);

    assertEquals("DATE '2007-7-20'", val1.toQueryString());
    assertEquals("DATE '2010-12-11'", val2.toQueryString());
  }
}