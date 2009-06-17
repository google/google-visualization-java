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
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.ColumnSort;
import com.google.visualization.datasource.query.ColumnValueFilter;
import com.google.visualization.datasource.query.ComparisonFilter;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryFilter;
import com.google.visualization.datasource.query.QueryFormat;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QueryLabels;
import com.google.visualization.datasource.query.QueryPivot;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.QuerySort;
import com.google.visualization.datasource.query.ScalarFunctionColumn;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.SortOrder;
import com.google.visualization.datasource.query.mocks.MockDataSource;
import com.google.visualization.datasource.query.parser.QueryBuilder;
import com.google.visualization.datasource.query.scalarfunction.Constant;
import com.google.visualization.datasource.query.scalarfunction.DateDiff;
import com.google.visualization.datasource.query.scalarfunction.Difference;
import com.google.visualization.datasource.query.scalarfunction.Product;
import com.google.visualization.datasource.query.scalarfunction.Quotient;
import com.google.visualization.datasource.query.scalarfunction.Sum;
import com.google.visualization.datasource.query.scalarfunction.TimeComponentExtractor;

import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for DataTableDataSourceTest.
 *
 * @author Yoah B.D.
 */
public class QueryEngineTest extends TestCase {

  private static void assertStringArraysEqual(String[] expected,
      String[] found) {
    assertTrue("Expected: " + Arrays.toString(expected) + " but found: "
        + Arrays.toString(found), Arrays.equals(expected, found));
  }

  private DataTable input;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    input = new DataTable();
    input.addColumn(new ColumnDescription("name", ValueType.TEXT, "label0"));
    input.addColumn(new ColumnDescription("weight", ValueType.NUMBER, "label1"));
    input.addColumn(new ColumnDescription("isPig", ValueType.BOOLEAN, "label2"));

    TableRow row;

    row = new TableRow();
    row.addCell(new TableCell(new TextValue("aaa")));
    row.addCell(new TableCell(new NumberValue(222)));
    row.addCell(new TableCell(BooleanValue.TRUE, "T"));
    input.addRow(row);

    row = new TableRow();
    row.addCell(new TableCell(new TextValue("ccc")));
    row.addCell(new TableCell(new NumberValue(111)));
    row.addCell(new TableCell(BooleanValue.TRUE, "T"));
    input.addRow(row);

    row = new TableRow();
    row.addCell(new TableCell(new TextValue("bbb")));
    row.addCell(new TableCell(new NumberValue(333)));
    row.addCell(new TableCell(BooleanValue.FALSE, "F"));
    input.addRow(row);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test an empty query, no modification from input to output,
   */
  public void testEmptyQuery() throws Exception {
    Query q = new Query();
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);
    List<ColumnDescription> cols = res.getColumnDescriptions();
    assertEquals(3, cols.size());
    assertEquals("name", cols.get(0).getId());
    assertEquals("weight", cols.get(1).getId());
    assertEquals("isPig", cols.get(2).getId());
    assertEquals(ValueType.TEXT, cols.get(0).getType());
    assertEquals(ValueType.NUMBER, cols.get(1).getType());
    assertEquals(ValueType.BOOLEAN, cols.get(2).getType());
    assertEquals("label0", cols.get(0).getLabel());
    assertEquals("label1", cols.get(1).getLabel());
    assertEquals("label2", cols.get(2).getLabel());

