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
 * Value Tester.
 *
 * @author Hillel M.
 */
public class ValueTest extends TestCase {

  public void testCompareTo() {
    Value val1 = new DateValue(2007, 1, 23);
    Value val2 = new DateValue(2007, 1, 23);
    Value val4 = new DateValue(2007, 1, 20);

    Value valTime1 = new TimeOfDayValue(10, 11, 21, 222);

    // Equal objects.
    assertTrue(0 == val1.compareTo(val1));
    assertTrue(0 == val1.compareTo(val2));
    assertTrue(0 == val2.compareTo(val1));

    // Same class x > y.
    assertTrue(0 < val2.compareTo(val4));
    assertTrue(0 > val4.compareTo(val2));

    // Different class x > y.
    try {
      val2.compareTo(valTime1);
      fail();
    } catch (ClassCastException e) {
      // Expected behavior.
    }
  }

  public void testEquals() {
    Number number = new Double(-12.3);
    Value numberValue1 = new NumberValue(-12.3);
    Value numberValue2 = new NumberValue(-12.3);
    Value numberValue3 = new NumberValue(12.3343);
    Value timeValue = new TimeOfDayValue(12, 1, 2, 3);

    // null == other.
    assertFalse(numberValue1.equals(null));

    // other.Class != this.Class.
    assertFalse(numberValue1.equals(timeValue));
    assertFalse(timeValue.equals(numberValue1));
    assertFalse(numberValue1.equals(number));

    // this == other.
    assertTrue(numberValue2.equals(numberValue2));

    // equal objects.
    assertTrue(numberValue1.equals(numberValue2));

    // not equal Values.
    assertFalse(numberValue1.equals(numberValue3));
    assertFalse(numberValue1.equals(timeValue));
  }
}