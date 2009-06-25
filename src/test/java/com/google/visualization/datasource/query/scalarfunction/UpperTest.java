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
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;


/**
 * Tests for the Upper scalar function.
 *
 * @author Yaniv S.
 */
public class UpperTest extends TestCase {
  public void testValidateParameters() {
    Upper upper = Upper.getInstance();

    // Verify that Upper does not accept 0 parameters.
    try {
      upper.validateParameters(Lists.<ValueType>newArrayList());
      fail();
    } catch (InvalidQueryException e) {
      // Do nothing - this is the expected behavior.
    }

    // Verify that Upper does not accept more than 1 parameter.
    try {
      upper.validateParameters(Lists.newArrayList(ValueType.TEXT,
                                                  ValueType.TEXT));
      fail();
    } catch (InvalidQueryException e) {
      // Do nothing - this is the expected behavior.
    }

    // Verify that Upper does not accept a non-Text parameter.
    try {
      upper.validateParameters(Lists.newArrayList(ValueType.DATE));
      fail();
    } catch (InvalidQueryException e) {
      // Do nothing - this is the expected behavior.
    }

    // Verify that Upper accepts 1 TEXT parameter.
    try {
      upper.validateParameters(Lists.newArrayList(ValueType.TEXT));
    } catch (InvalidQueryException e) {
      fail();
    }
  }

  public void testEvaluate() {
    Upper upper = Upper.getInstance();

    // Test empty string.
    TextValue textValue = (TextValue) upper.evaluate(
        Lists.<Value>newArrayList(new TextValue("")));
    assertEquals(textValue.getValue(), "");

    // Basic test.
    textValue = (TextValue) upper.evaluate(
        Lists.<Value>newArrayList(new TextValue("aBc")));
    assertEquals(textValue.getValue(), "ABC");
  }
}
