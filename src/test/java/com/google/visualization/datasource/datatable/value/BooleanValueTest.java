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
 * Test for BooleanValue
 *
 * @author Yoah B.D.
 */
public class BooleanValueTest extends TestCase {
  BooleanValue t;
  BooleanValue f;
  BooleanValue n;

  @Override
  public void setUp() {
    t = BooleanValue.TRUE;
    f = BooleanValue.FALSE;
    n = BooleanValue.getNullValue();
  }

  public void testValue() {
    assertTrue(t.getValue());
    assertFalse(f.getValue());
  }

  public void testFormatting() {
    assertEquals("true", t.toString());
    assertEquals("false", f.toString());
  }

  public void testNull() throws Exception {
    assertEquals("null", n.toString());
    assertTrue(n.isNull());

    assertFalse(f.isNull());
  }

  public void testNullValueException() throws Exception {
    try {
      n.getValue();
      fail("Shouldn't get here - should have thrown a null value exception");
    } catch (NullValueException nve) {
      // This is the expected behavior.
    }
  }

  public void testCompare() {
    BooleanValue t2 = BooleanValue.TRUE;
    BooleanValue f2 = BooleanValue.FALSE;

    assertTrue(t.compareTo(f) > 0);
    assertTrue(f.compareTo(t) < 0);
    assertTrue(t.compareTo(t) == 0);
    assertTrue(f.compareTo(f) == 0);
    assertTrue(t.compareTo(t2) == 0);
    assertTrue(f.compareTo(f2) == 0);

    assertTrue(t.compareTo(BooleanValue.getNullValue()) > 0);
    assertTrue(f.compareTo(BooleanValue.getNullValue()) > 0);
  }

  public void testComapreNullCases() {
     // Test null cases and classCast issues.
    BooleanValue val = BooleanValue.TRUE;
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
    BooleanValue val1 = BooleanValue.FALSE;
    BooleanValue valNull = BooleanValue.getNullValue();

    assertTrue(0 < val1.compareTo(valNull));
    assertTrue(0 > valNull.compareTo(val));

    // Test same object.
    assertTrue(0 == valNull.compareTo(BooleanValue.getNullValue()));
    assertTrue(0 == val.compareTo(val));

    // Test that compareTo can cast.
    Value val2 = BooleanValue.TRUE;
    assertTrue(0 == val.compareTo(val2));
    Value val3 = BooleanValue.getNullValue();
    assertTrue(0 < val.compareTo(val3));
  }

  public void testEquals() {
    BooleanValue t2 = BooleanValue.TRUE;
    BooleanValue f2 = BooleanValue.FALSE;
    BooleanValue null1 = BooleanValue.getNullValue();
    BooleanValue null2 = BooleanValue.getNullValue();

    assertTrue(t.equals(t));
    assertTrue(t.equals(t2));
    assertTrue(f.equals(f));
    assertTrue(f.equals(f2));
    assertTrue(null1.equals(null1));
    assertTrue(null1.equals(null2));

    assertFalse(t.equals(f));
    assertFalse(f.equals(t));
    assertFalse(t.equals(null1));
    assertFalse(f.equals(null1));
    assertFalse(null1.equals(t));
    assertFalse(null2.equals(t2));

    // Check interaction with other classes:
    assertFalse(t.equals(Integer.valueOf(7)));
    assertFalse(null1.equals(NumberValue.getNullValue()));
  }

  public void testHashCode() {
    assertTrue(1 == t.hashCode());
    assertTrue(0 ==  f.hashCode());
    assertTrue(-1 ==  n.hashCode());

  }

  public void testGetValueToFormat () {
    assertEquals(Boolean.TRUE, t.getObjectToFormat());
    assertEquals(Boolean.FALSE, f.getObjectToFormat());
    assertNull(n.getObjectToFormat());
  }

  public void testGetType() {
    assertEquals(ValueType.BOOLEAN, t.getType());
    assertEquals(ValueType.BOOLEAN, f.getType());
    assertEquals(ValueType.BOOLEAN, n.getType());
  }

  public void testToQueryString() {
    assertEquals("true", t.toQueryString());
    assertEquals("false", f.toQueryString());
    try {
      n.toQueryString();
      fail("Should have thrown an exception.");
    } catch (RuntimeException e) {
      // Expected behavior.
    }
  }
}