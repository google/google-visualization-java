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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Selection definition for a query.
 * Selection is defined as a list of column IDs. It can also include aggregations for
 * grouping/pivoting and scalar functions.
 * 
 *
 * @author Itai R.
 */
public class QuerySelection {

  /**
   * The list of columns to select.
   */
  private List<AbstractColumn> columns;

  /**
   * Construct an empty selection list.
   */
  public QuerySelection() {
    columns = Lists.newArrayList();
  }

  /**
   * Copy constructor.
   *
   * @param source The source query selection from which to construct.
   */
  public QuerySelection(QuerySelection source) {
    columns = Lists.newArrayList(source.columns);
  }

  /**
   * Returns true if the selection list is empty.
   *
   * @return True if the selection list is empty.
   */
  public boolean isEmpty() {
    return columns.isEmpty();
  }

  /**
   * Adds a column to the column list.
   *
   * @param column The column to select.
   */
  public void addColumn(AbstractColumn column) {
    columns.add(column);
  }

  /**
   * Returns the list of columns. This list is immutable.
   * @return The list of columns. This list is immutable.
   */
  public List<AbstractColumn> getColumns() {
    return ImmutableList.copyOf(columns);
  }

  /**
   * Returns all the columns that are AggregationColumns including aggregation
   * columns that are inside scalar function columns (e.g., year(min(a1))).
   *
   * @return All the columns that are AggregationColumns.
   */
  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      result.addAll(col.getAllAggregationColumns());
    }
    return result;
  }

  /**
   * Returns all the columns that are SimpleColumns including those inside
   * scalar function columns (e.g, year(a1)). Does not return simple columns
   * inside aggregation columns (e.g., sum(a1)).
   *
   * @return All the columns that are SimpleColumns.
   */
  public List<SimpleColumn> getSimpleColumns() {
    List<SimpleColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      result.addAll(col.getAllSimpleColumns());
    }
    return result;
  }

  /**
   * Returns all the columns that are ScalarFunctionColumns including scalar
   * functions columns that are inside other scalar function columns
   * (e.g., sum(year(a), year(b))).
   *
   * @return all the columns that are ScalarFunctionColumns.
   */
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      result.addAll(col.getAllScalarFunctionColumns());
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columns == null) ? 0 : columns.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    QuerySelection other = (QuerySelection) obj;
    if (columns == null) {
      if (other.columns != null) {
        return false;
      }
    } else if (!columns.equals(other.columns)) {
      return false;
    }
    return true;
  }
  
  /**
   * Returns a string that when fed to the query parser would produce an equal QuerySelection.
   * The string is returned without the SELECT keyword.
   * 
   * @return The query string.
   */
  public String toQueryString() {
    return Query.columnListToQueryString(columns); 
  }
}
