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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.mocks.MockDataSource;
import junit.framework.TestCase;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Tests to AggregationNode.java.
 *
 * @author Yoav G.
 */

public class AggregationNodeTest extends TestCase {

  /**
   * Mapping the columns to aggregate.
   */
  /* package */ static Set<String> columnsToAggregate;

  /**
   * Creating the column aggregation map (suits table2 and table3 in
   * mockDataSource).
   */
  protected static final void createColumnAggregationMap() {
    columnsToAggregate = Sets.newHashSet("Band", "Sales");
  }

  /**
   * Creates a new aggregation node.
   *
   * @return A new aggregation node.
   */
  protected static final AggregationNode newAggregationNode() {
    return new AggregationNode(columnsToAggregate, MockDataSource.getData(1));
  }

  /**
   * Adds a child to a given node.
   *
   * @param node The node to add a child to.
   * @param value The value representing the node to add.
   */
  protected static final void addChildToNode(AggregationNode node, Value value)
  {
    node.addChild(value, columnsToAggregate, MockDataSource.getData(1));
  }


  /**
   * Creates a column to value map according to a string array.
   *
   * @param values A string array.
   *
   * @return A column to value map.
   */
  protected static final Map<String, Value> createValueMap(String[] values) {
    Map<String, Value> result = Maps.newHashMap();
    result.put("Band", MockDataSource.toValue(values[0], ValueType.TEXT));
    result.put("Sales", MockDataSource.toValue(values[1], ValueType.NUMBER));
    return result;

  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    createColumnAggregationMap();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Tests the functionality of {@code AggregationNode.containsChild}.
   */
  public void testContainsChild() {
    AggregationNode node = newAggregationNode();
    node.addChild(new NumberValue(3), columnsToAggregate, MockDataSource.getData(1));
    assertEquals(true, node.containsChild(new NumberValue(3)));
    assertEquals(false, node.containsChild(new NumberValue(4)));
  }

  /**
   * Tests the functionality of {@code AggregationNode.getChild}.
   */
  public void testGetChild() {
    AggregationNode node = newAggregationNode();
    addChildToNode(node, new NumberValue(3));
    assertNotNull(node.getChild(new NumberValue(3)));
    try {
      node.getChild(new NumberValue(4));
      fail();
    }
    catch (NoSuchElementException e) {

    }
  }

  /**
   * Tests the functionality of {@code AggregationNode.addChild}.
   */
  public void testAddChild() {
    AggregationNode node = newAggregationNode();
    addChildToNode(node, new NumberValue(3));
    try {
      addChildToNode(node, new NumberValue(3));
      fail();
    }
    catch(IllegalArgumentException e) {
    }
  }

  /**
   * Tests empty aggregation.
   */
  public void testEmptyAggregation() {
    AggregationNode node = newAggregationNode();
    Map<String, Value> valuesByColumn = Maps.newHashMap();
    node.aggregate(valuesByColumn);
    assertEquals("0.0", node.getAggregationValue(
        "Band", AggregationType.COUNT).toString());
  }

  /**
   * Tests whether the node aggregates properly.
   */
  public void testAggregation() {
    AggregationNode node = newAggregationNode();
    node.aggregate(createValueMap(new String[]{"A", "100"}));
    node.aggregate(createValueMap(new String[]{"B", "50"}));
    assertEquals(new TextValue("B"), node.getAggregationValue(
        "Band", AggregationType.MAX));
    assertEquals("75.0", node.getAggregationValue(
        "Sales", AggregationType.AVG).toString());
  }
}
