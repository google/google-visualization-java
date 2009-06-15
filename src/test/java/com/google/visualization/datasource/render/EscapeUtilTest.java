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

package com.google.visualization.datasource.render;


import junit.framework.TestCase;

/**
 * Tests for Escape Util.
 *
 * @author Hillel M.
 */
public class EscapeUtilTest extends TestCase{

  public static void testJsonEscape() {
    // Check ' " < > \
    assertEquals("\\u0027ABC", EscapeUtil.jsonEscape("'ABC"));
    assertEquals("\\u0022ABC", EscapeUtil.jsonEscape("\"ABC"));
    assertEquals("\\\\", EscapeUtil.jsonEscape("\\"));
    assertEquals("\\u003cABC\\u003e", EscapeUtil.jsonEscape("<ABC>"));
    assertEquals("\\u003c/ABC\\u003e", EscapeUtil.jsonEscape("</ABC>"));

    // Normal string (all keyboard letters but ', ", \, <, >)
    String normalString = "`1234567890-=qwertyuiop[];lkjhgfdsazxcvbnm,.//*-+.0~!@#$%^&*()_+"
        + "QWERTYUIOP{}|:LKJHGFDSAZXCVBNM?;";
    assertEquals(normalString, EscapeUtil.jsonEscape(normalString));

    // Weird characters (non readable)
    String weirdString = "\n\t\r\\ \u0081\u0010\u2010\u2099\b\f";
    String escapedweirdString = "\\n\\t\\r\\\\ \\u0081\\u0010\\u2010\\u2099\\b\\f";
    assertEquals(escapedweirdString, EscapeUtil.jsonEscape(weirdString));

    // Normal string in Unicode (pqr)
    String normalStringInUnicode = "\u0070\u0071\u0072";
    assertEquals("pqr", EscapeUtil.jsonEscape(normalStringInUnicode));

    // Non English normal characters (Hebrew - me)
    String nonEnglishString = "\u05d0\u05e0\u05d9";
    assertEquals(nonEnglishString, EscapeUtil.jsonEscape(nonEnglishString));
  }
}