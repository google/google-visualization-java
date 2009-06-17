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
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.query.CompoundFilter.LogicalOperator;
import com.google.visualization.datasource.query.scalarfunction.Lower;
import com.google.visualization.datasource.query.scalarfunction.Quotient;

import junit.framework.TestCase;

/**
 * A test for the Query class.
 *
 * @author Yonatan B.Y.
 */
public class QueryTest extends TestCase {

  /**
   * A default query used for most of the tests.
   */
  private Query q;

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
    QueryGroup group = new QueryGroup();
    group.addColumn(new SimpleColumn("A"));
    q.setGroup(group);
    QueryPivot pivot = new QueryPivot();
    pivot.addColumn(new SimpleColumn("C"));
    q.setPivot(pivot);
  }

  /**
   * Tests a validation that should not throw any exception.
   */
  public void testValidatePositive() throws Exception {
    q.validate();
  }

  /**
   * Tests that duplicates are not allowed in the select clause.
   */
  public void testValidateDuplicates() {
    q.getSelection().addColumn(new SimpleColumn("D"));
    try {
      q.validate();
      fail();
    } catch (InvalidQueryException ex) {
      // Do nothing.
    }
  }

  /**
   * Tests that grouping and pivoting by the same column is not allowed.
   */
  public void testValidateGroupAndPivotBySameColumn() {
    q.getPivot().addColumn(new SimpleColumn("A"));
    try {
      q.validate();
      fail();
    } catch (InvalidQueryException ex) {
      //Expected behavior.
    }
  }
  
  public void testAggregationsNotAllowedInGroup() throws Exception {
    q.getGroup().addColumn(new AggregationColumn(new SimpleColumn("A"), AggregationType.AVG));
    try {
      q.validate();
      fail();
    } catch (InvalidQueryException ex) {
      // Great.
    }
  }
  
  public void testAggregationsNotAllowedInPivot() throws Exception {
    q.getPivot().addColumn(new AggregationColumn(new SimpleColumn("A"), AggregationType.AVG));
    try {
      q.validate();
      fail();
    } catch (InvalidQueryException ex) {
      // Great.
    }
  }
  
  public void testAggregationsNotAllowedInFilter() throws Exception {
    q.setFilter(new ColumnIsNullFilter(
        new AggregationColumn(new SimpleColumn("A"), AggregationType.AVG)));
    try {
      q.validate();
      fail();
    } catch (InvalidQueryException ex) {
      // Great.
    }
  }

  /**
   * Tests toQueryString().
   */
  public void testToQueryString() {
    q.setFilter(new CompoundFilter(LogicalOperator.AND, Lists.<QueryFilter>newArrayList(
        new ColumnIsNullFilter(new ScalarFunctionColumn(Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("F B")), Lower.getInstance())),
        new ColumnIsNullFilter(new ScalarFunctionColumn(Lists.<AbstractColumn>newArrayList(
            new SimpleColumn("F"), new SimpleColumn("B")), Quotient.getInstance())),
        q.getFilter())));
    assertEquals("SELECT `A`, MAX(`B`) WHERE (lower(`F B`) IS NULL) AND ((`F` / `B`) IS NULL) AND "
        + "(`A` > \"foo\") GROUP BY `A` PIVOT `C` ORDER BY `A` DESC LIMIT 7 OFFSET 17 "
        + "LABEL `A` \"bar\" FORMAT `A` \"foo\"", q.toQueryString());
    assertEquals("", new Query().toQueryString());
  }
}
