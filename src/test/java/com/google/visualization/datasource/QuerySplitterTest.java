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

package com.google.visualization.datasource;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.ColumnIsNullFilter;
import com.google.visualization.datasource.query.ColumnSort;
import com.google.visualization.datasource.query.ColumnValueFilter;
import com.google.visualization.datasource.query.ComparisonFilter;
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
import com.google.visualization.datasource.query.scalarfunction.TimeComponentExtractor;

import junit.framework.TestCase;

import java.util.List;


/**
 * A test for the QuerySplitter class.
 *
 * @author Yonatan B.Y.
 */
public class QuerySplitterTest extends TestCase {

  /**
   * Holds some default query for the tests.
   */
  private Query q = null;

  /**
   * Sets up the default query.
   */
  @Override
  public void setUp() throws Exception {
    q = new Query();
    QuerySelection selection = new QuerySelection();
    selection.addColumn(new SimpleColumn("A"));
    selection.addColumn(new AggregationColumn(new SimpleColumn("B"), AggregationType.MAX));
    q.setSelection(selection);
    QuerySort sort = new QuerySort();
    sort.addSort(new ColumnSort(new SimpleColumn("A"), SortOrder.DESCENDING));
    q.setSort(sort);
    QueryFilter filter = new ColumnValueFilter(new SimpleColumn("A"), new TextValue("foo"),
                                               ComparisonFilter.Operator.GT);
    q.setFilter(filter);
    q.setRowLimit(7);
    q.setRowOffset(17);
    QueryLabels labels = new QueryLabels();
    labels.addLabel(new SimpleColumn("A"), "bar");
    q.setLabels(labels);
    QueryFormat format = new QueryFormat();
    format.addPattern(new SimpleColumn("A"), "foo");
    q.setUserFormatOptions(format);
    QueryOptions options = new QueryOptions();
    options.setNoFormat(true);
    q.setOptions(options);
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("A"));
    q.setGroup(group);
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("C"));
    q.setPivot(pivot);
  }

  /**
   * Tests the query splitter with a datasource with SQL CapabilitySet, when the
   * query contains a pivot statement.
   */
  public void testSplitSQLWithPivot() throws Exception {
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SQL);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    QuerySelection selection = dataSourceQuery.getSelection();
    List<AbstractColumn> columns = selection.getColumns();
    assertEquals(3, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals("B", ((AggregationColumn) columns.get(1)).getAggregatedColumn().getId());
    assertEquals(AggregationType.MAX, ((AggregationColumn) columns.get(1)).getAggregationType());
    assertEquals("C", ((SimpleColumn) columns.get(2)).getId());
    assertFalse(dataSourceQuery.hasSort());
    ColumnValueFilter filter = (ColumnValueFilter) dataSourceQuery.getFilter();
    assertEquals("A", ((SimpleColumn) filter.getColumn()).getId());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasPivot());
    columns = dataSourceQuery.getGroup().getColumns();
    assertEquals(2, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals("C", ((SimpleColumn) columns.get(1)).getId());

    selection = completionQuery.getSelection();
    columns = selection.getColumns();
    assertEquals(2, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals("max-B", ((AggregationColumn) columns.get(1)).getAggregatedColumn().getId());
    assertEquals(AggregationType.MIN, ((AggregationColumn) columns.get(1)).getAggregationType());
    assertFalse(completionQuery.hasFilter());
    columns = completionQuery.getSort().getColumns();
    assertEquals(1, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals(7, completionQuery.getRowLimit());
    assertEquals(17, completionQuery.getRowOffset());
    assertTrue(completionQuery.getOptions().isNoFormat());
    assertTrue(completionQuery.hasLabels());
    assertTrue(completionQuery.hasUserFormatOptions());
    columns = completionQuery.getGroup().getColumns();
    assertEquals(1, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    columns = completionQuery.getPivot().getColumns();
    assertEquals(1, columns.size());
    assertEquals("C", ((SimpleColumn) columns.get(0)).getId());
  }

  /**
   * Tests the query splitter with a datasource with SQL CapabilitySet, when the
   * query does not contain a pivot statement.
   */
  public void testSplitSQLWithoutPivot() throws Exception {
    q.setPivot(null);
    AggregationColumn maxB = new AggregationColumn(new SimpleColumn("B"), AggregationType.MAX);
    q.getLabels().addLabel(maxB, "maxBLabel");
    q.getUserFormatOptions().addPattern(maxB, "maxB#");
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SQL);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    QuerySelection selection = dataSourceQuery.getSelection();
    List<AbstractColumn> columns = selection.getColumns();
    assertEquals(2, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals("B", ((AggregationColumn) columns.get(1)).getAggregatedColumn().getId());
    assertEquals(AggregationType.MAX, ((AggregationColumn) columns.get(1)).getAggregationType());
    columns = dataSourceQuery.getSort().getColumns();
    assertEquals(1, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    ColumnValueFilter filter = (ColumnValueFilter) dataSourceQuery.getFilter();
    assertEquals("A", ((SimpleColumn) filter.getColumn()).getId());
    assertEquals(7, dataSourceQuery.getRowLimit());
    assertEquals(17, dataSourceQuery.getRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasPivot());
    columns = dataSourceQuery.getGroup().getColumns();
    assertEquals(1, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());

    assertFalse(completionQuery.hasSelection());
    assertFalse(completionQuery.hasFilter());
    assertFalse(completionQuery.hasSort());
    assertFalse(completionQuery.hasRowLimit());
    assertFalse(completionQuery.hasRowOffset());
    assertTrue(completionQuery.getOptions().isNoFormat());
    assertTrue(completionQuery.hasLabels());
    // Test that labels and formats changed appropriately
    AbstractColumn[] labelColumns =
        completionQuery.getLabels().getColumns().toArray(new AbstractColumn[]{});
    if (labelColumns[1].getId().equals("A")) {
      AbstractColumn tmp = labelColumns[0];
      labelColumns[0] = labelColumns[1];
      labelColumns[1] = tmp;       
    }
    assertEquals(2, labelColumns.length);
    assertEquals("A", labelColumns[0].getId());
    AbstractColumn col = labelColumns[1];
    assertTrue(col instanceof SimpleColumn);
    assertEquals("max-B", ((SimpleColumn) col).getId());
    assertEquals("maxBLabel", completionQuery.getLabels().getLabel(col));
    AbstractColumn[] formatColumns =
        completionQuery.getUserFormatOptions().getColumns().toArray(new AbstractColumn[]{});
    if (formatColumns[1].getId().equals("A")) {
      AbstractColumn tmp = formatColumns[0];
      formatColumns[0] = formatColumns[1];
      formatColumns[1] = tmp;       
    }
    assertEquals(2, formatColumns.length);
    assertEquals("A", formatColumns[0].getId());
    col = formatColumns[1];
    assertTrue(col instanceof SimpleColumn);
    assertEquals("max-B", ((SimpleColumn) col).getId());
    assertEquals("maxB#", completionQuery.getUserFormatOptions().getPattern(col));
    
    assertFalse(completionQuery.hasGroup());
    assertFalse(completionQuery.hasPivot());
  }

  /**
   * Tests the query splitter with a datasource with SORT_AND_PAGINATION CapabilitySet, when the
   * query contains one of group/filter/pivot.
   */
  public void testSortAndPagination() throws Exception {
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SORT_AND_PAGINATION);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    assertFalse(dataSourceQuery.hasSelection());
    assertFalse(dataSourceQuery.hasFilter());
    assertFalse(dataSourceQuery.hasSort());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());

    assertEquals(q, completionQuery);
  }

  /**
   * Tests the query splitter with a datasource with SORT_AND_PAGINATION CapabilitySet, when the
   * query does not contain any of group/filter/pivot.
   */
  public void testSortAndPaginationOnSimpleQuery() throws Exception {
    q.setGroup(null);
    q.setFilter(null);
    q.setPivot(null);
    // Remove aggregation column:
    QuerySelection newSel = new QuerySelection();
    newSel.addColumn(new SimpleColumn("A"));
    q.setSelection(newSel);

    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SORT_AND_PAGINATION);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    assertFalse(dataSourceQuery.hasSelection());
    assertFalse(dataSourceQuery.hasFilter());
    List<AbstractColumn> columns = dataSourceQuery.getSort().getColumns();
    assertEquals(1, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals(7, dataSourceQuery.getRowLimit());
    assertEquals(17, dataSourceQuery.getRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());

    columns = completionQuery.getSelection().getColumns();
    assertEquals(1, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertFalse(completionQuery.hasFilter());
    assertFalse(completionQuery.hasSort());
    assertFalse(completionQuery.hasRowLimit());
    assertFalse(completionQuery.hasRowOffset());
    assertTrue(completionQuery.hasOptions());
    assertTrue(completionQuery.hasLabels());
    assertTrue(completionQuery.hasUserFormatOptions());
    assertFalse(completionQuery.hasGroup());
    assertFalse(completionQuery.hasPivot());
  }

  /**
   * Tests the query splitter with a datasource with SELECT CapabilitySet.
   */
  public void testSplitSelection() throws Exception {
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SELECT);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    List<AbstractColumn> columns = dataSourceQuery.getSelection().getColumns();
    assertEquals(3, columns.size());
    assertEquals("A", ((SimpleColumn) columns.get(0)).getId());
    assertEquals("B", ((SimpleColumn) columns.get(1)).getId());
    assertEquals("C", ((SimpleColumn) columns.get(2)).getId());
    assertFalse(dataSourceQuery.hasFilter());
    assertFalse(dataSourceQuery.hasSort());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());
    
    assertEquals(q, completionQuery);
  }

  /**
   * Tests the query splitter with a datasource with ALL CapabilitySet.
   */
  public void testSplitAll() throws Exception {
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.ALL);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    assertEquals(q, dataSourceQuery);

    assertFalse(completionQuery.hasSelection());
    assertFalse(completionQuery.hasFilter());
    assertFalse(completionQuery.hasSort());
    assertFalse(completionQuery.hasRowLimit());
    assertFalse(completionQuery.hasRowOffset());
    assertFalse(completionQuery.hasOptions());
    assertFalse(completionQuery.hasLabels());
    assertFalse(completionQuery.hasUserFormatOptions());
    assertFalse(completionQuery.hasGroup());
    assertFalse(completionQuery.hasPivot());
  }

  /**
   * Tests the query splitter with a datasource with NONE CapabilitySet.
   */
  public void testSplitNone() throws Exception {
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.NONE);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    assertNull(dataSourceQuery);
    assertEquals(q, completionQuery);
  }

  /**
   * Tests the query splitter with a datasource with SQL/SORT_AND_PAGINATION
   * CapabilitySets, when the query contains a scalar function column.
   */
  public void testSplittingWithScalarFunctions() throws Exception {
    List<AbstractColumn> columnList = Lists.<AbstractColumn>newArrayList(new SimpleColumn("A"));
    q.setFilter(new ColumnIsNullFilter(new ScalarFunctionColumn(columnList,
        TimeComponentExtractor.getInstance(TimeComponentExtractor.TimeComponent.YEAR))));
    
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SQL);
    Query dataSourceQuery = split.getDataSourceQuery();

    assertFalse(dataSourceQuery.hasSelection());
    assertFalse(dataSourceQuery.hasFilter());
    assertFalse(dataSourceQuery.hasSort());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());

    split = QuerySplitter.splitQuery(q, Capabilities.SORT_AND_PAGINATION);
    dataSourceQuery = split.getDataSourceQuery();

    assertFalse(dataSourceQuery.hasSelection());
    assertFalse(dataSourceQuery.hasFilter());
    assertFalse(dataSourceQuery.hasSort());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());
  }
  
  public void testSplittingSQLWithPivotWithLabel() throws Exception {
    QueryLabels labels = q.getLabels();
    labels.addLabel(new AggregationColumn(new SimpleColumn("B"), AggregationType.MAX), "bar");
    q.setLabels(labels);
    QueryPair split = QuerySplitter.splitQuery(q, Capabilities.SQL);
    Query dataSourceQuery = split.getDataSourceQuery();

    assertFalse(dataSourceQuery.hasSelection());
    assertFalse(dataSourceQuery.hasFilter());
    assertFalse(dataSourceQuery.hasSort());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());
  }

  public void testSortAndPaginationWithSkipping() throws Exception {
    Query testQuery = new Query();
    testQuery.copyFrom(q);
    testQuery.setGroup(null);
    testQuery.setFilter(null);
    testQuery.setPivot(null);
    testQuery.setRowSkipping(5);
    
    QueryPair split = QuerySplitter.splitQuery(testQuery, Capabilities.SORT_AND_PAGINATION);
    Query dataSourceQuery = split.getDataSourceQuery();
    Query completionQuery = split.getCompletionQuery();

    // The original query contains sort, skipping, limit, offset. We split it
    // for sort and pagination capabilities. Data source query should have sort,
    // and the completion query should have both skipping and pagination.
    assertFalse(dataSourceQuery.hasSelection());
    assertFalse(dataSourceQuery.hasFilter());
    assertTrue(dataSourceQuery.hasSort());
    assertFalse(dataSourceQuery.hasRowSkipping());
    assertFalse(dataSourceQuery.hasRowLimit());
    assertFalse(dataSourceQuery.hasRowOffset());
    assertFalse(dataSourceQuery.hasOptions());
    assertFalse(dataSourceQuery.hasLabels());
    assertFalse(dataSourceQuery.hasUserFormatOptions());
    assertFalse(dataSourceQuery.hasGroup());
    assertFalse(dataSourceQuery.hasPivot());

    testQuery.setSort(null);
    assertEquals(testQuery, completionQuery);
  }
}
