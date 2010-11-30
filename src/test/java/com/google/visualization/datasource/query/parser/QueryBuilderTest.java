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

package com.google.visualization.datasource.query.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.ColumnColumnFilter;
import com.google.visualization.datasource.query.ColumnIsNullFilter;
import com.google.visualization.datasource.query.ColumnSort;
import com.google.visualization.datasource.query.ColumnValueFilter;
import com.google.visualization.datasource.query.ComparisonFilter;
import com.google.visualization.datasource.query.CompoundFilter;
import com.google.visualization.datasource.query.NegationFilter;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryFilter;
import com.google.visualization.datasource.query.QueryFormat;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QueryLabels;
import com.google.visualization.datasource.query.QueryOptions;
import com.google.visualization.datasource.query.QueryPivot;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.QuerySort;
import com.google.visualization.datasource.query.ScalarFunctionColumn;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.SortOrder;
import com.google.visualization.datasource.query.scalarfunction.Difference;
import com.google.visualization.datasource.query.scalarfunction.Lower;
import com.google.visualization.datasource.query.scalarfunction.Product;
import com.google.visualization.datasource.query.scalarfunction.Quotient;
import com.google.visualization.datasource.query.scalarfunction.Sum;
import com.google.visualization.datasource.query.scalarfunction.TimeComponentExtractor;
import com.google.visualization.datasource.query.scalarfunction.Upper;

import junit.framework.TestCase;

import java.util.List;
import java.util.Set;

/**
 * Tests for QueryBuilder.
 *
 * @author Yonatan B.Y.
 */
