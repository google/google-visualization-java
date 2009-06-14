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
import java.text.ParsePosition;

/**
 * TextFormat Tester.
 *
 * @author Hillel M.
 */
public class TextFormatTest extends TestCase {

  private TextFormat textFormat;

  private StringBuffer sb;

  private FieldPosition fieldPos;

  private ParsePosition parsePos;

  public void testConstructor() {
    textFormat = new TextFormat();
    assertNotNull(textFormat);
  }

  /**
   * UFormat.format() calls
   * BooleanFormat.format(Object, StringBuffer, FieldPosition)
   */
  public void testFormat() {
    textFormat = new TextFormat();
    assertEquals("String", textFormat.format("String"));
    assertEquals("abcdefghijklmnop!@#$%^&*()",
        textFormat.format("abcdefghijklmnop!@#$%^&*()"));
  }

  public void testFormatThrowsException() {
    textFormat = new TextFormat();
    fieldPos = new FieldPosition(0);
    sb = new StringBuffer();
    try {
      // StringBuffer null.
      textFormat.format("String", null, fieldPos);
      fail();
    } catch (NullPointerException e) {
      assertTrue(0 == fieldPos.getBeginIndex());
      assertTrue(0 == fieldPos.getEndIndex());
    }
    fieldPos = new FieldPosition(0);
    try {
      // FieldPosition is null.
      textFormat.format("String", sb, null);
      fail();
    } catch (NullPointerException e) {
      // OK.
    }
    fieldPos = new FieldPosition(0);
    sb = new StringBuffer();
    try {
      // Object is null.
      textFormat.format(null, sb, fieldPos);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(0 == sb.length());
      assertTrue(0 == fieldPos.getBeginIndex());
      assertTrue(0 == fieldPos.getEndIndex());
    }
    fieldPos = new FieldPosition(0);
    sb = new StringBuffer();
    try {
      // Object is not instance of String
      textFormat.format(new Object(), sb, fieldPos);
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(0 == sb.length());
      assertTrue(0 == fieldPos.getBeginIndex());
      assertTrue(0 == fieldPos.getEndIndex());
    }
  }

  public void testFormatFieldPosition() {
    // Regular TextValue.
    textFormat = new TextFormat();
    fieldPos = new FieldPosition(0);
    sb = new StringBuffer();
    textFormat.format("Another String", sb, fieldPos);
    assertTrue(0 == fieldPos.getBeginIndex());
    assertTrue(("Another String".length() - 1) == fieldPos.getEndIndex());
    // Empty TextValue.
    fieldPos = new FieldPosition(0);
    sb = new StringBuffer();
    textFormat.format("", sb, fieldPos);
    assertTrue(0 == fieldPos.getBeginIndex());
    assertTrue(0 == fieldPos.getEndIndex());
  }

  public void testParse() {
    textFormat = new TextFormat();
    parsePos = new ParsePosition(0);
    assertEquals("abcdefghijklmnop!@#$%^&*()", textFormat.
        parseObject("abcdefghijklmnop!@#$%^&*()", parsePos));
  }

  public void testParseNullCases() {
    textFormat = new TextFormat();
    parsePos = new ParsePosition(0);
    try {
      // ParsePostion is null.
      textFormat.parseObject("true", null);
      fail();
    } catch (NullPointerException e) {
      // OK.
    }
    try {
      // source is null
      textFormat.parseObject(null, parsePos);
      fail();
    } catch (NullPointerException e) {
      // OK.
    }
  }

  public void testParseParsePosition() {
    textFormat = new TextFormat();
    ParsePosition pos = new ParsePosition(0);
    textFormat.parseObject("abcdefghijklmnop!@#$%^&*()", pos);
    assertTrue("abcdefghijklmnop!@#$%^&*()".length() == pos.getIndex());
  }
}