    assertEquals(3, res.getRows().size());
  }

  /**
   * Test sorting by a number column.
   */
  public void testSortByNumberAscending() throws Exception {
    Query q = new Query();
    QuerySort sort = new QuerySort();
    sort.addSort(new ColumnSort(new SimpleColumn("weight"),
        SortOrder.ASCENDING));
    q.setSort(sort);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);

    assertEquals(3, res.getRows().size());
    assertEquals("111.0", res.getRows().get(0).getCells().get(1).toString());
    assertEquals("222.0", res.getRows().get(1).getCells().get(1).toString());
    assertEquals("333.0", res.getRows().get(2).getCells().get(1).toString());
  }

  /**
   * Test sorting by a number column.
   */
  public void testSortByNumberDescending() throws Exception {
    Query q = new Query();
    QuerySort sort = new QuerySort();
    sort.addSort(new ColumnSort(new SimpleColumn("weight"),
        SortOrder.DESCENDING));
    q.setSort(sort);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);

    assertEquals(3, res.getRows().size());
    assertEquals("333.0", res.getRows().get(0).getCells().get(1).toString());
    assertEquals("222.0", res.getRows().get(1).getCells().get(1).toString());
    assertEquals("111.0", res.getRows().get(2).getCells().get(1).toString());
  }

  /**
   * Test sorting by a text column.
   */
  public void testSortByTextAscending() throws Exception {
    Query q = new Query();
    QuerySort sort = new QuerySort();
    sort.addSort(new ColumnSort(new SimpleColumn("name"), SortOrder.ASCENDING));
    q.setSort(sort);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);

    assertEquals(3, res.getRows().size());
    assertEquals("aaa", res.getRows().get(0).getCells().get(0).toString());
    assertEquals("bbb", res.getRows().get(1).getCells().get(0).toString());
    assertEquals("ccc", res.getRows().get(2).getCells().get(0).toString());
  }

  /**
   * Test sorting by a text column.
   */
  public void testSortByTextDescnding() throws Exception {
    Query q = new Query();
    QuerySort sort = new QuerySort();
    sort.addSort(new ColumnSort(new SimpleColumn("name"),
        SortOrder.DESCENDING));
    q.setSort(sort);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);

    assertEquals(3, res.getRows().size());
    assertEquals("ccc", res.getRows().get(0).getCells().get(0).toString());
    assertEquals("bbb", res.getRows().get(1).getCells().get(0).toString());
    assertEquals("aaa", res.getRows().get(2).getCells().get(0).toString());
  }

  /**
   * Test selection of no cols (should be as select all).
   */
  public void testSelectWithNoCols() throws Exception {
    Query q = new Query();
    QuerySelection sel = new QuerySelection();
    q.setSelection(sel);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);
    List<ColumnDescription> cols = res.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("name", cols.get(0).getId());
    assertEquals("weight", cols.get(1).getId());
    assertEquals("isPig", cols.get(2).getId());

    assertEquals(3, res.getRows().size());
    assertEquals(3, res.getRows().get(0).getCells().size());
  }

  /**
   * Test selection of the first col only.
   */
  public void testSelectionOfFirstCol() throws Exception {
    Query q = new Query();
    QuerySelection sel = new QuerySelection();
    sel.addColumn(new SimpleColumn("name"));
    q.setSelection(sel);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);
    List<ColumnDescription> cols = res.getColumnDescriptions();

    assertEquals(1, cols.size());
    assertEquals("name", cols.get(0).getId());

    assertEquals(3, res.getRows().size());
    assertEquals(1, res.getRows().get(0).getCells().size());
  }

  /**
   * Test selection of all but the first col.
   */
  public void testSelectionOfAllButFirstCol() throws Exception {
    Query q = new Query();
    QuerySelection sel = new QuerySelection();
    sel.addColumn(new SimpleColumn("weight"));
    sel.addColumn(new SimpleColumn("isPig"));
    q.setSelection(sel);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);
    List<ColumnDescription> cols = res.getColumnDescriptions();

    assertEquals(2, cols.size());
    assertEquals("weight", cols.get(0).getId());
    assertEquals("isPig", cols.get(1).getId());

    assertEquals(3, res.getRows().size());
    assertEquals(2, res.getRows().get(0).getCells().size());
    
    // Verify that formatted values survive the selection.
    assertEquals("T", res.getCell(0, 1).getFormattedValue());
  }

  /**
   * Test sorting by a text column, while this is the only selected column.
   */
  public void testSelectionAndSortByTextDescending() throws Exception {
    Query q = new Query();
    QuerySort sort = new QuerySort();
    sort.addSort(new ColumnSort(new SimpleColumn("name"),
        SortOrder.DESCENDING));
    q.setSort(sort);

    QuerySelection selection = new QuerySelection();
    selection.addColumn(new SimpleColumn("name"));
    q.setSelection(selection);
    DataTable res = QueryEngine.executeQuery(q, input, ULocale.US);
    List<ColumnDescription> cols = res.getColumnDescriptions();

    assertEquals(1, cols.size());
    assertEquals("name", cols.get(0).getId());

    assertEquals(3, res.getRows().size());
    assertEquals("ccc", res.getRows().get(0).getCells().get(0).toString());
    assertEquals("bbb", res.getRows().get(1).getCells().get(0).toString());
    assertEquals("aaa", res.getRows().get(2).getCells().get(0).toString());
  }

  public void testGrouping() throws Exception {

    DataTable res = MockDataSource.getData(1);

    // select max(songs), min(songs), year, avg(songs), sum(sales)
    // group by year, band
    Query q = new Query();
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("Year"));
    group.addColumn(new SimpleColumn("Band"));
    q.setGroup(group);
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("Songs"),
        AggregationType.MAX));
    selection.addColumn(new AggregationColumn(new SimpleColumn("Songs"),
        AggregationType.MIN));
    selection.addColumn(new SimpleColumn("Year"));
    selection.addColumn(new AggregationColumn(new SimpleColumn("Songs"),
        AggregationType.AVG));
    selection.addColumn(new AggregationColumn(new SimpleColumn("Sales"),
        AggregationType.SUM));
    q.setSelection(selection);
    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);
    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(5, cols.size());
    assertEquals("max Number of new songs", cols.get(0).getLabel());
    assertEquals("min Number of new songs", cols.get(1).getLabel());
    assertEquals("Just year", cols.get(2).getLabel());
    assertEquals("avg Number of new songs", cols.get(3).getLabel());
    assertEquals("sum Number of new sales", cols.get(4).getLabel());

    assertEquals("max-Songs", cols.get(0).getId());
    assertEquals("min-Songs", cols.get(1).getId());
    assertEquals("Year", cols.get(2).getId());
    assertEquals("avg-Songs", cols.get(3).getId());
    assertEquals("sum-Sales", cols.get(4).getId());

    // Test data
    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertStringArraysEqual(new String[]{"4.0", "2.0", "1994",
        "2.6666666666666665", "24.0"}, resultStrings[0]);
    assertStringArraysEqual(new String[]{"4.0", "2.0", "1994",
        "2.2222222222222223", "36.0"}, resultStrings[1]);
    assertStringArraysEqual(new String[]{"2.0", "2.0", "1996",
        "2.0", "70.0"}, resultStrings[2]);
    assertStringArraysEqual(new String[]{"4.0", "2.0", "1996",
        "2.5", "32.0"}, resultStrings[3]);
    assertStringArraysEqual(new String[]{"2.0", "2.0", "2003",
        "2.0", "67.0"}, resultStrings[4]);
  }

  public void testGroupingWithPivoting() throws Exception {
    DataTable res = MockDataSource.getData(1);
    // select max(sales), year, min(sales), avg(fans)
    // group by year
    // pivot by band, songs
    Query q = new Query();
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("Year"));
    q.setGroup(group);
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("Band"));
    pivot.addColumn(new SimpleColumn("Songs"));
    q.setPivot(pivot);
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("Sales"),
        AggregationType.MAX));
    selection.addColumn(new SimpleColumn("Year"));
    selection.addColumn(new AggregationColumn(new SimpleColumn("Sales"),
        AggregationType.MIN));
    selection.addColumn(new AggregationColumn(new SimpleColumn("Fans"),
        AggregationType.AVG));
    q.setSelection(selection);
    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(16, cols.size());
    assertEquals("Collection,2.0 max-Sales", cols.get(0).getId());
    assertEquals("Contraband,2.0 max-Sales", cols.get(1).getId());
    assertEquals("Contraband,4.0 max-Sales", cols.get(2).getId());
    assertEquals("Youthanasia,2.0 max-Sales", cols.get(3).getId());
    assertEquals("Youthanasia,4.0 max-Sales", cols.get(4).getId());
    assertEquals("Year", cols.get(5).getId());
    assertEquals("Collection,2.0 min-Sales", cols.get(6).getId());
    assertEquals("Contraband,2.0 min-Sales", cols.get(7).getId());
    assertEquals("Contraband,4.0 min-Sales", cols.get(8).getId());
    assertEquals("Youthanasia,2.0 min-Sales", cols.get(9).getId());
    assertEquals("Youthanasia,4.0 min-Sales", cols.get(10).getId());
    assertEquals("Collection,2.0 avg-Fans", cols.get(11).getId());
    assertEquals("Contraband,2.0 avg-Fans", cols.get(12).getId());
    assertEquals("Contraband,4.0 avg-Fans", cols.get(13).getId());
    assertEquals("Youthanasia,2.0 avg-Fans", cols.get(14).getId());
    assertEquals("Youthanasia,4.0 avg-Fans", cols.get(15).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(3, resultStrings.length);

    assertStringArraysEqual(new String[]{"null", "4.0", "4.0", "4.0", "4.0",
        "1994", "null", "4.0", "4.0", "4.0", "4.0", "null", "2575.0", "1900.0",
        "2686.75", "20.0"}, resultStrings[0]);
    assertStringArraysEqual(new String[]{"null", "46.0", "null", "4.0", "4.0",
        "1996", "null", "4.0", "null", "4.0", "4.0", "null",
        "10626.142857142857", "null", "1122.8333333333333", "3250.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"8.0", "null", "null", "null", "null",
        "2003", "4.0", "null", "null", "null", "null", "2249.733333333333",
        "null", "null", "null", "null"}, resultStrings[2]);
  }

  public void testGroupingByScalarFunction() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), count(dept), hour(lunchTime) group by hour(lunchtime)
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection);

    // Add group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setGroup(group);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res.clone(), ULocale.US);

    // Test column description
    List<ColumnDescription> cols =
        result.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("sum-age", cols.get(0).getId());
    assertEquals("count-dept", cols.get(1).getId());
    assertEquals("hour_lunchTime", cols.get(2).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(2, resultStrings.length);

    assertStringArraysEqual(new String[]{"119.0", "4.0", "12.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"54.0", "2.0", "13.0"},
        resultStrings[1]);

    // Select sum(age), count(name), datediff(hireDate, seniorityStartTime),
    // dept group by datediff(hireDate, seniorityStartTime), dept.
    q = new Query();

    // Add selection.
    selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new AggregationColumn(new SimpleColumn("name"),
        AggregationType.COUNT));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime"),
            new SimpleColumn("hireDate")), DateDiff.getInstance()));
    selection.addColumn(new SimpleColumn("dept"));
    q.setSelection(selection);

    // Add group.
    group = new QueryGroup();
    group.addColumn(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime"),
            new SimpleColumn("hireDate")), DateDiff.getInstance()));
    group.addColumn(new SimpleColumn("dept"));
    q.setGroup(group);

    q.validate();

    result = QueryEngine.executeQuery(q, res.clone(), ULocale.US);

    // Test column description
    cols = result.getColumnDescriptions();

    assertEquals(4, cols.size());
    assertEquals("sum-age", cols.get(0).getId());
    assertEquals("count-name", cols.get(1).getId());
    assertEquals("dateDiff_seniorityStartTime,hireDate", cols.get(2).getId());
    assertEquals("dept", cols.get(3).getId());

    resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(5, resultStrings.length);

    assertStringArraysEqual(new String[]{"57.0", "2.0", "null", "Eng"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"25.0", "1.0", "null", "Sales"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"32.0", "1.0", "891.0", "Sales"},
        resultStrings[2]);
    assertStringArraysEqual(new String[]{"24.0", "1.0", "1084.0", "Marketing"},
        resultStrings[3]);
    assertStringArraysEqual(new String[]{"35.0", "1.0", "1180.0", "Eng"},
        resultStrings[4]);
  }

  public void testPivotingByScalarFunction() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), count(dept) pivot hour(lunchtime)
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    q.setSelection(selection);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setPivot(pivot);

    q.validate();
    DataTable result = QueryEngine.executeQuery(q, res.clone(), ULocale.US);
    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(4, cols.size());
    assertEquals("12.0 sum-age", cols.get(0).getId());
    assertEquals("13.0 sum-age", cols.get(1).getId());
    assertEquals("12.0 count-dept", cols.get(2).getId());
    assertEquals("13.0 count-dept", cols.get(3).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(1, resultStrings.length);

    assertStringArraysEqual(new String[]{"119.0", "54.0", "4.0", "2.0"},
        resultStrings[0]);

    // select count(dept) pivot hour(lunchtime), datediff(hireDate,
    // seniorityStartTime)
    Query q1 = new Query();

    // Add selection.
    QuerySelection selection1 = new QuerySelection();
    selection1.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    q1.setSelection(selection1);

    // Add pivot.
    QueryPivot pivot1 = new QueryPivot();
    pivot1.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    pivot1.addColumn(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime"),
            new SimpleColumn("hireDate")), DateDiff.getInstance()));
    q1.setPivot(pivot1);

    q1.validate();

    DataTable result1 = QueryEngine.executeQuery(q1, res.clone(), ULocale.US);

    // Test column description
    List<ColumnDescription> cols1 = result1.getColumnDescriptions();

    assertEquals(5, cols1.size());
    assertEquals("12.0,null count-dept", cols1.get(0).getId());
    assertEquals("12.0,891.0 count-dept", cols1.get(1).getId());
    assertEquals("12.0,1180.0 count-dept", cols1.get(2).getId());
    assertEquals("13.0,null count-dept", cols1.get(3).getId());
    assertEquals("13.0,1084.0 count-dept", cols1.get(4).getId());

    resultStrings = MockDataSource.queryResultToStringMatrix(result1);
    assertEquals(1, resultStrings.length);

    assertStringArraysEqual(new String[]{"2.0", "1.0", "1.0", "1.0", "1.0"},
        resultStrings[0]);
  }

  public void testGroupingByScalarFunctionWithPivoting() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), count(dept), hour(lunchTime) group by hour(lunchtime)
    // pivot isSenior.
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection);

    // Add group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setGroup(group);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("isSenior"));
    q.setPivot(pivot);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(5, cols.size());
    assertEquals("false sum-age", cols.get(0).getId());
    assertEquals("true sum-age", cols.get(1).getId());
    assertEquals("false count-dept", cols.get(2).getId());
    assertEquals("true count-dept", cols.get(3).getId());
    assertEquals("hour_lunchTime", cols.get(4).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(2, resultStrings.length);

    assertStringArraysEqual(new String[]{"52.0", "67.0", "2.0", "2.0", "12.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"30.0", "24.0", "1.0", "1.0", "13.0"},
        resultStrings[1]);

    res = MockDataSource.getData(3);

    // select count(dept), datediff(hireDate, seniorityStartTime) group by
    // datediff(hireDate, seniorityStartTime) pivot isSenior.
    q = new Query();

    // Add selection.
    selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime"),
            new SimpleColumn("hireDate")), DateDiff.getInstance()));
    q.setSelection(selection);

    // Add group.
    group = new QueryGroup();
    group.addColumn(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime"),
            new SimpleColumn("hireDate")), DateDiff.getInstance()));
    q.setGroup(group);

    // Add pivot.
    pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("isSenior"));
    q.setPivot(pivot);

    q.validate();

    result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    cols = result.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("false count-dept", cols.get(0).getId());
    assertEquals("true count-dept", cols.get(1).getId());
    assertEquals("dateDiff_seniorityStartTime,hireDate", cols.get(2).getId());

    resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(4, resultStrings.length);

    assertStringArraysEqual(new String[]{"3.0", "null", "null"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"null", "1.0", "891.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"null", "1.0", "1084.0"},
        resultStrings[2]);
    assertStringArraysEqual(new String[]{"null", "1.0", "1180.0"},
        resultStrings[3]);
  }

  public void testPivotingByScalarFunctionWithGrouping() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), count(dept) group by isSenior pivot hour(lunchtime)
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    selection.addColumn(new SimpleColumn("isSenior"));
    q.setSelection(selection);

    // Add Group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("isSenior"));
    q.setGroup(group);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setPivot(pivot);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(5, cols.size());
    assertEquals("12.0 sum-age", cols.get(0).getId());
    assertEquals("13.0 sum-age", cols.get(1).getId());
    assertEquals("12.0 count-dept", cols.get(2).getId());
    assertEquals("13.0 count-dept", cols.get(3).getId());
    assertEquals("isSenior", cols.get(4).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(2, resultStrings.length);

    assertStringArraysEqual(new String[]{"52.0", "30.0", "2.0", "1.0",
        "false"}, resultStrings[0]);
    assertStringArraysEqual(new String[]{"67.0", "24.0", "2.0", "1.0",
        "true"}, resultStrings[1]);

    // selectcount(dept) group by isSenior pivot datediff(hireDate,
    // seniorityStartTime)
    q = new Query();
    res = MockDataSource.getData(3);

    // Add selection.
    selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    selection.addColumn(new SimpleColumn("isSenior"));
    q.setSelection(selection);

    // Add Group.
    group = new QueryGroup();
    group.addColumn(new SimpleColumn("isSenior"));
    q.setGroup(group);

    // Add pivot.
    pivot = new QueryPivot();
    pivot.addColumn(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime"),
            new SimpleColumn("hireDate")), DateDiff.getInstance()));
    q.setPivot(pivot);

    q.validate();

    result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    cols = result.getColumnDescriptions();

    assertEquals(5, cols.size());
    assertEquals("null count-dept", cols.get(0).getId());
    assertEquals("891.0 count-dept", cols.get(1).getId());
    assertEquals("1084.0 count-dept", cols.get(2).getId());
    assertEquals("1180.0 count-dept", cols.get(3).getId());
    assertEquals("isSenior", cols.get(4).getId());

    resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(2, resultStrings.length);

    assertStringArraysEqual(new String[]{"3.0", "null", "null", "null",
        "false"}, resultStrings[0]);
    assertStringArraysEqual(new String[]{"null", "1.0", "1.0", "1.0",
        "true"}, resultStrings[1]);
  }

  public void testGroupingAndPivotingByScalarFunction() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select count(dept), year(seniorityStartTime) group by
    // year(seniorityStartTime) pivot hour(lunchtime)
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("dept"),
        AggregationType.COUNT));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn)
            new SimpleColumn("seniorityStartTime")), TimeComponentExtractor.
        getInstance(TimeComponentExtractor.TimeComponent.YEAR)));

    q.setSelection(selection);

    // Add Group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new ScalarFunctionColumn(Lists.newArrayList((AbstractColumn)
        new SimpleColumn("seniorityStartTime")), TimeComponentExtractor.
        getInstance(TimeComponentExtractor.TimeComponent.YEAR)));
    q.setGroup(group);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new ScalarFunctionColumn(Lists.newArrayList(
        (AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setPivot(pivot);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("12.0 count-dept", cols.get(0).getId());
    assertEquals("13.0 count-dept", cols.get(1).getId());
    assertEquals("year_seniorityStartTime", cols.get(2).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(3, resultStrings.length);

    assertStringArraysEqual(new String[]{"2.0", "1.0", "null"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"1.0", "null", "2005.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"1.0", "1.0", "2007.0"},
        resultStrings[2]);
  }

  public void testSelectionOfScalarFunction() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select name, dept, hour(lunchTime)
    Query q = new Query();
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new SimpleColumn("name"));
    selection.addColumn(new SimpleColumn("dept"));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));

    q.setSelection(selection);
    q.validate();
    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("name", cols.get(0).getId());
    assertEquals("dept", cols.get(1).getId());
    assertEquals("hour_lunchTime", cols.get(2).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(6, resultStrings.length);

    assertStringArraysEqual(new String[]{"John", "Eng", "12.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"Dave", "Eng", "12.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"Sally", "Eng", "13.0"},
        resultStrings[2]);
    assertStringArraysEqual(new String[]{"Ben", "Sales", "12.0"},
        resultStrings[3]);
    assertStringArraysEqual(new String[]{"Dana", "Sales", "12.0"},
        resultStrings[4]);
    assertStringArraysEqual(new String[]{"Mike", "Marketing", "13.0"},
        resultStrings[5]);
  }

  public void testSelectionOfScalarFunctionWithGrouping() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), dept, hour(min(lunchTime)) group by dept
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new SimpleColumn("dept"));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new AggregationColumn(
            new SimpleColumn("lunchTime"), AggregationType.MIN)),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection);

    // Add group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("dept"));
    q.setGroup(group);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("sum-age", cols.get(0).getId());
    assertEquals("dept", cols.get(1).getId());
    assertEquals("hour_min-lunchTime", cols.get(2).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(3, resultStrings.length);

    assertStringArraysEqual(new String[]{"92.0", "Eng", "12.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"24.0", "Marketing", "13.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"57.0", "Sales", "12.0"},
        resultStrings[2]);

    // Test group-by column with scalar function in the selection:
    // select count(name), hour(lunchTime) group by lunchtime
    q = new Query();
    // Add selection
    QuerySelection selection1 = new QuerySelection();
    selection1.addColumn(new AggregationColumn(new SimpleColumn("name"),
        AggregationType.COUNT));
    selection1.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("lunchTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection1);

    // Add group.
    QueryGroup group1 = new QueryGroup();
    group1.addColumn(new SimpleColumn("lunchTime"));
    q.setGroup(group1);

    q.validate();

    DataTable result1 = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols1 = result1.getColumnDescriptions();

    assertEquals(2, cols1.size());
    assertEquals("count-name", cols1.get(0).getId());
    assertEquals("hour_lunchTime", cols1.get(1).getId());

    String[][] resultStrings1 =
        MockDataSource.queryResultToStringMatrix(result1);
    assertEquals(2, resultStrings1.length);

    assertStringArraysEqual(new String[]{"4.0", "12.0"},
        resultStrings1[0]);
    assertStringArraysEqual(new String[]{"2.0", "13.0"},
        resultStrings1[1]);
  }

  public void testSelectionOfScalarFunctionWithPivoting() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), hour(min(lunchTime)) pivot dept
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new AggregationColumn(
            new SimpleColumn("lunchTime"), AggregationType.MIN)),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("dept"));
    q.setPivot(pivot);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(6, cols.size());
    assertEquals("Eng sum-age", cols.get(0).getId());
    assertEquals("Marketing sum-age", cols.get(1).getId());
    assertEquals("Sales sum-age", cols.get(2).getId());
    assertEquals("Eng hour_min-lunchTime", cols.get(3).getId());
    assertEquals("Marketing hour_min-lunchTime", cols.get(4).getId());
    assertEquals("Sales hour_min-lunchTime", cols.get(5).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(1, resultStrings.length);

    assertStringArraysEqual(new String[]{"92.0", "24.0", "57.0", "12.0", "13.0",
        "12.0"}, resultStrings[0]);
  }

  public void testSelectionOfScalarFunctionWithGroupingAndPivoting()
      throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), day(hireDate), hour(min(lunchTime)) group by hireDate
    // pivot dept
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY)));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new AggregationColumn(
            new SimpleColumn("lunchTime"), AggregationType.MIN)),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection);

    //Add group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("hireDate"));
    q.setGroup(group);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("dept"));
    q.setPivot(pivot);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(7, cols.size());
    assertEquals("Eng sum-age", cols.get(0).getId());
    assertEquals("Marketing sum-age", cols.get(1).getId());
    assertEquals("Sales sum-age", cols.get(2).getId());
    assertEquals("day_hireDate", cols.get(3).getId());
    assertEquals("Eng hour_min-lunchTime", cols.get(4).getId());
    assertEquals("Marketing hour_min-lunchTime", cols.get(5).getId());
    assertEquals("Sales hour_min-lunchTime", cols.get(6).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(4, resultStrings.length);

    assertStringArraysEqual(new String[]{"null", "null", "32.0", "10.0", "null",
        "null", "12.0"}, resultStrings[0]);
    assertStringArraysEqual(new String[]{"35.0", "null", "25.0", "8.0", "12.0",
        "null", "12.0"}, resultStrings[1]);
    assertStringArraysEqual(new String[]{"null", "24.0", "null", "10.0", "null",
        "13.0", "null"}, resultStrings[2]);
    assertStringArraysEqual(new String[]{"57.0", "null", "null", "10.0", "12.0",
        "null", "null"}, resultStrings[3]);
  }

  public void testSelectionOfScalarFunctionWithEmptyTable() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), day(hireDate), hour(min(lunchTime)) where name="liron"
    // group by hireDate pivot dept
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY)));
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new AggregationColumn(
            new SimpleColumn("lunchTime"), AggregationType.MIN)),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR)));
    q.setSelection(selection);

    //Add filter.
    QueryFilter filter = new ColumnValueFilter(new SimpleColumn("name"),
        new TextValue("name"), ComparisonFilter.Operator.EQ);
    q.setFilter(filter);

    //Add group.
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("hireDate"));
    q.setGroup(group);

    // Add pivot.
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("dept"));
    q.setPivot(pivot);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);
    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(3, cols.size());
    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(0, resultStrings.length);
  }

  public void testOrderByScalarFunction() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select day(hireDate), hireDate, age order by day(hireDate)
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY)));
    selection.addColumn(new SimpleColumn("hireDate"));
    selection.addColumn(new SimpleColumn("age"));
    q.setSelection(selection);

    // Add sort.
    QuerySort sort = new QuerySort();
    sort.addSort(new ScalarFunctionColumn(
        Lists.newArrayList((AbstractColumn) new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY)), SortOrder.ASCENDING);
    q.setSort(sort);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(3, cols.size());
    assertEquals("day_hireDate", cols.get(0).getId());
    assertEquals("hireDate", cols.get(1).getId());
    assertEquals("age", cols.get(2).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(6, resultStrings.length);

    assertStringArraysEqual(new String[]{"8.0", "2004-09-08", "35.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"8.0", "2004-09-08", "25.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"10.0", "2005-10-10", "27.0"},
        resultStrings[2]);
    assertStringArraysEqual(new String[]{"10.0", "2005-10-10", "30.0"},
        resultStrings[3]);
    assertStringArraysEqual(new String[]{"10.0", "2002-10-10", "32.0"},
        resultStrings[4]);
    assertStringArraysEqual(new String[]{"10.0", "2005-01-10", "24.0"},
        resultStrings[5]);
  }

  public void testSelectionOfArithmeticExpression() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select day(hireDate) + age * hour(seniorityStartTime), age
    Query q = new Query();

    // Add selection.
    QuerySelection selection = new QuerySelection();
    AbstractColumn col1  = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY));
    AbstractColumn col2 = new SimpleColumn("age");
    AbstractColumn col3  = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR));
    AbstractColumn col4 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col2, col3), Product.getInstance());
    AbstractColumn col5 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, col4), Sum.getInstance());
    selection.addColumn(col5);
    selection.addColumn(new SimpleColumn("age"));
    q.setSelection(selection);
    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(2, cols.size());
    assertEquals("sum_day_hireDate,product_age,hour_seniorityStartTime",
        cols.get(0).getId());
    assertEquals("age", cols.get(1).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(6, resultStrings.length);

    assertStringArraysEqual(new String[]{"533.0", "35.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"null", "27.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"null", "30.0"},
        resultStrings[2]);
    assertStringArraysEqual(new String[]{"394.0", "32.0"},
        resultStrings[3]);
    assertStringArraysEqual(new String[]{"null", "25.0"},
        resultStrings[4]);
    assertStringArraysEqual(new String[]{"346.0", "24.0"},
        resultStrings[5]);
  }

  public void testGroupByArithmeticExpression() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select sum(age), day(hireDate) - hour(seniorityStartTime)
    // group by day(hireDate) - hour(seniorityStartTime)
    Query q = new Query();

    AbstractColumn col1  = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY));
    AbstractColumn col2  = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR));
    AbstractColumn col3 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, col2),
        Difference.getInstance());

    // Add selection
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new AggregationColumn(new SimpleColumn("age"),
        AggregationType.SUM));
    selection.addColumn(col3);
    q.setSelection(selection);

    // Add group
    QueryGroup group = new QueryGroup();
    group.addColumn(col3);
    q.setGroup(group);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols = result.getColumnDescriptions();

    assertEquals(2, cols.size());
    assertEquals("sum-age", cols.get(0).getId());
    assertEquals("difference_day_hireDate,hour_seniorityStartTime",
        cols.get(1).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(4, resultStrings.length);

    assertStringArraysEqual(new String[]{"82.0", "null"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"35.0", "-7.0"},
        resultStrings[1]);
    assertStringArraysEqual(new String[]{"24.0", "-4.0"},
        resultStrings[2]);
    assertStringArraysEqual(new String[]{"32.0", "-2.0"},
        resultStrings[3]);
  }

  public void testFilterArithmeticExpression() throws Exception {
    DataTable res = MockDataSource.getData(3);

    // select day(hireDate), hour(seniorityStartTime) where
    // day(hireDate) - hour(seniorityStartTime) < -5
    // group by day(hireDate) - hour(seniorityStartTime)
    Query q = new Query();

    AbstractColumn col1  = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("hireDate")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.DAY));
    AbstractColumn col2  = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("seniorityStartTime")),
        TimeComponentExtractor.getInstance(
            TimeComponentExtractor.TimeComponent.HOUR));
    AbstractColumn col3 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, col2),
        Difference.getInstance());

    // Add selection
    QuerySelection selection = new QuerySelection();
    selection.addColumn(col1);
    selection.addColumn(col2);
    q.setSelection(selection);

    // Add Filter
    QueryFilter filter = new ColumnValueFilter(col3, new NumberValue(-5),
        ComparisonFilter.Operator.GT);
    q.setFilter(filter);

    q.validate();

    DataTable result = QueryEngine.executeQuery(q, res, ULocale.US);

    // Test column description
    List<ColumnDescription> cols =  result.getColumnDescriptions();

    assertEquals(2, cols.size());
    assertEquals("day_hireDate", cols.get(0).getId());
    assertEquals("hour_seniorityStartTime", cols.get(1).getId());

    String[][] resultStrings = MockDataSource.queryResultToStringMatrix(result);
    assertEquals(2, resultStrings.length);

    assertStringArraysEqual(new String[]{"10.0", "12.0"},
        resultStrings[0]);
    assertStringArraysEqual(new String[]{"10.0", "14.0"},
        resultStrings[1]);
  }

  public void testLabels() throws Exception {
    Query query = new Query();

    DataTable data = MockDataSource.getData(0);

    QuerySelection selection = new QuerySelection();
    selection.addColumn(new SimpleColumn("name"));
    selection.addColumn(new SimpleColumn("isAlive"));
    query.setSelection(selection);

    QueryLabels labels = new QueryLabels();
    labels.addLabel(new SimpleColumn("isAlive"),
        "New isAlive Label");
    query.setLabels(labels);

    DataTable res = QueryEngine.executeQuery(query, data, ULocale.US);

    assertEquals("Pet Name", res.getColumnDescription("name").getLabel());
    assertEquals("New isAlive Label", res.getColumnDescription("isAlive").getLabel());
  }

  public void testFormatAndLabelOnPivotColumns() throws Exception {
    Query query = new Query();
    QuerySelection selection = new QuerySelection(); // SELECT (sum(sales) / 7)
    List<AbstractColumn> columns = Lists.newArrayList();
    columns.add(new AggregationColumn(new SimpleColumn("Sales"), AggregationType.SUM));
    columns.add(new ScalarFunctionColumn(Lists.<AbstractColumn>newArrayList(),
        new Constant(new NumberValue(7))));
    AbstractColumn selectedColumn = new ScalarFunctionColumn(columns, Quotient.getInstance());
    selection.addColumn(selectedColumn);

    query.setSelection(selection);
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("Year"));
    query.setGroup(group);
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("Band"));
    pivot.addColumn(new SimpleColumn("Songs"));
    query.setPivot(pivot);
    QueryLabels labels = new QueryLabels();
    labels.addLabel(selectedColumn, "foo");
    query.setLabels(labels);
    QueryFormat format = new QueryFormat();
    format.addPattern(selectedColumn, "'$'@@@");
    query.setUserFormatOptions(format);

    DataTable data = MockDataSource.getData(1);

    // Also tests different locale (note the commas in the numbers)
    DataTable res = QueryEngine.executeQuery(query, data, ULocale.FRENCH);

    List<ColumnDescription> columnDescriptions = res.getColumnDescriptions();
    assertEquals(5, columnDescriptions.size());
    assertEquals("", res.getRow(0).getCell(0).getFormattedValue()); // null
    assertEquals("$2,29", res.getRow(0).getCell(1).getFormattedValue());
    assertEquals("$1,14", res.getRow(0).getCell(2).getFormattedValue());
    assertEquals("$4,57", res.getRow(0).getCell(3).getFormattedValue());
    assertEquals("$0,571", res.getRow(0).getCell(4).getFormattedValue());
    assertEquals("Collection,2.0 foo", columnDescriptions.get(0).getLabel());
    assertEquals("Contraband,2.0 foo", columnDescriptions.get(1).getLabel());
    assertEquals("Contraband,4.0 foo", columnDescriptions.get(2).getLabel());
    assertEquals("Youthanasia,2.0 foo", columnDescriptions.get(3).getLabel());
    assertEquals("Youthanasia,4.0 foo", columnDescriptions.get(4).getLabel());
  }
  
  // We used to have a bug with this throwing a runtime exception
  public void testAggregationAppearsTwice() throws Exception {
    Query q = QueryBuilder.getInstance().parseQuery("SELECT isAlive, sum(weight), sum(weight)+1"
        + " GROUP BY isAlive");
    DataTable data = MockDataSource.getData(0);
    DataTable res = QueryEngine.executeQuery(q, data, ULocale.US);
    assertEquals(2, res.getNumberOfRows());
    assertEquals("false", res.getRow(0).getCell(0).getValue().toString());
    assertEquals("2011.0", res.getRow(0).getCell(1).getValue().toString());
    assertEquals("2012.0", res.getRow(0).getCell(2).getValue().toString());
    assertEquals("true", res.getRow(1).getCell(0).getValue().toString());
    assertEquals("1567.0", res.getRow(1).getCell(1).getValue().toString());
    assertEquals("1568.0", res.getRow(1).getCell(2).getValue().toString());
  }
  
  // Tests that the format operation saves the pattern on the column description.
  public void testFormatStoresPattern() throws Exception {
    Query q = QueryBuilder.getInstance().parseQuery("FORMAT weight 'a#'");
    DataTable data = MockDataSource.getData(0);
    DataTable res = QueryEngine.executeQuery(q, data, ULocale.US);
    assertEquals("a#", res.getColumnDescription("weight").getPattern());
  }
  
  public void testQueryDoesntRuinDataSourcePatterns() throws Exception {
    Query q = QueryBuilder.getInstance().parseQuery("SELECT isAlive, weight WHERE height > 20 "
        + "ORDER BY weight LIMIT 3 OFFSET 2");
    DataTable data = MockDataSource.getData(0).clone();
    data.getColumnDescription("weight").setPattern("f#");
    DataTable res = QueryEngine.executeQuery(q, data, ULocale.US);
    assertEquals("f#", res.getColumnDescription("weight").getPattern());
  }

  public void testQueryWithLikeOperator() throws InvalidQueryException {
    Query q = QueryBuilder.getInstance().parseQuery("SELECT Band WHERE Band like 'Co%'");
    DataTable data = MockDataSource.getData(1).clone();
    DataTable res = QueryEngine.executeQuery(q, data, ULocale.US);
    assertEquals(28, res.getNumberOfRows());
    for (int i = 0; i < res.getNumberOfRows(); i++) {
      assertTrue(res.getValue(i,0).toString().startsWith("Co"));
    }
  }
}