public class QueryBuilderTest extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testSelect() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("SeLeCt  c1,c2  , c3 ");

    assertEquals(null, query.getSort());
    QuerySelection selection = query.getSelection();
    assertEquals(Lists.newArrayList(new SimpleColumn("c1"),
        new SimpleColumn("c2"), new SimpleColumn("c3")),
        selection.getColumns());
  }

  public void testSelectAll() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("Select *");
    assertTrue(query.isEmpty());
  }

  public void testUnicodeInput() throws Exception {
    // Select aleph, bet, gimmel. (Hebrew).
    Query query = QueryBuilder.getInstance().parseQuery("SELECT `\u05d0`,`\u05d1`,`\u05d2` ");

    assertEquals(null, query.getSort());
    QuerySelection selection = query.getSelection();
    assertEquals(Lists.newArrayList(new SimpleColumn("\u05d0"),
        new SimpleColumn("\u05d1"), new SimpleColumn("\u05d2")),
        selection.getColumns());

    query = QueryBuilder.getInstance().parseQuery("SELECT * WHERE c='\u311e'");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter cvf = (ColumnValueFilter) filter;
    assertEquals("c", cvf.getColumn().getId());
    Value v = cvf.getValue();
    assertEquals(ValueType.TEXT, v.getType());
    assertEquals("\u311e", ((TextValue) cvf.getValue()).getValue());
  }

  public void testOrderBy() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "OrDeR   bY  c1,c2 desc , c3 ASC");
    assertEquals(null, query.getSelection());
    QuerySort sort = query.getSort();
    List<ColumnSort> columnSorts = sort.getSortColumns();
    assertEquals(3, columnSorts.size());
    assertEquals(new SimpleColumn("c1"), columnSorts.get(0).getColumn());
    assertEquals(new SimpleColumn("c2"), columnSorts.get(1).getColumn());
    assertEquals(new SimpleColumn("c3"), columnSorts.get(2).getColumn());
    assertEquals(SortOrder.ASCENDING, columnSorts.get(0).getOrder());
    assertEquals(SortOrder.DESCENDING, columnSorts.get(1).getOrder());
    assertEquals(SortOrder.ASCENDING, columnSorts.get(2).getOrder());
  }

  public void testSelectAndOrderBy() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "SELect c1   ,    c2 , c3   ORDER BY c4 DESC");
    QuerySelection selection = query.getSelection();
    assertEquals(Lists.newArrayList(new SimpleColumn("c1"),
        new SimpleColumn("c2"), new SimpleColumn("c3")),
        selection.getColumns());
    QuerySort sort = query.getSort();
    List<ColumnSort> columnSorts = sort.getSortColumns();
    assertEquals(1, columnSorts.size());
    assertEquals(new SimpleColumn("c4"), columnSorts.get(0).getColumn());
    assertEquals(SortOrder.DESCENDING, columnSorts.get(0).getOrder());
  }

  public void testOptions() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "  OpTiOnS   nO_ValUes   ");

    QueryOptions options = query.getOptions();
    assertTrue(options.isNoValues());
    assertFalse(options.isNoFormat());
  }

  public void testGroupingAndPivoting() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        " SELECT a, min(c1), b, avg(c2) group by a,b pivot  c,d");
    QuerySelection selection = query.getSelection();
    QueryGroup group = query.getGroup();
    QueryPivot pivot = query.getPivot();
    assertEquals(Lists.newArrayList(new SimpleColumn("a"),
        new AggregationColumn(new SimpleColumn("c1"), AggregationType.MIN),
        new SimpleColumn("b"),
        new AggregationColumn(new SimpleColumn("c2"), AggregationType.AVG)),
        selection.getColumns());
    assertEquals(Lists.newArrayList("a", "b"), group.getColumnIds());
    assertEquals(Lists.newArrayList("c", "d"), pivot.getColumnIds());
  }

  public void testStrangeAggregations() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        " SELECT `a, b`, avg, min(min), max(max) group by `a, b`, avg "
            + " pivot count");
    QuerySelection selection = query.getSelection();
    QueryGroup group = query.getGroup();
    QueryPivot pivot = query.getPivot();
    assertEquals(Lists.newArrayList(new SimpleColumn("a, b"),
        new SimpleColumn("avg"),
        new AggregationColumn(new SimpleColumn("min"), AggregationType.MIN),
        new AggregationColumn(new SimpleColumn("max"), AggregationType.MAX)),
        selection.getColumns());
    assertEquals(Lists.newArrayList("a, b", "avg"), group.getColumnIds());
    assertEquals(Lists.newArrayList("count"), pivot.getColumnIds());
  }

  public void testEmptyQuery() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("  ");
    assertEquals(null, query.getSelection());
    assertEquals(null, query.getSort());
  }

  public void testNullQuery() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(null);
    assertEquals(null, query.getSelection());
    assertEquals(null, query.getSort());
  }

  public void testBadQuery1() throws Exception {
    try {
      QueryBuilder.getInstance().parseQuery("order by c1 asc desc");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testBadQuery2() throws Exception {
    try {
      QueryBuilder.getInstance().parseQuery("select c1,,c2");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  // LIMIT and OFFSET tests

  public void testValidLimit() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" limit 100 ");
    assertEquals(100, query.getRowLimit());
  }

  public void testInvalidLimit() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" limit 100.5 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testInvalidLimit2() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" limit gaga ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testUnspecifiedLimit() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" limit ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testValidOffset() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" offset 100 ");
    assertEquals(100, query.getRowOffset());
  }

  public void testInvalidOffset() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" offset 100.5 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testInvalidOffset2() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" offset gaga ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testUnspecifiedOffset() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" offset ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testLimitAndOffset() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" limit 100 offset 10 ");
    assertEquals(100, query.getRowLimit());
    assertEquals(10, query.getRowOffset());
  }

  public void testReverseLimitAndOffset() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" offset 10 limit 10 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testNegativeLimit() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" offset -10 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testNegativeOffset() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" offset -1 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  // SKIPPING tests

  public void testSkipping() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" skipping 10 ");
    assertEquals(10, query.getRowSkipping());
  }
  
  public void testNegativeSkipping() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" skipping -1 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }
  
  public void testZeroSkipping() throws Exception {
    // Zero value is OK, it means no skipping
    Query query = QueryBuilder.getInstance().parseQuery(" skipping 0 ");
    assertEquals(0, query.getRowSkipping());
  }
  
  public void testSkippingLimitAndOffset() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" skipping 10 limit 100 offset 10  ");
    assertEquals(10, query.getRowSkipping());
    assertEquals(100, query.getRowLimit());
    assertEquals(10, query.getRowOffset());
  }
  
  public void testUnorderedLimitSkippingAndOffset() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(" limit 100 skipping 10 offset 10 ");
      fail("Should have thrown an exception.");
    }
    catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  // LABEL clause tests

  public void testOneValidLabelWithDoubleQuotes() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("select c1 label c1 \"Label 1\" ");
    QueryLabels labels = query.getLabels();
    assertEquals("Label 1", labels.getLabel(new SimpleColumn("c1")));
  }

  public void testOneValidLabelWithSingleQuote() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("select c1 label c1 'Label 1' ");
    QueryLabels labels = query.getLabels();
    assertEquals("Label 1", labels.getLabel(new SimpleColumn("c1")));
  }

  public void testTwoValidLabels() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "select c1,c2 label c1 'Label 1', c2 'Label 2' ");
    QueryLabels labels = query.getLabels();
    assertEquals("Label 1", labels.getLabel(new SimpleColumn("c1")));
    assertEquals("Label 2", labels.getLabel(new SimpleColumn("c2")));
  }

  public void testInvalidLabel() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery("select c1 label c1 gaga ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testInvalidEmptyLabel() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery("select c1 label c1 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testDuplicateLabelColumn() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery("select c1 label c1 'gaga', c1 'gugu' ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testInvalidTwoLabelsNoComma() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(
          "select c1, c2 label c1,c2 'label 1' c2 'label 2' ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  // FORMAT clause tests

  public void testOneValidFormatWithDoubleQuotes() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("select c1 Format c1 \"Format 1\" ");
    QueryFormat formats = query.getUserFormatOptions();
    assertEquals("Format 1", formats.getPattern(new SimpleColumn("c1")));
  }

  public void testOneValidFormatWithSingleQuote() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("select c1 Format c1 'Format 1' ");
    QueryFormat formats = query.getUserFormatOptions();
    assertEquals("Format 1", formats.getPattern(new SimpleColumn("c1")));
  }

  public void testTwoValidFormats() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "select c1,c2 Format c1 'Format 1', c2 'Format 2' ");
    QueryFormat formats = query.getUserFormatOptions();
    assertEquals("Format 1", formats.getPattern(new SimpleColumn("c1")));
    assertEquals("Format 2", formats.getPattern(new SimpleColumn("c2")));
  }

  public void testInvalidFormat() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery("select c1 Format c1 gaga ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testInvalidEmptyFormat() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery("select c1 Format c1 ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testInvalidTwoFormatsNoComma() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery(
          "select c1,c2 Format c1 'Format 1' c2 'Format 2' ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testDuplicateFormatColumn() throws Exception {
    try {
      Query query = QueryBuilder.getInstance().parseQuery("select c1 format c1 'gaga', c1 'gugu' ");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  // WHERE clause tests:

  public void testColumnColumnFilter() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE C1 > `c 2`");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnColumnFilter);
    ColumnColumnFilter f = (ColumnColumnFilter) filter;
    assertEquals("C1", f.getFirstColumn().getId());
    assertEquals("c 2", f.getSecondColumn().getId());
    assertEquals(ComparisonFilter.Operator.GT, f.getOperator());
  }

  public void testColumnValueFilterWithNumber() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE c1 <= 7.55");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("c1", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof NumberValue);
    NumberValue v = (NumberValue) val;
    assertEquals(7.55, v.getValue());
    assertEquals(ComparisonFilter.Operator.LE, f.getOperator());
  }

  public void testColumnValueFilterWithText1() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE `selEct` = 'baba'");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("selEct", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof TextValue);
    TextValue v = (TextValue) val;
    assertEquals("baba", v.toString());
    assertEquals(ComparisonFilter.Operator.EQ, f.getOperator());
  }

  public void testColumnValueFilterWithText2() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE MiN <> \"baba\"");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("MiN", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof TextValue);
    TextValue v = (TextValue) val;
    assertEquals("baba", v.toString());
    assertEquals(ComparisonFilter.Operator.NE, f.getOperator());
  }

  public void testColumnValueFilterWithBoolean1() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE c1 != trUe");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("c1", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof BooleanValue);
    BooleanValue v = (BooleanValue) val;
    assertEquals(true, v.getValue());
    assertEquals(ComparisonFilter.Operator.NE, f.getOperator());
  }

  public void testColumnValueFilterWithBoolean2() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE `min` >= FalSe");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("min", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof BooleanValue);
    BooleanValue v = (BooleanValue) val;
    assertEquals(false, v.getValue());
    assertEquals(ComparisonFilter.Operator.GE, f.getOperator());
    assertEquals(false, f.isComparisonOrderReversed());
  }

  public void testReverseColumnValueFilter1() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE -.3 < `ba ba`");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("ba ba", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof NumberValue);
    NumberValue v = (NumberValue) val;
    assertEquals(-0.3, v.getValue());
    assertEquals(ComparisonFilter.Operator.LT, f.getOperator());
    assertEquals(true, f.isComparisonOrderReversed());
  }

  public void testReverseColumnValueFilter2() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE 'select' > count  ");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("count", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof TextValue);
    TextValue v = (TextValue) val;
    assertEquals("select", v.toString());
    assertEquals(ComparisonFilter.Operator.GT, f.getOperator());
    assertEquals(true, f.isComparisonOrderReversed());
  }

  public void testReverseColumnValueFilter3() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE false <> `false`  ");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof ColumnValueFilter);
    ColumnValueFilter f = (ColumnValueFilter) filter;
    assertEquals("false", f.getColumn().getId());
    Value val = f.getValue();
    assertTrue(val instanceof BooleanValue);
    BooleanValue v = (BooleanValue) val;
    assertEquals(false, v.getValue());
    assertEquals(ComparisonFilter.Operator.NE, f.getOperator());
    assertEquals(true, f.isComparisonOrderReversed());
  }

  public void testNegationFilter() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(" WHERE not (c1 < c2) ");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof NegationFilter);
    NegationFilter f = (NegationFilter) filter;
    QueryFilter subFilter = f.getSubFilter();
    assertTrue(subFilter instanceof ColumnColumnFilter);
    ColumnColumnFilter ccf = (ColumnColumnFilter) subFilter;
    assertEquals("c1", ccf.getFirstColumn().getId());
    assertEquals("c2", ccf.getSecondColumn().getId());
    assertEquals(ComparisonFilter.Operator.LT, ccf.getOperator());
  }

  public void testAndFilter() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        " WHERE c1 < c2 AND 4 >= `WHERE` aNd (`c1` < `c 3`)");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof CompoundFilter);
    CompoundFilter f = (CompoundFilter) filter;
    assertEquals(CompoundFilter.LogicalOperator.AND, f.getOperator());
    List<QueryFilter> subFilters = f.getSubFilters();
    assertEquals(3, subFilters.size());
    QueryFilter f1 = subFilters.get(0);
    QueryFilter f2 = subFilters.get(1);
    QueryFilter f3 = subFilters.get(2);
    assertTrue(f1 instanceof ColumnColumnFilter);
    assertTrue(f2 instanceof ColumnValueFilter);
    assertTrue(f3 instanceof ColumnColumnFilter);
    Set<String> allColumnIds = f.getAllColumnIds();
    assertEquals(Sets.newHashSet("c1", "c2", "c 3", "WHERE"), allColumnIds);
  }

  public void testMultipleAndAssociativity() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        " WHERE `Date` > '2008-06-01' and `RoleId` != 47 and "
            + "`RoleId` != 6 and `RoleId` != 8 and `RoleId` != 2");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof CompoundFilter);
    CompoundFilter f = (CompoundFilter) filter;
    assertEquals(CompoundFilter.LogicalOperator.AND, f.getOperator());
    List<QueryFilter> subFilters = f.getSubFilters();
    ColumnValueFilter[] filters = new ColumnValueFilter[5];
    assertEquals(5, subFilters.size());
    for (int i = 0; i < 5; i++) {
      filters[i] = (ColumnValueFilter) subFilters.get(i);
    }
    assertEquals("Date", filters[0].getColumn().getId());
    assertEquals("RoleId", filters[1].getColumn().getId());
    assertEquals("RoleId", filters[2].getColumn().getId());
    assertEquals("RoleId", filters[3].getColumn().getId());
    assertEquals("RoleId", filters[4].getColumn().getId());

    assertEquals(ComparisonFilter.Operator.GT, filters[0].getOperator());
    assertEquals(ComparisonFilter.Operator.NE, filters[1].getOperator());
    assertEquals(ComparisonFilter.Operator.NE, filters[2].getOperator());
    assertEquals(ComparisonFilter.Operator.NE, filters[3].getOperator());
    assertEquals(ComparisonFilter.Operator.NE, filters[4].getOperator());

    assertEquals(new TextValue("2008-06-01"), filters[0].getValue());
    assertEquals(new NumberValue(47), filters[1].getValue());
    assertEquals(new NumberValue(6), filters[2].getValue());
    assertEquals(new NumberValue(8), filters[3].getValue());
    assertEquals(new NumberValue(2), filters[4].getValue());

  }

  public void testOrFilterWithExtraneousParantheses() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        " WHERE (((c1 < c2)) OR 4 >= `WHERE` OR (`c1` < `c 3`))");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof CompoundFilter);
    CompoundFilter f = (CompoundFilter) filter;
    assertEquals(CompoundFilter.LogicalOperator.OR, f.getOperator());
    List<QueryFilter> subFilters = f.getSubFilters();
    assertEquals(3, subFilters.size());
    QueryFilter f1 = subFilters.get(0);
    QueryFilter f2 = subFilters.get(1);
    QueryFilter f3 = subFilters.get(2);
    assertTrue(f1 instanceof ColumnColumnFilter);
    assertTrue(f2 instanceof ColumnValueFilter);
    assertTrue(f3 instanceof ColumnColumnFilter);
    Set<String> allColumnIds = f.getAllColumnIds();
    assertEquals(Sets.newHashSet("c1", "c2", "c 3", "WHERE"), allColumnIds);
  }

  public void testReallyComplexFilter() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "SELECT c1,c2 WHERE (NOT ((c1 < c2) OR (NOT (c3 < " +
            "c4))) AND (NOT ((NOT (NOT (c5 < c6))) OR (c7 < c8))) AND " +
            "((c9 < c10) AND ((c11 < c12) OR ((c13 < c14) AND (c15 < c16)))))");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof CompoundFilter);
    CompoundFilter f = (CompoundFilter) filter;
    assertEquals(CompoundFilter.LogicalOperator.AND, f.getOperator());
    List<QueryFilter> subFilters = f.getSubFilters();
    assertEquals(3, subFilters.size());
    QueryFilter f1 = subFilters.get(0);
    QueryFilter f2 = subFilters.get(1);
    QueryFilter f3 = subFilters.get(2);

    // f1: (!((c1 < c2) || (!(c3 < c4)))
    assertTrue(f1 instanceof NegationFilter);
    NegationFilter nf = (NegationFilter) f1;
    QueryFilter subFilter = nf.getSubFilter();
    assertTrue(subFilter instanceof CompoundFilter);
    CompoundFilter cf = (CompoundFilter) subFilter;
    assertEquals(CompoundFilter.LogicalOperator.OR, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c1", "c2"),
        subFilters.get(0).getAllColumnIds());
    assertTrue(subFilters.get(1) instanceof NegationFilter);
    nf = (NegationFilter) subFilters.get(1);
    assertTrue(nf.getSubFilter() instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c3", "c4"),
        nf.getSubFilter().getAllColumnIds());

    //f2: (!((!(!(c5 < c6))) || (c7 < c8)))
    assertTrue(f2 instanceof NegationFilter);
    nf = (NegationFilter) f2;
    assertTrue(nf.getSubFilter() instanceof CompoundFilter);
    cf = (CompoundFilter) nf.getSubFilter();
    assertEquals(CompoundFilter.LogicalOperator.OR, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof NegationFilter);
    assertTrue(subFilters.get(1) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c7", "c8"),
        subFilters.get(1).getAllColumnIds());
    nf = (NegationFilter) subFilters.get(0);
    assertTrue(nf.getSubFilter() instanceof NegationFilter);
    nf = (NegationFilter) nf.getSubFilter();
    assertTrue(nf.getSubFilter() instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c5", "c6"),
        nf.getSubFilter().getAllColumnIds());

    //f3: ((c9 < c10) && ((c11 < c12)||((c13 < c14)&&(c15 < c16))))
    assertTrue(f3 instanceof CompoundFilter);
    cf = (CompoundFilter) f3;
    assertEquals(CompoundFilter.LogicalOperator.AND, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c9", "c10"),
        subFilters.get(0).getAllColumnIds());
    assertTrue(subFilters.get(1) instanceof CompoundFilter);
    cf = (CompoundFilter) subFilters.get(1);
    assertEquals(CompoundFilter.LogicalOperator.OR, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c11", "c12"),
        subFilters.get(0).getAllColumnIds());
    assertTrue(subFilters.get(1) instanceof CompoundFilter);
    cf = (CompoundFilter) subFilters.get(1);
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnColumnFilter);
    assertTrue(subFilters.get(1) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c13", "c14"),
        subFilters.get(0).getAllColumnIds());
    assertEquals(Sets.newHashSet("c15", "c16"),
        subFilters.get(1).getAllColumnIds());

  }

  public void testPrecedence() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery(
        "SELECT c1,c2 WHERE NOT c1 < c2 OR NOT c3 < " +
            "c4 AND NOT c5 < c6 OR c7 < c8 AND " +
            "c9 < c10 AND c11 < c12 OR c13 < c14 AND c15 < c16");
        // interpretation: (!(c1 < c2)) or
        //                 ((not (c3 < c4)) and (not (c5 < c6))) or
        //                 ((c7 < c8) and (c9 < c10) and (c11 < c12)) or
        //                 ((c13 < c14) and (c15 < c16))
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof CompoundFilter);
    CompoundFilter f = (CompoundFilter) filter;
    assertEquals(CompoundFilter.LogicalOperator.OR, f.getOperator());
    List<QueryFilter> subFilters = f.getSubFilters();
    assertEquals(4, subFilters.size());
    QueryFilter f1 = subFilters.get(0);
    QueryFilter f2 = subFilters.get(1);
    QueryFilter f3 = subFilters.get(2);
    QueryFilter f4 = subFilters.get(3);

    // f1: (!(c1 < c2))
    assertTrue(f1 instanceof NegationFilter);
    NegationFilter nf = (NegationFilter) f1;
    QueryFilter subFilter = nf.getSubFilter();
    assertTrue(subFilter instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c1", "c2"), subFilter.getAllColumnIds());

    // f2: ((!(c3<c4)) && (!(c5<c6)))))
    assertTrue(f2 instanceof CompoundFilter);
    CompoundFilter cf = (CompoundFilter) f2;
    assertEquals(CompoundFilter.LogicalOperator.AND, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof NegationFilter);
    assertTrue(subFilters.get(1) instanceof NegationFilter);
    QueryFilter tmp = ((NegationFilter) subFilters.get(0)).getSubFilter();
    assertTrue(tmp instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c3", "c4"), tmp.getAllColumnIds());
    tmp = ((NegationFilter) subFilters.get(1)).getSubFilter();
    assertTrue(tmp instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c5", "c6"), tmp.getAllColumnIds());

    //f3: ((c7 < c8) && (c9 < c10) && (c11 < c12))
    assertTrue(f3 instanceof CompoundFilter);
    cf = (CompoundFilter) f3;
    assertEquals(CompoundFilter.LogicalOperator.AND, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(3, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnColumnFilter);
    assertTrue(subFilters.get(1) instanceof ColumnColumnFilter);
    assertTrue(subFilters.get(2) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c7", "c8"),
        subFilters.get(0).getAllColumnIds());
    assertEquals(Sets.newHashSet("c9", "c10"),
        subFilters.get(1).getAllColumnIds());
    assertEquals(Sets.newHashSet("c11", "c12"),
        subFilters.get(2).getAllColumnIds());

    // f4: ((c13 < c14) and (c15 < c16))
    assertTrue(f4 instanceof CompoundFilter);
    cf = (CompoundFilter) f4;
    assertEquals(CompoundFilter.LogicalOperator.AND, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnColumnFilter);
    assertTrue(subFilters.get(1) instanceof ColumnColumnFilter);
    assertEquals(Sets.newHashSet("c13", "c14"),
        subFilters.get(0).getAllColumnIds());
    assertEquals(Sets.newHashSet("c15", "c16"),
        subFilters.get(1).getAllColumnIds());

  }

  public void testIsNull() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("WHERE a iS nULl");
    assertTrue(query.getFilter() instanceof ColumnIsNullFilter);
    ColumnIsNullFilter filter = (ColumnIsNullFilter) query.getFilter();
    assertEquals("a", filter.getColumn().getId());
  }

  public void testIsNotNull() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("WHERE a iS NOt nULl");
    assertTrue(query.getFilter() instanceof NegationFilter);
    NegationFilter filter = (NegationFilter) query.getFilter();
    assertTrue(filter.getSubFilter() instanceof ColumnIsNullFilter);
    ColumnIsNullFilter filter2 = (ColumnIsNullFilter) filter.getSubFilter();
    assertEquals("a", filter2.getColumn().getId());
  }

  // GROUP and PIVOT clause tests

  public void testSimpleGroupAndPivot() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("select min(salary), "
        + "avg(height) group by dept, subdept pivot year, month");
    assertEquals(Lists.<AbstractColumn>newArrayList(
        new AggregationColumn(new SimpleColumn("salary"), AggregationType.MIN),
        new AggregationColumn(new SimpleColumn("height"), AggregationType.AVG)),
        query.getSelection().getColumns());
    assertEquals(Lists.newArrayList("dept", "subdept"),
        query.getGroup().getColumnIds());
    assertEquals(Lists.newArrayList("year", "month"),
        query.getPivot().getColumnIds());
  }

  public void testDateLiterals() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("WHERE c1 > DATE '2006-08-23'");
    assertEquals(new DateValue(2006, 7, 23),
        ((ColumnValueFilter) query.getFilter()).getValue());
    query = QueryBuilder.getInstance().parseQuery(
        "WHERE c1 = DaTeTiMe '2007-01-30 15:33:22.432'");
    assertEquals(new DateTimeValue(2007, 0, 30, 15, 33, 22, 432),
        ((ColumnValueFilter) query.getFilter()).getValue());
    query = QueryBuilder.getInstance().parseQuery(
        "WHERE c1 = timesTaMP '2007-01-30 15:33:22.432'");
    assertEquals(new DateTimeValue(2007, 0, 30, 15, 33, 22, 432),
        ((ColumnValueFilter) query.getFilter()).getValue());
    query = QueryBuilder.getInstance().parseQuery("WHERE c1 != TimeOfDay '15:33:22'");
    assertEquals(new TimeOfDayValue(15, 33, 22),
        ((ColumnValueFilter) query.getFilter()).getValue());
  }

  public void testBadDateLiteral() throws Exception {
    try {
      QueryBuilder.getInstance().parseQuery("WHERE c1 > DATE '12:32:43'");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Supposed to throw an exception.
    }
    try {
      // 2005 was not a leap year.
      QueryBuilder.getInstance().parseQuery("WHERE c1 > DATE '2005-02-29'");
      fail("Should have thrown an exception.");
    } catch (InvalidQueryException e) {
      // Supposed to throw an exception.
    }
  }

  public void testAdvancedStringOperators() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("WHERE c1 MATCHES "
        + "'.*a[^b]*b' AND 'abc' MATCHES c2 AND 'foo' CONTAINS c3"
        + " AND c4 CONTAINS 'bar' AND c5 STARTS WITH 'baz' AND c6"
        + " ENDS WITH 'chiko' AND 'baba' STARTS WITH c7 AND 'gaga'"
        + " ENDS WITH c8");
    CompoundFilter filter = (CompoundFilter) query.getFilter();
    List<QueryFilter> subFilters = filter.getSubFilters();
    assertEquals(ComparisonFilter.Operator.MATCHES,
        ((ComparisonFilter) subFilters.get(0)).getOperator());
    assertEquals(ComparisonFilter.Operator.CONTAINS,
        ((ComparisonFilter) subFilters.get(3)).getOperator());
    assertEquals(ComparisonFilter.Operator.STARTS_WITH,
        ((ComparisonFilter) subFilters.get(4)).getOperator());
    assertEquals(ComparisonFilter.Operator.ENDS_WITH,
        ((ComparisonFilter) subFilters.get(5)).getOperator());
  }

  public void testSelectionOfArithmeticExpressions() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("SELECT ((c1+c2)/c3*c4)-(c5/c6+year(c7))");
    ScalarFunctionColumn col1 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c1"),
            new SimpleColumn("c2")), Sum.getInstance());
    ScalarFunctionColumn col2 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, new SimpleColumn("c3")),
        Quotient.getInstance());
    ScalarFunctionColumn col3 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col2, new SimpleColumn("c4")),
        Product.getInstance());
    ScalarFunctionColumn col4 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c5"),
            new SimpleColumn("c6")), Quotient.getInstance());
    ScalarFunctionColumn col5 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col4,
            new ScalarFunctionColumn(Lists.<AbstractColumn>newArrayList(
                new SimpleColumn("c7")), TimeComponentExtractor.getInstance(
                TimeComponentExtractor.TimeComponent.YEAR))),
        Sum.getInstance());
    ScalarFunctionColumn col6 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col3, col5),
        Difference.getInstance());
    QuerySelection selection = query.getSelection();
    assertEquals(1, selection.getColumns().size());
    assertEquals(col6, selection.getColumns().get(0));
  }

  public void testFilterOfArithmeticExpression() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("Where ((c1+c2)/c3*c4)"
        + " > (c5/c6+year(c7)) and (c8+c9=0 or c10<>c11) ");
    QueryFilter filter = query.getFilter();
    assertTrue(filter instanceof CompoundFilter);
    CompoundFilter f = (CompoundFilter) filter;
    assertEquals(CompoundFilter.LogicalOperator.AND, f.getOperator());
    List<QueryFilter> subFilters = f.getSubFilters();
    assertEquals(2, subFilters.size());
    QueryFilter f1 = subFilters.get(0);
    QueryFilter f2 = subFilters.get(1);

    // f1: ((c1+c2)/c3*c4) > (c5/c6+year(c7))
    ScalarFunctionColumn col1 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c1"),
            new SimpleColumn("c2")), Sum.getInstance());
    ScalarFunctionColumn col2 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, new SimpleColumn("c3")),
        Quotient.getInstance());
    ScalarFunctionColumn col3 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col2, new SimpleColumn("c4")),
        Product.getInstance());
    ScalarFunctionColumn col4 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c5"),
            new SimpleColumn("c6")), Quotient.getInstance());
    ScalarFunctionColumn col5 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col4,
            new ScalarFunctionColumn(Lists.<AbstractColumn>newArrayList(
                new SimpleColumn("c7")), TimeComponentExtractor.getInstance(
                TimeComponentExtractor.TimeComponent.YEAR))),
        Sum.getInstance());
    assertTrue(f1 instanceof ColumnColumnFilter);
    ColumnColumnFilter ccf = (ColumnColumnFilter) f1;
    assertEquals(col3, ccf.getFirstColumn());
    assertEquals(col5, ccf.getSecondColumn());

    // f2: (c8+c9=0 or c10<>c11)
    assertTrue(f2 instanceof CompoundFilter);
    CompoundFilter cf = (CompoundFilter) f2;
    assertEquals(CompoundFilter.LogicalOperator.OR, cf.getOperator());
    subFilters = cf.getSubFilters();
    assertEquals(2, subFilters.size());
    assertTrue(subFilters.get(0) instanceof ColumnValueFilter);
    assertTrue(subFilters.get(1) instanceof ColumnColumnFilter);
    assertEquals(new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c8"),
            new SimpleColumn("c9")), Sum.getInstance()),
        ((ColumnValueFilter) subFilters.get(0)).getColumn());
    assertEquals(new NumberValue(0),
        ((ColumnValueFilter) subFilters.get(0)).getValue());
    assertEquals(Sets.newHashSet("c10", "c11"),
        subFilters.get(1).getAllColumnIds());
    assertEquals(ComparisonFilter.Operator.EQ,
        ((ComparisonFilter) subFilters.get(0)).getOperator());
    assertEquals(ComparisonFilter.Operator.NE,
        ((ComparisonFilter) subFilters.get(1)).getOperator());
  }

  public void testBadFilterOfArithmeticExpression() throws Exception {
    try {
      QueryBuilder.getInstance().parseQuery("Where ((c1+c2/c3*c4)"
          + " > (c5/c6+year(c7)) and (c8+c9=0 or c10<>c11) ");
      fail("Should have thrown an exception, missing ')': Where ((c1+c2/c3*c4)"
          + "> (c5/c6+year(c7)) and (c8+c9=0 or c10<>c11)");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
    try {
      QueryBuilder.getInstance().parseQuery("Where ((c1+)/c3*c4)"
          + " > (c5/c6+year(c7)) and (c8+c9=0 or c10<>c11) ");
      fail("Should have thrown an exception: 'Where ((c1+)/c3*c4)"
          + "> (c5/c6+year(c7)) and (c8+c9=0 or c10<>c11)'");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
    try {
      QueryBuilder.getInstance().parseQuery("Where ((c1+c2)/c3*c4)"
          + " > (c5/c6+year(c7)) and (c8+c9=0 or c10*c11) ");
      fail("Should have thrown an exception: 'Where ((c1+c2)/c3*c4)"
          + "> (c5/c6+year(c7)) and (c8+c9=0 or c10*c11)'");
    } catch (InvalidQueryException e) {
      // Expected behavior.
    }
  }

  public void testOrderByOfArithmeticExpression() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("order by ((c1+c2)/c3/c4)");
    QuerySort sort = query.getSort();
    List<ColumnSort> columnSorts = sort.getSortColumns();
    assertEquals(1, columnSorts.size());
    ScalarFunctionColumn col1 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c1"),
            new SimpleColumn("c2")), Sum.getInstance());
    ScalarFunctionColumn col2 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, new SimpleColumn("c3")),
        Quotient.getInstance());
    ScalarFunctionColumn col3 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col2, new SimpleColumn("c4")),
        Quotient.getInstance());
    assertEquals(col3, columnSorts.get(0).getColumn());
    assertEquals(SortOrder.ASCENDING, columnSorts.get(0).getOrder());
  }

  public void testPivotAndGroupByArithmeticExpression() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("select sum(c7) group "
        + "by ((c1+c2)*c3/c4) pivot `c5` -   `c6`");
    QueryGroup group = query.getGroup();
    QueryPivot pivot = query.getPivot();
    List<AbstractColumn> groupColumns = group.getColumns();
    assertEquals(1, groupColumns.size());
    List<AbstractColumn> pivotColumns = pivot.getColumns();
    assertEquals(1, pivotColumns.size());

    ScalarFunctionColumn col1 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c1"),
            new SimpleColumn("c2")), Sum.getInstance());
    ScalarFunctionColumn col2 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col1, new SimpleColumn("c3")),
        Product.getInstance());
    ScalarFunctionColumn col3 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(col2, new SimpleColumn("c4")),
        Quotient.getInstance());
    assertEquals(col3,  groupColumns.get(0));

    ScalarFunctionColumn col4 = new ScalarFunctionColumn(
        Lists.<AbstractColumn>newArrayList(new SimpleColumn("c5"),
            new SimpleColumn("c6")), Difference.getInstance());
    assertEquals(col4,  pivotColumns.get(0));
  }

  public void testVeryLongFilterQuery() throws Exception {
    QueryBuilder.getInstance().parseQuery("WHERE ((((((((a+b)*c"
        + "*d+e+f/(g+h*(d+b)))*3.3+d)/b+(d*3+year(c1)/month(c2)))-9+a)+b*c*d*e"
        + ")-year(d))< (((((((a+b)*c*d+e+f/(g+h*(d+b)))*3.3+100*`r`)/b+(d*3+"
        + "year(c1)/month(c2)))-9+a)+b*c*d*e)-year(d))) AND ((((((((a+`b d`)*"
        + "c*d+e+f/(g+h*(d+b)))*3.3+d)/b+(d*3+year(c1)/month(c2)))-9+a)+b*c*d"
        + "*e)-year(d))< (((((((a+bilo)*c*d+e+f/(g+h*(d+b)))*3.3+100*`r`)/b+("
        + "d*3+year(c1)/month(c2)))-9+a)+b*c*d*e)-year(d))) AND ((((((((a+`b d"
        + "`)*c*gfdd+e+f/(g+h*(d+b)))*3.3+d)/b+(d*3+year(c1)/month(c2)))-9+a)"
        + "+b*c*d*e)-year(d))< (((((((a+bsdilo)*c*d+e+f/(g+h*(d+b)))*3.3+100"
        + "*`r`)/b+(d*3+year(c1)/month(c2)))-9+a)+b*c*d*e)-year(d))) OR (((((("
        + "((a+`b d`)*c*d+e+f/(g+hhh*(ffd+b)))*3.3+d)/b+(d*3+year(c1)/month(c2)"
        + "))-9+a)+b*c*d*e)-year(d))< (((((((a+bilo)*c*d+e+f/(g+h*(d+bss)))*"
        + "3.3+100*`r`)/b+(d*3+year(c1)/month(c2)))-9+a)+b*c*d*e)-year(d))) OR"
        + " NOT (a + b < 3)");
  }

  public void testScalarFunctions() throws Exception {
    // Test lower function.
    Query query = QueryBuilder.getInstance().parseQuery("Select lower(c1)");
    QuerySelection selection = query.getSelection();
    assertEquals(selection.getColumns().size(), 1);
    assertEquals(
        ((ScalarFunctionColumn) selection.getColumns().get(0)).getFunction(),
        Lower.getInstance());

    // Test upper function.
    query = QueryBuilder.getInstance().parseQuery("Select upper(c1)");
    selection = query.getSelection();
    assertEquals(selection.getColumns().size(), 1);
    assertEquals(
        ((ScalarFunctionColumn) selection.getColumns().get(0)).getFunction(),
        Upper.getInstance());
  }

  public void testLikeOperator() throws Exception {
    Query query = QueryBuilder.getInstance().parseQuery("where A like 'foo%bar'");
    ColumnValueFilter filter = (ColumnValueFilter) query.getFilter();
    assertEquals(ComparisonFilter.Operator.LIKE, filter.getOperator());
    assertEquals("A", ((SimpleColumn) filter.getColumn()).getId());
    assertEquals("foo%bar", ((TextValue) filter.getValue()).toString());
  }
}
