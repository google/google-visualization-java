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

package com.google.visualization.datasource.query;

import junit.framework.TestCase;

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.ValueType;


/**
 * Test for ColumnValueFilter
 *
 * @author Yonatan B.Y.
 */
public class ColumnValueFilterTest extends TestCase {

  public void testVariousFilters() {
    TableRow row = new TableRow();
    row.addCell(new TableCell("a"));
    row.addCell(new TableCell(123));
    row.addCell(new TableCell("a"));

    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("c1", ValueType.TEXT, "c1"));
    table.addColumn(new ColumnDescription("c2", ValueType.TEXT, "c2"));
    table.addColumn(new ColumnDescription("c3", ValueType.TEXT, "c3"));

    ColumnValueFilter filter = new ColumnValueFilter(new SimpleColumn("c2"),
        new NumberValue(100), ComparisonFilter.Operator.GE);
    assertTrue(filter.isMatch(table, row));
    filter = new ColumnValueFilter(new SimpleColumn("c2"),
        new NumberValue(100), ComparisonFilter.Operator.LE, true);
    assertTrue(filter.isMatch(table, row));
    filter = new ColumnValueFilter(new SimpleColumn("c2"),
        new NumberValue(100), ComparisonFilter.Operator.LE);
    assertFalse(filter.isMatch(table, row));
    filter = new ColumnValueFilter(new SimpleColumn("c1"),
        new TextValue("b"), ComparisonFilter.Operator.GT);
    assertFalse(filter.isMatch(table, row));
    filter = new ColumnValueFilter(new SimpleColumn("c1"),
        new TextValue("b"), ComparisonFilter.Operator.LT, true);
    assertFalse(filter.isMatch(table, row));
  }

  public void testToQueryString() {
    ColumnValueFilter filter1 = new ColumnValueFilter(new SimpleColumn("c2"),
        new NumberValue(100.23), ComparisonFilter.Operator.GE);
    ColumnValueFilter filter2 = new ColumnValueFilter(
        new AggregationColumn(new SimpleColumn("c3"), AggregationType.MAX),
        new DateValue(2007, 2, 3), ComparisonFilter.Operator.LIKE, true);

    assertEquals("`c2` >= 100.23", filter1.toQueryString());
    assertEquals("DATE '2007-3-3' LIKE MAX(`c3`)", filter2.toQueryString());

  }
}
