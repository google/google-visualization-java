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

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.mocks.MockDataSource;

/**
 * Tests for AggregationTree.java.
 *
 * @author Yoav G.
 */

public class AggregationTreeTest extends TestCase {

  /**
   * Column types suits for table2 and table3 in mockDataSource.
   */
  private static final ValueType[] types = new ValueType[] {
      ValueType.TEXT,  ValueType.NUMBER, ValueType.NUMBER
  };

  @Override
  public void setUp() throws Exception {
    super.setUp();
    AggregationNodeTest.createColumnAggregationMap();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * @return an aggregation tree.
   */
  protected static final AggregationTree newAggregationTree() {
    return new AggregationTree(AggregationNodeTest.columnsToAggregate, MockDataSource.getData(1));
  }

  /**
   * Creates a tree path from string input.
   * @param values The values.
   * @return Tree path.
   */
  protected static final AggregationPath createPath(String [] values) {
    AggregationPath result = new AggregationPath();
    for (int i = 0; i < values.length; i++) {
      result.add(MockDataSource.toValue(values[i], types[i]));
    }
    return result;
  }

  /**
   * Tests that non existing nodes cause an exception (and that existing nodes
   * function properly).
   */
  public void testGetNode() {
    AggregationTree tree = newAggregationTree();
    tree.aggregate(createPath(new String[] {"Bla", "3"}),
        AggregationNodeTest.createValueMap(new String[]{"A", "100"}));
    tree.getNode(createPath(new String[] {"Bla", "3"}));
    try {
      tree.getNode(createPath(new String[] {"B", "3"}));
      fail();
    }
    catch (NoSuchElementException e) {

    }

    try {
      tree.getNode(createPath(new String[] {"Bla", "3", "5"}));
      fail();
    }
    catch (NoSuchElementException e) {
    }
  }

  /**
   * Tests aggregations of empty and non empty paths in the tree.
   */
  public void testAggregation() {
    AggregationTree tree = newAggregationTree();
    tree.aggregate(createPath(new String[] {"Bla", "3"}),
        AggregationNodeTest.createValueMap(new String[]{"A", "100"}));
    tree.aggregate(createPath(new String[] {"Bla", "3", "5"}),
        AggregationNodeTest.createValueMap(new String[]{"B", "50"}));
    tree.aggregate(createPath(new String[]{"4"}),
        AggregationNodeTest.createValueMap(new String[]{"C", "10"}));

    Assert.assertEquals(new TextValue("A"),
        tree.getNode(createPath(new String[] {}))
        .getAggregationValue("Band", AggregationType.MIN));

    assertEquals("75.0", tree.getNode(createPath(
        new String[] {"Bla", "3"})).getAggregationValue(
        "Sales", AggregationType.AVG).toString());

    assertEquals("160.0", tree.getNode(createPath(
        new String[] {})).getAggregationValue(
            "Sales", AggregationType.SUM).toString());
  }

  /**
   * Test the getPathsToLeaves functionality.
   */
  public void testPathsToLeaves() {
    AggregationTree tree = newAggregationTree();
    tree.aggregate(createPath(new String[] {"Bla", "3"}),
        AggregationNodeTest.createValueMap(new String[]{"A", "100"}));
    tree.aggregate(createPath(new String[] {"Bla", "3", "5"}),
        AggregationNodeTest.createValueMap(new String[]{"B", "50"}));
    tree.aggregate(createPath(new String[]{"4"}),
        AggregationNodeTest.createValueMap(new String[]{"C", "10"}));
    Set<AggregationPath> paths = tree.getPathsToLeaves();
    assertEquals(2, paths.size());
    for (AggregationPath path : paths) {
      List<Value> curPath = path.getValues();
      if (curPath.size() == 3) {
        assertEquals("Bla", ((TextValue)curPath.get(0)).toString());
        assertEquals(3.0, ((NumberValue)curPath.get(1)).getValue());
        assertEquals(5.0, ((NumberValue)curPath.get(2)).getValue());
      }
      else {
        assertEquals(1, curPath.size());
        assertEquals("4", ((TextValue)curPath.get(0)).toString());
      }
    }
  }
}
