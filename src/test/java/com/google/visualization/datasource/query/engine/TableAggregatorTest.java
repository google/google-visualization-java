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

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.mocks.MockDataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Tests for TableAggregator.java.
 *
 * @author Yoav G.
 */

public class TableAggregatorTest extends TestCase {

  /**
   * An ordered list of columns to group by.
   */
  /* package */ List<String> groupByColumns;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    AggregationNodeTest.createColumnAggregationMap();
    groupByColumns = Lists.newArrayList();
    groupByColumns.add("Year");
    groupByColumns.add("Songs");

  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Creates a column to value map according to a string array.
   *
   * @param values A string array.
   *
   * @return A column to value map.
   */
  protected static final AggregationPath generatePath(String[] values) {
    AggregationPath result = new AggregationPath();
    if (values.length > 0) {
      result.add(MockDataSource.toValue(values[0], ValueType.TEXT));
    }
    if (values.length > 1) {
      result.add(MockDataSource.toValue(values[1], ValueType.NUMBER));
    }
    return result;
  }

  /**
   * Tests that a group which does not exist in the table causes an exeption.
   */
  public void testNoSuchElement() {
    TableAggregator tableAggregator = newTableAggregator(1);
    try {
      tableAggregator.getAggregationValue(generatePath(
          new String[]{"1995", "2"}), "Sales", AggregationType.COUNT);
      fail();
    } catch (NoSuchElementException e) {
      // Expected behavior.
    }
  }

  /**
   * Tests that a column that is not aggregated causes an exception.
   */
  public void testNoSuchColumn () {
    TableAggregator tableAggregator = newTableAggregator(1);

    try {
      assertNull(tableAggregator.getAggregationValue(generatePath(
          new String[]{"1994", "2"}), "Saless", AggregationType.COUNT));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }

    try {
      assertNull(tableAggregator.getAggregationValue(generatePath(
          new String[]{"1994", "2"}), "Songs", AggregationType.COUNT));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected behavior.
    }
  }

  /**
   * Tests aggregation values on short paths
   */
  public void testShortPath () {
    TableAggregator tableAggregator = newTableAggregator(1);

    assertEquals("60.0", tableAggregator.getAggregationValue(
        generatePath(new String[]{"1994"}), "Sales",
        AggregationType.SUM).toString());

    Assert.assertEquals(new TextValue("Youthanasia"), tableAggregator.
        getAggregationValue(generatePath(new String[]{}), "Band",
        AggregationType.MAX));
  }

  /**
   * Tests aggregation value of a tree leaf.
   */
  public void testlongPath() {
    TableAggregator tableAggregator = newTableAggregator(1);
    assertEquals("48.0", tableAggregator.getAggregationValue(
        generatePath(new String[]{"1994", "2"}), "Sales",
        AggregationType.SUM).toString());
  }

  /**
   * Tests that null values are ignored in aggregation.
   */
  public void testNullValues() {
    TableAggregator tableAggregator = newTableAggregator(2);

    assertEquals("4.0", tableAggregator.getAggregationValue(
        generatePath(new String[]{"1994"}), "Sales",
        AggregationType.SUM).toString());
    assertEquals("4.0", tableAggregator.getAggregationValue(
        generatePath(new String[]{"1994", "2"}), "Sales",
        AggregationType.SUM).toString());
    assertEquals("4.0", tableAggregator.getAggregationValue(
        generatePath(new String[]{}), "Sales",
        AggregationType.SUM).toString());
  }
  /**
   * Tests that a null value is considered a group by value.
   */
  public void testElementNull() {
    TableAggregator tableAggregator = newTableAggregator(2);

    assertEquals("0.0", tableAggregator.getAggregationValue(
        generatePath(new String[]{"1994", null}), "Sales",
        AggregationType.SUM).toString());
  }

  /**
   * Tests that a null value is considered a group by value (that in this case
   * does not exist).
   */
  public void testNoElementNull() {
    TableAggregator tableAggregator = newTableAggregator(2);
    try {
    tableAggregator.getAggregationValue(generatePath
        (new String[]{"2003", null}), "Sales", AggregationType.SUM);
      fail();
    } catch (NoSuchElementException e) {
      // Expected behavior.
    }
  }

  /**
   * Creates a table aggregator based on a mock data base.
   *
   * @param tableNum The table number.
   *
   * @return A table aggregator.
   */
  private TableAggregator newTableAggregator(int tableNum) {
    return new TableAggregator(groupByColumns,
        AggregationNodeTest.columnsToAggregate,
        MockDataSource.getData(tableNum));
  }

  public void testGetRowPath() {
    TableAggregator aggregator = newTableAggregator(1);
    DataTable table = MockDataSource.getData(1);
    TableRow row = table.getRow(0);
    List<Value> path = aggregator.getRowPath(row, table, 0).getValues();
    assertEquals(1, path.size());
    assertEquals("1994", path.get(0).toString());

    row = table.getRow(6);
    path = aggregator.getRowPath(row, table, 1).getValues();
    assertEquals(2, path.size());
    assertEquals("1994", path.get(0).toString());
    assertEquals(2.0, ((NumberValue) path.get(1)).getValue());
  }

}
