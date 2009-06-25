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
 * Tests for the ToDate scalar function.
 *
 * @author Liron L.
 */
public class ToDateTest extends TestCase {
  public void testValidateParameters() throws InvalidQueryException {
    ToDate toDate = ToDate.getInstance();

    // Validate datetime, date, number and string paremeters.
    toDate.validateParameters(Lists.newArrayList(ValueType.DATETIME));
    toDate.validateParameters(Lists.newArrayList(ValueType.DATE));
    toDate.validateParameters(Lists.newArrayList(ValueType.NUMBER));

    // Should throw an exception.
    try {
      toDate.validateParameters(Lists.newArrayList(ValueType.TIMEOFDAY));
      fail("Should have thrown a ScalarFunctionException: The toDiff "
          + "function was given a timeofday parameter to validate.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }

    // Should throw an exception.
    try {
      toDate.validateParameters(Lists.newArrayList(ValueType.DATE,
          ValueType.NUMBER));
      fail("Should have thrown a ScalarFunctionException: The toDAte "
          + "function was given 2 parameters.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testEvaluate() {
    ToDate toDate = ToDate.getInstance();
    List<Value> valuesList1 =
        Lists.<Value>newArrayList(new DateValue(2008, 1, 13));
    List<Value> valuesList2 =
        Lists.<Value>newArrayList(
            new DateTimeValue(2008, 2, 23, 9, 12, 22, 333));
    List<Value> valuesList3 =
        Lists.<Value>newArrayList(new NumberValue(1234567890000.53421423424));
List<Value> valuesList4 =
        Lists.<Value>newArrayList(new NumberValue(-1234567890000.0));
    assertEquals(new DateValue(2008, 1, 13), toDate.evaluate(valuesList1));
    assertEquals(new DateValue(2008, 2, 23), toDate.evaluate(valuesList2));
    assertEquals(new DateValue(2009, 1, 13), toDate.evaluate(valuesList3));
    assertEquals(new DateValue(1930, 10, 18), toDate.evaluate(valuesList4));

  }
}
