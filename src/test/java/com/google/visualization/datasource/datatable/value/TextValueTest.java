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

import com.ibm.icu.util.ULocale;
import junit.framework.TestCase;

import java.util.Comparator;

/**
 * Test for TextValue
 *
 * @author Yoah B.D.
 */
public class TextValueTest extends TestCase {

  public void testValue() {
    String v = "abc";
    TextValue cell = new TextValue(v);
    assertEquals(v, cell.toString());
  }

  public void testCompare() {
    TextValue abc = new TextValue("abc");
    TextValue def = new TextValue("def");
    TextValue empty = new TextValue("");
    TextValue abc_copy = new TextValue("abc");

    assertTrue(abc.compareTo(def) < 0);
    assertTrue(def.compareTo(abc) > 0);
    assertTrue(abc.compareTo(abc) == 0);
    assertTrue(abc.compareTo(abc_copy) == 0);
    assertTrue(abc.compareTo(empty) > 0);
  }

  /**
   * Tests the equals() method in TextValue.
   */
  public void testEquals() {
    TextValue abc1 = new TextValue("abc");
    TextValue abc2 = new TextValue("abc");
    TextValue empty1 = new TextValue("");
    TextValue empty2 = new TextValue("");

    assertTrue(abc1.equals(abc1));
    assertTrue(abc1.equals(abc2));
    assertTrue(empty1.equals(empty1));
    assertTrue(empty1.equals(empty2));

    assertFalse(abc1.equals(empty1));
    assertFalse(empty1.equals(abc1));

    // Check interaction with other classes (e.g., String).
    assertFalse(abc1.equals("abc"));
  }

  public void testGetType() {
    TextValue val = new TextValue("asdf");
    assertEquals(ValueType.TEXT, val.getType());
  }

  public void testToString() {
    TextValue val1 = new TextValue("val1");
    TextValue emptyValue = new TextValue("");

    assertEquals("val1", val1.toString());
    assertEquals("", emptyValue.toString());
  }

  public void testHashCode() {
    TextValue asdfghjkl = new TextValue("asdfghjkl");
    TextValue emptyValue = new TextValue("");

    assertTrue("asdfghjkl".hashCode() == asdfghjkl.hashCode());
    assertTrue("".hashCode() == emptyValue.hashCode());
  }

  public void testGetValueToFormat() {
    TextValue testVal = new TextValue("goboazmauda");
    TextValue emptyValue = new TextValue("");

    assertEquals("goboazmauda", testVal.getObjectToFormat());
    assertEquals("", emptyValue.getObjectToFormat());
  }

  public void testComapreNullCases() {
    // Test null cases and classCast issues.
    TextValue val = new TextValue("go/");
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
    // Test same object.
    assertTrue(0 == val.compareTo(val));

    // Test that compareTo can cast.
    Value val2 = new TextValue("go/");
    assertTrue(0 == val.compareTo(val2));
  }

  public void testIsNull() {
    TextValue val = TextValue.getNullValue();
    TextValue val1 = new TextValue("cheers");
    assertTrue(val.isNull());
    assertFalse(val1.isNull());
  }

  /**
   * Tests the localized comparator.
   */
  public void testLocalizedComparator() {
    // Test that strings are compared according to the given locale.
    Comparator<TextValue> rootComparator = TextValue.getTextLocalizedComparator(
        ULocale.ROOT);
    Comparator<TextValue> frComparator = TextValue.getTextLocalizedComparator(
        ULocale.FRENCH);
    TextValue text1 = new TextValue("cot\u00E9");
    TextValue text2 = new TextValue("c\u00F4te");
    assertEquals(-1, rootComparator.compare(text1, text2));
    assertEquals(1, frComparator.compare(text1, text2));
  }

  public void testToQueryString() {
    TextValue t1 = new TextValue("foo bar");
    TextValue t2 = new TextValue("foo\"bar");
    TextValue t3 = new TextValue("foo'bar");
    TextValue t4 = new TextValue("both \" and '");

    assertEquals("\"foo bar\"", t1.toQueryString());
    assertEquals("'foo\"bar'", t2.toQueryString());
    assertEquals("\"foo'bar\"", t3.toQueryString());

    try {
      t4.toQueryString();
      fail("Should have thrown an exception.");
    } catch (RuntimeException e) {
      // Everything's OK.
    }
  }
}