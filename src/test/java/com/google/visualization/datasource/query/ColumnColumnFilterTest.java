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
import com.google.visualization.datasource.datatable.value.ValueType;


/**
 * Test for ColumnColumnFilter
 *
 * @author Yonatan B.Y.
 */
public class ColumnColumnFilterTest extends TestCase {
  public void testMatch() {
    TableRow row = new TableRow();
    row.addCell(new TableCell("a"));
    row.addCell(new TableCell(123));
    row.addCell(new TableCell("a"));

    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("c1", ValueType.TEXT, "c1"));
    table.addColumn(new ColumnDescription("c2", ValueType.TEXT, "c2"));
    table.addColumn(new ColumnDescription("c3", ValueType.TEXT, "c3"));

    ColumnColumnFilter filter = new ColumnColumnFilter(new SimpleColumn("c1"),
        new SimpleColumn("c3"), ComparisonFilter.Operator.LE);
    assertTrue(filter.isMatch(table, row));
  }

  public void testNoMatch() {
    TableRow row = new TableRow();
    row.addCell(new TableCell("a"));
    row.addCell(new TableCell(123));
    row.addCell(new TableCell("a"));

    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("c1", ValueType.TEXT, "c1"));
    table.addColumn(new ColumnDescription("c2", ValueType.TEXT, "c2"));
    table.addColumn(new ColumnDescription("c3", ValueType.TEXT, "c3"));

    ColumnColumnFilter filter = new ColumnColumnFilter(new SimpleColumn("c3"),
        new SimpleColumn("c1"), ComparisonFilter.Operator.NE);
    assertFalse(filter.isMatch(table, row));
  }

  public void testToQueryString() {
    ColumnColumnFilter filter1 = new ColumnColumnFilter(new SimpleColumn("c3"),
        new AggregationColumn(new SimpleColumn("c4"), AggregationType.AVG),
        ComparisonFilter.Operator.NE);
    ColumnColumnFilter filter2 = new ColumnColumnFilter(
        new AggregationColumn(new SimpleColumn("c4"), AggregationType.SUM),
        new SimpleColumn("c1"), ComparisonFilter.Operator.STARTS_WITH);

    assertEquals("`c3` != AVG(`c4`)", filter1.toQueryString());
    assertEquals("SUM(`c4`) STARTS WITH `c1`", filter2.toQueryString());
  }
}
