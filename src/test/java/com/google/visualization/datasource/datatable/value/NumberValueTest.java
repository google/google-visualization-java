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

import junit.framework.TestCase;

/**
 * Test for NumberValue.
 *
 * @author Yoah B.D.
 */
public class NumberValueTest extends TestCase {

  public void testValue() {
    double[] values = {0, -1, 100, 3.2, -12.3};
    for (double v : values) {
      NumberValue cell = new NumberValue(v);
      assertEquals(v, cell.getValue());
    }
  }

  public void testFormatting() {
    double[] values = {0, -1, 100, 3.2, -12.3, .01};
    for (double v : values) {
      NumberValue cell = new NumberValue(v);
      assertEquals(Double.toString(v), cell.toString());
    }
  }

  public void testNull() throws Exception {
    NumberValue n = NumberValue.getNullValue();
    assertEquals("null", n.toString());
    assertTrue(n.isNull());

    NumberValue a = new NumberValue(1);
    assertFalse(a.isNull());
  }

  public void testCompare() {
    NumberValue c0 = new NumberValue(0);
    NumberValue c1 = new NumberValue(1);
    NumberValue c2 = new NumberValue(2);
    NumberValue c1Copy = new NumberValue(1);
    NumberValue m1 = new NumberValue(-0.1);
    NumberValue m2 = new NumberValue(-0.2);

    assertTrue(c0.compareTo(c1) < 0);
    assertTrue(c2.compareTo(c1) > 0);
    assertTrue(c2.compareTo(c2) == 0);
    assertTrue(c1.compareTo(c1Copy) == 0);
    assertTrue(m1.compareTo(m2) > 0);
    assertTrue(m2.compareTo(m1) < 0);

    assertTrue(m2.compareTo(NumberValue.getNullValue()) > 0);
    assertTrue(NumberValue.getNullValue().compareTo(c1) < 0);
  }

   public void testComapreNullCases() {
    // Test null cases and classCast issues.
    NumberValue val = new NumberValue(1234);
    try {
      val.compareTo(null);
      fail();
    } catch (NullPointerException e) {
      // Expected behavior.
    }
    try {
      val.compareTo(new DateValue(123, 1, 21));
      fail();
    } catch (ClassCastException e) {
      // Expected behavior.
    }

    // Test NULL_VALUE cases.
    NumberValue val1 = new NumberValue(-222);
    NumberValue valNull = NumberValue.getNullValue();

    assertTrue(0 < val1.compareTo(valNull));
    assertTrue(0 > valNull.compareTo(val));

    // Test same object.
    assertTrue(0 == valNull.compareTo(NumberValue.getNullValue()));
    assertTrue(0 == val.compareTo(val));

    // Test that compareTo can cast.
    Value val2 = new NumberValue(4321);
    assertTrue(0 > val.compareTo(val2));
    Value val3 = NumberValue.getNullValue();
    assertTrue(0 < val.compareTo(val3));
  }

  /**
   * Test the equals() method of NumberValue.
   */
  public void testEquals() {
    NumberValue n1 = new NumberValue(-17.3);
    NumberValue n2 = new NumberValue(-17.3);
    NumberValue zero = new NumberValue(0);
    NumberValue nullV = NumberValue.getNullValue();

    assertTrue(n1.equals(n1));
    assertTrue(n1.equals(n2));
    assertTrue(nullV.equals(nullV));

    assertFalse(n1.equals(zero));
    assertFalse(n1.equals(nullV));
    assertFalse(nullV.equals(zero));

    // Check interaction with other classes.
    assertFalse(n1.equals(new Double(-17.3)));
    assertFalse(nullV.equals(BooleanValue.getNullValue()));
  }

  public void testGetColumnType() {
    NumberValue n1 = new NumberValue(-17.3);
    NumberValue nullV = NumberValue.getNullValue();

    assertEquals(ValueType.NUMBER, n1.getType());
    assertEquals(ValueType.NUMBER, nullV.getType());
  }

  public void testGetValue() {
    NumberValue n1 = new NumberValue(-2131217.3121121);
    NumberValue nullV = NumberValue.getNullValue();

    assertEquals(-2131217.3121121, n1.getValue());
    try {
      nullV.getValue();
      fail();
    } catch (NullValueException e) {
      // Expected behavior.
    }
  }

  public void testToString() {
    NumberValue n1 = new NumberValue(-2131217.3121121);
    NumberValue nullV = NumberValue.getNullValue();

    assertEquals(Double.toString(-2131217.3121121), n1.toString());
    assertEquals("null", nullV.toString());
  }

  public void testIsNull() {
    NumberValue n1 = new NumberValue(-2131217.3121121);
    NumberValue nullV = NumberValue.getNullValue();

    assertFalse(n1.isNull());
    assertTrue(nullV.isNull());
  }

  public void testHashCode() {
    NumberValue n1 = new NumberValue(-2131217.3121121);
    NumberValue nullV = NumberValue.getNullValue();

    assertTrue(0 == nullV.hashCode());
    assertTrue(new Double(-2131217.3121121).hashCode() == n1.hashCode());
  }

  public void testGetValueToFormat() {
    NumberValue n1 = new NumberValue(-2131217.3121121);
    NumberValue nullV1 = NumberValue.getNullValue();
    NumberValue nullV2 = new NumberValue(0);

    assertEquals(-2131217.3121121, n1.getObjectToFormat());
    assertNull(nullV1.getObjectToFormat());
    assertEquals((double) 0, nullV2.getObjectToFormat());
  }

  public void testToQueryString() {
    NumberValue n1 = new NumberValue(-123);
    NumberValue n2 = new NumberValue(0);
    NumberValue n3 = new NumberValue(-12.45);

    assertEquals("-123.0", n1.toQueryString());
    assertEquals("0.0", n2.toQueryString());
    assertEquals("-12.45", n3.toQueryString());
  }
}