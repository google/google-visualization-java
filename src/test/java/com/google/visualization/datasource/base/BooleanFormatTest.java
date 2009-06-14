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

package com.google.visualization.datasource.base;

import junit.framework.TestCase;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * BooleanFormat Tester.
 *
 * @author Hillel M.
 */
public class BooleanFormatTest extends TestCase {

  private Boolean trueValue = Boolean.TRUE;

  private Boolean falseValue = Boolean.FALSE;

  private BooleanFormat boolFormat;

  private BooleanFormat boolFormat1;

  private BooleanFormat boolFormat2;

  private BooleanFormat boolFormat3;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    boolFormat = new BooleanFormat();
    boolFormat1 = new BooleanFormat("yes", "no");
    boolFormat2 = new BooleanFormat("T:F");
    boolFormat3 = new BooleanFormat("X", "O");
  }

  public void testConstructor() {
    BooleanFormat booleanFormat = new BooleanFormat("1", "0");
    assertNotNull(booleanFormat);
  }

  // The method UFormat.format() calls
  // BooleanFormat.format(Object, StringBuffer, FieldPosition)
  public void testFormat() {
    assertEquals("true", boolFormat.format(trueValue));
    assertEquals("yes", boolFormat1.format(trueValue));
    assertEquals("T", boolFormat2.format(trueValue));
    assertEquals("X", boolFormat3.format(trueValue));

    assertEquals("false", boolFormat.format(falseValue));
    assertEquals("no", boolFormat1.format(falseValue));
    assertEquals("F", boolFormat2.format(falseValue));
    assertEquals("O", boolFormat3.format(falseValue));
  }


  public void testFormatThrowsException() {
    FieldPosition pos = new FieldPosition(0);
    StringBuffer sb = new StringBuffer();
    
    try {
      boolFormat.format(Integer.valueOf(7));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected.
    }
    
    boolFormat.format(null);
    assertTrue(0 == pos.getBeginIndex());
    assertTrue(0 == pos.getEndIndex());
    
    try {
      // StringBuffer is null.
      boolFormat.format(trueValue, null, pos);
      fail();
    } catch (NullPointerException e) {
      assertTrue(0 == pos.getBeginIndex());
      assertTrue(0 == pos.getEndIndex());
    }
    try {
      // FieldPosition is null.
      boolFormat.format(trueValue, sb, null);
      fail();
    } catch (NullPointerException e) {
      // OK.
    }
    pos = new FieldPosition(0);
    sb = new StringBuffer();
    // Object is null.
    boolFormat.format(null, sb, pos);
    assertTrue(0 == pos.getBeginIndex());
    assertTrue(0 == pos.getEndIndex());
    assertTrue(0 == sb.length());
    
    pos = new FieldPosition(0);
    try {
      // Object is not instance of Boolean.
      boolFormat.format(new Object(), new StringBuffer(), pos);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(0 == pos.getBeginIndex());
      assertTrue(0 == pos.getEndIndex());
      assertTrue(0 == sb.length());
    }
  }

  public void testFieldPosition() {

    // Boolean trueValue.
    FieldPosition pos = new FieldPosition(0);
    StringBuffer sb = new StringBuffer();
    boolFormat.format(trueValue, sb, pos);
    assertTrue(0 == pos.getBeginIndex());
    assertTrue(("true".length() - 1) == pos.getEndIndex());

    // Boolean falseValue.
    pos = new FieldPosition(0);
    sb = new StringBuffer();
    boolFormat.format(falseValue, sb, pos);
    assertTrue(0 == pos.getBeginIndex());
    assertTrue(("false".length() - 1) == pos.getEndIndex());
  }

  public void testParse() throws ParseException {

    // == Boolean trueValue.
    assertEquals(trueValue, boolFormat.parseObject("true"));
    // non-case sensitive.
    assertEquals(trueValue, boolFormat.parseObject("tRuE"));
    assertEquals(trueValue, boolFormat1.parseObject("yes"));
    assertEquals(trueValue, boolFormat2.parseObject("T"));
    assertEquals(trueValue, boolFormat3.parseObject("X"));

    // == Boolean falseValue.
    assertEquals(falseValue, boolFormat.parseObject("false"));
    assertEquals(falseValue, boolFormat1.parseObject("no"));
    // non-case sensitive.
    assertEquals(falseValue, boolFormat1.parseObject("nO"));
    assertEquals(falseValue, boolFormat2.parseObject("F"));
    assertEquals(falseValue, boolFormat3.parseObject("O"));
  }

  public void testParseNullCases() {
    ParsePosition pos = new ParsePosition(0);
    try {
      boolFormat.parseObject("true", null);
      fail();
    } catch (NullPointerException e) {
      // OK.
    }
    try {
      boolFormat.parseObject(null, pos);
      fail();
    } catch (NullPointerException e) {
      // OK.
    }
    Object value = boolFormat.parseObject("not a good string", pos);
    assertNull(value);
    assertTrue(0 == pos.getErrorIndex());
  }

  public void testParseParsePosition() {
    ParsePosition pos = new ParsePosition(0);
    boolFormat.parseObject("true", pos);
    assertTrue("true".length() == pos.getIndex());

    pos = new ParsePosition(0);
    boolFormat.parseObject("false", pos);
    assertTrue("false".length() == pos.getIndex());
  }
}
