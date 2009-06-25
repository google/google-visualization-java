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
 * Pivoting definition for a query.
 * Pivoting is defined as a list of column IDs to pivot.
 *
 * @author Yoav G.
 * @author Yonatan B.Y.
 */
public class QueryPivot {

  /**
   * The list of pivot columns.
   */
  private List<AbstractColumn> columns;

  /**
   * Constructs a query pivot with empty lists.
   */
  public QueryPivot() {
    columns = Lists.newArrayList();
  }

  /**
   * Adds a column to pivot.
   *
   * @param column The column to add.
   */
  public void addColumn(AbstractColumn column) {
    columns.add(column);
  }

  /**
   * Returns the list of pivot column IDs. This list is immutable.
   *
   * @return The list of pivot column IDs. This list is immutable.
   */
  public List<String> getColumnIds() {
    List<String> columnIds = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      columnIds.add(col.getId());
    }
    return ImmutableList.copyOf(columnIds);
  }

  /**
   * Returns a list of all simple columns' IDs in this pivot.
   *
   * @return A list of all simple columns' IDs in this pivot.
   */
  public List<String> getSimpleColumnIds() {
    List<String> columnIds = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      columnIds.addAll(col.getAllSimpleColumnIds());
    }
    return columnIds;
  }

  /**
   * Returns the list of pivot columns. This list is immutable.
   *
   * @return The list of pivot columns. This list is immutable.
   */
  public List<AbstractColumn> getColumns() {
    return ImmutableList.copyOf(columns);
  }

  /**
   * Returns the list of pivot simple columns.
   *
   * @return The list of pivot simple columns.
   */
  public List<SimpleColumn> getSimpleColumns() {
    List<SimpleColumn> simpleColumns = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      simpleColumns.addAll(col.getAllSimpleColumns());
    }
    return simpleColumns;
  }

  /**
   * Returns the list of pivot scalar function columns.
   *
   * @return The list of pivot scalar function columns.
   */
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      scalarFunctionColumns.addAll(col.getAllScalarFunctionColumns());
    }
    return scalarFunctionColumns;
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
    QueryPivot other = (QueryPivot) obj;
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
   * Returns a string that when fed to the query parser would produce an equal QueryPivot.
   * The string is returned without the PIVOT keywors.
   * 
   * @return The query string.
   */
  public String toQueryString() {
    return Query.columnListToQueryString(columns); 
  }
}
