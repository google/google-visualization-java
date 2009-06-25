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

import org.apache.commons.lang.text.StrBuilder;

import java.util.List;

/**
 * Sorting definition for a query.
 * Sort is defined as a list of column sorts where the first is the primary sort order,
 * the second is the secondary sort order, etc.
 *
 * @author Yoah B.D.
 */
public class QuerySort {

  /**
   * The list of columns by which to sort.
   */
  private List<ColumnSort> sortColumns;

  /**
   * Constructs an empty sort list.
   */
  public QuerySort() {
    sortColumns = Lists.newArrayList();
  }

  /**
   * Returns true if the sort list is empty.
   *
   * @return True if the sort list is empty.
   */
  public boolean isEmpty() {
    return sortColumns.isEmpty();
  }

  /**
   * Adds a column sort condition.
   * Validates that the column ID is not already specified in the sort.
   *
   * @param columnSort The column sort condition.
   */
  public void addSort(ColumnSort columnSort) {
    sortColumns.add(columnSort);
  }

  /**
   * Adds a column sort condition.
   * Validates that the column ID is not already specified in the sort.
   *
   * @param column The column to sort by.
   * @param order The requested ordering.
   */
  public void addSort(AbstractColumn column, SortOrder order) {
    addSort(new ColumnSort(column, order));
  }

  /**
   * Returns the list of sort columns. This list is immutable.
   *
   * @return The list of sort columns. This list is immutable.
   */
  public List<ColumnSort> getSortColumns() {
    return ImmutableList.copyOf(sortColumns);
  }

  /**
   * Returns a list of columns held by this query sort.
   *
   * @return A list of columns held by this query sort.
   */
  public List<AbstractColumn> getColumns() {
    List<AbstractColumn> result =
        Lists.newArrayListWithExpectedSize(sortColumns.size());
    for (ColumnSort columnSort : sortColumns) {
      result.add(columnSort.getColumn());
    }
    return result;
  }

  /**
   * Returns all the columns that are AggregationColumns including aggregation
   * columns that are inside scalar function columns (e.g., year(min(a1))).
   *
   * @return All the columns that are AggregationColumns.
   */
  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (ColumnSort columnSort : sortColumns) {
      AbstractColumn col = columnSort.getColumn();
      for (AggregationColumn innerCol : col.getAllAggregationColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
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
    for (ColumnSort columnSort : sortColumns) {
      AbstractColumn col = columnSort.getColumn();
      for (ScalarFunctionColumn innerCol : col.getAllScalarFunctionColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sortColumns == null) ? 0 : sortColumns.hashCode());
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
    QuerySort other = (QuerySort) obj;
    if (sortColumns == null) {
      if (other.sortColumns != null) {
        return false;
      }
    } else if (!sortColumns.equals(other.sortColumns)) {
      return false;
    }
    return true;
  }
  
  /**
   * Returns a string that when fed to the query parser would produce an equal QuerySort.
   * The string returned does not contain the ORDER BY keywords.
   * 
   * @return The query string.
   */
  public String toQueryString() {
    StrBuilder builder = new StrBuilder();
    List<String> stringList = Lists.newArrayList();
    for (ColumnSort colSort : sortColumns) {
      stringList.add(colSort.toQueryString());
    }
    builder.appendWithSeparators(stringList, ", ");
    return builder.toString();
  }
}
