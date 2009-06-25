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

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import junit.framework.TestCase;

import java.util.List;
import java.util.Set;

/**
 * Test for CompoundFilter.
 *
 * @author Yonatan B.Y.
 */
public class CompoundFilterTest extends TestCase {
  public void testAndFilter() {
    TableRow trueRow = new TableRow();
    trueRow.addCell(new TableCell("a"));
    trueRow.addCell(new TableCell(123));
    trueRow.addCell(new TableCell("a"));
    TableRow falseRow = new TableRow();
    falseRow.addCell(new TableCell("a"));
    falseRow.addCell(new TableCell(123));
    falseRow.addCell(new TableCell("b"));

    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("c1", ValueType.TEXT, "c1"));
    table.addColumn(new ColumnDescription("c2", ValueType.TEXT, "c2"));
    table.addColumn(new ColumnDescription("c3", ValueType.TEXT, "c3"));

    ColumnColumnFilter filter1 = new ColumnColumnFilter(new SimpleColumn("c1"),
        new SimpleColumn("c3"), ComparisonFilter.Operator.EQ);
    ColumnValueFilter filter2 = new ColumnValueFilter(new SimpleColumn("c2"),
        new NumberValue(100), ComparisonFilter.Operator.GT);
    List<QueryFilter> subfilterList = Lists.newArrayList();
    subfilterList.add(filter1);
    subfilterList.add(filter2);
    CompoundFilter compoundFilter = new CompoundFilter(
        CompoundFilter.LogicalOperator.AND, subfilterList);
    assertTrue(compoundFilter.isMatch(table, trueRow));
    assertFalse(compoundFilter.isMatch(table, falseRow));
  }

  public void testOrFilter() {
    TableRow falseRow = new TableRow();
    falseRow.addCell(new TableCell("a"));
    falseRow.addCell(new TableCell(123));
    falseRow.addCell(new TableCell("a"));
    TableRow trueRow = new TableRow();
    trueRow.addCell(new TableCell("a"));
    trueRow.addCell(new TableCell(123));
    trueRow.addCell(new TableCell("b"));

    DataTable table = new DataTable();
    table.addColumn(new ColumnDescription("c1", ValueType.TEXT, "c1"));
    table.addColumn(new ColumnDescription("c2", ValueType.TEXT, "c2"));
    table.addColumn(new ColumnDescription("c3", ValueType.TEXT, "c3"));

    ColumnColumnFilter filter1 = new ColumnColumnFilter(new SimpleColumn("c1"),
        new SimpleColumn("c3"), ComparisonFilter.Operator.NE);
    ColumnValueFilter filter2 = new ColumnValueFilter(new SimpleColumn("c2"),
        new NumberValue(1000), ComparisonFilter.Operator.GT);
    List<QueryFilter> subfilterList = Lists.newArrayList();
    subfilterList.add(filter1);
    subfilterList.add(filter2);
    CompoundFilter compoundFilter = new CompoundFilter(
        CompoundFilter.LogicalOperator.OR, subfilterList);
    assertTrue(compoundFilter.isMatch(table, trueRow));
    assertFalse(compoundFilter.isMatch(table, falseRow));
  }

  public void testGetAllColumnIds() {
    SimpleColumn col1 = new SimpleColumn("c1");
    SimpleColumn col2 = new SimpleColumn("c2");
    SimpleColumn col3 = new SimpleColumn("c3");
    SimpleColumn col4 = new SimpleColumn("c4");
    ColumnColumnFilter filter1 = new ColumnColumnFilter(col1, col3,
        ComparisonFilter.Operator.NE);
    ColumnValueFilter filter2 = new ColumnValueFilter(col2,
        new NumberValue(1000), ComparisonFilter.Operator.GT);
    ColumnColumnFilter filter3 = new ColumnColumnFilter(col1, col4,
        ComparisonFilter.Operator.LE);
    ColumnValueFilter filter4 = new ColumnValueFilter(col3,
        BooleanValue.TRUE, ComparisonFilter.Operator.GT);
    List<QueryFilter> subfilterList1 = Lists.newArrayList();
    subfilterList1.add(filter1);
    subfilterList1.add(filter2);
    CompoundFilter compoundFilter1 = new CompoundFilter(
        CompoundFilter.LogicalOperator.OR, subfilterList1);
    List<QueryFilter> subfilterList2 = Lists.newArrayList();
    subfilterList2.add(filter3);
    subfilterList2.add(filter4);
    CompoundFilter compoundFilter2 = new CompoundFilter(
        CompoundFilter.LogicalOperator.AND, subfilterList2);
    List<QueryFilter> subfilterList3 = Lists.newArrayList();
    subfilterList3.add(compoundFilter1);
    subfilterList3.add(compoundFilter2);
    CompoundFilter compoundFilter3 = new CompoundFilter(
        CompoundFilter.LogicalOperator.OR,  subfilterList3);

    Set<String> allColumnIds = compoundFilter3.getAllColumnIds();
    assertTrue(allColumnIds.containsAll(Lists.newArrayList("c1",
        "c2", "c3", "c4")));
  }

  public void testToQueryString() {
    SimpleColumn col1 = new SimpleColumn("c1");
    AggregationColumn col2 = new AggregationColumn(new SimpleColumn("c2"),
        AggregationType.COUNT);
    SimpleColumn col3 = new SimpleColumn("c3");
    SimpleColumn col4 = new SimpleColumn("c4");
    ColumnColumnFilter filter1 = new ColumnColumnFilter(col1, col3,
        ComparisonFilter.Operator.NE);
    ColumnValueFilter filter2 = new ColumnValueFilter(col2,
        new NumberValue(1000), ComparisonFilter.Operator.GT);
    ColumnColumnFilter filter3 = new ColumnColumnFilter(col1, col4,
        ComparisonFilter.Operator.CONTAINS);
    ColumnValueFilter filter4 = new ColumnValueFilter(col3,
        BooleanValue.TRUE, ComparisonFilter.Operator.GT, true);
    List<QueryFilter> subfilterList1 = Lists.newArrayList();
    subfilterList1.add(filter1);
    subfilterList1.add(filter2);
    subfilterList1.add(filter3);
    CompoundFilter compoundFilter1 = new CompoundFilter(
        CompoundFilter.LogicalOperator.OR, subfilterList1);
    List<QueryFilter> subfilterList2 = Lists.newArrayList();
    subfilterList2.add(filter3);
    subfilterList2.add(filter4);
    CompoundFilter compoundFilter2 = new CompoundFilter(
        CompoundFilter.LogicalOperator.AND, subfilterList2);
    List<QueryFilter> subfilterList3 = Lists.newArrayList();
    subfilterList3.add(compoundFilter1);
    subfilterList3.add(compoundFilter2);
    CompoundFilter compoundFilter3 = new CompoundFilter(
        CompoundFilter.LogicalOperator.OR,  subfilterList3);

    assertEquals("((`c1` != `c3`) OR (COUNT(`c2`) > 1000.0) OR (`c1` CONTAINS "
        + "`c4`)) OR ((`c1` CONTAINS `c4`) AND (true > `c3`))",
        compoundFilter3.toQueryString());
  }
}
