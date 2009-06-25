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

package com.google.visualization.datasource.query.scalarfunction;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests for the DateDiff scalar function.
 *
 * @author Liron L.
 */
public class DateDiffTest extends TestCase {
  public void testValidateParameters() throws InvalidQueryException {
    List<ValueType> types = Lists.newArrayList(ValueType.DATE,
        ValueType.DATETIME);
    DateDiff dateDiff = DateDiff.getInstance();

    // Validate date paremeter.
    dateDiff.validateParameters(types);

    // Should throw an exception.
    try {
      dateDiff.validateParameters(Lists.newArrayList(ValueType.DATE,
          ValueType.NUMBER));
      fail("Should have thrown a ScalarFunctionException: The dateDiff "
          + "function was given a number parameter to validate.");
    } catch (InvalidQueryException e) {
      // do nothing
    }

    // Validate datetime paremeter.
    types = Lists.newArrayList(ValueType.DATETIME, ValueType.DATETIME);
    dateDiff.validateParameters(types);

    // Should throw an exception.
    try {
      dateDiff.validateParameters(Lists.newArrayList(ValueType.DATE));
      fail("Should have thrown a ScalarFunctionException: The dateDiff "
          + "function was given only 1 parameter.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testEvaluate() {
    DateDiff dateDiff = DateDiff.getInstance();
    List<Value> valuesList1 =
        Lists.<Value>newArrayList(new DateValue(2008, 1, 13),
            new DateValue(2008, 2, 13));
    List<Value> valuesList2 = Lists.newArrayList(new DateValue(2008, 1, 13),
            new DateTimeValue(2008, 1, 13, 9, 12, 22, 333));
    List<Value> valuesList3 =
        Lists.<Value>newArrayList(new DateTimeValue(2008, 6, 1, 9, 12, 22, 333),
            new DateTimeValue(2008, 10, 1, 9, 12, 22, 333));
    List<Value> valuesList4 = Lists.<Value>newArrayList(new DateTimeValue(
        2008, 10, 1, 9, 12, 22, 333),
        new DateTimeValue(2008, 6, 1, 9, 12, 20, 333));

    assertEquals(new NumberValue(-29), dateDiff.evaluate(valuesList1));
    assertEquals(new NumberValue(0), dateDiff.evaluate(valuesList2));
    assertEquals(new NumberValue(-123), dateDiff.evaluate(valuesList3));
    assertEquals(new NumberValue(123), dateDiff.evaluate(valuesList4));
  }
}
