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

package com.google.visualization.datasource.query.engine;

import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AggregationType;

import junit.framework.TestCase;

/**
 * Tests for ColumnValueAggregator.java.
 *
 * @author Yoav G.
 */

public class ColumnValueAggregatorTest extends TestCase {

  /**
   * Test all functionalities by aggregating two numbers.
   */
  public void testNumberAggregation() {
    ValueAggregator aggregator = new ValueAggregator(ValueType.NUMBER);
    aggregator.aggregate(new NumberValue(1));
    aggregator.aggregate(new NumberValue(3));

    assertEquals(new NumberValue(4.0), aggregator.getValue(AggregationType.SUM));
    assertEquals(new NumberValue(2.0), aggregator.getValue(AggregationType.AVG));
    assertEquals(new NumberValue(2), aggregator.getValue(AggregationType.COUNT));
    assertEquals(new NumberValue(1.0), aggregator.getValue(AggregationType.MIN));
    assertEquals(new NumberValue(3.0), aggregator.getValue(AggregationType.MAX));
  }

  /**
   * Test string aggregation.
   */
  public void testStringAggregation() {
    ValueAggregator aggregator = new ValueAggregator(ValueType.TEXT);
    aggregator.aggregate(new TextValue("a"));
    aggregator.aggregate(new TextValue("b"));

    try {
      aggregator.getValue(AggregationType.SUM);
      fail();
    } catch (UnsupportedOperationException e) {
      // Expected behavior.
    }
    try {
      aggregator.getValue(AggregationType.AVG);
      fail();
    } catch (UnsupportedOperationException e) {
      // Expected behavior.
    }

    assertEquals(new NumberValue(2), aggregator.getValue(AggregationType.COUNT));
    assertEquals(new TextValue("a"), aggregator.getValue(AggregationType.MIN));
    assertEquals(new TextValue("b"), aggregator.getValue(AggregationType.MAX));

  }

  public void testGetValueFromEmptyAggregation() {
    ValueAggregator aggregator = new ValueAggregator(ValueType.DATE);
    DateValue dateNull = DateValue.getNullValue();
    NumberValue numberNull = NumberValue.getNullValue();

    assertEquals(new NumberValue(0), aggregator.getValue(AggregationType.COUNT));

    assertEquals(dateNull, aggregator.getValue(AggregationType.MIN));
    assertEquals(dateNull, aggregator.getValue(AggregationType.MAX));

    assertEquals(numberNull, aggregator.getValue(AggregationType.AVG));
    assertEquals(numberNull, aggregator.getValue(AggregationType.SUM));
  }
}